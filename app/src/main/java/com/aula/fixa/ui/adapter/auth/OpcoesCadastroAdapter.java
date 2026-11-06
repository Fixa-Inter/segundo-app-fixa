package com.aula.fixa.ui.adapter.auth;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.OpcaoCadastro;

import java.util.List;

/** Renderiza opções de instituição e unidade no dropdown de cadastro. */
public final class OpcoesCadastroAdapter extends ArrayAdapter<OpcaoCadastro> {
    public OpcoesCadastroAdapter(Context context) {
        super(context, R.layout.item_app_dropdown);
        setDropDownViewResource(R.layout.item_app_dropdown);
    }

    public void substituir(List<OpcaoCadastro> opcoes) {
        clear();
        if (opcoes != null) addAll(opcoes);
        notifyDataSetChanged();
    }

}
