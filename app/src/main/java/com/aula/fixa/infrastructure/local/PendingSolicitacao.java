package com.aula.fixa.infrastructure.local;

import com.aula.fixa.domain.model.solicitacao.NovaSolicitacao;
import java.util.ArrayList;
import java.util.List;

/** Payload local da fila de sincronização; nunca é enviado diretamente como contrato HTTP. */
public final class PendingSolicitacao {
    public String titulo;
    public String descricaoProblema;
    public long localUnidadeId;
    public String descricaoLocal;
    public long categoriaEquipamentoId;
    public List<String> urlsFoto = new ArrayList<>();
    public List<String> fotosLocais = new ArrayList<>();

    public static PendingSolicitacao from(NovaSolicitacao request) {
        PendingSolicitacao pending = new PendingSolicitacao();
        pending.titulo = request.titulo;
        pending.descricaoProblema = request.descricao;
        pending.localUnidadeId = request.localId;
        pending.descricaoLocal = request.descricaoLocal;
        pending.categoriaEquipamentoId = request.categoriaId;
        pending.urlsFoto.addAll(request.fotos);
        pending.fotosLocais.addAll(request.fotosLocais);
        return pending;
    }
}
