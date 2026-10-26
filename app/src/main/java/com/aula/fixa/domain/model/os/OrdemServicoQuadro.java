package com.aula.fixa.domain.model.os;

public final class OrdemServicoQuadro {
    public final long id;
    public final String titulo, descricao, categoriaProblema, categoriaEquipamento, prioridade, dataPrevista, status, nomeUsuario;
    public OrdemServicoQuadro(long id,String titulo,String descricao,String categoriaProblema,String categoriaEquipamento,String prioridade,String dataPrevista,String status,String nomeUsuario) {
        this.id=id; this.titulo=titulo; this.descricao=descricao; this.categoriaProblema=categoriaProblema;
        this.categoriaEquipamento=categoriaEquipamento; this.prioridade=prioridade; this.dataPrevista=dataPrevista; this.status=status; this.nomeUsuario=nomeUsuario;
    }
}
