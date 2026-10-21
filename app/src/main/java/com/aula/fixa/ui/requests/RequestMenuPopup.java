package com.aula.fixa.ui.requests;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.RequestItem;
import com.aula.fixa.ui.common.AppDialogs;
import com.aula.fixa.ui.common.DataChangeNotifier;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import com.aula.fixa.data.remote.dto.CadastroOption;
import java.util.List;

/** Menu "Editar / Excluir" ancorado no botão de opções do card (Figma "Pop-up 1"). */
public final class RequestMenuPopup {

    private RequestMenuPopup() {}

    public static void show(View anchor, RequestItem item) {
        View content = LayoutInflater.from(anchor.getContext())
                .inflate(R.layout.popup_app_request_menu, null, false);
        PopupWindow popup = new PopupWindow(content,
                anchor.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setElevation(anchor.getResources().getDisplayMetrics().density * 6);

        content.findViewById(R.id.tvPopupEdit).setOnClickListener(v -> {
            popup.dismiss();
            new Thread(() -> {
                    try {
                        com.google.gson.JsonObject details = new RequestRepository(new SessionStorage(anchor.getContext())).detalhes(item.id);
                        RequestRepository repository = new RequestRepository(new SessionStorage(anchor.getContext()));
                        List<CadastroOption> locations = repository.options(repository.locais());
                        List<CadastroOption> devices = repository.options(repository.categorias());
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                AppDialogs.showEditRequest(anchor.getContext(), item, details, locations, devices, (title, description, location, device, deletedPhotoIds) -> {
                                    new Thread(() -> {
                                        try {
                                            Long localId = locations.stream().filter(option -> option.getNome().equals(location))
                                                    .map(CadastroOption::getId).findFirst().orElse(null);
                                            Long categoryId = devices.stream().filter(option -> option.getNome().equals(device))
                                                    .map(CadastroOption::getId).findFirst().orElse(null);
                                            new RequestRepository(new SessionStorage(anchor.getContext())).atualizar(
                                                    item.id, title, description, localId, null, categoryId, null, deletedPhotoIds);
                                            DataChangeNotifier.notifyChanged();
                                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                                    android.widget.Toast.makeText(anchor.getContext(), "Solicitação atualizada com sucesso.", android.widget.Toast.LENGTH_SHORT).show());
                                        } catch (Exception e) {
                                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> android.widget.Toast.makeText(anchor.getContext(), e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
                                        }
                                    }).start();
                                }));
                    } catch (Exception e) {
                        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                        handler.post(() -> android.widget.Toast.makeText(anchor.getContext(), e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
                    }
            }).start();
        });
        content.findViewById(R.id.tvPopupDelete).setOnClickListener(v -> {
            popup.dismiss();
            AppDialogs.showDeleteConfirm(anchor.getContext(), () -> new Thread(() -> {
                        try {
                            new RequestRepository(new SessionStorage(anchor.getContext())).excluir(item.id);
                            DataChangeNotifier.notifyChanged();
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                    android.widget.Toast.makeText(anchor.getContext(), "Solicitação excluída com sucesso.", android.widget.Toast.LENGTH_SHORT).show());
                        } catch (Exception e) {
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                    android.widget.Toast.makeText(anchor.getContext(), e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
                        }
                    }).start());
        });
        popup.showAsDropDown(anchor, -popup.getWidth() + anchor.getWidth(), 0);
    }
}
