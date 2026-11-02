# Edição de OS pelo Quadro — registro de ajustes

Revisão da skill `android-xml-builder`: 2026-11-02. A edição mantém as duas etapas já existentes; somente o indicador e a posição das ações seguem o padrão visual do checklist.

| Tela / elemento | Valor aplicado | Recurso de origem | Aprovação / transformação |
| --- | --- | --- | --- |
| Editar OS: indicador de etapas | Duas etapas, “Informações” e “Técnico”; círculos, linha, rótulos e margem superior usam as dimensões, cores e tipografia do indicador de checklist | `view_app_step_indicator.xml`, `app_step_indicator_*`, `bg_app_circle_*`, `bg_app_indicator_line_*`, `TextAppearance.FIXA.App.Step.*` | Pedido explícito de replicar o design do checklist; adaptado às duas etapas existentes, sem mudar a navegação |
| Editar OS: ações | Botões abaixo do novo indicador em ambas as telas | Constraints reposicionadas em `OperationsScreenFragment`; margens originais dos botões preservadas | Necessário para evitar sobreposição após mostrar o indicador; lógica dos botões mantida |
| Criar OS: informações, tarefas e técnico | Indicador compartilhado com o checklist, inclusive margem responsiva; etapa atual muda entre as três telas e ações ficam abaixo dele | `view_app_step_indicator.xml`, `app_step_indicator_margin_top`, demais recursos `app_step_indicator_*` | Solicitação para aplicar o mesmo design, posição e dimensões; três etapas originais mantidas |
