package com.aula.fixa.ui.view.os;

import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.aula.fixa.R;
import com.aula.fixa.adapter.foto.FotoViewModel;
import com.aula.fixa.adapter.foto.FotoViewModelFactory;
import com.aula.fixa.adapter.os.ExecucaoOSViewModel;
import com.aula.fixa.adapter.os.ExecucaoOSViewModelFactory;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.view.operations.OperationsActivity;
import com.aula.fixa.ui.view.operations.OperationsScreenFragment;

import java.util.ArrayList;
import java.util.List;

/** Registra a observação de conclusão da OS com fotos já hospedadas. */
public final class ConclusaoOSFluxo {
    private static final int MAX_PHOTOS = 3;

    private final OperationsScreenFragment host;
    private final OperationsActivity activity;
    private final View root;
    private final long orderId;
    private final ExecucaoOSViewModel osViewModel;
    private final FotoViewModel fotoViewModel;
    private final List<String> fotos = new ArrayList<>();
    private int pendingPhotoUploads;

    public ConclusaoOSFluxo(OperationsScreenFragment host, OperationsActivity activity, View root, long orderId) {
        this.host = host;
        this.activity = activity;
        this.root = root;
        this.orderId = orderId;
        osViewModel = new ViewModelProvider(host, new ExecucaoOSViewModelFactory(host.requireContext()))
                .get(ExecucaoOSViewModel.class);
        fotoViewModel = new ViewModelProvider(host, new FotoViewModelFactory(host.requireContext()))
                .get(FotoViewModel.class);
    }

    public void bind() {
        TextView title = root.findViewById(R.id.ops_5717_6300);
        if (title != null) title.setText("Finalizar OS");

        View back = root.findViewWithTag("image:arrow-left");
        if (back != null) back.setOnClickListener(v -> activity.back());

        setReadableSizes();
        AutoCompleteTextView difficulty = root.findViewById(R.id.ops_5717_6328_input);
        bindDifficultyDropdown(difficulty);

        View addPhotos = root.findViewById(R.id.ops_5717_6298);
        if (addPhotos != null) addPhotos.setOnClickListener(v -> {
            if (photoCount() >= MAX_PHOTOS) {
                Toast.makeText(host.requireContext(), "É possível adicionar no máximo 3 fotos.", Toast.LENGTH_SHORT).show();
                return;
            }
            host.selectCompletionPhotos();
        });

        TextView photoLabel = root.findViewById(R.id.ops_5717_6299);
        if (photoLabel != null) photoLabel.setText("Adicionar fotos (1 a 3 obrigatórias)");

        View cancel = root.findViewById(R.id.ops_5717_6323);
        View submit = root.findViewById(R.id.ops_5717_6325);
        bindBottomButtons(cancel, submit);
        if (submit != null) {
            submit.setOnClickListener(v -> submit(difficulty,
                    root.findViewById(R.id.ops_5717_6332),
                    root.findViewById(R.id.ops_5717_6336)));
        }
    }

    private void setReadableSizes() {
        String[] labels = {"Grau de dificuldade", "Solução aplicada", "Dificuldades encontradas (opcional)", "Fotos"};
        for (String label : labels) {
            TextView view = root.findViewWithTag("text:" + label);
            if (view != null) view.setTextSize(17);
        }
        TextView photoAction = root.findViewById(R.id.ops_5717_6299);
        if (photoAction != null) photoAction.setTextSize(15);
        EditText solution = root.findViewById(R.id.ops_5717_6332);
        EditText issues = root.findViewById(R.id.ops_5717_6336);
        if (solution != null) solution.setTextSize(15);
        if (issues != null) issues.setTextSize(15);
    }

