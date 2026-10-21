package com.aula.fixa.domain.model;

public class NotificationItem {
    public final String header;   // não nulo quando o item é cabeçalho de seção
    public final String text;
    public final String time;
    public final boolean unread;

    private NotificationItem(String header, String text, String time, boolean unread) {
        this.header = header;
        this.text = text;
        this.time = time;
        this.unread = unread;
    }

    public static NotificationItem header(String title) {
        return new NotificationItem(title, null, null, false);
    }

    public static NotificationItem item(String text, String time, boolean unread) {
        return new NotificationItem(null, text, time, unread);
    }

    public boolean isHeader() {
        return header != null;
    }
}
