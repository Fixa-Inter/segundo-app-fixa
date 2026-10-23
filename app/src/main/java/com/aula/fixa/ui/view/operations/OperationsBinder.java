package com.aula.fixa.ui.view.operations;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import com.aula.fixa.R;
import com.aula.fixa.adapter.operations.OperationsViewModel;
import com.aula.fixa.ui.config.TouchTargets;
import com.aula.fixa.ui.config.operations.ScreenCatalog;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Interações e acessibilidade dos componentes nativos compartilhados. */
public final class OperationsBinder {
    private final OperationsActivity activity;
    private final View root;
    private final JSONObject spec;
    private final String key;
    private final OperationsDialogFragment dialog;
    private final List<EditText> inputs = new ArrayList<>();
    private final List<View> cards = new ArrayList<>();

    public OperationsBinder(OperationsActivity activity, View root, String key,
            OperationsDialogFragment dialog) {
        this.activity = activity; this.root = root; this.key = key; this.dialog = dialog;
        spec = ScreenCatalog.get(activity).screen(key);
    }

    public void bind() {
        root.setClipToOutline(true);
        JSONArray bindings = spec.optJSONArray("bindings");
        OperationsActions actions = new OperationsActions(activity, this, key, dialog);
        for (int i = 0; bindings != null && i < bindings.length(); i++) {
            JSONObject item = bindings.optJSONObject(i);
            if (item == null) continue;
            int id = activity.getResources().getIdentifier(item.optString("id"), "id", activity.getPackageName());
            View view = root.findViewById(id);
            if (view == null) continue;
            String kind = item.optString("kind");
            JSONArray crop=item.optJSONArray("crop");
            if(view instanceof android.widget.ImageView && crop!=null && crop.length()==4) {
                android.widget.ImageView image=(android.widget.ImageView)view;
                image.post(()->{
                    if(image.getDrawable()==null)return;
                    double width=item.optJSONArray("bounds").optDouble(2);
                    float factor=(float)(image.getWidth()/width);
                    android.graphics.Matrix matrix=new android.graphics.Matrix();
                    matrix.setScale((float)crop.optDouble(2)*factor/image.getDrawable().getIntrinsicWidth(),(float)crop.optDouble(3)*factor/image.getDrawable().getIntrinsicHeight());
                    matrix.postTranslate((float)crop.optDouble(0)*factor,(float)crop.optDouble(1)*factor);
                    image.setImageMatrix(matrix);
                });
            }
            if (view instanceof TextView) applyRich((TextView) view, item.optJSONArray("rich"));
            if(view instanceof TextView && !(view instanceof EditText)
                    && ((TextView)view).getMaxLines()==1
                    && (item.optJSONArray("rich")==null||item.optJSONArray("rich").length()==0)
                    && activity.getResources().getConfiguration().fontScale<=1.15f) {
                TextView text=(TextView)view;
                text.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override public void onLayoutChange(View v,int l,int t,int r,int b,int oldL,int oldT,int oldR,int oldB) {
                        if(r<=l)return;
                        text.removeOnLayoutChangeListener(this);
                        float width=r-l-text.getPaddingLeft()-text.getPaddingRight();
                        float painted=text.getPaint().measureText(text.getText().toString());
                        if(painted>width && width>0 && painted<width*1.06f)
                            text.setTextScaleX(width/(painted+1));
                    }
                });
            }
            if (view instanceof EditText) {
                EditText field = (EditText) view;
                String label = item.optString("label");
                String saved = activity.model.field(key, label);
                if (saved == null && spec.optString("name").startsWith("edit_")
                        && (label.startsWith("Nome") || label.startsWith("Título") || label.equals("Identificador"))
                        && !activity.model.selectedTitle().isEmpty()) saved=activity.model.selectedTitle();
                if (saved != null) field.setText(saved);
                activity.model.field(key,label,field.getText().toString());
                inputs.add(field);
                field.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        activity.model.field(key, label, s.toString());
                        if (label.equals("Pesquisar")) filter(s.toString());
                    }
                    @Override public void afterTextChanged(Editable e) {}
                });
                if (label.equals("Pesquisar")) field.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
                else if (label.equalsIgnoreCase("Email")) field.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                else if (label.equalsIgnoreCase("Telefone")) field.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
                else if (label.equalsIgnoreCase("Senha")) field.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                else if (label.contains("Periodicidade") || label.equals("Novo valor")) field.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                if(label.equals("Valor antigo")){field.setFocusable(false);field.setCursorVisible(false);}
                if (label.equals("Local") || label.equals("Prioridade") || label.equals("Competência") || label.contains("dificuldade") || label.equals("Categoria") || label.equals("Tipo") || label.equals("Campo obrigatório")) {
                    field.setShowSoftInputOnFocus(false);
                    field.setOnClickListener(v -> actions.choose(field, label));
                }
                if (label.equals("Data") || label.contains("nascimento") || label.contains("primeira abertura")) {
                    field.setShowSoftInputOnFocus(false);
                    field.setOnClickListener(v -> actions.date(field));
                }
                TouchTargets.expand(field);
            } else {
                if (kind.equals("card")) cards.add(view);
                Runnable action = actions.action(item, view);
                if (action != null) {
                    view.setClickable(true); view.setFocusable(true);
                    String label = item.optString("text", "");
                    if (label.isEmpty()) label = actions.description(item);
                    if (!label.isEmpty()) view.setContentDescription(label);
                    view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                    view.setOnClickListener(v -> {
                        if(kind.equals("card"))activity.model.selectEntry(OperationsViewModel.scope(key),item.optString("id"),item.optString("text"));
                        action.run();
                    });
                    TouchTargets.expand(view);
                }
            }
        }
        // O label permanece ligado ao campo para leitores de tela.
        for (EditText input : inputs) label(root, input);
        String query = activity.model.field(key, "Pesquisar");
        if (query != null) filter(query);
        OperationsData.bind(activity,root,spec);
        if(activity.getResources().getConfiguration().fontScale>1.15f) growForAccessibility(root);
    }
    private void growForAccessibility(View view) {
        if(view instanceof TextView && !(view instanceof EditText)) {
            TextView text=(TextView)view;text.setMaxLines(Integer.MAX_VALUE);
            ViewGroup.LayoutParams params=view.getLayoutParams();
            if(params.height>0)text.setMinimumHeight(Math.max(text.getMinimumHeight(),params.height));
            params.height=ViewGroup.LayoutParams.WRAP_CONTENT;view.setLayoutParams(params);
        } else if(String.valueOf(view.getTag()).startsWith("card:")) {
            ViewGroup.LayoutParams params=view.getLayoutParams();if(params.height>0)view.setMinimumHeight(params.height);
            params.height=ViewGroup.LayoutParams.WRAP_CONTENT;view.setLayoutParams(params);
        }
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)growForAccessibility(((ViewGroup)view).getChildAt(i));
    }

    private void label(View view, EditText input) {
        if (view instanceof TextView && !(view instanceof EditText)
                && ((TextView) view).getText().toString().equals(fieldLabel(input))) {
            view.setLabelFor(input.getId());
        }
        if (view instanceof ViewGroup) for (int i=0; i<((ViewGroup)view).getChildCount(); i++) label(((ViewGroup)view).getChildAt(i), input);
    }

    private void applyRich(TextView view, JSONArray rich) {
        if (rich == null || rich.length() == 0) return;
        SpannableStringBuilder text = new SpannableStringBuilder();
        for (int i = 0; i < rich.length(); i++) {
            JSONObject run = rich.optJSONObject(i);
            if (run == null) continue;
            if (text.length() > 0) text.append(' ');
            int start = text.length();
            text.append(run.optString("text"));
            int font = activity.getResources().getIdentifier("poppins_" + run.optString("weight", "regular"), "font", activity.getPackageName());
            int color = activity.getResources().getIdentifier(run.optString("color"), "color", activity.getPackageName());
            Typeface typeface = font == 0 ? null : ResourcesCompat.getFont(activity, font);
            if (typeface != null) text.setSpan(new TypefaceSpan(typeface), start, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (color != 0) text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity, color)), start, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            float scale=(float)run.optDouble("scale",1);
            if(scale!=1f)text.setSpan(new android.text.style.RelativeSizeSpan(scale),start,text.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        view.setText(text);
    }

    private String text(View view) {
        String result = view instanceof TextView ? ((TextView)view).getText().toString() : "";
        if (view instanceof ViewGroup) for (int i=0; i<((ViewGroup)view).getChildCount();i++) result += " " + text(((ViewGroup)view).getChildAt(i));
        return result;
    }

    private void filter(String query) {
        String needle = query.toLowerCase(new Locale("pt", "BR"));
        for (View card : cards) card.setVisibility(text(card).toLowerCase(new Locale("pt", "BR")).contains(needle) ? View.VISIBLE : View.GONE);
    }

    public boolean validate() {
        boolean valid = true;
        for (EditText input : inputs) {
            String label = fieldLabel(input).toLowerCase(new Locale("pt", "BR"));
            if (label.equals("pesquisar") || label.contains("opcional") || label.contains("observaç") || label.contains("impedimento")) continue;
            if (input.getText().toString().trim().isEmpty()) {
                input.setError(activity.getString(R.string.ops_required));
                if (valid) input.requestFocus();
                valid = false;
            }
            if(label.equals("novo valor")) {
                try {int value=Integer.parseInt(input.getText().toString());if(value<0||value>10)throw new NumberFormatException();}
                catch(NumberFormatException e){input.setError(activity.getString(R.string.ops_score_range));valid=false;}
            }
        }
        return valid;
    }

    private String fieldLabel(EditText input) {
        String tag=String.valueOf(input.getTag());
        return tag.startsWith("field:")?tag.substring(6):String.valueOf(input.getContentDescription());
    }
}
