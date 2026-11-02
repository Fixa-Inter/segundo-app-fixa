package com.aula.fixa.infrastructure.auth.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Persiste a sessão de autenticação protegida por uma chave do Android Keystore. */
public final class SessionStorage {
    private static final String PREFERENCES = "fixa_secure_session";
    private static final String KEY_ALIAS = "fixa_session_key";
    private static final String TOKEN_KEY = "access_token";
    private static final String IV_KEY = "access_token_iv";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private static final String REFRESH_IV_KEY = "refresh_token_iv";
    private static final String SESSION_GENERATION_KEY = "session_generation";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SharedPreferences preferences;
    private final Context applicationContext;

    public SessionStorage(Context context) {
        applicationContext = context.getApplicationContext();
        preferences = applicationContext
                .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("O access token não pode ser vazio");
        }
        try {
            EncryptedValue encrypted = encrypt(token);
            preferences.edit()
                    .putString(TOKEN_KEY, encrypted.value)
                    .putString(IV_KEY, encrypted.iv)
                    .remove(REFRESH_TOKEN_KEY)
                    .remove(REFRESH_IV_KEY)
                    .putLong(SESSION_GENERATION_KEY, sessionGeneration() + 1L)
                    .apply();
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível proteger a sessão", exception);
        }
    }

    public void saveTokens(String accessToken, String refreshToken) {
        if (accessToken == null || accessToken.isBlank() || refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Access token e refresh token são obrigatórios");
        }
        savePair(accessToken, refreshToken, true);
    }

    public void saveRefreshedTokens(String accessToken, String refreshToken) {
        if (accessToken == null || accessToken.isBlank() || refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Access token e refresh token são obrigatórios");
        }
        savePair(accessToken, refreshToken, false);
    }

    private void savePair(String accessToken, String refreshToken, boolean newSession) {
        try {
            EncryptedValue access = encrypt(accessToken);
            EncryptedValue refresh = encrypt(refreshToken);
            SharedPreferences.Editor editor = preferences.edit()
                    .putString(TOKEN_KEY, access.value)
                    .putString(IV_KEY, access.iv)
                    .putString(REFRESH_TOKEN_KEY, refresh.value)
                    .putString(REFRESH_IV_KEY, refresh.iv);
            if (newSession) editor.putLong(SESSION_GENERATION_KEY, sessionGeneration() + 1L);
            editor.apply();
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível proteger a sessão", exception);
        }
    }

    @Nullable
    public String getToken() {
        String encrypted = preferences.getString(TOKEN_KEY, null);
        String encodedIv = preferences.getString(IV_KEY, null);
        if (encrypted == null || encodedIv == null) return null;
        try {
            return decrypt(encrypted, encodedIv);
        } catch (Exception exception) {
            clear();
            return null;
        }
    }

    @Nullable
    public String getRefreshToken() {
        String encrypted = preferences.getString(REFRESH_TOKEN_KEY, null);
        String encodedIv = preferences.getString(REFRESH_IV_KEY, null);
        if (encrypted == null || encodedIv == null) return null;
        try {
            return decrypt(encrypted, encodedIv);
        } catch (Exception exception) {
            clear();
            return null;
        }
    }

    public Context applicationContext() { return applicationContext; }

    public long sessionGeneration() {
        return preferences.getLong(SESSION_GENERATION_KEY, 0L);
    }

    public boolean hasSession() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clear() {
        long generation = sessionGeneration();
        preferences.edit().clear().putLong(SESSION_GENERATION_KEY, generation).apply();
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
        }
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build());
        return generator.generateKey();
    }

    private EncryptedValue encrypt(String value) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        return new EncryptedValue(
                Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8))),
                Base64.getEncoder().encodeToString(cipher.getIV())
        );
    }

    private String decrypt(String encrypted, String encodedIv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(),
                new GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.getDecoder().decode(encodedIv)));
        return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
    }

    private static final class EncryptedValue {
        final String value;
        final String iv;
        EncryptedValue(String value, String iv) { this.value = value; this.iv = iv; }
    }
}
