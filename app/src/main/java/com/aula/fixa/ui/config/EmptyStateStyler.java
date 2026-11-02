package com.aula.fixa.ui.config;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.aula.fixa.R;
import android.view.View;
import android.view.ViewGroup;

/** Aplica a ilustração e a tipografia padrão aos estados sem resultados/sem dados. */
public final class EmptyStateStyler {
    private EmptyStateStyler() { }

    public static void apply(TextView message) {
        if (message == null) return;
        Context context = message.getContext();
        int imageSize = context.getResources().getDimensionPixelSize(R.dimen.app_empty_state_image_size);
        int gap = context.getResources().getDimensionPixelSize(R.dimen.app_empty_state_gap);
        Drawable illustration = ContextCompat.getDrawable(context, R.drawable.img_empty_results_hires);
        if (illustration != null) {
            illustration = illustration.mutate();
            illustration.setBounds(0, 0, imageSize, imageSize);
        }
        message.setCompoundDrawables(null, illustration, null, null);
        message.setCompoundDrawablePadding(gap);
        TextViewCompat.setTextAppearance(message, R.style.TextAppearance_FIXA_App_EmptyState);
        message.setGravity(Gravity.CENTER);
        message.setIncludeFontPadding(false);
        message.setMinHeight(imageSize + gap + context.getResources().getDimensionPixelSize(R.dimen.app_empty_state_text_min_height));
        message.setMaxLines(4);
    }

    public static TextView create(Context context, CharSequence text) {
        TextView message = new TextView(context);
        apply(message);
        message.setText(text);
        return message;
    }

    public static TextView showInConstraint(ConstraintLayout parent, int anchorId,
                                            CharSequence text, Runnable onRetry) {
        removeFromConstraint(parent);
        FrameLayout overlay = new FrameLayout(parent.getContext());
        overlay.setTag(EmptyStateStyler.class.getName());
        overlay.setClickable(true);
        overlay.setFocusable(true);
        Drawable background = parent.getBackground();
        ViewParent ancestor = parent.getParent();
        while (background == null && ancestor instanceof View) {
            background = ((View) ancestor).getBackground();
            ancestor = ancestor.getParent();
        }
        if (background != null && background.getConstantState() != null) {
            overlay.setBackground(background.getConstantState().newDrawable().mutate());
        } else if (background instanceof ColorDrawable) {
            overlay.setBackground(new ColorDrawable(((ColorDrawable) background).getColor()));
        } else if (background != null) {
            overlay.setBackground(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        TextView message = create(parent.getContext(), text);
        FrameLayout.LayoutParams messageParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        int side = parent.getResources().getDimensionPixelSize(R.dimen.app_events_title_margin_start);
        messageParams.setMargins(side, 0, side, 0);
        overlay.addView(message, messageParams);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(0, 0);
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToBottom = anchorId;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        int margin = parent.getResources().getDimensionPixelSize(R.dimen.app_empty_state_gap);
        params.setMargins(margin, margin, margin, margin);
        if (onRetry == null) {
            message.setOnClickListener(null);
            overlay.setOnClickListener(null);
        } else {
            View.OnClickListener retry = v -> onRetry.run();
            message.setOnClickListener(retry);
            overlay.setOnClickListener(retry);
        }
        parent.addView(overlay, params);
        overlay.bringToFront();
        return message;
    }

    public static TextView showBelowBack(View root, CharSequence text, Runnable onRetry) {
        ConstraintLayout parent = findConstraintLayout(root);
        if (parent == null) return null;
        View back = parent.findViewWithTag("image:arrow-left");
        if (back == null) back = parent.findViewById(R.id.btnBack);
        if (back == null) back = parent.getChildCount() == 0 ? null : parent.getChildAt(0);
        if (back == null) return null;
        if (back.getId() == View.NO_ID) back.setId(View.generateViewId());
        return showInConstraint(parent, back.getId(), text, onRetry);
    }

    public static void removeFromRoot(View root) {
        ConstraintLayout parent = findConstraintLayout(root);
        removeFromConstraint(parent);
    }

    public static void removeFromConstraint(ConstraintLayout parent) {
        if (parent == null) return;
        View previous = parent.findViewWithTag(EmptyStateStyler.class.getName());
        if (previous != null) parent.removeView(previous);
    }

    private static ConstraintLayout findConstraintLayout(View view) {
        if (view instanceof ConstraintLayout) return (ConstraintLayout) view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                ConstraintLayout found = findConstraintLayout(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
}
