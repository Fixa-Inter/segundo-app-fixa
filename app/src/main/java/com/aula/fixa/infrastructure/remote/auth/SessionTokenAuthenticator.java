package com.aula.fixa.infrastructure.remote.auth;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.RefreshTokenResponse;
import com.aula.fixa.ui.view.auth.LoginActivity;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

/** Renews an expired access token once and retries the protected request. */
public final class SessionTokenAuthenticator implements Authenticator {
    private static final String TAG = "SessionTokenAuthenticator";
    private static final AtomicBoolean LOGIN_REDIRECT_STARTED = new AtomicBoolean(false);

    @Nullable
    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if (responseCount(response) > 1) return null;
        String path = response.request().url().encodedPath();
        if (path.startsWith("/api/v1/auth/")) return null;

        SessionStorage session = ApiClient.activeSession();
        if (session == null) return null;

        synchronized (session) {
            String currentAccess = session.getToken();
            String failedAccess = bearerValue(response.request());

            // A parallel request may already have rotated and stored the access token.
            if (currentAccess != null && !currentAccess.isBlank()
                    && !currentAccess.equals(failedAccess)) {
                return withBearer(response.request(), currentAccess);
            }

            String refreshToken = session.getRefreshToken();
            if (refreshToken == null || refreshToken.isBlank()) {
                expireSession(session);
                return null;
            }

            Call<RefreshTokenResponse> call = ApiClient.auth().refresh(
                    new com.aula.fixa.infrastructure.remote.dto.RefreshTokenRequest(refreshToken));
            retrofit2.Response<RefreshTokenResponse> refreshed;
            try {
                refreshed = call.execute();
            } catch (IOException networkError) {
                Log.e(TAG, "Falha de rede ao renovar sessão após HTTP 401", networkError);
                return null;
            }

            if (refreshed.isSuccessful() && refreshed.body() != null
                    && notBlank(refreshed.body().token)
                    && notBlank(refreshed.body().refreshToken)) {
                String newAccess = refreshed.body().token;
                session.saveRefreshedTokens(newAccess, refreshed.body().refreshToken);
                LOGIN_REDIRECT_STARTED.set(false);
                return withBearer(response.request(), newAccess);
            }

            int status = refreshed.code();
            String errorBody = "";
            try {
                if (refreshed.errorBody() != null) errorBody = refreshed.errorBody().string();
            } catch (IOException readError) {
                Log.e(TAG, "Falha lendo resposta do endpoint de refresh", readError);
            }
            Log.e(TAG, "POST /api/v1/auth/refresh falhou: HTTP " + status + " - " + errorBody);
            if (status == 401) expireSession(session);
            return null;
        }
    }

    public static void resetLoginRedirect() {
        LOGIN_REDIRECT_STARTED.set(false);
    }

    private static void expireSession(SessionStorage session) {
        session.clear();
        ApiClient.clearActiveSession(session);
        if (!LOGIN_REDIRECT_STARTED.compareAndSet(false, true)) return;

        Context context = session.applicationContext();
        Intent login = new Intent(context, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try {
            context.startActivity(login);
        } catch (RuntimeException ignored) {
            LOGIN_REDIRECT_STARTED.set(false);
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static Request withBearer(Request request, String token) {
        return request.newBuilder().header("Authorization", "Bearer " + token).build();
    }

    private static String bearerValue(Request request) {
        String value = request.header("Authorization");
        return value != null && value.startsWith("Bearer ") ? value.substring("Bearer ".length()) : null;
    }

    private static int responseCount(Response response) {
        int count = 1;
        for (Response prior = response.priorResponse(); prior != null; prior = prior.priorResponse()) count++;
        return count;
    }
}
