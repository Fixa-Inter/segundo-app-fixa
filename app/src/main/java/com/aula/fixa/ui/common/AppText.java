package com.aula.fixa.ui.common;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
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

    /**
     * Título em dois tons do Figma: primeira palavra em Poppins Bold (cor do TextView) e o restante em
     * Poppins SemiBold na cor informada. Usa só recursos, sem hex nas strings.
     */
    public static void setTwoTone(@NonNull TextView view, @StringRes int lead, @StringRes int rest,
                                  @ColorRes int restColor) {
        String leadText = view.getContext().getString(lead);
        SpannableStringBuilder out = new SpannableStringBuilder(leadText).append(' ')
                .append(view.getContext().getString(rest));
        int restStart = leadText.length() + 1;
        Typeface bold = ResourcesCompat.getFont(view.getContext(), R.font.poppins_bold);
        Typeface semibold = ResourcesCompat.getFont(view.getContext(), R.font.poppins_semibold);
        if (bold != null) out.setSpan(new TypefaceSpan(bold), 0, leadText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (semibold != null) out.setSpan(new TypefaceSpan(semibold), restStart, out.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.setSpan(new ForegroundColorSpan(ContextCompat.getColor(view.getContext(), restColor)),
                restStart, out.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
