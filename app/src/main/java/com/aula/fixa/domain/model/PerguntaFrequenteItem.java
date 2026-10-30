package com.aula.fixa.domain.model;

public class PerguntaFrequenteItem {
    public final String question;
    public final String answer;
    public boolean expanded;

    public PerguntaFrequenteItem(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
}
