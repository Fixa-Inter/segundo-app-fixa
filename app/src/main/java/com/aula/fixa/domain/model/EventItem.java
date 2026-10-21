package com.aula.fixa.domain.model;

public class EventItem {
    public final long id;
    public final String day;
    public final String dayLabel;
    public final String title;
    public final String time;
    public final String dateKey;

    public EventItem(String day, String dayLabel, String title, String time) {
        this(0, day, dayLabel, title, time, "");
    }

    public EventItem(long id, String day, String dayLabel, String title, String time) {
        this(id, day, dayLabel, title, time, "");
    }

    public EventItem(long id, String day, String dayLabel, String title, String time, String dateKey) {
        this.id = id;
        this.day = day;
        this.dayLabel = dayLabel;
        this.title = title;
        this.time = time;
        this.dateKey = dateKey;
    }
}
