package com.aula.fixa.application.auth;

/** Define a mensagem segura apresentada pela aplicação para uma falha de operação. */
public final class MensagemErro {
    private static final String MENSAGEM_PADRAO = "Não foi possível concluir a operação.";

    private MensagemErro() { }

    public static String from(Throwable erro) {
        if (erro == null || erro.getMessage() == null || erro.getMessage().trim().isEmpty()) {
            return MENSAGEM_PADRAO;
        }
        return erro.getMessage();
    }
}
