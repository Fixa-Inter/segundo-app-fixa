package com.aula.fixa.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.aula.fixa.R;
import com.aula.fixa.model.RequestItem;
import com.google.android.material.button.MaterialButton;

/** Pop-ups do Figma (confirmação de exclusão, logout e edição de solicitação). */
public final class AppDialogs {

    private AppDialogs() {}

    public static void showDeleteConfirm(Context context) {
        showConfirm(context, R.drawable.img_dialog_illustration, R.string.app_dialog_delete_title,
                R.string.app_dialog_delete_body, R.string.app_delete, null);
    }

    public static void showLogoutConfirm(Context context, Runnable onConfirm) {
        showConfirm(context, R.drawable.img_dialog_illustration, R.string.app_dialog_logout_title,
                R.string.app_dialog_logout_body, R.string.app_dialog_logout_confirm, onConfirm);
    }

    private static void showConfirm(Context context, int imageRes, int titleRes, int bodyRes,
                                    int confirmRes, Runnable onConfirm) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_app_confirm, null, false);
        ((ImageView) content.findViewById(R.id.ivDialogImage)).setImageResource(imageRes);
        ((TextView) content.findViewById(R.id.tvDialogTitle)).setText(titleRes);
        ((TextView) content.findViewById(R.id.tvDialogBody)).setText(bodyRes);
        MaterialButton primary = content.findViewById(R.id.btnDialogPrimary);
        primary.setText(confirmRes);

        AlertDialog dialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_FIXA_App_Dialog)
                .setView(content)
                .create();
        sizeDialog(dialog, R.dimen.app_dialog_width);
        content.findViewById(R.id.btnDialogSecondary).setOnClickListener(v -> dialog.dismiss());
        primary.setOnClickListener(v -> {
            dialog.dismiss();
            if (onConfirm != null) onConfirm.run();
        });
        dialog.show();
    }

    /** Largura do Figma limitada a 90% da tela para caber em qualquer aparelho. */
    private static void sizeDialog(AlertDialog dialog, int widthRes) {
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() == null) return;
            int desired = dialog.getContext().getResources().getDimensionPixelSize(widthRes);
            int max = (int) (dialog.getContext().getResources().getDisplayMetrics().widthPixels * 0.9f);
            dialog.getWindow().setLayout(Math.min(desired, max), WindowManager.LayoutParams.WRAP_CONTENT);
        });
    }

    public static void showEditRequest(Context context, RequestItem item) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_app_edit_request, null, false);
        ((EditText) content.findViewById(R.id.etEditTitle)).setText(item.title);
        ((EditText) content.findViewById(R.id.etEditLocation)).setText(item.location);

        AlertDialog dialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_FIXA_App_Dialog)
                .setView(content)
                .create();
        sizeDialog(dialog, R.dimen.app_dialog_edit_width);
        content.findViewById(R.id.btnEditCancel).setOnClickListener(v -> dialog.dismiss());
        content.findViewById(R.id.btnEditSave).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
