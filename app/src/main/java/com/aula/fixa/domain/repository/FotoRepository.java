package com.aula.fixa.domain.repository;

public interface FotoRepository {
    interface Callback {
        void aoConcluir(String url, String identificadorExclusao);
        void aoReceberTokenExclusao(String token);
        void aoFalhar(String mensagem);
    }
    void enviarSolicitacao(String uri, Callback callback);
    void enviarPerfil(String uri, Callback callback);
    void excluir(String token, Callback callback);
}
