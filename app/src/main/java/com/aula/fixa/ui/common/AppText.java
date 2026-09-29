package com.aula.fixa.ui.common;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;

import com.aula.fixa.R;

/**
 * Textos bicolores/bipeso do Figma via HTML nas strings. Como o projeto usa um arquivo por peso
 * (poppins_regular/semibold/bold), as tags são convertidas em TypefaceSpan com a fonte real:
 * <b> = Poppins Bold (700), <u> = Poppins SemiBold (600), <i> = Poppins Regular (400).
 * <font color> continua funcionando normalmente.
 */
public final class AppText {

    private AppText() {}

    public static void setHtml(@NonNull TextView view, @StringRes int res, Object... args) {
        String raw = args.length == 0 ? view.getContext().getString(res) : view.getContext().getString(res, args);
        setHtml(view, raw);
    }

    public static void setHtml(@NonNull TextView view, @NonNull String html) {
        Spanned parsed = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
        SpannableStringBuilder out = new SpannableStringBuilder(parsed);

        for (StyleSpan span : out.getSpans(0, out.length(), StyleSpan.class)) {
            int font = span.getStyle() == Typeface.BOLD ? R.font.poppins_bold : R.font.poppins_regular;
            replace(view, out, span, font);
        }
        for (UnderlineSpan span : out.getSpans(0, out.length(), UnderlineSpan.class)) {
            replace(view, out, span, R.font.poppins_semibold);
        }
        view.setText(out);
    }

    private static void replace(TextView view, SpannableStringBuilder out, Object span, @FontRes int font) {
        int start = out.getSpanStart(span);
        int end = out.getSpanEnd(span);
        out.removeSpan(span);
        Typeface typeface = ResourcesCompat.getFont(view.getContext(), font);
        if (typeface != null) {
            out.setSpan(new TypefaceSpan(typeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
