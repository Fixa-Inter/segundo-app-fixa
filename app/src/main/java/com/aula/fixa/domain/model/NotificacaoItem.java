package com.aula.fixa.domain.model;

public class NotificacaoItem {
    public final String header;   // não nulo quando o item é cabeçalho de seção
    public final String text;
    public final String time;
    public final boolean unread;

    private NotificacaoItem(String header, String text, String time, boolean unread) {
        this.header = header;
        this.text = text;
        this.time = time;
        this.unread = unread;
    }

    public static NotificacaoItem header(String title) {
        return new NotificacaoItem(title, null, null, false);
    }

    public static NotificacaoItem item(String text, String time, boolean unread) {
        return new NotificacaoItem(null, text, time, unread);
    }

    public boolean isHeader() {
        return header != null;
    }
}
