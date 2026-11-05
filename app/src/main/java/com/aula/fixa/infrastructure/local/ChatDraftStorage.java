package com.aula.fixa.infrastructure.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.aula.fixa.domain.model.chat.ChatPendingDraft;
import com.aula.fixa.domain.repository.chat.ChatDraftRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Persiste com segurança o envio incerto no SQLite, isolado pela identidade local da sessão. */
public final class ChatDraftStorage implements ChatDraftRepository {
    private static final String LEGACY_PREFS = "fixa_chat_pending_drafts";
    private final OfflineDatabase database;
    private final SessionStorage session;
    private final SharedPreferences legacyPreferences;
    private final String ownerKey;
    private final String legacyKey;
    private final Gson gson = new Gson();

    public ChatDraftStorage(Context context, SessionStorage session) {
        Context appContext = context.getApplicationContext();
        this.database = OfflineDatabase.get(appContext);
        this.session = session;
        this.ownerKey = session.localDataOwnerKey();
        String token = session.getToken();
        this.legacyKey = "draft_" + digest(token == null ? "" : token);
        this.legacyPreferences = appContext.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE);
    }

    @Override public synchronized void save(ChatPendingDraft draft) {
        if (draft == null) throw new IllegalArgumentException("O rascunho não pode ser nulo.");
        try {
            byte[] serialized = gson.toJson(draft).getBytes(StandardCharsets.UTF_8);
            database.saveChatDraft(ownerKey, session.encryptLocalData(serialized));
        } catch (Exception error) {
            throw new IllegalStateException("Não foi possível salvar o rascunho local.", error);
        }
    }

    @Override public synchronized ChatPendingDraft load() {
        byte[] payload = database.chatDraft(ownerKey);
        if (payload != null) return decode(payload);
        return migrateLegacyDraft();
    }

    @Override public synchronized void clear() {
        boolean legacyRemoved = legacyPreferences.edit().remove(legacyKey).commit();
        if (!legacyRemoved) throw new IllegalStateException("Não foi possível limpar o rascunho legado.");
        database.deleteChatDraft(ownerKey);
    }

    private ChatPendingDraft decode(byte[] encrypted) {
        try {
            byte[] serialized = session.decryptLocalData(encrypted);
            return gson.fromJson(new String(serialized, StandardCharsets.UTF_8), ChatPendingDraft.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Migra o rascunho da conta ativa antes de removê-lo das SharedPreferences antigas. */
    private ChatPendingDraft migrateLegacyDraft() {
        String json = legacyPreferences.getString(legacyKey, null);
        if (json == null) return null;
        try {
            ChatPendingDraft draft = gson.fromJson(json, ChatPendingDraft.class);
            if (draft == null || draft.message == null) return null;
            save(draft);
            legacyPreferences.edit().remove(legacyKey).commit();
            return draft;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String digest(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 8; i++) result.append(String.format(java.util.Locale.ROOT, "%02x", bytes[i]));
            return result.toString();
        } catch (Exception ignored) {
            return "local";
        }
    }
}
