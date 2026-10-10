package com.aula.fixa.ui.newrequest;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/** Estado do formulário "Abrir solicitação", compartilhado entre os passos (escopo do nav graph). */
public class NewRequestViewModel extends ViewModel {
    public String title = "";
    public String description = "";
    public String location = "";
    public String locationDescription = "";
    public String device = "";
    public final List<Integer> photos = new ArrayList<>();

    public void clear() {
        title = description = location = locationDescription = device = "";
        photos.clear();
    }
}
