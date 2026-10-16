package com.aula.fixa.domain.model;

public class EventItem {
    public final String day;
    public final String dayLabel;
    public final String title;
    public final String time;

    public EventItem(String day, String dayLabel, String title, String time) {
        this.day = day;
        this.dayLabel = dayLabel;
        this.title = title;
        this.time = time;
    }
}
