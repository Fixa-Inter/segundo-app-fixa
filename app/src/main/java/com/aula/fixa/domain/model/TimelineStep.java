package com.aula.fixa.domain.model;

public class TimelineStep {

    public enum State { DONE, CURRENT, REFUSED, PENDING }

    public final String title;
    public final String date;
    public final String time;
    public final State state;

    public TimelineStep(String title, String date, String time, State state) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.state = state;
    }
}
