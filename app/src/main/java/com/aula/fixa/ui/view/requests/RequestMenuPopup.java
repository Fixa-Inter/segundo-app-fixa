package com.aula.fixa.ui.view.requests;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.R;
import com.aula.fixa.adapter.cadastro.CadastroViewModel;
import com.aula.fixa.adapter.cadastro.CadastroViewModelFactory;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoViewModelFactory;
import com.aula.fixa.domain.model.AtualizacaoSolicitacao;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.Problema;
import com.aula.fixa.domain.model.ProblemaItem;
import com.aula.fixa.ui.config.AppDialogs;
import com.aula.fixa.ui.config.DataChangeNotifier;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/** Menu de ações da solicitação, mantendo as operações nas camadas de aplicação e adapter. */
public final class RequestMenuPopup {
    private RequestMenuPopup() { }

    public static void show(@NonNull View anchor, @NonNull ProblemaItem item) {
        FragmentActivity activity = (FragmentActivity) anchor.getContext();
        SolicitacaoViewModel solicitacoes = new ViewModelProvider(activity,
                new SolicitacaoViewModelFactory(activity)).get(SolicitacaoViewModel.class);
        CadastroViewModel cadastros = new ViewModelProvider(activity,
                new CadastroViewModelFactory(activity)).get(CadastroViewModel.class);
        show(anchor, item, solicitacoes, cadastros);
    }

    public static void show(@NonNull View anchor, @NonNull ProblemaItem item,
                            @NonNull SolicitacaoViewModel solicitacoes,
                            @NonNull CadastroViewModel cadastros) {
        View content = LayoutInflater.from(anchor.getContext()).inflate(R.layout.popup_app_request_menu, null, false);
        PopupWindow popup = new PopupWindow(content,
                anchor.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setElevation(anchor.getResources().getDisplayMetrics().density * 6);

        content.findViewById(R.id.tvPopupEdit).setOnClickListener(v -> {
            popup.dismiss();
            carregarDadosEdicao(anchor, item, solicitacoes, cadastros);
        });
        content.findViewById(R.id.tvPopupDelete).setOnClickListener(v -> {
            popup.dismiss();
            AppDialogs.showDeleteConfirm(anchor.getContext(), () -> solicitacoes.excluir(item.id,
                    new SolicitacaoViewModel.Callback<Void>() {
                        @Override public void aoConcluir(Void ignored) {
                            DataChangeNotifier.notifyChanged();
                            Toast.makeText(anchor.getContext(), R.string.app_request_deleted_success, Toast.LENGTH_SHORT).show();
                        }
                        @Override public void aoFalhar(Throwable erro) { mostrarErro(anchor, erro); }
                    }));
        });
        popup.showAsDropDown(anchor, -popup.getWidth() + anchor.getWidth(), 0);
    }

    private static void carregarDadosEdicao(View anchor, ProblemaItem item,
                                            SolicitacaoViewModel solicitacoes,
                                            CadastroViewModel cadastros) {
        final Problema[] details = new Problema[1];
        final List<OpcaoCadastro>[] locations = new List[]{null};
        final List<OpcaoCadastro>[] categories = new List[]{null};
        final Throwable[] errors = new Throwable[3];
        final boolean[] done = new boolean[3];
        Runnable show = () -> {
            if (!done[0] || !done[1] || !done[2]) return;
            for (Throwable error : errors) if (error != null) { mostrarErro(anchor, error); return; }
            if (details[0] == null) return;
            AppDialogs.showEditRequest(anchor.getContext(), item, details[0],
                    locations[0] == null ? Collections.emptyList() : locations[0],
                    categories[0] == null ? Collections.emptyList() : categories[0],
                    (title, description, location, category, deletedPhotoIds) -> {
                        Long localId = findId(locations[0], location);
                        Long categoryId = findId(categories[0], category);
                        solicitacoes.atualizar(new AtualizacaoSolicitacao(item.id, title, description,
                                localId, null, categoryId, deletedPhotoIds),
                                new SolicitacaoViewModel.Callback<Problema>() {
                                    @Override public void aoConcluir(Problema result) {
                                        DataChangeNotifier.notifyChanged();
                                        Toast.makeText(anchor.getContext(), R.string.app_request_updated_success, Toast.LENGTH_SHORT).show();
                                    }
                                    @Override public void aoFalhar(Throwable erro) { mostrarErro(anchor, erro); }
                                });
                    });
        };
        solicitacoes.detalhar(item.id, new SolicitacaoViewModel.Callback<Problema>() {
            @Override public void aoConcluir(Problema result) { details[0] = result; done[0] = true; show.run(); }
            @Override public void aoFalhar(Throwable erro) { errors[0] = erro; done[0] = true; show.run(); }
        });
        cadastros.listarLocais(new CadastroViewModel.Callback<List<OpcaoCadastro>>() {
            @Override public void aoConcluir(List<OpcaoCadastro> result) { locations[0] = result; done[1] = true; show.run(); }
            @Override public void aoFalhar(Throwable erro) { errors[1] = erro; done[1] = true; show.run(); }
        });
        cadastros.listarCategorias(new CadastroViewModel.Callback<List<OpcaoCadastro>>() {
            @Override public void aoConcluir(List<OpcaoCadastro> result) { categories[0] = result; done[2] = true; show.run(); }
            @Override public void aoFalhar(Throwable erro) { errors[2] = erro; done[2] = true; show.run(); }
        });
    }

    private static Long findId(List<OpcaoCadastro> options, String name) {
        if (options == null) return null;
        for (OpcaoCadastro option : options) if (option.getNome().equals(name)) return option.getId();
        return null;
    }

    private static void mostrarErro(View anchor, Throwable erro) {
        Toast.makeText(anchor.getContext(), AutenticacaoViewModel.messageFrom(erro), Toast.LENGTH_LONG).show();
    }
}
