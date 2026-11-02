package com.aula.fixa.ui.config;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.aula.fixa.R;

/** Shared field sizing, color and safe inner spacing for custom popups and sheets. */
public final class PopupFieldStyler {
    private static final int FIELD_HEIGHT_DP = 56;
    private static final int TEXTAREA_HEIGHT_DP = 112;
    private static final int POPUP_BOTTOM_SPACE_DP = 16;

    private PopupFieldStyler() {}

    public static void apply(View popup) {
        if (popup == null) return;
        styleFields(popup);
        if (popup instanceof ViewGroup) {
            ViewGroup root = (ViewGroup) popup;
            int bottom = dp(popup, POPUP_BOTTOM_SPACE_DP);
            if (root.getPaddingBottom() < bottom) {
                root.setPadding(root.getPaddingLeft(), root.getPaddingTop(), root.getPaddingRight(), bottom);
            }
        }
    }

    /** Keeps long delete names readable and moves both actions below the complete prompt. */
    public static void fitDeletePrompt(View popup, TextView prompt, View cancelLabel, View confirm) {
        if (popup == null || prompt == null || confirm == null) return;
        prompt.setSingleLine(false);
        prompt.setMaxLines(3);
        prompt.setEllipsize(null);
        prompt.setGravity(Gravity.CENTER);
        ViewGroup parent = prompt.getParent() instanceof ViewGroup ? (ViewGroup) prompt.getParent() : null;
        if (!(parent instanceof ConstraintLayout)) return;
        ConstraintLayout container = (ConstraintLayout) parent;
        View cancel = cancelLabel != null && cancelLabel.getParent() instanceof View
                ? (View) cancelLabel.getParent() : null;
        if (cancel == null) return;

        ViewGroup.LayoutParams promptRaw = prompt.getLayoutParams();
        if (promptRaw instanceof ConstraintLayout.LayoutParams) {
            ConstraintLayout.LayoutParams promptParams = (ConstraintLayout.LayoutParams) promptRaw;
            promptParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            prompt.setLayoutParams(promptParams);
        }
        if (!(cancel.getLayoutParams() instanceof ConstraintLayout.LayoutParams)
                || !(confirm.getLayoutParams() instanceof ConstraintLayout.LayoutParams)) return;

        ConstraintLayout.LayoutParams cancelParams = (ConstraintLayout.LayoutParams) cancel.getLayoutParams();
        cancelParams.topToTop = -1;
        cancelParams.bottomToBottom = -1;
        cancelParams.topToBottom = prompt.getId();
        cancelParams.topMargin = dp(popup, 24);
        cancelParams.setMarginStart(dp(popup, 35));
        cancelParams.setMarginEnd(dp(popup, 12));
        cancel.setLayoutParams(cancelParams);

        ConstraintLayout.LayoutParams confirmParams = (ConstraintLayout.LayoutParams) confirm.getLayoutParams();
        confirmParams.topToTop = cancel.getId();
        confirmParams.bottomToBottom = cancel.getId();
        confirmParams.topToBottom = -1;
        confirmParams.startToStart = -1;
        confirmParams.startToEnd = cancel.getId();
        confirmParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        confirmParams.setMarginStart(dp(popup, 12));
        confirmParams.setMarginEnd(dp(popup, 35));
        confirm.setLayoutParams(confirmParams);
    }

    private static void styleFields(View view) {
        if (view instanceof EditText) styleField((EditText) view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) styleFields(group.getChildAt(i));
        }
    }

    private static void styleField(EditText field) {
        int fieldHeight = dp(field, FIELD_HEIGHT_DP);
        ViewGroup.LayoutParams params = field.getLayoutParams();
        boolean multiline = (field.getInputType() & android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
        boolean existingLargeArea = multiline && params != null && params.height > fieldHeight;
        int desiredHeight = dp(field, existingLargeArea ? TEXTAREA_HEIGHT_DP : FIELD_HEIGHT_DP);

        View parent = field.getParent() instanceof View ? (View) field.getParent() : null;
        boolean fillsFieldContainer = params instanceof ConstraintLayout.LayoutParams
                && params.height == 0 && parent instanceof ViewGroup
                && field.getTag() instanceof String && ((String) field.getTag()).startsWith("field:");
        if (fillsFieldContainer && parent.getLayoutParams() != null) {
            ViewGroup.LayoutParams containerParams = parent.getLayoutParams();
            boolean largeContainer = multiline && containerParams.height > fieldHeight;
            containerParams.height = dp(field, largeContainer ? TEXTAREA_HEIGHT_DP : FIELD_HEIGHT_DP);
            parent.setLayoutParams(containerParams);
            parent.setBackgroundResource(R.drawable.bg_app_dialog_field);
            field.setBackgroundColor(Color.TRANSPARENT);
        } else {
            if (params != null && params.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                params.height = desiredHeight;
                field.setLayoutParams(params);
            }
            field.setBackgroundResource(R.drawable.bg_app_dialog_field);
        }

        field.setTextColor(ContextCompat.getColor(field.getContext(), R.color.app_text_black));
        field.setHintTextColor(ContextCompat.getColor(field.getContext(), R.color.app_text_placeholder_dark));
        field.setTypeface(ResourcesCompat.getFont(field.getContext(), R.font.poppins_regular));
        field.setIncludeFontPadding(false);
        int horizontalGravity = field.getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK;
        if (horizontalGravity == 0) horizontalGravity = Gravity.START;
        field.setGravity(multiline ? Gravity.TOP | horizontalGravity : Gravity.CENTER_VERTICAL | horizontalGravity);
    }

    private static int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }
}
