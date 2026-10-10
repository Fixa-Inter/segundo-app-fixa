package com.aula.fixa.ui.requests;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.aula.fixa.R;
import com.aula.fixa.model.RequestItem;
import com.aula.fixa.ui.common.AppDialogs;

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
            AppDialogs.showEditRequest(anchor.getContext(), item);
        });
        content.findViewById(R.id.tvPopupDelete).setOnClickListener(v -> {
            popup.dismiss();
            // Figma: motivo (pop-up 7005) e, ao excluir, a confirmação "Deseja deletar esse registro?" (6997).
            AppDialogs.showDeleteWithReason(anchor.getContext(),
                    () -> AppDialogs.showDeleteConfirm(anchor.getContext(), null));
        });
        popup.showAsDropDown(anchor, -popup.getWidth() + anchor.getWidth(), 0);
    }
}
