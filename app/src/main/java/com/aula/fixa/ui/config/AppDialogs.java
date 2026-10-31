package com.aula.fixa.ui.config;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.solicitacao.ProblemaItem;
import com.aula.fixa.domain.model.solicitacao.Problema;
import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import com.aula.fixa.domain.model.OpcaoCadastro;

/**
 * Pop-ups do Figma: confirmações com ilustração, exclusão com motivo e edição de solicitação.
 */
public final class AppDialogs {

    public interface EditRequestCallback {
        void onSave(String title, String description, String location, String device, List<Long> deletedPhotoIds);
    }

    private AppDialogs() {
    }

    /**
     * Figma "pop-up 6997": deletar registro (usado para itens sem motivo obrigatório).
     */
    public static void showDeleteConfirm(Context context, @Nullable Runnable onConfirm) {
        showConfirm(context, R.drawable.img_dialog_illustration_hires, R.string.app_dialog_delete_title,
                R.string.app_dialog_delete_body, R.string.app_delete, onConfirm);
    }

    /**
     * Figma "pop-up 7041": salvar alterações.
     */
    public static void showSaveConfirm(Context context, @Nullable Runnable onConfirm) {
        showConfirm(context, R.drawable.img_dialog_save_hires, R.string.app_dialog_save_title,
                R.string.app_dialog_save_body, R.string.app_save, onConfirm);
    }

