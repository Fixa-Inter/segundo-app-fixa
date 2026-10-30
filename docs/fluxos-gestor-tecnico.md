# FIXA — telas de Gestor e Técnico

Esta proposta acrescenta 135 layouts e estados de interface: 91 do Gestor e 44 do Técnico. Base de revisão: dev em 0c09928a4042cbd02c9891b540c5bdf2efa12ce9. Somente arquivos novos; nenhum arquivo existente da base é alterado.

## Referência e organização

[Figma — cópia de estudante](https://www.figma.com/design/zgDd0qejEdOT2E16qQOsJs/Fixa--Copy-?node-id=319-2). As seções finais utilizadas são Gestor — Kenzo e Técnico — Kenzo. O vínculo dos 178 frames diretos, incluindo autenticação e componentes aproveitados da base, está em mapa-figma-xml.json.

Os XMLs estão em app/src/main/res/layout, com os prefixos fragment_ops_manager_, dialog_ops_manager_, fragment_ops_technician_ e dialog_ops_technician_. Os recursos de cor, texto, tipografia e dimensões usam o sufixo _ops e medidas para 360dp, sw400dp, sw600dp e sw840dp. As fontes Poppins e os auxiliares comuns existentes são reutilizados. Não há capturas de telas inteiras usadas como interface: imagens e ícones são recursos locais individuais.

activity_operations.xml, view_ops_bottom_nav.xml e nav_graph_operations.xml compõem a apresentação compartilhada. As classes novas em ui/operations cuidam da navegação, campos, diálogos e estado de apresentação. O catálogo em assets/operations/screens.json preserva os nós Figma e o vínculo com os componentes nativos.

## Como revisar sem alterar a entrada atual do app

Os fluxos de Gestor e Técnico são acessados pelo aplicativo principal após a autenticação. Não há uma galeria de protótipos separada no launcher.

O manifesto principal, MainActivity, o grafo de navegação existente e todos os outros arquivos anteriores permanecem intactos. Por isso, a entrada automática dos novos perfis pelo login não está aplicada. A integração ao fluxo existente deve ser combinada em uma alteração posterior.

## Comportamento e limites

As telas incluem navegação local, diálogos, campos editáveis, seleção de datas e opções, pesquisa, validação básica e rascunhos por SavedStateHandle. Inclusões, edições e exclusões usam dados de demonstração. O seletor de fotos é do Android e a exportação produz um XLSX de demonstração.

Os novos fluxos não gravam suas operações no servidor. Integração com a API, permissões de negócio, notificações, conversa com a Manu e dados reais de exportação estão pendentes. O trabalho de integração já presente na dev foi preservado.

O texto e as imagens seguem o protótipo, que contém alguns nomes, mensagens e placeholders repetidos. No Técnico, atalhos sem tela própria no protótipo usam Perfil e Checklists. Esse mapeamento está isolado no novo host.

## Responsividade e validação

O dispositivo prioritário para a feira é o Galaxy Tab A7 Lite em retrato. Foram conferidos os 135 estados em telefones de 360dp e 412dp e, na resolução 800×1340, em duas densidades ilustrativas (160 e 213dpi). A revisão anterior inclui capturas dos dois perfis de tablet, auditoria das medidas e revisão visual. As telas mais compridas usam rolagem vertical.

O ambiente de emulação utiliza Android 16/API36. A densidade e a versão do Android do tablet físico não foram confirmadas; teclado, rolagem, tamanho das fontes e desempenho ainda precisam da conferência final nele. Esses testes comprovam as configurações descritas, sem garantir todo dispositivo ou configuração.

A validação para publicação usa uma cópia limpa do commit desta proposta. Os comandos e resultados finais constam no PR. O lint global da base tem erros anteriores em arquivos preservados; esses erros não devem ser ocultados nem corrigidos como parte desta adição de telas.
