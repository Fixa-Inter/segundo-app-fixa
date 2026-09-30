package com.aula.fixa.ui.common;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.aula.fixa.R;
import com.aula.fixa.model.RequestItem;
import com.google.android.material.button.MaterialButton;

/** Pop-ups do Figma: confirmações com ilustração, exclusão com motivo e edição de solicitação. */
public final class AppDialogs {

    private AppDialogs() {}

    /** Figma "pop-up 6997": deletar registro (usado para itens sem motivo obrigatório). */
    public static void showDeleteConfirm(Context context, @Nullable Runnable onConfirm) {
        showConfirm(context, R.drawable.img_dialog_illustration, R.string.app_dialog_delete_title,
                R.string.app_dialog_delete_body, R.string.app_delete, onConfirm);
    }

    /** Figma "pop-up 7041": salvar alterações. */
    public static void showSaveConfirm(Context context, @Nullable Runnable onConfirm) {
        showConfirm(context, R.drawable.img_dialog_illustration, R.string.app_dialog_save_title,
                R.string.app_dialog_save_body, R.string.app_save, onConfirm);
    }

    /** Voltar com alterações pendentes: pergunta se salva antes de sair. */
    public static void showDiscardConfirm(Context context, Runnable onSave, Runnable onDiscard) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_app_confirm, null, false);
        ((ImageView) content.findViewById(R.id.ivDialogImage)).setImageResource(R.drawable.img_dialog_illustration);
        ((TextView) content.findViewById(R.id.tvDialogTitle)).setText(R.string.app_dialog_save_title);
        ((TextView) content.findViewById(R.id.tvDialogBody)).setText(R.string.app_dialog_discard_body);
        MaterialButton secondary = content.findViewById(R.id.btnDialogSecondary);
        MaterialButton primary = content.findViewById(R.id.btnDialogPrimary);
        secondary.setText(R.string.app_dialog_discard_confirm);
        primary.setText(R.string.app_save);

        AlertDialog dialog = build(context, content, R.dimen.app_dialog_width);
        secondary.setOnClickListener(v -> { dialog.dismiss(); onDiscard.run(); });
        primary.setOnClickListener(v -> { dialog.dismiss(); onSave.run(); });
        dialog.show();
    }

    /** Figma "pop-up 7049": sair da conta. */
    public static void showLogoutConfirm(Context context, Runnable onConfirm) {
        showConfirm(context, R.drawable.img_dialog_illustration, R.string.app_dialog_logout_title,
                R.string.app_dialog_logout_body, R.string.app_dialog_logout_confirm, onConfirm);
    }

    private static void showConfirm(Context context, @DrawableRes int imageRes, @StringRes int titleRes,
                                    @StringRes int bodyRes, @StringRes int confirmRes, @Nullable Runnable onConfirm) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_app_confirm, null, false);
        ((ImageView) content.findViewById(R.id.ivDialogImage)).setImageResource(imageRes);
        ((TextView) content.findViewById(R.id.tvDialogTitle)).setText(titleRes);
        ((TextView) content.findViewById(R.id.tvDialogBody)).setText(bodyRes);
        MaterialButton primary = content.findViewById(R.id.btnDialogPrimary);
        primary.setText(confirmRes);

        AlertDialog dialog = build(context, content, R.dimen.app_dialog_width);
        content.findViewById(R.id.btnDialogSecondary).setOnClickListener(v -> dialog.dismiss());
        primary.setOnClickListener(v -> {
            dialog.dismiss();
            if (onConfirm != null) onConfirm.run();
        });
        dialog.show();
    }

    /** Figma "pop-up 7005": excluir solicitação informando o motivo. */
    public static void showDeleteWithReason(Context context, @Nullable Runnable onConfirm) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_app_delete_reason, null, false);
        EditText reason = content.findViewById(R.id.etDeleteReason);
        AlertDialog dialog = build(context, content, R.dimen.app_dialog_width);
        content.findViewById(R.id.btnDeleteCancel).setOnClickListener(v -> dialog.dismiss());
        content.findViewById(R.id.btnDeleteConfirm).setOnClickListener(v -> {
            if (TextUtils.isEmpty(reason.getText().toString().trim())) {
                reason.setError(context.getString(R.string.app_dialog_delete_reason_hint));
                return;
            }
            dialog.dismiss();
            if (onConfirm != null) onConfirm.run();
        });
        dialog.show();
    }

    /** Figma "pop-up 7015": editar solicitação. Salvar abre "Salvar alterações?"; Cancelar com mudanças pergunta. */
    public static void showEditRequest(Context context, RequestItem item) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_app_edit_request, null, false);
        EditText title = content.findViewById(R.id.etEditTitle);
        EditText description = content.findViewById(R.id.etEditDescription);
        EditText location = content.findViewById(R.id.etEditLocation);
        EditText device = content.findViewById(R.id.etEditDevice);
        title.setText(item.title);
        location.setText(item.location);
        final String initial = snapshot(title, description, location, device);

        AlertDialog dialog = build(context, content, R.dimen.app_dialog_edit_width);
        dialog.setCancelable(false);
        Runnable close = dialog::dismiss;
        Runnable askIfDirty = () -> {
            if (initial.equals(snapshot(title, description, location, device))) {
                dialog.dismiss();
            } else {
                showDiscardConfirm(context, () -> showSaveConfirm(context, close), close);
            }
        };
        content.findViewById(R.id.btnEditCancel).setOnClickListener(v -> askIfDirty.run());
        content.findViewById(R.id.btnEditSave).setOnClickListener(v -> showSaveConfirm(context, close));
        dialog.setOnKeyListener((d, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.getAction() == android.view.KeyEvent.ACTION_UP) {
                askIfDirty.run();
                return true;
            }
            return false;
        });
        dialog.show();
    }

    private static String snapshot(EditText... fields) {
        StringBuilder sb = new StringBuilder();
        for (EditText f : fields) sb.append(f.getText()).append('\u0000');
        return sb.toString();
    }

    /** Largura do Figma limitada a 90% da tela para caber em qualquer aparelho. */
    private static AlertDialog build(Context context, View content, int widthRes) {
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_FIXA_App_Dialog)
                .setView(content)
                .create();
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() == null) return;
            int desired = context.getResources().getDimensionPixelSize(widthRes);
            int max = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9f);
            dialog.getWindow().setLayout(Math.min(desired, max), WindowManager.LayoutParams.WRAP_CONTENT);
        });
        return dialog;
    }
}
