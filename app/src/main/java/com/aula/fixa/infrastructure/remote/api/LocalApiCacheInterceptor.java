package com.aula.fixa.infrastructure.remote.api;

import android.util.Base64;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.local.NetworkAvailability;
import com.aula.fixa.infrastructure.local.OfflineDatabase;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.function.Supplier;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/** Cache SQLite de leituras GET, separado por sessão e mantido mesmo quando a resposta envelhece. */
final class LocalApiCacheInterceptor implements Interceptor {
    private static final int MAX_CACHE_BODY_BYTES = 4 * 1024 * 1024;
    private final Supplier<SessionStorage> sessionProvider;

    LocalApiCacheInterceptor(Supplier<SessionStorage> sessionProvider) { this.sessionProvider = sessionProvider; }

    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        SessionStorage session = sessionProvider.get();
        OfflineDatabase database = session == null ? null : OfflineDatabase.get(session.applicationContext());

        boolean cacheable = "GET".equalsIgnoreCase(request.method())
                && session != null
                && session.hasSession() && database != null
                && !request.url().encodedPath().startsWith("/api/v1/auth/");

        if (!cacheable) {
            if (session != null
                    && !NetworkAvailability.isOnline(session.applicationContext())
                    && !"GET".equalsIgnoreCase(request.method())
            ) {
                throw new IOException("Esta ação exige conexão com a internet.");
            }

            return chain.proceed(request);
        }

        String owner = session.localDataOwnerKey();
        String key = hash(request.url().toString());

        if (!NetworkAvailability.isOnline(session.applicationContext())) {
            Response cached = fromCache(request, key, owner, session);

            if (cached != null) return cached;
            throw new IOException("Sem conexão e sem dados locais para esta consulta.");
        }

        try {
            Response response = chain.proceed(request);
            if (response.isSuccessful() && response.code() == 200 && response.body() != null
                    && response.body().contentType() != null
                    && response.body().contentType().toString().toLowerCase(java.util.Locale.ROOT).contains("json")) {
                try {
                    byte[] body = response.peekBody(MAX_CACHE_BODY_BYTES).bytes();
                    if (body.length > 0 && body.length < MAX_CACHE_BODY_BYTES) {
                        database.put(key, owner, session.encryptLocalData(body), response.body().contentType().toString());
                    }
                } catch (Exception ignored) {}
            }

            return response;
        } catch (IOException networkError) {
            Response cached = fromCache(request, key, owner, session);
            if (cached != null) return cached;
            throw networkError;
        }
    }

    private Response fromCache(Request request, String key, String owner, SessionStorage session) {
        try {
            OfflineDatabase.CachedResponse cached = OfflineDatabase.get(session.applicationContext()).get(key, owner);
            if (cached == null) return null;

            MediaType mediaType = cached.contentType == null ? MediaType.parse("application/json; charset=utf-8") : MediaType.parse(cached.contentType);
            byte[] payload = session.decryptLocalData(cached.payload);

            return new Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200)
                    .message("Offline cache")
                    .header("Content-Type", cached.contentType == null ? "application/json; charset=utf-8" : cached.contentType)
                    .header("X-Fixa-Offline-Cache", "true")
                    .body(ResponseBody.create(mediaType, payload))
                    .sentRequestAtMillis(cached.cachedAt).receivedResponseAtMillis(cached.cachedAt).build();

        } catch (Exception invalidCache) { return null; }
    }

    private static String hash(String value) throws IOException {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(digest, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        } catch (Exception error) { throw new IOException("Não foi possível identificar o cache local.", error); }
    }

}
