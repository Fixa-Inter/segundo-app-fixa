package com.aula.fixa.ui.config;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aula.fixa.R;

import java.util.IdentityHashMap;
import java.util.Map;

/** Loader de página limitado à área do fragmento; a barra de navegação permanece interativa. */
public final class AppLoading {
    private static final Map<Dialog, View> ACTIVE_OVERLAYS = new IdentityHashMap<>();

    private AppLoading() { }

    public static boolean isActive() {
        synchronized (ACTIVE_OVERLAYS) {
            return !ACTIVE_OVERLAYS.isEmpty();
        }
    }

    public static Dialog show(Context context) {
        Dialog handle = new Dialog(context);
        Activity activity = activityOf(context);
        if (activity == null) return handle;

        FrameLayout content = activity.findViewById(android.R.id.content);
        if (content == null) return handle;

        FrameLayout overlay = new FrameLayout(activity);
        overlay.setBackgroundColor(Color.WHITE);
        overlay.setClickable(true);
        overlay.setFocusable(true);
        overlay.setFocusableInTouchMode(true);
        overlay.setOnTouchListener((view, event) -> true);
        overlay.addView(loaderContent(activity), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, contentHeight(activity, content), Gravity.TOP);
        content.addView(overlay, params);
        content.post(() -> {
            if (overlay.getParent() == content) {
                ViewGroup.LayoutParams current = overlay.getLayoutParams();
                current.height = contentHeight(activity, content);
                overlay.setLayoutParams(current);
            }
        });
        synchronized (ACTIVE_OVERLAYS) {
            ACTIVE_OVERLAYS.put(handle, overlay);
        }
        return handle;
    }

    private static View loaderContent(Activity activity) {
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);

        int spinnerSize = dp(activity, 56);
        FrameLayout spinnerFrame = new FrameLayout(activity);
        AppLoadingSpinner spinner = new AppLoadingSpinner(activity);
        spinnerFrame.addView(spinner, new FrameLayout.LayoutParams(spinnerSize, spinnerSize));

        ImageView logo = new ImageView(activity);
        logo.setImageResource(R.drawable.ic_launcher_foreground);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int logoSize = dp(activity, 27);
        spinnerFrame.addView(logo, new FrameLayout.LayoutParams(logoSize, logoSize, Gravity.CENTER));
        content.addView(spinnerFrame, new LinearLayout.LayoutParams(spinnerSize, spinnerSize));

        TextView label = new TextView(activity);
        label.setText("Carregando página...");
        label.setTextColor(Color.rgb(83, 88, 97));
        label.setTextSize(14);
        label.setGravity(Gravity.CENTER);
        label.setIncludeFontPadding(false);
        try {
            label.setTypeface(activity.getResources().getFont(R.font.poppins_regular));
        } catch (Exception ignored) {
            label.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        }
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = dp(activity, 14);
        content.addView(label, labelParams);
        return content;
    }

    private static int contentHeight(Activity activity, FrameLayout content) {
        View appRoot = activity.findViewById(R.id.main);
        if (appRoot == null) appRoot = activity.findViewById(R.id.operationsRoot);
        View navigation = activity.findViewById(R.id.bottomNav);
        if (navigation == null) navigation = activity.findViewById(R.id.operationsBar);

        int[] contentPosition = new int[2];
        content.getLocationOnScreen(contentPosition);
        if (navigation != null && navigation.getVisibility() == View.VISIBLE && navigation.getHeight() > 0) {
            int[] navigationPosition = new int[2];
            navigation.getLocationOnScreen(navigationPosition);
            return Math.max(0, navigationPosition[1] - contentPosition[1]);
        }
        if (appRoot != null && appRoot.getHeight() > 0) {
            int[] rootPosition = new int[2];
            appRoot.getLocationOnScreen(rootPosition);
            return Math.max(0, rootPosition[1] + appRoot.getHeight() - contentPosition[1]);
        }
        return content.getHeight() > 0 ? content.getHeight() : ViewGroup.LayoutParams.MATCH_PARENT;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    @Nullable
    private static Activity activityOf(Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) return (Activity) current;
            Context next = ((ContextWrapper) current).getBaseContext();
            if (next == current) break;
            current = next;
        }
        return current instanceof Activity ? (Activity) current : null;
    }

    public static void hide(Dialog handle) {
        if (handle == null) return;
        View overlay;
        synchronized (ACTIVE_OVERLAYS) {
            overlay = ACTIVE_OVERLAYS.remove(handle);
        }
        removeOverlay(overlay);
    }

    /** Limpa o loader anterior antes da navegação para outra aba. */
    public static void hideAll() {
        View[] overlays;
        synchronized (ACTIVE_OVERLAYS) {
            overlays = ACTIVE_OVERLAYS.values().toArray(new View[0]);
            ACTIVE_OVERLAYS.clear();
        }
        for (View overlay : overlays) removeOverlay(overlay);
    }

    private static void removeOverlay(View overlay) {
        if (overlay != null && overlay.getParent() instanceof ViewGroup) {
            ((ViewGroup) overlay.getParent()).removeView(overlay);
        }
    }
}
