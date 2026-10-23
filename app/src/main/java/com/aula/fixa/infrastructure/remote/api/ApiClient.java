package com.aula.fixa.infrastructure.remote.api;

import retrofit2.Retrofit;
import okhttp3.OkHttpClient;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import java.util.concurrent.TimeUnit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private ApiClient() {}

    private static OkHttpClient.Builder clientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .callTimeout(120, TimeUnit.SECONDS);
    }

    public static AuthApi auth() {
        return new Retrofit.Builder()
                .baseUrl("https://mobile-api.fixa.solutions")
                .client(clientBuilder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(AuthApi.class);
    }

    private static Retrofit authenticatedRetrofit(SessionStorage session) {
        OkHttpClient client = clientBuilder()
                .addInterceptor(chain -> {
                    String token = session.getToken();
                    okhttp3.Request.Builder request = chain.request().newBuilder();

                    if (token != null && !token.trim().isEmpty())
                        request.header("Authorization", "Bearer " + token);

                    return chain.proceed(request.build());
                }).build();


        return new Retrofit.Builder()
                .baseUrl("https://mobile-api.fixa.solutions")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static SolicitacaoApi solicitacoes(SessionStorage session) {
        return authenticatedRetrofit(session).create(SolicitacaoApi.class);
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
}
