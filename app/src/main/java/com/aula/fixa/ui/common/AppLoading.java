package com.aula.fixa.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/** Indicador padrão para chamadas de rede. */
public final class AppLoading {
    private AppLoading() {}

    public static Dialog show(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FrameLayout overlay = new FrameLayout(context);
        overlay.setBackgroundColor(Color.argb(150, 0, 0, 0));
        ProgressBar progress = new ProgressBar(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(72, 72, Gravity.CENTER);
        overlay.addView(progress, params);
        dialog.setContentView(overlay);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0f);
            window.setLayout(-1, -1);
        }
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setLayout(-1, -1);
        return dialog;
    }

    public static void hide(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }
}
