package com.aula.fixa.infrastructure.remote;

public final class ApiException extends Exception {
    public final int status;
    public final String body;

    public ApiException(int status, String body) {
        super("API returned HTTP " + status);
        this.status = status;
        this.body = body == null ? "" : body;
        System.out.println("[API ERROR BODY] HTTP " + status + " - " + this.body);
    }
}
