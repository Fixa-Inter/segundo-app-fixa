package com.aula.fixa.ui.adapter.chat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.HorizontalScrollView;
import android.animation.ObjectAnimator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.chat.ChatEntry;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Renderização textual segura: não interpreta HTML/markdown como comandos.
 */
public final class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_MESSAGE = 0;
    private static final int TYPE_TYPING = 1;
    private final List<ChatEntry> entries;

    public ChatMessageAdapter(List<ChatEntry> entries) {
        this.entries = entries;
    }

    @Override
    public int getItemViewType(int position) {
        return "typing-indicator".equals(entries.get(position).localStatus) ? TYPE_TYPING : TYPE_MESSAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        if (type == TYPE_TYPING) {
            return createTypingHolder(parent);
        }
        return createMessageHolder(parent);
    }

    private Holder createMessageHolder(ViewGroup parent) {
        FrameLayout row = new FrameLayout(parent.getContext());
        row.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setPadding(dp(parent, 14), dp(parent, 9), dp(parent, 14), dp(parent, 9));
        ImageView avatar = new ImageView(parent.getContext());
        FrameLayout.LayoutParams avatarParams = new FrameLayout.LayoutParams(dp(parent, 34), dp(parent, 34), Gravity.START | Gravity.BOTTOM);
        row.addView(avatar, avatarParams);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        TextView bubble = new TextView(parent.getContext());
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bubbleParams.setMargins(dp(parent, 8), 0, dp(parent, 8), 0);
        bubble.setLayoutParams(bubbleParams);
        bubble.setMaxWidth((int) (parent.getResources().getDisplayMetrics().widthPixels * .78f));
        bubble.setTextSize(15);
        bubble.setTextColor(Color.rgb(24, 28, 40));
        bubble.setPadding(dp(parent, 14), dp(parent, 11), dp(parent, 14), dp(parent, 6));
        TextView time = new TextView(parent.getContext());
        time.setTextSize(10);
        time.setTextColor(Color.rgb(105, 112, 126));
        LinearLayout column = new LinearLayout(parent.getContext());
        column.setOrientation(LinearLayout.VERTICAL);
        column.addView(bubble);
        column.addView(time);
        FrameLayout.LayoutParams columnParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.START | Gravity.BOTTOM);
        row.addView(column, columnParams);
        return new Holder(row, avatar, column, bubble, time);
    }

    private TypingHolder createTypingHolder(ViewGroup parent) {
        LinearLayout row = new LinearLayout(parent.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.BOTTOM);
        row.setPadding(dp(parent, 14), dp(parent, 5), dp(parent, 14), dp(parent, 5));
        row.setContentDescription(parent.getContext().getString(R.string.app_chat_typing));
        row.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        ImageView avatar = new ImageView(parent.getContext());
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(parent, 34), dp(parent, 34)));
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(avatar).load(R.drawable.pensativo).circleCrop().into(avatar);
        LinearLayout bubble = new LinearLayout(parent.getContext());
        bubble.setGravity(Gravity.CENTER);
        bubble.setOrientation(LinearLayout.HORIZONTAL);
        bubble.setPadding(dp(parent, 13), dp(parent, 8), dp(parent, 13), dp(parent, 8));
        bubble.setBackground(background(Color.rgb(230, 239, 250), 18, 4, 18, 18));
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bubbleParams.setMargins(dp(parent, 8), 0, dp(parent, 8), 0);
        bubble.setLayoutParams(bubbleParams);
        List<TextView> dots = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            TextView dot = new TextView(parent.getContext());
            dot.setText("•");
            dot.setTextSize(18);
            dot.setTextColor(Color.rgb(21, 61, 125));
            dot.setIncludeFontPadding(false);
            dot.setPadding(dp(parent, 2), 0, dp(parent, 2), 0);
            bubble.addView(dot);
            dots.add(dot);
        }
        row.addView(avatar);
        row.addView(bubble);
        return new TypingHolder(row, dots);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TypingHolder) {
            ((TypingHolder) holder).start();
            return;
        }
        Holder h = (Holder) holder;
        ChatEntry entry = entries.get(position);
        boolean assistant = "assistant".equalsIgnoreCase(entry.role);
        if ("date-divider".equals(entry.localStatus)) {
            for (int i = h.column.getChildCount() - 2; i >= 1; i--) {
                h.column.removeViewAt(i);
            }
            h.avatar.setVisibility(View.GONE);
            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) h.column.getLayoutParams();
            p.width = ViewGroup.LayoutParams.MATCH_PARENT;
            p.gravity = Gravity.CENTER;
            p.leftMargin = 0;
            p.rightMargin = 0;
            h.column.setLayoutParams(p);
            h.column.setGravity(Gravity.CENTER);
            h.bubble.setBackgroundColor(Color.TRANSPARENT);
            h.bubble.setGravity(Gravity.CENTER);
            h.bubble.setTextColor(Color.rgb(105, 112, 126));
            h.bubble.setTextSize(12);
            h.bubble.setText(entry.text);
            h.time.setVisibility(View.GONE);
            return;
        }
        FrameLayout.LayoutParams columnParams = (FrameLayout.LayoutParams) h.column.getLayoutParams();
        columnParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        columnParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        columnParams.gravity = assistant ? Gravity.START | Gravity.BOTTOM : Gravity.END | Gravity.BOTTOM;
        columnParams.leftMargin = assistant ? dp(h.row, 42) : 0;
        columnParams.rightMargin = 0;
        h.column.setLayoutParams(columnParams);
        h.bubble.setTextSize(15);
        h.bubble.setGravity(Gravity.START);
        h.avatar.setVisibility(assistant ? View.VISIBLE : View.GONE);
        if (assistant) {
            Glide.with(h.avatar).load(emotionAsset(entry.emotion)).circleCrop().into(h.avatar);
            h.column.setGravity(Gravity.START);
            h.bubble.setBackground(background(Color.rgb(230, 239, 250), 18, 4, 18, 18));
            h.bubble.setTextColor(Color.rgb(24, 28, 40));
            h.time.setGravity(Gravity.START);
            h.time.setPadding(dp(h.row, 12), dp(h.row, 3), 0, 0);
        } else {
            h.column.setGravity(Gravity.END);
            h.bubble.setBackground(background(Color.rgb(21, 61, 125), 18, 4, 18, 18));
            h.bubble.setTextColor(Color.WHITE);
            h.time.setGravity(Gravity.END);
            h.time.setPadding(0, dp(h.row, 3), dp(h.row, 12), 0);
        }
        h.bubble.setTypeface(null, Typeface.NORMAL);
        h.bubble.setText(("Enviando".equalsIgnoreCase(entry.localStatus) || entry.localStatus == null
                ? "" : entry.localStatus + "\n") + (entry.text == null ? "" : entry.text));
        for (int i = h.column.getChildCount() - 2; i >= 1; i--) {
            h.column.removeViewAt(i);
        }
        if (entry.images != null && !entry.images.isEmpty()) {
            LinearLayout strip = new LinearLayout(h.row.getContext());
            strip.setOrientation(LinearLayout.HORIZONTAL);
            for (String url : entry.images) {
                ImageView photo = new ImageView(h.row.getContext());
                photo.setLayoutParams(new LinearLayout.LayoutParams(dp(h.row, 72), dp(h.row, 72)));
                photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                LinearLayout.LayoutParams photoParams = (LinearLayout.LayoutParams) photo.getLayoutParams();
                photoParams.setMargins(dp(h.row, 3), dp(h.row, 5), dp(h.row, 3), 0);
                Glide.with(photo).load(url).centerCrop().into(photo);
                strip.addView(photo);
            }
            HorizontalScrollView scroll = new HorizontalScrollView(h.row.getContext());
            scroll.setHorizontalScrollBarEnabled(false);
            scroll.addView(strip);
            h.column.addView(scroll, 1);
        }
        if (entry.localTimestamp > 0) {
            h.time.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(entry.localTimestamp)));
        } else {
            h.time.setText("");
        }
        h.time.setVisibility(entry.localTimestamp > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof TypingHolder) {
            ((TypingHolder) holder).stop();
        }
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    private static int emotionAsset(String emotion) {
        if (emotion == null) {
            return R.drawable.neutro;
        }
        switch (emotion.trim().toUpperCase(Locale.ROOT)) {
            case "CURIOSO":
                return R.drawable.curioso;
            case "PENSATIVO":
                return R.drawable.pensativo;
            case "CONFIRMACAO":
                return R.drawable.confirmacao;
            case "SUCESSO":
                return R.drawable.sucesso;
            case "EMPATICO":
                return R.drawable.empatico;
            case "ALERTA":
                return R.drawable.alerta;
            case "ERRO":
                return R.drawable.erro;
            case "BLOQUEADO":
                return R.drawable.bloqueado;
            case "CONSELHEIRO":
                return R.drawable.conselheiro;
            case "AGUARDANDO":
                return R.drawable.aguardando;
            case "CELEBRACAO":
                return R.drawable.celebracao;
            default:
                return R.drawable.neutro;
        }
    }

    private static GradientDrawable background(int color, int tl, int tr, int br, int bl) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadii(new float[]{dpValue(tl), dpValue(tl), dpValue(tr), dpValue(tr), dpValue(br), dpValue(br), dpValue(bl), dpValue(bl)});
        return d;
    }

    private static int dpValue(float value) {
        return (int) (value * android.content.res.Resources.getSystem().getDisplayMetrics().density + .5f);
    }

    private static int dp(View v, int value) {
        return (int) (value * v.getResources().getDisplayMetrics().density + .5f);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final FrameLayout row;
        final LinearLayout column;
        final ImageView avatar;
        final TextView bubble, time;

        Holder(View item, ImageView avatar, LinearLayout column, TextView bubble, TextView time) {
            super(item);
            this.row = (FrameLayout) item;
            this.avatar = avatar;
            this.column = column;
            this.bubble = bubble;
            this.time = time;
        }
    }

    private static final class TypingHolder extends RecyclerView.ViewHolder {
        private final List<TextView> dots;
        private final List<ObjectAnimator> animators = new java.util.ArrayList<>();

        TypingHolder(View item, List<TextView> dots) {
            super(item);
            this.dots = dots;
        }

        void start() {
            if (!animators.isEmpty()) {
                return;
            }
            float lift = 5 * itemView.getResources().getDisplayMetrics().density;
            for (int i = 0; i < dots.size(); i++) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(dots.get(i), View.TRANSLATION_Y, 0f, -lift, 0f);
                animator.setDuration(900);
                animator.setStartDelay(i * 180L);
                animator.setRepeatCount(ObjectAnimator.INFINITE);
                animator.start();
                animators.add(animator);
            }
        }

        void stop() {
            for (ObjectAnimator animator : animators) {
                animator.cancel();
            }
            animators.clear();
        }
    }
}
