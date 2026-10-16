package com.aula.fixa.data.remote.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private ApiClient() {}

    public static AuthApi auth() {
        return new Retrofit.Builder()
                .baseUrl("https://segundo-api-app.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(AuthApi.class);
    }
}
