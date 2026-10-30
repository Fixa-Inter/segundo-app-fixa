package com.aula.fixa.ui.config;

import android.app.Activity;
import android.content.Intent;
import com.aula.fixa.domain.enums.TipoAcesso;
import com.aula.fixa.ui.view.main.MainActivity;
import com.aula.fixa.ui.view.operations.OperationsActivity;

/** Direciona para o fluxo visual permitido pelo tipo de acesso da sessão. */
public final class NavegacaoAcesso {
    private NavegacaoAcesso() { }

    public static boolean abrir(Activity activity, TipoAcesso tipoAcesso) {
        Intent intent;
        if (tipoAcesso == TipoAcesso.SOLICITANTE) {
            intent = new Intent(activity, MainActivity.class);

        } else if (tipoAcesso == TipoAcesso.TECNICO) {
            intent = new Intent(activity, OperationsActivity.class)
                    .putExtra(OperationsActivity.EXTRA_ROLE, "technician");

        } else if (tipoAcesso == TipoAcesso.GESTOR
                || tipoAcesso == TipoAcesso.ADMINISTRADOR) {
            intent = new Intent(activity, OperationsActivity.class)
                    .putExtra(OperationsActivity.EXTRA_ROLE, "manager");

        } else {
            return false;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        activity.startActivity(intent);
        activity.finish();

        return true;
    }
}