    private void bindDifficultyDropdown(AutoCompleteTextView difficulty) {
        if (difficulty == null) return;
        String[] choices = {"Fácil", "Médio", "Difícil"};
        difficulty.setTextSize(15);
        difficulty.setKeyListener(null);
        difficulty.setFocusable(false);
        difficulty.setThreshold(0);
        difficulty.setAdapter(new ArrayAdapter<>(host.requireContext(), R.layout.item_app_dropdown, choices));
        difficulty.setOnClickListener(v -> difficulty.showDropDown());
        difficulty.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) difficulty.showDropDown();
            return false;
        });
    }

    private void bindBottomButtons(View cancel, View submit) {
        if (cancel == null || submit == null || !(submit.getParent() instanceof ConstraintLayout)) return;
        ConstraintLayout parent = (ConstraintLayout) submit.getParent();
        int buttonWidth = dp(140);
        int buttonHeight = dp(50);
        ConstraintLayout.LayoutParams cancelParams = new ConstraintLayout.LayoutParams(buttonWidth, buttonHeight);
        cancelParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        cancelParams.endToStart = submit.getId();
        cancelParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        cancelParams.setMargins(dp(28), 0, dp(12), dp(26));
        cancel.setLayoutParams(cancelParams);
        cancel.setBackgroundResource(R.drawable.bg_ops_6fc7f4db18);
        if (cancel instanceof TextView) {
            TextView text = (TextView) cancel;
            text.setText("Voltar");
            text.setTextSize(16);
            text.setGravity(android.view.Gravity.CENTER);
        }

        ConstraintLayout.LayoutParams submitParams = new ConstraintLayout.LayoutParams(buttonWidth, buttonHeight);
        submitParams.startToEnd = cancel.getId();
        submitParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        submitParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        submitParams.setMargins(dp(12), 0, dp(28), dp(26));
        submit.setLayoutParams(submitParams);
        if (submit instanceof TextView) {
            TextView text = (TextView) submit;
            text.setText("Finalizar");
            text.setTextSize(16);
        }
        cancel.setOnClickListener(v -> activity.back());
    }

    public void uploadPhotos(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) return;
        int available = MAX_PHOTOS - photoCount();
        int count = Math.min(available, uris.size());
        if (count <= 0) {
            Toast.makeText(host.requireContext(), "É possível adicionar no máximo 3 fotos.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (count < uris.size()) {
            Toast.makeText(host.requireContext(), "Só foram adicionadas fotos até o limite de 3.", Toast.LENGTH_SHORT).show();
        }

        pendingPhotoUploads += count;
        updatePhotoCount();
        for (int i = 0; i < count; i++) uploadPhoto(uris.get(i));
    }

    private void uploadPhoto(Uri uri) {
        fotoViewModel.enviarSolicitacao(uri, new FotoViewModel.Callback() {
            @Override public void aoSucesso(String url, String publicId) {
                if (!host.isAdded()) return;
                host.requireActivity().runOnUiThread(() -> {
                    synchronized (fotos) { fotos.add(url); }
                    pendingPhotoUploads--;
                    updatePhotoCount();
                });
            }

            @Override public void aoReceberTokenExclusao(String token) { }

            @Override public void aoFalhar(String message) {
                if (!host.isAdded()) return;
                host.requireActivity().runOnUiThread(() -> {
                    pendingPhotoUploads--;
                    updatePhotoCount();
                    Toast.makeText(host.requireContext(), message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private int photoCount() {
        synchronized (fotos) { return fotos.size() + pendingPhotoUploads; }
    }

    private void updatePhotoCount() {
        TextView label = root.findViewById(R.id.ops_5717_6299);
        if (label == null) return;
        if (pendingPhotoUploads > 0) label.setText("Enviando fotos… " + fotos.size() + "/3");
        else if (fotos.isEmpty()) label.setText("Adicionar fotos (1 a 3 obrigatórias)");
        else label.setText(fotos.size() + "/3 foto(s) adicionada(s)");
    }

    private void submit(AutoCompleteTextView difficulty, EditText solution, EditText issues) {
        String value = difficulty == null ? "" : String.valueOf(difficulty.getText()).trim();
        String code = "Fácil".equals(value) ? "FACIL" : "Médio".equals(value) ? "MEDIO" : "Difícil".equals(value) ? "DIFICIL" : "";
        String observation = String.valueOf(solution == null ? "" : solution.getText()).trim();
        if (code.isEmpty()) {
            if (difficulty != null) difficulty.setError("Selecione o grau de dificuldade.");
            return;
        }
        if (observation.isEmpty()) {
            if (solution != null) solution.setError("Informe a solução aplicada.");
            return;
        }
        if (pendingPhotoUploads > 0) {
            Toast.makeText(host.requireContext(), "Aguarde o envio das fotos.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fotos.isEmpty()) {
            Toast.makeText(host.requireContext(), "Adicione ao menos uma foto.", Toast.LENGTH_SHORT).show();
            return;
        }

        String difficulties = String.valueOf(issues == null ? "" : issues.getText()).trim();
        if (!difficulties.isEmpty()) observation += "\n\nDificuldades encontradas: " + difficulties;
        android.app.Dialog loading = AppLoading.show(host.requireContext());
        List<String> uploaded;
        synchronized (fotos) { uploaded = new ArrayList<>(fotos); }
        osViewModel.finalizar(orderId, code, observation, uploaded, new ExecucaoOSViewModel.Callback<Void>() {
            @Override public void success(Void ignored) {
                AppLoading.hide(loading);
                activity.openName("execution_completed");
            }

            @Override public void failure(Throwable error) {
                AppLoading.hide(loading);
                Toast.makeText(host.requireContext(), com.aula.fixa.adapter.auth.AutenticacaoViewModel.messageFrom(error),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private int dp(int value) {
        return (int) (value * host.getResources().getDisplayMetrics().density + 0.5f);
    }
}
