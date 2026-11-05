package com.aula.fixa.infrastructure.remote.api;

import retrofit2.Retrofit;
import okhttp3.OkHttpClient;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.BuildConfig;
import com.aula.fixa.infrastructure.remote.auth.SessionTokenAuthenticator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static final String API_BASE_URL = "https://mobile-api.fixa.solutions/";
    private static final String NOSQL_API_BASE_URL = "https://nosql-api.fixa.solutions/";
    private static final Map<String, Retrofit> RETROFITS_BY_URL = new HashMap<>();
    private static volatile SessionStorage activeSession;

    private ApiClient() {}

    private static OkHttpClient.Builder clientBuilder(String baseUrl) {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .callTimeout(120, TimeUnit.SECONDS)
                // Chat POST pode criar registros operacionais; nunca repetir automaticamente.
                .retryOnConnectionFailure(!baseUrl.equals(normalizeBaseUrl(BuildConfig.IA_API_BASE_URL)))
                .followRedirects(!baseUrl.equals(normalizeBaseUrl(BuildConfig.IA_API_BASE_URL)))
                .followSslRedirects(!baseUrl.equals(normalizeBaseUrl(BuildConfig.IA_API_BASE_URL)))
                .authenticator(new SessionTokenAuthenticator())
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request();
                    String path = request.url().encodedPath();
                    SessionStorage session = activeSession;
                    String token = session == null || path.startsWith("/api/v1/auth/")
                            ? null : session.getToken();
                    okhttp3.Request.Builder builder = request.newBuilder();
                    if (baseUrl.equals(normalizeBaseUrl(BuildConfig.IA_API_BASE_URL))
                            && request.header("Accept") == null) builder.header("Accept", "application/json");
                    if (token != null && !token.trim().isEmpty()) builder.header("Authorization", "Bearer " + token);
                    return chain.proceed(builder.build());
                })
                .addInterceptor(new LocalApiCacheInterceptor(() -> activeSession));
    }

    public static AuthApi auth() {
        return retrofit(API_BASE_URL).create(AuthApi.class);
    }

    public static SessionStorage activeSession() { return activeSession; }

    public static synchronized void clearActiveSession(SessionStorage session) {
        if (activeSession == session) activeSession = null;
    }

    private static Retrofit authenticatedRetrofit(SessionStorage session) {
        activeSession = session;
        return retrofit(API_BASE_URL);
    }

    private static Retrofit authenticatedNoSqlRetrofit(SessionStorage session) {
        activeSession = session;
        return retrofit(NOSQL_API_BASE_URL);
    }

    /** Reuses one Retrofit and its OkHttpClient for each API base URL in this app session. */
    private static synchronized Retrofit retrofit(String baseUrl) {
        Retrofit existing = RETROFITS_BY_URL.get(baseUrl);
        if (existing != null) return existing;

        Retrofit created = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(clientBuilder(baseUrl).build())
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

    public static NotificacaoApi notificacoes(SessionStorage session) {
        return authenticatedRetrofit(session).create(NotificacaoApi.class);
    }

    public static LocalUnidadeApi locais(SessionStorage session) {
        return authenticatedRetrofit(session).create(LocalUnidadeApi.class);
    }

    public static CategoriaEquipamentoApi categorias(SessionStorage session) {
        return authenticatedRetrofit(session).create(CategoriaEquipamentoApi.class);
    }

    public static ModeloEquipamentoApi modelosEquipamento(SessionStorage session) {
        return authenticatedRetrofit(session).create(ModeloEquipamentoApi.class);
    }

    public static MarcaEquipamentoApi marcasEquipamento(SessionStorage session) {
        return authenticatedRetrofit(session).create(MarcaEquipamentoApi.class);
    }

    public static EquipamentoApi equipamentos(SessionStorage session) {
        return authenticatedRetrofit(session).create(EquipamentoApi.class);
    }

    public static ModeloChecklistApi modelosChecklist(SessionStorage session) {
        return authenticatedNoSqlRetrofit(session).create(ModeloChecklistApi.class);
    }

    public static InspecaoChecklistApi inspecoesChecklist(SessionStorage session) {
        return authenticatedNoSqlRetrofit(session).create(InspecaoChecklistApi.class);
    }

    public static ChatApi chat(SessionStorage session) {
        activeSession = session;
        return retrofit(normalizeBaseUrl(BuildConfig.IA_API_BASE_URL)).create(ChatApi.class);
    }

    public static JevApi jev(SessionStorage session) {
        activeSession = session;
        return retrofit(normalizeBaseUrl(BuildConfig.IA_API_BASE_URL)).create(JevApi.class);
    }

    private static String normalizeBaseUrl(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) normalized = "https://ia-api.fixa.solutions/";
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }

    public static TecnicoApi tecnicos(SessionStorage session) {
        return authenticatedRetrofit(session).create(TecnicoApi.class);
    }
}
