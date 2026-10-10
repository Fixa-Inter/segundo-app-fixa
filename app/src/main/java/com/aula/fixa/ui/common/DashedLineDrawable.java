package com.aula.fixa.ui.common;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Linha vertical tracejada centralizada nos bounds. O shape "line" do XML só desenha na horizontal,
 * por isso a timeline (Figma: traço 1.2dp, tracejado 2/2) usa este drawable.
 */
public class DashedLineDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DashedLineDrawable(@ColorInt int color, float strokeWidth, float dash, float gap) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setPathEffect(new DashPathEffect(new float[]{dash, gap}, 0));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect b = getBounds();
        float x = b.exactCenterX();
        canvas.drawLine(x, b.top, x, b.bottom, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
