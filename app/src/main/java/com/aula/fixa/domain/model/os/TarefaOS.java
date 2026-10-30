package com.aula.fixa.domain.model.os;

public final class TarefaOS {
    public final long id;
    public final String titulo, descricao, status;
    public TarefaOS(long id,String titulo,String descricao,String status){this.id=id;this.titulo=titulo;this.descricao=descricao;this.status=status;}
}
