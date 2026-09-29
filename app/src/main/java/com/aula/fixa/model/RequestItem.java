package com.aula.fixa.model;

/** Solicitação exibida nos cards (dados de exemplo enquanto não há backend). */
public class RequestItem {

    public enum Status { ANALYSIS, APPROVED, DONE, REFUSED }

    public final String title;
    public final String location;
    public final String date;
    public final Status status;

    public RequestItem(String title, String location, String date, Status status) {
        this.title = title;
        this.location = location;
        this.date = date;
        this.status = status;
    }
}
