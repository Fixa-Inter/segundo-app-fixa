package com.aula.fixa.ui.view.requests;

import com.aula.fixa.domain.enums.StatusProblema;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.solicitacao.ProblemaItem;

import java.util.Arrays;
import java.util.List;

/** Dados de exemplo espelhando o Figma até a integração com backend. */
public final class SampleRequests {

    private SampleRequests() {}

    public static List<ProblemaItem> all() {
        return Arrays.asList(
                // Descrição, equipamento e foto do Figma "Editar solicitação" (pop-up 7015).
                new ProblemaItem("Luz piscando", "Sala T07 - 2º andar", "11 Mar 2026", StatusProblema.PENDENTE,
                        "A luz pisca com frequência e apresenta ruído.", "Lâmpada", R.drawable.img_edit_photo_lamp),
                new ProblemaItem("Daniel farmou aura", "Átrio", "11 Mar 2026", StatusProblema.REPROVADO),
                new ProblemaItem("Projetor quebrado", "Sala T02 - 2º andar", "11 Mar 2026", StatusProblema.CONCLUIDO),
                new ProblemaItem("Torneira quebrada", "Banheiro - 2º Andar", "11 Mar 2026", StatusProblema.APROVADO)
        );
    }

    public static List<ProblemaItem> home() {
        return all().subList(0, 2);
    }
}
