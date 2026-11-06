package com.aula.fixa.ui.config;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.method.LinkMovementMethod;

import com.aula.fixa.R;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Exibe os documentos legais locais em uma página rolável e formatada em Markdown. */
public final class AboutDocumentViewer {
    private static final int INK = Color.rgb(19, 9, 67);
    private static final int PAGE = Color.rgb(246, 248, 252);

    private AboutDocumentViewer() { }

    public static void show(Context context, String title, String assetName) {
        Dialog dialog = new Dialog(context);
        LinearLayout page = new LinearLayout(context);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(PAGE);

        LinearLayout toolbar = new LinearLayout(context);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(context, 12), dp(context, 8), dp(context, 20), dp(context, 8));
        toolbar.setBackgroundColor(Color.WHITE);
        ImageButton back = new ImageButton(context);
        back.setImageResource(R.drawable.figma_eee40);
        back.setContentDescription(context.getString(R.string.app_back));
        back.setBackgroundColor(Color.TRANSPARENT);
        toolbar.addView(back, new LinearLayout.LayoutParams(dp(context, 48), dp(context, 48)));
        TextView heading = new TextView(context);
        heading.setText(title);
        heading.setTextColor(INK);
        heading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        heading.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(context, R.font.poppins_semibold));
        heading.setMaxLines(2);
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        headingParams.setMarginStart(dp(context, 8));
        toolbar.addView(heading, headingParams);
        page.addView(toolbar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        TextView body = new TextView(context);
        body.setTextColor(INK);
        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        body.setLineSpacing(dp(context, 3), 1f);
        body.setPadding(dp(context, 24), dp(context, 24), dp(context, 24), dp(context, 36));
        String markdown = readAsset(context, assetName);
        Markwon markwon = Markwon.builder(context).usePlugin(TablePlugin.create(context)).build();
        markwon.setMarkdown(body, markdown);
        body.setMovementMethod(LinkMovementMethod.getInstance());
        scroll.addView(body, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        dialog.setContentView(page);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        back.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    private static String readAsset(Context context, String assetName) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open("legal/" + assetName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) content.append(line).append('\n');
        } catch (Exception error) {
            return "# Documento indisponível\n\nNão foi possível carregar este documento. Tente novamente mais tarde.";
        }
        return content.toString();
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
