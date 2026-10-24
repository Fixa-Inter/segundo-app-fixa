package com.aula.fixa.ui.view.operations;

import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.foto.FotoViewModel;
import com.aula.fixa.adapter.foto.FotoViewModelFactory;
import com.aula.fixa.adapter.perfil.PerfilViewModel;
import com.aula.fixa.adapter.perfil.PerfilViewModelFactory;
import com.aula.fixa.domain.model.AtualizacaoPerfil;
import com.aula.fixa.domain.model.Perfil;
import com.aula.fixa.ui.config.AppDialogs;
import com.aula.fixa.ui.config.DataChangeNotifier;
import com.aula.fixa.ui.config.operations.ScreenCatalog;
import com.aula.fixa.ui.view.auth.VerifyEmailActivity;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

/** Conecta as telas de perfil do gestor e técnico aos mesmos casos de uso do solicitante. */
final class OperationsProfileFlow {
    interface PhotoActions {
        void escolher();
        void capturar();
    }

    private final OperationsScreenFragment fragment;
    private final OperationsActivity activity;
    private final View root;
    private final String screen;
    private final PerfilViewModel perfil;
    private final FotoViewModel fotos;
    private final AutenticacaoViewModel autenticacao;
    private final PhotoActions photoActions;
    private EditText nome, nascimento, email, cargo, senha;
    private String fotoPendente;
    private String snapshotInicial;

    OperationsProfileFlow(OperationsScreenFragment fragment, OperationsActivity activity,
                          View root, String screen, PhotoActions photoActions) {
        this.fragment = fragment;
        this.activity = activity;
        this.root = root;
        this.screen = screen;
        this.photoActions = photoActions;
        perfil = new ViewModelProvider(activity, new PerfilViewModelFactory(activity)).get(PerfilViewModel.class);
        fotos = new ViewModelProvider(activity, new FotoViewModelFactory(activity)).get(FotoViewModel.class);
        autenticacao = AutenticacaoViewModel.obter(fragment, activity);
    }

    void bind() {
        if (screen.endsWith("edit_profile")) bindEdit();
        else if (screen.endsWith("my_profile")) bindMyProfile();
        else bindProfile();
        carregarPerfil();
    }

    private void bindProfile() {
        setClick(boundText("Editar perfil"), () -> activity.openName("edit_profile"));
        View myProfile = boundText("Meu perfil");
        setClick(myProfile, () -> activity.openName("my_profile"));
        setClick(imageNamed("user"), () -> activity.openName("my_profile"));
        clearText(boundText("Ryan Cursino"));
        clearText(boundText("ryan.cursino@gmail.com"));
    }

    private void bindMyProfile() {
        setClick(imageNamed("edit"), () -> activity.openName("edit_profile"));
        clearText(boundText("Ryan Cursino de Moraes"));
        clearText(boundText("26/11/2002"));
        clearText(boundText("ryan.cursino@gmail.com"));
        clearText(boundText("+(55) 11 123456789"));
        clearText(boundText("*************"));
        clearText(boundText("Instituto J&F, R. Irineu José Bordon, 365"));
    }

