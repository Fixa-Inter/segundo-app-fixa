# Registro de ocorrência — registro de ajustes

Revisão da skill `android-xml-builder`: 2026-11-02. O fluxo é compartilhado por Gestor e Técnico por meio dos mesmos layouts de criação.

| Tela / elemento | Padrão aplicado | Origem / transformação |
| --- | --- | --- |
| Tela inicial do fluxo | Quatro passos em lista, título, botão Continuar e Cancelar, espelhando a entrada de “Abrir solicitação” | `fragment_new_request_intro.xml`, dimensões e estilos `app_new_request_*`; quarta etapa adicionada para fotos |
| Título e espaçamento da tela inicial | Título `app_primary`; conectores 28/34/40/44dp e intervalos 8/10/12/12dp conforme largura | Paleta `app_primary` e ajuste solicitado nesta conversa; dimensões em `values*` |
| Cabeçalho e campos | Hierarquia de “Abrir solicitação”: título Poppins, labels, campos suaves e rolagem acima do rodapé fixo | `fragment_new_request_description.xml`, `TextAppearance.FIXA.App.StepTitle`, `TextAppearance.FIXA.App.Step.Label`, `TextAppearance.FIXA.App.Step.Field` |
| Progresso | Quatro etapas: Dados, Local, Técnica e Fotos; etapa atual azul, concluídas verdes e pendentes cinzas | `view_app_step_indicator.xml` e recursos `bg_app_circle_*` / `bg_app_indicator_line_*`, adaptados de três para quatro etapas existentes no fluxo de ocorrência |
| Rótulos do progresso | Largura acompanha o texto, preservando a largura mínima e evitando corte nas bordas | `view_ops_occurrence_progress.xml`, `app_step_indicator_label_min_width`; constraints centralizam cada rótulo sob seu círculo |
| Posição do progresso | A barra fica fixa abaixo da área rolável dos campos e imediatamente acima do rodapé com os botões, na mesma posição nas quatro etapas | `occurrenceStepIndicator`, limitado entre `occurrenceCreateScroll` e `occurrenceCreateFooter` |
| Ação final | Texto do botão simplificado para “Criar”; descrição acessível da ação de entrada permanece “Criar ocorrência” | `ops_occurrence_create_action` separado de `ops_occurrence_create` |
| Ações | Voltar/Cancelar à esquerda e ação primária à direita, fixas na base | `view_app_step_actions.xml`, dimensões `app_button_height_lg`, `app_button_step_width`, `app_step_actions_*` |
| Campos de ocorrência | Título, descrição, local, descrição do local, categorias, prioridade e fotos preservados; removidas as frases explicativas abaixo dos títulos | IDs e lógica atuais em `OcorrenciaGestorFluxo.bindCreateStep`; fundos e dimensões do fluxo “Abrir solicitação” |

Os layouts da introdução e dos quatro passos de criação são compartilhados pelo Gestor e Técnico por `OperationsScreenFragment`. Paisagem usa o layout padrão com rolagem; tablets reaproveitam dimensões `values-sw600dp` já usadas pelo fluxo de solicitação. Não foram criadas variantes dedicadas porque os recursos do fluxo de solicitação existentes já cobrem as larguras suportadas.
