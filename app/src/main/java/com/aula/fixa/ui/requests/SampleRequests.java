package com.aula.fixa.ui.requests;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.RequestItem;

import java.util.Arrays;
import java.util.List;

/** Dados de exemplo espelhando o Figma até a integração com backend. */
public final class SampleRequests {

    private SampleRequests() {}

    public static List<RequestItem> all() {
        return Arrays.asList(
                // Descrição, equipamento e foto do Figma "Editar solicitação" (pop-up 7015).
                new RequestItem("Luz piscando", "Sala T07 - 2º andar", "11 Mar 2026", RequestItem.Status.ANALYSIS,
                        "A luz pisca com frequência e apresenta ruído.", "Lâmpada", R.drawable.img_edit_photo_lamp),
                new RequestItem("Daniel farmou aura", "Átrio", "11 Mar 2026", RequestItem.Status.REFUSED),
                new RequestItem("Projetor quebrado", "Sala T02 - 2º andar", "11 Mar 2026", RequestItem.Status.DONE),
                new RequestItem("Torneira quebrada", "Banheiro - 2º Andar", "11 Mar 2026", RequestItem.Status.APPROVED)
        );
    }

    public static List<RequestItem> home() {
        return all().subList(0, 2);
    }
}
