package com.aula.fixa.ui.config.operations;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Relação explícita entre as telas Android e os nós finais do protótipo. */
public final class ScreenCatalog {
    private static ScreenCatalog instance;
    private final Map<String, JSONObject> screens = new LinkedHashMap<>();

    private ScreenCatalog(Context context) {
        try (var stream = context.getAssets().open("operations/screens.json")) {
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = stream.read(buffer)) != -1) bytes.write(buffer, 0, count);
            JSONArray data = new JSONArray(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);
                screens.put(item.getString("key"), item);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível abrir o catálogo de telas", e);
        }
    }

    public static synchronized ScreenCatalog get(Context context) {
        if (instance == null) instance = new ScreenCatalog(context.getApplicationContext());
        return instance;
    }

    public JSONObject screen(String key) {
        JSONObject screen = screens.get(key);
        if (screen == null) throw new IllegalArgumentException("Tela desconhecida: " + key);
        return screen;
    }

    public Map<String, JSONObject> all() { return java.util.Collections.unmodifiableMap(screens); }

    public int layout(Context context, String key) {
        int id = context.getResources().getIdentifier(screen(key).optString("layout"), "layout", context.getPackageName());
        if (id == 0) throw new IllegalStateException("Layout não encontrado: " + key);
        return id;
    }
}
