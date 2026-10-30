package com.aula.fixa.domain.repository.os;
import com.aula.fixa.domain.model.os.OrdemServicoQuadro;
import com.aula.fixa.domain.model.os.TarefaOS;
import java.util.List;
public interface ExecucaoOSRepository {
    OrdemServicoQuadro detalhes(long id)throws Exception; List<TarefaOS> tarefas(long osId)throws Exception;
    void atualizarTarefa(TarefaOS tarefa)throws Exception; void criarTarefa(long osId,String titulo,String descricao)throws Exception;
    void excluirTarefa(long tarefaId)throws Exception; void excluirOS(long osId)throws Exception;
    void finalizarOS(long osId, String dificuldade, String observacao, java.util.List<String> fotos)throws Exception;
    void atualizarOS(long osId, Long tecnicoId, String categoriaProblema, String dataPrevista, String prioridade)throws Exception;
}
