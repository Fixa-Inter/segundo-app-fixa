package com.aula.fixa.domain.model.ocorrencia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OcorrenciaItem {
    public final long id;
    public final String titulo, descricao, categoriaProblema, prioridade, localUnidade,
            descricaoLocal, dataCriacao, categoriaEquipamento, autor;
    public final Long localUnidadeId, categoriaEquipamentoId;
    public final List<OcorrenciaFoto> fotos;

    public OcorrenciaItem(long id, String titulo, String descricao, String categoriaProblema,
            String prioridade, String localUnidade, String descricaoLocal, String dataCriacao,
            String categoriaEquipamento, String autor, List<OcorrenciaFoto> fotos) {
        this(id,titulo,descricao,categoriaProblema,prioridade,localUnidade,descricaoLocal,dataCriacao,categoriaEquipamento,autor,null,null,fotos);
    }
    public OcorrenciaItem(long id, String titulo, String descricao, String categoriaProblema,
            String prioridade, String localUnidade, String descricaoLocal, String dataCriacao,
            String categoriaEquipamento, String autor, Long localUnidadeId, Long categoriaEquipamentoId, List<OcorrenciaFoto> fotos) {
        this.id=id; this.titulo=text(titulo); this.descricao=text(descricao);
        this.categoriaProblema=text(categoriaProblema); this.prioridade=text(prioridade);
        this.localUnidade=text(localUnidade); this.descricaoLocal=text(descricaoLocal);
        this.dataCriacao=text(dataCriacao); this.categoriaEquipamento=text(categoriaEquipamento);
        this.autor=text(autor); this.localUnidadeId=localUnidadeId; this.categoriaEquipamentoId=categoriaEquipamentoId; this.fotos=Collections.unmodifiableList(
                fotos==null?new ArrayList<>():new ArrayList<>(fotos));
    }
    private static String text(String value){return value==null?"":value;}
}
