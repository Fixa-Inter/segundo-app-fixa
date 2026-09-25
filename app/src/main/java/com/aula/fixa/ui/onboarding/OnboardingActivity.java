package com.aula.fixa.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aula.fixa.R;
import com.aula.fixa.ui.auth.LoginActivity;
import com.aula.fixa.ui.common.ImmersiveActivity;

public class OnboardingActivity extends ImmersiveActivity {

    private static final String STATE_CURRENT_STEP = "current_onboarding_step";
    private static final int FIRST_STEP = 0;
    private static final int SECOND_STEP = 1;
    private static final int THIRD_STEP = 2;

    private int currentStep = FIRST_STEP;

    private View root;
    private View backButton;
    private ImageView illustration;
    private TextView title;
    private TextView body;
    private View firstIndicator;
    private View secondIndicator;
    private View thirdIndicator;
    private View regularActions;
    private View startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backButton = findViewById(R.id.btn_back);
        illustration = findViewById(R.id.iv_onboarding_illustration);
        title = findViewById(R.id.tv_onboarding_title);
        body = findViewById(R.id.tv_onboarding_body);
        firstIndicator = findViewById(R.id.indicator_first);
        secondIndicator = findViewById(R.id.indicator_second);
        thirdIndicator = findViewById(R.id.indicator_third);
        regularActions = findViewById(R.id.group_regular_actions);
        startButton = findViewById(R.id.btn_start);

        if (savedInstanceState != null) {
            currentStep = savedInstanceState.getInt(STATE_CURRENT_STEP, FIRST_STEP);
        }

        backButton.setOnClickListener(view -> showPreviousStep());
        findViewById(R.id.btn_skip).setOnClickListener(view -> completeOnboarding());
        findViewById(R.id.btn_next).setOnClickListener(view -> showNextStep());
        startButton.setOnClickListener(view -> completeOnboarding());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentStep == FIRST_STEP) {
                    finish();
                } else {
                    showPreviousStep();
                }
            }
        });

        renderStep();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_STEP, currentStep);
        super.onSaveInstanceState(outState);
    }

    private void showNextStep() {
        if (currentStep < THIRD_STEP) {
            currentStep++;
            renderStep();
        }
    }

    private void showPreviousStep() {
        if (currentStep > FIRST_STEP) {
            currentStep--;
            renderStep();
        }
    }

    private void renderStep() {
        if (currentStep == FIRST_STEP) {
            root.setBackgroundResource(R.color.onboarding_background);
            backButton.setVisibility(View.GONE);
            illustration.setImageResource(R.drawable.img_onboarding_tracking);
            title.setText(R.string.onboarding_tracking_title);
            body.setText(R.string.onboarding_tracking_body);
        } else if (currentStep == SECOND_STEP) {
            root.setBackgroundResource(R.color.onboarding_background_second);
            backButton.setVisibility(View.VISIBLE);
            illustration.setImageResource(R.drawable.img_onboarding_organize);
            title.setText(R.string.onboarding_organize_title);
            body.setText(R.string.onboarding_organize_body);
        } else {
            root.setBackgroundResource(R.color.onboarding_background_third);
            backButton.setVisibility(View.VISIBLE);
            illustration.setImageResource(R.drawable.img_onboarding_schedule);
            title.setText(R.string.onboarding_schedule_title);
            body.setText(R.string.onboarding_schedule_body);
        }

        firstIndicator.setBackgroundResource(currentStep == FIRST_STEP
                ? R.drawable.bg_onboarding_indicator_active
                : R.drawable.bg_onboarding_indicator_inactive);
        secondIndicator.setBackgroundResource(currentStep == SECOND_STEP
                ? R.drawable.bg_onboarding_indicator_active
                : R.drawable.bg_onboarding_indicator_inactive);
        thirdIndicator.setBackgroundResource(currentStep == THIRD_STEP
                ? R.drawable.bg_onboarding_indicator_active
                : R.drawable.bg_onboarding_indicator_inactive);

        boolean isLastStep = currentStep == THIRD_STEP;
        regularActions.setVisibility(isLastStep ? View.GONE : View.VISIBLE);
        startButton.setVisibility(isLastStep ? View.VISIBLE : View.GONE);
    }

    private void completeOnboarding() {
        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
