package com.aula.fixa.domain.model.solicitacao;

import com.aula.fixa.domain.enums.StatusProblema;

public class EtapaLinhaTempo {

    public final String title;
    public final String date;
    public final String time;
    public final StatusProblema status;
    public final boolean atual;

    public EtapaLinhaTempo(String title, String date, String time, StatusProblema status, boolean atual) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.status = status;
        this.atual = atual;
    }
}
