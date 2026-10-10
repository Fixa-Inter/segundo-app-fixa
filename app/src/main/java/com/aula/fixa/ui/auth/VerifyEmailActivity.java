package com.aula.fixa.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;

public class VerifyEmailActivity extends RecoveryActivity {

    private static final String EXTRA_EMAIL = "email";
    /** Quantos caracteres do usuário ficam visíveis antes da máscara (Figma: "rapha******gmail.com"). */
    private static final int VISIBLE_PREFIX = 5;

    private final EditText[] digits = new EditText[6];

    public VerifyEmailActivity() {
        super(R.layout.activity_verify_email);
    }

    public static Intent intent(@NonNull Context context, @Nullable String email) {
        return new Intent(context, VerifyEmailActivity.class).putExtra(EXTRA_EMAIL, email);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppText.setTwoTone(findViewById(R.id.tvTitle), R.string.recovery_sent_title_lead,
                R.string.recovery_sent_title_rest, R.color.app_primary_dark);
        ((TextView) findViewById(R.id.tvDescription)).setText(
                getString(R.string.recovery_sent_description, mask(getIntent().getStringExtra(EXTRA_EMAIL))));

        int[] ids = {R.id.etCode1, R.id.etCode2, R.id.etCode3, R.id.etCode4, R.id.etCode5, R.id.etCode6};
        for (int i = 0; i < ids.length; i++) {
            digits[i] = findViewById(ids[i]);
            digits[i].setContentDescription(getString(R.string.recovery_code_digit, i + 1));
            bindDigit(i);
        }

        TextView resend = findViewById(R.id.tvResend);
        String lead = getString(R.string.recovery_resend_lead);
        SpannableStringBuilder text = new SpannableStringBuilder(lead).append(' ')
                .append(getString(R.string.recovery_resend_action));
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.app_primary)),
                lead.length() + 1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        resend.setText(text);
        resend.setOnClickListener(v -> {
            for (EditText digit : digits) digit.setText("");
            digits[0].requestFocus();
            Toast.makeText(this, R.string.recovery_resend_done, Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnConfirmCode).setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class)));
    }

    /** Ao digitar avança para a próxima caixa; apagar numa caixa vazia volta para a anterior. */
    private void bindDigit(int index) {
        EditText digit = digits[index];
        digit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                digit.setActivated(s.length() == 1);
                if (s.length() == 1 && index < digits.length - 1) digits[index + 1].requestFocus();
            }
        });
        digit.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN
                    && digit.getText().length() == 0 && index > 0) {
                digits[index - 1].setText("");
                digits[index - 1].requestFocus();
                return true;
            }
            return false;
        });
    }

    private String mask(@Nullable String email) {
        if (TextUtils.isEmpty(email) || !email.contains("@")) return getString(R.string.recovery_sent_sample_email);
        String user = email.substring(0, email.indexOf('@'));
        String domain = email.substring(email.indexOf('@') + 1);
        return user.substring(0, Math.min(VISIBLE_PREFIX, user.length())) + "******" + domain;
    }
}