    private void bindEdit() {
        nome = field("Nome");
        nascimento = field("Data de nascimento");
        email = field("Email");
        cargo = field("Telefone");
        senha = field("Senha");
        if (nome != null) nome.setText("");
        if (nascimento != null) {
            nascimento.setText("");
            lockReadOnly(nascimento);
        }
        if (email != null) {
            email.setText("");
            lockReadOnly(email);
        }
        if (cargo != null) cargo.setText("");
        if (senha != null) {
            senha.setText("*************");
            lockPassword(senha);
            senha.setOnClickListener(v -> requestPasswordChange());
        }

        setClick(imageNamed("check"), () -> AppDialogs.showSaveConfirm(activity, this::saveProfile));
        int cameraId = viewId(activity.role().equals("manager") ? 7413 : 9995);
        View camera = root.findViewById(cameraId);
        if (camera != null) {
            camera.setClickable(true);
            camera.setOnClickListener(v -> choosePhoto());
        }
        int backId = viewId(activity.role().equals("manager") ? 7415 : 9997);
        setClick(root.findViewById(backId), this::leave);
        fragment.requireActivity().getOnBackPressedDispatcher().addCallback(
                fragment.getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override public void handleOnBackPressed() { leave(); }
                });
    }

    private void carregarPerfil() {
        ImageView avatar = avatar();
        if (avatar != null) avatar.setImageResource(R.drawable.img_avatar_115);
        perfil.obter(new PerfilViewModel.Callback<Perfil>() {
            @Override public void aoConcluir(Perfil data) {
                runIfActive(() -> renderProfile(data));
            }
            @Override public void aoFalhar(Throwable error) {
                runIfActive(() -> {
                    if (avatar != null) avatar.setImageResource(R.drawable.img_avatar_115);
                    Toast.makeText(activity, error.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void renderProfile(Perfil data) {
        if (screen.endsWith("edit_profile")) {
            if (nome != null) nome.setText(data.nomeCompleto);
            if (nascimento != null) nascimento.setText(data.dataNascimento);
            if (email != null) email.setText(data.email);
            if (cargo != null) cargo.setText(data.cargo);
            if (senha != null) senha.setText("*************");
            snapshotInicial = snapshot();
        } else if (screen.endsWith("my_profile")) {
            setText(boundText("Ryan Cursino de Moraes"), data.nomeCompleto);
            setText(boundText("26/11/2002"), data.dataNascimento);
            setText(boundText("ryan.cursino@gmail.com"), data.email);
            setText(boundText("+(55) 11 123456789"), data.cargo);
            setText(boundText("*************"), "*************");
            setText(boundText("Instituto J&F, R. Irineu José Bordon, 365"), formatCnpj(data.cnpjUnidade));
        } else {
            setText(boundText("Ryan Cursino"), data.nomeCompleto);
            setText(boundText("ryan.cursino@gmail.com"), data.email);
        }
        ImageView avatar = avatar();
        if (avatar != null) Glide.with(root).load(data.urlFotoPerfil.trim().isEmpty() ? null : data.urlFotoPerfil)
                .placeholder(R.drawable.img_avatar_115).fallback(R.drawable.img_avatar_115)
                .error(R.drawable.img_avatar_115).circleCrop().into(avatar);
    }

    private void saveProfile() {
        if (nome == null || cargo == null) return;
        perfil.atualizar(new AtualizacaoPerfil(nome.getText().toString(), cargo.getText().toString(), fotoPendente),
                new PerfilViewModel.Callback<Perfil>() {
                    @Override public void aoConcluir(Perfil ignored) {
                        if (!active()) return;
                        activity.back();
                        DataChangeNotifier.notifyChanged();
                    }
                    @Override public void aoFalhar(Throwable error) {
                        if (active()) Toast.makeText(activity, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void leave() {
        Runnable exit = activity::back;
        if (snapshotInicial == null || snapshotInicial.equals(snapshot())) {
            exit.run();
        } else {
            AppDialogs.showDiscardConfirm(activity,
                    () -> AppDialogs.showSaveConfirm(activity, () -> {
                        saveProfile();
                    }), exit);
        }
    }

    private String snapshot() {
        return value(nome) + "\u0000" + value(nascimento) + "\u0000" + value(email)
                + "\u0000" + value(cargo) + "\u0000" + String.valueOf(fotoPendente);
    }

    private void requestPasswordChange() {
        String address = value(email).trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
            Toast.makeText(activity, R.string.recovery_email_invalid, Toast.LENGTH_LONG).show();
            return;
        }
        if (senha != null) senha.setEnabled(false);
        new Thread(() -> {
            try {
                autenticacao.requestPasswordRecovery(address);
                runIfActive(() -> {
                    if (senha != null) senha.setEnabled(true);
                    fragment.startActivity(VerifyEmailActivity.intent(activity, address, true));
                });
            } catch (Exception error) {
                runIfActive(() -> {
                    if (senha != null) senha.setEnabled(true);
                    Toast.makeText(activity, AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void choosePhoto() {
        new AlertDialog.Builder(activity).setItems(new String[]{"Escolher da galeria", "Tirar foto"},
                (dialog, choice) -> {
                    if (photoActions == null) return;
                    if (choice == 0) photoActions.escolher(); else photoActions.capturar();
                }).show();
    }

    void uploadPhoto(Uri uri) {
        if (uri == null || !active()) return;
        fotos.enviarPerfil(uri, new FotoViewModel.Callback() {
            @Override public void aoSucesso(String url, String publicId) {
                fotoPendente = url;
                runIfActive(() -> {
                    ImageView image = avatar();
                    if (image != null) Glide.with(root).load(url).placeholder(R.drawable.img_avatar_115)
                            .error(R.drawable.img_avatar_115).circleCrop().into(image);
                });
            }
            @Override public void aoReceberTokenExclusao(String token) { }
            @Override public void aoFalhar(String message) {
                runIfActive(
                        () -> Toast.makeText(activity, message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private EditText field(String label) {
        JSONArray bindings = ScreenCatalog.get(activity).screen(screen).optJSONArray("bindings");
        for (int i = 0; bindings != null && i < bindings.length(); i++) {
            JSONObject binding = bindings.optJSONObject(i);
            if (binding == null || !label.equals(binding.optString("label"))) continue;
            View view = root.findViewById(activity.getResources().getIdentifier(
                    binding.optString("id"), "id", activity.getPackageName()));
            if (view instanceof EditText) return (EditText) view;
        }
        return null;
    }

    private View boundText(String text) {
        JSONArray bindings = ScreenCatalog.get(activity).screen(screen).optJSONArray("bindings");
        for (int i = 0; bindings != null && i < bindings.length(); i++) {
            JSONObject binding = bindings.optJSONObject(i);
            if (binding == null || !text.equals(binding.optString("text"))) continue;
            int id = activity.getResources().getIdentifier(binding.optString("id"), "id", activity.getPackageName());
            View view = root.findViewById(id);
            if (view instanceof TextView) return view;
        }
        return null;
    }

    private View imageNamed(String name) {
        JSONArray bindings = ScreenCatalog.get(activity).screen(screen).optJSONArray("bindings");
        for (int i = 0; bindings != null && i < bindings.length(); i++) {
            JSONObject binding = bindings.optJSONObject(i);
            if (binding == null || !name.equals(binding.optString("name"))) continue;
            return root.findViewById(activity.getResources().getIdentifier(
                    binding.optString("id"), "id", activity.getPackageName()));
        }
        return null;
    }

    private ImageView avatar() {
        View view = imageNamed("image 170");
        return view instanceof ImageView ? (ImageView) view : null;
    }

    private int viewId(int suffix) {
        return activity.getResources().getIdentifier("ops_5717_" + suffix, "id", activity.getPackageName());
    }

    private boolean active() {
        return fragment.isAdded() && fragment.getView() != null && root.getParent() == fragment.getView();
    }

    private void runIfActive(Runnable action) {
        root.post(() -> {
            if (active()) action.run();
        });
    }

    private static void setClick(View view, Runnable action) {
        if (view == null) return;
        view.setClickable(true);
        view.setOnClickListener(ignored -> action.run());
    }

    private static void setText(View view, String text) {
        if (view instanceof TextView) ((TextView) view).setText(text == null ? "" : text);
    }

    private static void clearText(View view) { setText(view, ""); }

    private static void lockReadOnly(EditText input) {
        input.setEnabled(false);
        input.setFocusable(false);
        input.setFocusableInTouchMode(false);
        input.setCursorVisible(false);
        input.setClickable(false);
        input.setOnClickListener(null);
    }

    private static void lockPassword(EditText input) {
        input.setEnabled(true);
        input.setFocusable(false);
        input.setFocusableInTouchMode(false);
        input.setCursorVisible(false);
        input.setClickable(true);
    }

    private static String value(EditText field) {
        return field == null ? "" : field.getText().toString();
    }

    private static String formatCnpj(String cnpj) {
        String digits = cnpj == null ? "" : cnpj.replaceAll("\\D", "");
        if (digits.length() != 14) return cnpj == null ? "" : cnpj;
        return digits.substring(0, 2) + "." + digits.substring(2, 5) + "."
                + digits.substring(5, 8) + "/" + digits.substring(8, 12) + "-"
                + digits.substring(12, 14);
    }
}
