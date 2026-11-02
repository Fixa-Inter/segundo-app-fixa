package com.aula.fixa.ui.config;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/** Applies the app's page-heading palette: first word blue, remaining words deep navy. */
public final class TwoToneTitleStyler {
    private TwoToneTitleStyler() {}

    public static void apply(View root) {
        if (root == null) return;
        applyRecursive(root, false);
    }

    private static void applyRecursive(View view, boolean insideCard) {
        if (view instanceof TextView && !(view instanceof EditText)) {
            TextView text = (TextView) view;
            if (isSolidNavyHeading(text)) {
                applySolidNavy(text);
            } else if (!insideCard && isPageTitle(text)) {
                applyColors(text);
            }
        }
        if (!(view instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) view;
        boolean childInsideCard = insideCard || group instanceof RecyclerView
                || group instanceof MaterialCardView || isTaggedCard(group);
        for (int i = 0; i < group.getChildCount(); i++) {
            applyRecursive(group.getChildAt(i), childInsideCard);
        }
    }

    private static boolean isTaggedCard(View view) {
        Object tag = view.getTag();
        return tag instanceof String && ((String) tag).startsWith("card:");
    }

    private static boolean isSolidNavyHeading(TextView view) {
        if (view instanceof android.widget.Button) return false;
        String value = view.getText() == null ? ""
                : view.getText().toString().trim().toLowerCase(Locale.ROOT);
        return value.equals("meu perfil") || value.equals("editar perfil")
                || value.equals("configurações") || value.equals("faq")
                || value.equals("sobre o app") || value.equals("exportar excel")
                || value.equals("informações pessoais") || value.equals("documentos")
                || value.startsWith("o que é") || value.equals("notificações")
                || value.equals("hoje") || value.equals("última semana");
    }

    private static void applySolidNavy(TextView view) {
        CharSequence text = view.getText();
        if (text instanceof Spannable) {
            Spannable spannable = (Spannable) text;
            ForegroundColorSpan[] spans = spannable.getSpans(0, spannable.length(), ForegroundColorSpan.class);
            for (ForegroundColorSpan span : spans) spannable.removeSpan(span);
        }
        view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.app_primary_deep));
    }

    private static boolean isPageTitle(TextView view) {
        CharSequence value = view.getText();
        if (value == null || value.toString().trim().isEmpty()) return false;
        if (android.graphics.Color.luminance(view.getCurrentTextColor()) > 0.82f) return false;
        String name = "";
        if (view.getId() != View.NO_ID) {
            try { name = view.getResources().getResourceEntryName(view.getId()).toLowerCase(Locale.ROOT); }
            catch (Exception ignored) {}
        }
        if (name.contains("filter") || name.contains("step") || name.contains("summary")
                || name.endsWith("label")) return false;
        Object tag = view.getTag();
        if ("text:Manu".equals(tag) || "text:Sair".equals(tag)) return false;
        boolean namedTitle = name.contains("title") || name.contains("heading") || name.contains("header");
        boolean largeCatalogTitle = name.startsWith("ops_") && tag instanceof String
                && ((String) tag).startsWith("text:")
                && view.getTextSize() / view.getResources().getDisplayMetrics().scaledDensity >= 24f;
        return namedTitle || largeCatalogTitle;
    }

    private static void applyColors(TextView view) {
        String text = view.getText().toString();
        int start = 0;
        while (start < text.length() && Character.isWhitespace(text.charAt(start))) start++;
        int end = start;
        while (end < text.length() && !Character.isWhitespace(text.charAt(end))) end++;
        if (start == end) return;

        int light = ContextCompat.getColor(view.getContext(), R.color.app_primary);
        int dark = ContextCompat.getColor(view.getContext(), R.color.app_primary_deep);
        if (hasExpectedColors(view.getText(), start, end, text.length(), light, dark)) return;

        SpannableStringBuilder styled = new SpannableStringBuilder(view.getText());
        ForegroundColorSpan[] previous = styled.getSpans(0, styled.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : previous) styled.removeSpan(span);
        styled.setSpan(new ForegroundColorSpan(light), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (end < styled.length()) {
            styled.setSpan(new ForegroundColorSpan(dark), end, styled.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        view.setText(styled, TextView.BufferType.SPANNABLE);
    }

    private static boolean hasExpectedColors(CharSequence text, int firstStart, int firstEnd,
                                              int length, int light, int dark) {
        if (!(text instanceof Spanned)) return false;
        Spanned spanned = (Spanned) text;
        boolean firstCorrect = false;
        boolean restCorrect = length == firstEnd;
        for (ForegroundColorSpan span : spanned.getSpans(0, length, ForegroundColorSpan.class)) {
            int spanStart = spanned.getSpanStart(span), spanEnd = spanned.getSpanEnd(span);
            int color = span.getForegroundColor();
            if (color == light && spanStart <= firstStart && spanEnd >= firstEnd) firstCorrect = true;
            if (length > firstEnd && color == dark && spanStart <= firstEnd && spanEnd == length) restCorrect = true;
        }
        return firstCorrect && restCorrect;
    }
}
