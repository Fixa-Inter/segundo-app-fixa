package com.aula.fixa.infrastructure.remote.api;

import retrofit2.Retrofit;
import okhttp3.OkHttpClient;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static final String API_BASE_URL = "https://mobile-api.fixa.solutions/";
    private static final Map<String, Retrofit> RETROFITS_BY_URL = new HashMap<>();
    private static volatile SessionStorage activeSession;

    private ApiClient() {}

    private static OkHttpClient.Builder clientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .callTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request();
                    String path = request.url().encodedPath();
                    SessionStorage session = activeSession;
                    String token = session == null || path.startsWith("/api/v1/auth/")
                            ? null : session.getToken();

                    if (token == null || token.trim().isEmpty()) return chain.proceed(request);
                    return chain.proceed(request.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build());
                });
    }

    public static AuthApi auth() {
        return retrofit(API_BASE_URL).create(AuthApi.class);
    }

    private static Retrofit authenticatedRetrofit(SessionStorage session) {
        activeSession = session;
        return retrofit(API_BASE_URL);
    }

    /** Reuses one Retrofit and its OkHttpClient for each API base URL in this app session. */
    private static synchronized Retrofit retrofit(String baseUrl) {
        Retrofit existing = RETROFITS_BY_URL.get(baseUrl);
        if (existing != null) return existing;

        Retrofit created = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(clientBuilder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RETROFITS_BY_URL.put(baseUrl, created);
        return created;
    }

    public static SolicitacaoApi solicitacoes(SessionStorage session) {
        return authenticatedRetrofit(session).create(SolicitacaoApi.class);
    }

    public static OcorrenciaApi ocorrencias(SessionStorage session) {
        return authenticatedRetrofit(session).create(OcorrenciaApi.class);
    }

    public static OrdemServicoApi ordensServico(SessionStorage session) {
        return authenticatedRetrofit(session).create(OrdemServicoApi.class);
    }

    public static TarefaApi tarefas(SessionStorage session) {
        return authenticatedRetrofit(session).create(TarefaApi.class);
    }

    public static ObservacaoConclusaoApi observacoesConclusao(SessionStorage session) {
        return authenticatedRetrofit(session).create(ObservacaoConclusaoApi.class);
    }

    public static PerfilApi perfil(SessionStorage session) {
        return authenticatedRetrofit(session).create(PerfilApi.class);
    }

    public static EventoApi eventos(SessionStorage session) {
        return authenticatedRetrofit(session).create(EventoApi.class);
    }

    public static LocalUnidadeApi locais(SessionStorage session) {
        return authenticatedRetrofit(session).create(LocalUnidadeApi.class);
    }

    public static CategoriaEquipamentoApi categorias(SessionStorage session) {
        return authenticatedRetrofit(session).create(CategoriaEquipamentoApi.class);
    }

    public static TecnicoApi tecnicos(SessionStorage session) {
        return authenticatedRetrofit(session).create(TecnicoApi.class);
    }
}