    /**
     * Voltar com alterações pendentes: pergunta se salva antes de sair.
     */
    public static void showDiscardConfirm(Context context, Runnable onSave, Runnable onDiscard) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_app_confirm, null, false);
        ((ImageView) content.findViewById(R.id.ivDialogImage)).setImageResource(R.drawable.img_dialog_save_hires);
        ((TextView) content.findViewById(R.id.tvDialogTitle)).setText(R.string.app_dialog_save_title);
        ((TextView) content.findViewById(R.id.tvDialogBody)).setText(R.string.app_dialog_discard_body);
        MaterialButton secondary = content.findViewById(R.id.btnDialogSecondary);
        MaterialButton primary = content.findViewById(R.id.btnDialogPrimary);
        secondary.setText(R.string.app_dialog_discard_confirm);
        primary.setText(R.string.app_save);

        AlertDialog dialog = build(context, content, R.dimen.app_dialog_width);
        secondary.setOnClickListener(v -> {
            dialog.dismiss();
            onDiscard.run();
        });
        primary.setOnClickListener(v -> {
            dialog.dismiss();
            onSave.run();
        });
        dialog.show();
    }

    /**
     * Figma "pop-up 7049": sair da conta.
     */
    public static void showLogoutConfirm(Context context, Runnable onConfirm) {
        showConfirm(context, R.drawable.img_dialog_logout_hires, R.string.app_dialog_logout_title,
                R.string.app_dialog_logout_body, R.string.app_dialog_logout_confirm, onConfirm);
    }

    private static void showConfirm(
            Context context, @DrawableRes int imageRes, @StringRes int titleRes,
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
            if (onConfirm != null) {
                onConfirm.run();
            }
        });
        dialog.show();
    }

    /**
     * Figma "pop-up 7005": excluir solicitação informando o motivo.
     */
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
            if (onConfirm != null) {
                onConfirm.run();
            }
        });
        dialog.show();
    }

    /**
     * Figma "pop-up 7015": editar solicitação. Salvar abre "Salvar alterações?"; Cancelar com mudanças pergunta.
     */
    public static void showEditRequest(Context context, ProblemaItem item) {
        showEditRequest(context, item, null, (EditRequestCallback) null);
    }

    public static void showEditRequest(Context context, ProblemaItem item, @Nullable Runnable onSave) {
        showEditRequest(context, item, null, onSave == null ? null : (title, description, location, device, deleted) -> onSave.run());
    }

    public static void showEditRequest(Context context, ProblemaItem item, @Nullable EditRequestCallback onSave) {
        showEditRequest(context, item, null, onSave);
    }

    public static void showEditRequest(
            Context context, ProblemaItem item, Problema details,
            @Nullable EditRequestCallback onSave) {
        showEditRequest(context, item, details, null, null, onSave);
    }

    public static void showEditRequest(
            Context context, ProblemaItem item, Problema details,
            List<OpcaoCadastro> locations, List<OpcaoCadastro> devices,
            @Nullable EditRequestCallback onSave) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_app_edit_request, null, false);
        EditText title = content.findViewById(R.id.etEditTitle);
        EditText description = content.findViewById(R.id.etEditDescription);
        EditText location = content.findViewById(R.id.etEditLocation);
        EditText device = content.findViewById(R.id.etEditDevice);
        title.setText(item.title);
        description.setText(item.description);
        location.setText(item.location);
        device.setText(item.device);
        setEditOptions(content, locations, devices);
        LinearLayout photosLayout = content.findViewById(R.id.layoutPhotos);
        View addPhoto = content.findViewById(R.id.btnAddPhoto);
        List<Long> deletedPhotoIds = new ArrayList<>();
        View photo = content.findViewById(R.id.layoutEditPhoto1);
        photo.setVisibility(View.GONE);
        if (details != null && !details.fotos.isEmpty()) {
            for (com.aula.fixa.domain.model.solicitacao.FotoProblema photoModel : details.fotos) {
                long photoId = photoModel.id;
                FrameLayout frame = new FrameLayout(context);
                LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(100, -1);
                frameParams.setMargins(0, 0, 8, 0);
                frame.setLayoutParams(frameParams);
                ImageView image = new ImageView(context);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                frame.addView(image, new FrameLayout.LayoutParams(-1, 90));
                ImageButton remove = new ImageButton(context);
                remove.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                remove.setContentDescription("Remover foto");
                remove.setBackgroundColor(0xFFE6545A);
                remove.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                FrameLayout.LayoutParams removeParams = new FrameLayout.LayoutParams(32, 32, android.view.Gravity.TOP | android.view.Gravity.END);
                frame.addView(remove, removeParams);
                Glide.with(context).load(photoModel.url)
                        .placeholder(R.drawable.img_edit_photo_lamp).into(image);
                remove.setOnClickListener(v -> {
                    deletedPhotoIds.add(photoId);
                    photosLayout.removeView(frame);
                });
                photosLayout.addView(frame, 0);
            }
        } else if (item.photo != 0) {
            photo.setVisibility(View.VISIBLE);
            ((ImageView) content.findViewById(R.id.ivEditPhoto1)).setImageResource(item.photo);
        }
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
        content.findViewById(R.id.btnEditSave).setOnClickListener(v -> showSaveConfirm(context, () -> {
            if (onSave != null) {
                onSave.onSave(title.getText().toString().trim(), description.getText().toString().trim(),
                        location.getText().toString().trim(), device.getText().toString().trim(), deletedPhotoIds);
            }
            close.run();
        }));
        dialog.setOnKeyListener((d, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.getAction() == android.view.KeyEvent.ACTION_UP) {
                askIfDirty.run();
                return true;
            }
            return false;
        });
        dialog.show();
    }

    public static void setEditOptions(View content, List<OpcaoCadastro> locations, List<OpcaoCadastro> devices) {
        AutoCompleteTextView location = content.findViewById(R.id.etEditLocation);
        AutoCompleteTextView device = content.findViewById(R.id.etEditDevice);
        location.setKeyListener(null);
        location.setFocusable(false);
        location.setCursorVisible(false);
        device.setKeyListener(null);
        device.setFocusable(false);
        device.setCursorVisible(false);
        if (locations != null) {
            location.setAdapter(new ArrayAdapter<>(content.getContext(), R.layout.item_app_dropdown, locations));
        }
        if (devices != null) {
            device.setAdapter(new ArrayAdapter<>(content.getContext(), R.layout.item_app_dropdown, devices));
        }
        location.setOnClickListener(v -> location.showDropDown());
        device.setOnClickListener(v -> device.showDropDown());
        location.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                location.showDropDown();
            }
            return false;
        });
        device.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                device.showDropDown();
            }
            return false;
        });
    }


    private static String snapshot(EditText... fields) {
        StringBuilder sb = new StringBuilder();
        for (EditText f : fields) {
            sb.append(f.getText()).append('\u0000');
        }
        return sb.toString();
    }

    /**
     * Largura do Figma limitada a 90% da tela para caber em qualquer aparelho.
     */
    private static AlertDialog build(Context context, View content, int widthRes) {
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_FIXA_App_Dialog)
                .setView(content)
                .create();
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() == null) {
                return;
            }
            int desired = context.getResources().getDimensionPixelSize(widthRes);
            int max = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9f);
            dialog.getWindow().setLayout(Math.min(desired, max), WindowManager.LayoutParams.WRAP_CONTENT);
        });
        return dialog;
    }
}
