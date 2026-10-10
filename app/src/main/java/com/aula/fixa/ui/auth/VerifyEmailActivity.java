package com.aula.fixa.ui.auth;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;

public class VerifyEmailActivity extends RecoveryActivity {

    public VerifyEmailActivity() {
        super(R.layout.activity_verify_email);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppText.setTwoTone(findViewById(R.id.tvTitle), R.string.recovery_sent_title_lead,
                R.string.recovery_sent_title_rest, R.color.app_primary_dark);
    }
}
