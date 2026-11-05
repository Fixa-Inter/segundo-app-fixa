package com.aula.fixa.ui.config;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.core.content.ContextCompat;

import com.aula.fixa.R;

/** Anel de carregamento da tela, com trilha clara e segmento azul em rotação. */
final class AppLoadingSpinner extends View {
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arc = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private final float strokeWidth;
    private float startAngle = -90f;
    private ValueAnimator animator;

    AppLoadingSpinner(Context context) {
        super(context);
        float density = context.getResources().getDisplayMetrics().density;
        strokeWidth = 2.4f * density;
        track.setStyle(Paint.Style.STROKE);
        track.setStrokeWidth(strokeWidth);
        track.setColor(0xFFD9E0E8);
        arc.setStyle(Paint.Style.STROKE);
        arc.setStrokeWidth(strokeWidth);
        arc.setStrokeCap(Paint.Cap.ROUND);
        arc.setColor(ContextCompat.getColor(context, R.color.app_primary));
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(1250L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(value -> {
            startAngle = (float) value.getAnimatedValue() - 90f;
            invalidate();
        });
        animator.start();
    }

    @Override protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float inset = strokeWidth / 2f + 1f;
        bounds.set(inset, inset, getWidth() - inset, getHeight() - inset);
        canvas.drawArc(bounds, 0f, 360f, false, track);
        canvas.drawArc(bounds, startAngle, 88f, false, arc);
    }
}
