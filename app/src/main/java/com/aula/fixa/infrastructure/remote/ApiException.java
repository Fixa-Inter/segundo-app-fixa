package com.aula.fixa.infrastructure.remote;

public final class ApiException extends Exception {
    public final int status;
    public final String body;

    public ApiException(int status, String body) {
        super(userMessage(status, body));
        this.status = status;
        this.body = body == null ? "" : body;
        System.out.println("[API ERROR BODY] HTTP " + status + " - " + this.body);
    }

    private static String userMessage(int status, String body) {
        try {
            Object message = new org.json.JSONObject(body == null ? "" : body).opt("message");
            if (message instanceof org.json.JSONArray) {
                String joined = ((org.json.JSONArray) message).join("\n").replace("\"", "");
                if (!joined.trim().isEmpty()) return joined;
            }
            if (message != null && !message.toString().trim().isEmpty()) return message.toString();
        } catch (Exception ignored) { }
        return "A API retornou um erro (HTTP " + status + ").";
    }
}
