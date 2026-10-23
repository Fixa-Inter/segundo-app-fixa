package com.aula.fixa.domain.model;

import com.aula.fixa.domain.enums.StatusProblema;

/** Solicitação exibida nos cards (dados de exemplo enquanto não há backend). */
public class ProblemaItem {
    public final long id;

    public final String title;
    public final String location;
    public final String date;
    public final StatusProblema status;
    public final String description;
    public final String device;
    /** Foto anexada (drawable de exemplo) ou 0 quando não há. */
    public final int photo;

    public ProblemaItem(String title, String location, String date, StatusProblema status) {
        this(0, title, location, date, status, "", "", 0);
    }

    public ProblemaItem(String title, String location, String date, StatusProblema status,
                       String description, String device, int photo) {
        this(0, title, location, date, status, description, device, photo);
    }

    public ProblemaItem(long id, String title, String location, String date, StatusProblema status,
                       String description, String device, int photo) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
        this.status = status;
        this.description = description;
        this.device = device;
        this.photo = photo;
    }
}
