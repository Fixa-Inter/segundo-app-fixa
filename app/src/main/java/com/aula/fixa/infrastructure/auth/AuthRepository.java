package com.aula.fixa.infrastructure.auth;

import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.AuthApi;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.SessaoUsuario;
import com.aula.fixa.domain.model.ResultadoLoginGoogle;
import com.aula.fixa.domain.model.ResultadoRecuperacaoSenha;
import com.aula.fixa.domain.repository.AutenticacaoRepository;
import com.aula.fixa.infrastructure.remote.dto.OpcaoCadastroResponse;
import com.aula.fixa.infrastructure.remote.dto.CadastroUsuarioRequest;
import com.aula.fixa.infrastructure.remote.dto.LoginGoogleRequest;
import com.aula.fixa.infrastructure.remote.dto.LoginRequest;
import com.aula.fixa.infrastructure.remote.dto.RedefinirSenhaRequest;
import com.aula.fixa.infrastructure.remote.dto.ValidarCodigoRequest;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Response;
import com.aula.fixa.infrastructure.remote.dto.LoginGoogleResponse;
import com.aula.fixa.infrastructure.remote.dto.LoginResponse;
import com.aula.fixa.infrastructure.remote.dto.RedefinirSenhaResponse;

public final class AuthRepository implements AutenticacaoRepository {
    private final AuthApi api = ApiClient.auth();
    private final SessionStorage session;

    public AuthRepository(SessionStorage session) {
        this.session = session;
    }

    @Override public SessaoUsuario login(String email, String password) throws Exception {
        LoginRequest body = new LoginRequest();
        body.email = email;
        body.senha = password;
        LoginResponse result = execute(api.login(body));
        return new SessaoUsuario(result.email, result.token);
    }

    @Override public ResultadoLoginGoogle loginGoogle(String idToken) throws Exception {
        LoginGoogleRequest body = new LoginGoogleRequest();
        body.idToken = idToken;
        LoginGoogleResponse result = execute(api.loginGoogle(body));
        return new ResultadoLoginGoogle(result.status, result.email, result.nome, result.token);
    }

    @Override public SessaoUsuario cadastrar(
            String name,
            String email,
            String password,
            long unitId,
            String role,
            String birthDate
    ) throws Exception {
        CadastroUsuarioRequest body = new CadastroUsuarioRequest();
        body.nomeCompleto = name;
        body.email = email;
        body.senha = password;
        body.unidadeId = unitId;
        body.cargo = role;
        body.dataNascimento = birthDate;
        LoginResponse result = execute(api.register(body));
        return new SessaoUsuario(result.email, result.token);
    }

    @Override public ResultadoRecuperacaoSenha solicitarRecuperacao(String email) throws Exception {
        RedefinirSenhaResponse result = execute(api.requestPasswordRecovery(android.net.Uri.encode(email)));
        return new ResultadoRecuperacaoSenha(result.nome, result.email, result.mensagem);
    }

    @Override public boolean validarCodigo(String email, int code) throws Exception {
        ValidarCodigoRequest body = new ValidarCodigoRequest();
        body.email = email;
        body.codigo = code;
        Response<Boolean> response = api.validateRecoveryCode(body).execute();
        if (!response.isSuccessful()) {
            throw new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
        }
        return Boolean.TRUE.equals(response.body());
    }

    @Override public String redefinirSenha(String email, int code, String newPassword) throws Exception {
        RedefinirSenhaRequest body = new RedefinirSenhaRequest();
        body.email = email;
        body.codigo = code;
        body.novaSenha = newPassword;
        Response<ResponseBody> response = api.resetPassword(body).execute();
        if (!response.isSuccessful()) {
            throw new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
        }
        return response.body() == null ? "" : response.body().string();
    }

    @Override public List<OpcaoCadastro> instituicoes() throws Exception {
        return parseOptions(api.institutions().execute());
    }

    @Override public List<OpcaoCadastro> unidades(long institutionId) throws Exception {
        return parseOptions(api.units(institutionId).execute());
    }

    private List<OpcaoCadastro> parseOptions(Response<List<OpcaoCadastroResponse>> response) throws Exception {
        if (!response.isSuccessful() || response.body() == null) {
            throw new ApiException(
                    response.code(),
                    response.errorBody() == null ? "" : response.errorBody().string()
            );
        }
        List<OpcaoCadastro> options = new java.util.ArrayList<>();
        for (OpcaoCadastroResponse option : response.body()) options.add(new OpcaoCadastro(option.id, option.nome));
        return options;
    }


    private <T> T execute(Call<T> call) throws Exception {
        Response<T> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            throw new IOException("Não foi possível conectar à API", e);
        }

        if (!response.isSuccessful()) {
            throw new ApiException(
                    response.code(),
                    response.errorBody() == null ? "" : response.errorBody().string()
            );
        }

        T result = response.body();
        if (result == null) throw new IOException("A API retornou uma resposta vazia.");
        String token = tokenFrom(result);
        if (!token.trim().isEmpty()) {
            session.saveToken(token);
        }
        return result;
    }

    private static String tokenFrom(Object response) {
        if (response instanceof LoginResponse) return ((LoginResponse) response).token;
        if (response instanceof LoginGoogleResponse) return ((LoginGoogleResponse) response).token;
        return "";
    }

    public static String messageFrom(Throwable throwable) {
        if (throwable instanceof ApiException) {
            try {
                Object message = new JSONObject(((ApiException) throwable).body).opt("message");
                if (message instanceof org.json.JSONArray) return ((org.json.JSONArray) message).join("\\n").replace("\"", "");
                if (message != null && !message.toString().trim().isEmpty()) {
                    return message.toString();
                }
            } catch (Exception ignored) { }
        }
        return throwable.getMessage() == null ? "Não foi possível concluir a operação." : throwable.getMessage();
    }
}
