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
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SharedPreferences preferences;

    public SessionStorage(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("O access token não pode ser vazio");
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            String encrypted = Base64.getEncoder().encodeToString(
                    cipher.doFinal(token.getBytes(StandardCharsets.UTF_8)));
            String iv = Base64.getEncoder().encodeToString(cipher.getIV());
            preferences.edit().putString(TOKEN_KEY, encrypted).putString(IV_KEY, iv).apply();
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
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(),
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.getDecoder().decode(encodedIv)));
            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            clear();
            return null;
        }
    }

    public boolean hasSession() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clear() {
        preferences.edit().clear().apply();
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
}
