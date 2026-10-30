package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.OpcaoCadastroResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface LocalUnidadeApi {
    @GET("api/v1/local-unidade") Call<List<OpcaoCadastroResponse>> locais();
}
