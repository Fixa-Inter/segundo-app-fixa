package com.aula.fixa.model;

public class FaqItem {
    public final String question;
    public final String answer;
    public boolean expanded;

    public FaqItem(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
}
