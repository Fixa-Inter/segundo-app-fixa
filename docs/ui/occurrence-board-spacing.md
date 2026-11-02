# Espaçamento — ocorrências e quadro de OS

Revisão da skill `android-xml-builder`: 2026-11-02. Ajustes preservam os tamanhos e o conteúdo; no quadro de OS, o deslocamento do campo de busca compensa a subida dos filtros.

| Tela / elemento | Valor aplicado | Recurso de origem | Aprovação / transformação |
| --- | --- | --- | --- |
| Ocorrências: busca e filtro | Margem superior do conjunto reduzida de 8dp para 0dp; filtro continua alinhado verticalmente à busca | `fragment_ops_manager_occurrences.xml`, `managerOccurrencesSearchContainer` | Pedido para subir a busca junto ao filtro |
| Ocorrências: lista | Respiro aumentado de 8dp para 16dp; mantém a lista na posição anterior apesar da busca subir 8dp | `fragment_ops_manager_occurrences.xml`, `managerOccurrencesList` | Pedido para separar busca e cards sem deslocar os cards |
| Quadro de OS: filtros de status | Margem superior reduzida de 18dp para 8dp | `fragment_ops_manager_board.xml`, `ops_status_row` | Pedido para subir somente os filtros |
| Quadro de OS: conteúdo abaixo dos filtros | Margem da busca aumentada de 12dp para 22dp; posição da busca e dos cards permanece | `fragment_ops_manager_board.xml`, `ops_5717_8592` | Compensação de 10dp para manter o conteúdo abaixo no mesmo lugar |
