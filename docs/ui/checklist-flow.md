# Fluxo de checklist — registro de ajustes

Revisão da skill `android-xml-builder`: 2026-10-31. Valores de alinhamento e paleta reutilizam recursos já existentes; tamanhos novos foram solicitados nesta conversa.

| Tela / elemento | Valor aplicado | Recurso de origem | Aprovação / transformação |
| --- | --- | --- | --- |
| Lista de checklists: título e cards | Mesma margem horizontal, 28dp no fallback e 64dp em `sw600dp` | `app_events_title_margin_start` em `values/dimens_app.xml` e `values-sw600dp/dimens_app.xml` | Pedido de alinhamento; margem existente reutilizada |
| Cards de checklist | Categoria 16sp; rótulos 12sp; valores 14sp, com fonte Poppins | `ops_sp_16_0`, `ops_sp_12_0`, `ops_sp_14_0` e fontes do projeto | Pedido para ampliar textos; dimensões variam em `sw600dp` |
| Tela de inspeção: título, pesquisa e lista | Margens alinhadas pelo mesmo token; busca usa dimensão equivalente a 42 e ícone ajustado à barra | `app_events_title_margin_start`, `ops_dp_42_0` | Pedido de alinhamento e redução leve; recurso responsivo existente |
| Cards de equipamento | Altura 112dp no fallback e 128dp em `sw600dp`; nome sobe e status usa `ops_sp_10_0` | `app_inspection_equipment_card_height`, `ops_dp_20_0`, `ops_dp_35_0`, `ops_sp_10_0` | Altura definida pelo pedido; valores tablet ajustados para manter proporção |
| Formulário de inspeção | Título em `#153D7D` / `#0B013C`; campos `#DEE3EB`; estrelas limitadas a cinco com preenchimento `#153D7D`; ações alinhadas e abaixo do conteúdo | `ops_color_ff153d7d`, `ops_color_ff0b013c`, `ops_color_ffdee3eb`, `ops_dp_40_0`, `app_events_title_margin_start` | Cores da paleta existente; alterações e comportamento solicitados nesta conversa |
| Cards de checklist: sombra e conteúdo | Sombra 1dp, sem elevação extra na faixa; conteúdo com margem inicial reduzida e respiro direito maior; lista não recorta a sombra | `ops_dp_1_0`, `ops_dp_35_0`, `ops_dp_20_0` | Ajuste solicitado nesta conversa; recursos responsivos existentes |
| Filtro de status | Texto centralizado verticalmente, 12sp de base e padding reduzido para evitar corte | `ops_sp_12_0`, `ops_dp_12_0`, `ops_dp_30_0` | Ajuste solicitado nesta conversa; dimensões responsivas existentes |
