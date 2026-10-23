package com.aula.fixa.domain.model;

import androidx.annotation.DrawableRes;

/** Solicitação exibida nos cards (dados de exemplo enquanto não há backend). */
public class RequestItem {
    public final long id;

    public enum Status { PENDING, APPROVED, DONE, REFUSED }

    public final String title;
    public final String location;
    public final String date;
    public final Status status;
    public final String description;
    public final String device;
    /** Foto anexada (drawable de exemplo) ou 0 quando não há. */
    @DrawableRes
    public final int photo;

    public RequestItem(String title, String location, String date, Status status) {
        this(0, title, location, date, status, "", "", 0);
    }

    public RequestItem(String title, String location, String date, Status status,
                       String description, String device, @DrawableRes int photo) {
        this(0, title, location, date, status, description, device, photo);
    }

    public RequestItem(long id, String title, String location, String date, Status status,
                       String description, String device, @DrawableRes int photo) {
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
