package com.aula.fixa.infrastructure.remote.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import okhttp3.ResponseBody;
import java.util.List;
import com.aula.fixa.infrastructure.remote.dto.OpcaoCadastroResponse;
import com.aula.fixa.infrastructure.remote.dto.CadastroUsuarioRequest;
import com.aula.fixa.infrastructure.remote.dto.LoginGoogleRequest;
import com.aula.fixa.infrastructure.remote.dto.LoginGoogleResponse;
import com.aula.fixa.infrastructure.remote.dto.LoginRequest;
import com.aula.fixa.infrastructure.remote.dto.LoginResponse;
import com.aula.fixa.infrastructure.remote.dto.RedefinirSenhaRequest;
import com.aula.fixa.infrastructure.remote.dto.RedefinirSenhaResponse;
import com.aula.fixa.infrastructure.remote.dto.ValidarCodigoRequest;

public interface AuthApi {

    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("api/v1/auth/login/google")
    Call<LoginGoogleResponse> loginGoogle(@Body LoginGoogleRequest body);

    @POST("api/v1/auth/cadastro")
    Call<LoginResponse> register(@Body CadastroUsuarioRequest body);

    @POST("api/v1/auth/esqueceu-senha/{email}")
    Call<RedefinirSenhaResponse> requestPasswordRecovery(@Path(value = "email", encoded = true) String email);

    @POST("api/v1/auth/validar-codigo")
    Call<Boolean> validateRecoveryCode(@Body ValidarCodigoRequest body);

    @POST("api/v1/auth/redefinir-senha")
    Call<ResponseBody> resetPassword(@Body RedefinirSenhaRequest body);

    @GET("api/v1/auth/instituicoes")
    Call<List<OpcaoCadastroResponse>> institutions();

    @GET("api/v1/auth/instituicoes/{instituicaoId}/unidades")
    Call<List<OpcaoCadastroResponse>> units(@Path("instituicaoId") long institutionId);
}
