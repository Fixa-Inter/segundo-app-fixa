package com.aula.fixa.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.view.WindowCompat;

import com.aula.fixa.MainActivity;
import com.aula.fixa.R;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.AuthRepository;
import com.aula.fixa.ui.common.ImmersiveActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONObject;

public class LoginActivity extends ImmersiveActivity {
    private AuthRepository authRepository;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(
                        result.getData()
                );

                try {
                    firebaseAuthWithGoogle(task.getResult(ApiException.class).getIdToken());
                } catch (ApiException exception) {
                    showGoogleError(new IllegalStateException(
                            "Não foi possível selecionar a conta Google (código " + exception.getStatusCode() + ").",
                            exception
                    ));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(true);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(new SessionStorage(this));
        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(this, options);

        findViewById(R.id.btnLogin).setOnClickListener(view -> loginNormal());
        findViewById(R.id.btnGoogle).setOnClickListener(view -> {
            findViewById(R.id.btnGoogle).setEnabled(false);
            firebaseAuth.signOut();
            googleSignInClient.signOut().addOnCompleteListener(task ->
                    googleLauncher.launch(googleSignInClient.getSignInIntent())
            );
        });
        findViewById(R.id.tvRegisterBottom).setOnClickListener(view -> openRegister());
        findViewById(R.id.tvForgotPassword).setOnClickListener(view ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void loginNormal() {
        String email = ((EditText) findViewById(R.id.etEmail)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.etPassword)).getText().toString();

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Informe e-mail e senha.", Toast.LENGTH_SHORT).show();
            return;
        }

        findViewById(R.id.btnLogin).setEnabled(false);
        new Thread(() -> {
            try {
                authRepository.login(email, password);
                runOnUiThread(this::openMain);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    findViewById(R.id.btnLogin).setEnabled(true);
                    showLoginError(e);
                });
            }
        }).start();
    }

    private void firebaseAuthWithGoogle(String googleIdToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful() || firebaseAuth.getCurrentUser() == null) {
                showGoogleError();
                return;
            }

            firebaseAuth.getCurrentUser().getIdToken(false).addOnCompleteListener(tokenTask -> {
                if (!tokenTask.isSuccessful() || tokenTask.getResult() == null) {
                    showGoogleError();
                    return;
                }

                new Thread(() -> {
                    try {
                        String firebaseToken = tokenTask.getResult().getToken();
                        JSONObject response = authRepository.loginGoogle(firebaseToken);

                        String status = response.optString("status");
                        String responseEmail = response.optString("email", "");
                        if ("AUTHENTICATED".equals(status) && !response.optString("token", "").isBlank()) {
                            runOnUiThread(this::openMain);
                        } else if ("REGISTRATION_REQUIRED".equals(status)
                                || (!responseEmail.isBlank() && response.optString("token", "").isBlank())) {
                            Intent intent = new Intent(this, RegisterActivity.class)
                                    .putExtra("google_email", responseEmail)
                                    .putExtra("google_name", response.optString("nome"));

                            runOnUiThread(() -> {
                                startActivity(intent);
                                finish();
                            });

                        } else {
                            showGoogleError(new IllegalStateException("A API não retornou um status de autenticação válido."));
                        }
                    } catch (Exception e) {
                        showGoogleError(e);
                    }
                }).start();
            });
        });
    }

    private void openRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showLoginError(Throwable error) {
        Toast.makeText(this, AuthRepository.messageFrom(error), Toast.LENGTH_LONG).show();
    }

    private void showGoogleError() {
        showGoogleError(new IllegalStateException("Não foi possível validar sua conta Google."));
    }

    private void showGoogleError(Throwable error) {
        runOnUiThread(() -> {
            findViewById(R.id.btnGoogle).setEnabled(true);
            Toast.makeText(this, AuthRepository.messageFrom(error), Toast.LENGTH_LONG).show();
        });
    }
}
