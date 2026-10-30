package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.OpcaoCadastroResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MarcaEquipamentoApi {
    @GET("api/v1/equipamentos/marcas") Call<List<OpcaoCadastroResponse>> listar();
}
