package com.aula.fixa.domain.model.equipamento;

public final class ModeloEquipamentoItem {
    public final long id;
    public final String nome;
    public final String categoria;
    public final String marca;
    public final long quantidadeAparelhos;
    public final long marcaId;
    public final long categoriaId;
    public final String descricao;

    public ModeloEquipamentoItem(long id, String nome, String categoria, String marca,
                                 long quantidadeAparelhos, long marcaId, long categoriaId, String descricao) {
        this.id = id;
        this.nome = nome == null ? "" : nome;
        this.categoria = categoria == null ? "" : categoria;
        this.marca = marca == null ? "" : marca;
        this.quantidadeAparelhos = quantidadeAparelhos;
        this.marcaId = marcaId;
        this.categoriaId = categoriaId;
        this.descricao = descricao == null ? "" : descricao;
    }
}
