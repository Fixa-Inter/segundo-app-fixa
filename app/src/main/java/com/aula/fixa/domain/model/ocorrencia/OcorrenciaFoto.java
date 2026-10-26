package com.aula.fixa.domain.model.ocorrencia;

public final class OcorrenciaFoto {
    public final long id;
    public final String url;
    public OcorrenciaFoto(long id,String url){this.id=id;this.url=url==null?"":url;}
}
