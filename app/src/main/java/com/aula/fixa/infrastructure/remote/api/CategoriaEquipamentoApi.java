package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.OpcaoCadastroResponse;
import com.aula.fixa.infrastructure.remote.dto.CategoriaEquipamentoRequest;
import com.aula.fixa.infrastructure.remote.dto.CategoriaEquipamentoUpdateRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface CategoriaEquipamentoApi {
    @GET("api/v1/equipamentos/categorias") Call<List<OpcaoCadastroResponse>> categorias();
    @POST("api/v1/equipamentos/categorias") Call<OpcaoCadastroResponse> criar(@Body CategoriaEquipamentoRequest request);
    @PATCH("api/v1/equipamentos/categorias") Call<OpcaoCadastroResponse> atualizar(@Body CategoriaEquipamentoUpdateRequest request);
    @DELETE("api/v1/equipamentos/categorias/{categoriaId}") Call<OpcaoCadastroResponse> excluir(@Path("categoriaId") long id);
}
