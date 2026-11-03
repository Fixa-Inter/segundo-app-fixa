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
        return abrir(activity, tipoAcesso, null);
    }

    public static boolean abrir(Activity activity, TipoAcesso tipoAcesso, Intent sourceIntent) {
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

        if (sourceIntent != null) {
            String notificationType = sourceIntent.getStringExtra("notification_type");
            if (notificationType == null) notificationType = sourceIntent.getStringExtra("tipoNotificacao");
            String referenceId = sourceIntent.getStringExtra("notification_reference_id");
            if (referenceId == null) referenceId = sourceIntent.getStringExtra("referenciaId");
            String notificationId = sourceIntent.getStringExtra("notification_id");
            if (notificationId == null) notificationId = sourceIntent.getStringExtra("notificacaoId");
            if (notificationType != null) intent.putExtra("notification_type", notificationType);
            if (referenceId != null) intent.putExtra("notification_reference_id", referenceId);
            if (notificationId != null) intent.putExtra("notification_id", notificationId);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        activity.startActivity(intent);
        activity.finish();

        return true;
    }
}
