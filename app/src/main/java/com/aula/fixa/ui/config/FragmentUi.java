package com.aula.fixa.ui.config;

import android.view.View;

import androidx.fragment.app.Fragment;

/** Safe UI dispatch for callbacks that may finish after a fragment leaves the screen. */
public final class FragmentUi {
    private FragmentUi() {}

    public static void runIfViewActive(Fragment fragment, View view, Runnable action) {
        if (view == null) return;
        view.post(() -> {
            if (fragment.isAdded() && fragment.getView() == view) action.run();
        });
    }
}
