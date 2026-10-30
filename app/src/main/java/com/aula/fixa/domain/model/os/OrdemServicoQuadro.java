package com.aula.fixa.domain.model.os;

public final class OrdemServicoQuadro {
    public final long id;
    public final String titulo, descricao, categoriaProblema, categoriaEquipamento, prioridade, dataPrevista, status, nomeUsuario;
    public final String dataCriacao, localUnidade, descricaoLocal, solicitante;
    public OrdemServicoQuadro(long id,String titulo,String descricao,String categoriaProblema,String categoriaEquipamento,String prioridade,String dataPrevista,String status,String nomeUsuario) {
        this(id,titulo,descricao,categoriaProblema,categoriaEquipamento,prioridade,dataPrevista,status,nomeUsuario,null,null,null,null);
    }
    public OrdemServicoQuadro(long id,String titulo,String descricao,String categoriaProblema,String categoriaEquipamento,String prioridade,String dataPrevista,String status,String nomeUsuario,String dataCriacao,String localUnidade,String descricaoLocal,String solicitante) {
        this.id=id; this.titulo=titulo; this.descricao=descricao; this.categoriaProblema=categoriaProblema;
        this.categoriaEquipamento=categoriaEquipamento; this.prioridade=prioridade; this.dataPrevista=dataPrevista; this.status=status; this.nomeUsuario=nomeUsuario;
        this.dataCriacao=dataCriacao; this.localUnidade=localUnidade; this.descricaoLocal=descricaoLocal; this.solicitante=solicitante;
    }
}
