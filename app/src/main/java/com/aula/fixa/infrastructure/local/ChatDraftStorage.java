package com.aula.fixa.infrastructure.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.aula.fixa.domain.model.chat.ChatPendingDraft;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/** Rascunho incerto local, isolado por hash do token da conta; nunca é reenviado automaticamente. */
public final class ChatDraftStorage {
    private static final String PREFS = "fixa_chat_pending_drafts";
    private final SharedPreferences preferences;
    private final String key;
    private final Gson gson = new Gson();

    public ChatDraftStorage(Context context, SessionStorage session) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        key = "draft_" + digest(session.getToken() == null ? "" : session.getToken());
    }
    public void save(ChatPendingDraft draft) { preferences.edit().putString(key, gson.toJson(draft)).apply(); }
    public ChatPendingDraft load() { try { String json=preferences.getString(key,null); return json==null?null:gson.fromJson(json,ChatPendingDraft.class); } catch(Exception ignored) { return null; } }
    public void clear() { preferences.edit().remove(key).apply(); }

    private static String digest(String value) {
        try {
            byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result=new StringBuilder(); for(int i=0;i<8;i++) result.append(String.format(java.util.Locale.ROOT,"%02x",bytes[i]));
            return result.toString();
        } catch(Exception ignored) { return "local"; }
    }
}
