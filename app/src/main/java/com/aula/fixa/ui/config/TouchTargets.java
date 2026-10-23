package com.aula.fixa.ui.config;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aula.fixa.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Expande a área de toque de ícones pequenos para o mínimo de 48dp sem alterar o layout visual.
 * Suporta vários filhos no mesmo pai (o Android aceita só um TouchDelegate por View).
 */
public final class TouchTargets {

    private TouchTargets() {}

    public static void expand(@NonNull View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        parent.post(() -> {
            int min = view.getResources().getDimensionPixelSize(R.dimen.app_touch_target);
            Rect rect = new Rect();
            view.getHitRect(rect);
            rect.inset(-Math.max(0, (min - rect.width()) / 2), -Math.max(0, (min - rect.height()) / 2));

            TouchDelegate current = parent.getTouchDelegate();
            Composite composite = current instanceof Composite ? (Composite) current : new Composite(parent);
            composite.add(new TouchDelegate(rect, view));
            parent.setTouchDelegate(composite);
        });
    }

    private static final class Composite extends TouchDelegate {
        private final List<TouchDelegate> delegates = new ArrayList<>();

        Composite(View host) {
            super(new Rect(), host);
        }

        void add(TouchDelegate delegate) {
            delegates.add(delegate);
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {
            for (TouchDelegate delegate : delegates) {
                MotionEvent copy = MotionEvent.obtain(event);
                boolean handled = delegate.onTouchEvent(copy);
                copy.recycle();
                if (handled) return true;
            }
            return false;
        }
    }
}
