# Política de Privacidade — FIXA e Manu

**Versão:** 0.1 — minuta para validação  
**Data da minuta:** 02/11/2026  
**Situação:** não aprovada para publicação. Os itens marcados **[PREENCHER/VALIDAR]** precisam ser confirmados pelo responsável pelo FIXA.

Esta minuta descreve o tratamento de dados documentado para o FIXA e para a assistente Manu. Ela não presume prazos de retenção, fornecedores, bases legais ou mecanismos que ainda não foram confirmados.

## 1. Quem trata os dados

- **Controlador do FIXA:** [PREENCHER: razão social, CNPJ e endereço].
- **Organização cliente/unidade:** [VALIDAR: em quais operações a instituição cliente decide finalidade e meios do tratamento e atua como controladora].
- **Operador e fornecedores:** [PREENCHER: empresas que hospedam, processam, armazenam ou recebem dados, seus papéis e países de tratamento].
- **Canal de privacidade/Encarregado:** [fixai.inter2026@gmail.com](mailto:fixai.inter2026@gmail.com).

A definição dos papéis de controlador e operador deve refletir quem efetivamente decide as finalidades e os meios do tratamento em cada operação, e não apenas o nome usado no contrato. A [ANPD orienta a identificação dos agentes de tratamento e do encarregado](https://www.gov.br/anpd/pt-br/centrais-de-conteudo/materiais-educativos-e-publicacoes/guia-orientativo-para-definicoes-dos-agentes-de-tratamento-e-do-encarregado).

## 2. Dados tratados

Conforme o recurso utilizado, o FIXA pode tratar:

- **Conta e contexto de acesso:** identificadores de usuário e sessão, perfil de acesso, unidade e informações de perfil necessárias ao uso do serviço.
- **Dados operacionais:** conteúdo de solicitações, ocorrências, eventos, feedback, indicadores e registros de manutenção que o usuário consulta ou envia pelas ferramentas autorizadas.
- **Imagens e anexos:** arquivos ou URLs de imagens enviados nos fluxos autorizados.
- **Uso da Manu:** mensagens, histórico de conversas, metadados de sessão, resumos, propostas de operação e resultados de execução. O projeto também utiliza representações vetoriais para recuperação de contexto.
- **Dados técnicos:** registros necessários para acompanhar falhas e funcionamento. Métricas não devem incluir mensagens, segredos ou identificadores pessoais como rótulos.

Não envie senhas, tokens, códigos de autenticação, chaves privadas ou dados pessoais que não sejam necessários à atividade.

## 3. Finalidades

Os dados são usados, conforme a funcionalidade, para:

1. autenticar usuários e aplicar permissões, autoria, unidade e sessão;
2. executar os fluxos operacionais do FIXA solicitados pelo usuário;
3. responder conversas, recuperar contexto e manter o histórico da Manu;
4. preparar, confirmar e encaminhar operações às ferramentas de negócio;
5. proteger o serviço, diagnosticar falhas e manter sua rastreabilidade técnica.

A Manu só deve executar operações disponibilizadas pelas ferramentas e autorizadas pelo serviço. O perfil declarado na conversa não substitui a identidade autenticada.

## 4. Dados enviados a fornecedores e transferências

O projeto utiliza modelos e integrações externas conforme o recurso, além de componentes de armazenamento e recuperação de contexto. Portanto, não se deve afirmar que todos os dados ficam exclusivamente no dispositivo ou em servidores operados pelo FIXA.

**[PREENCHER/VALIDAR antes da publicação]:** inventário de provedores (incluindo modelos de IA, armazenamento de imagens, hospedagem, bases vetoriais e observabilidade), categorias de dados compartilhados, países de processamento, papéis contratuais, salvaguardas aplicáveis e mecanismos para transferências internacionais. O documento de política da Manu não identifica todos esses fornecedores nem seus países.

## 5. Retenção, exclusão e cópias

O projeto persiste conversas e metadados, URLs de anexos, propostas e resultados de operações, resumos e representações vetoriais para recuperação de contexto. Os prazos de retenção e o procedimento de descarte completo ainda não estão definidos nesta documentação.

Excluir uma conversa pelo aplicativo pode removê-la do histórico ativo. Isso **não comprova apagamento físico imediato** de bancos de dados, resumos, vetores, anexos, sistemas de fornecedores ou backups. O FIXA não deve anunciar prazo ou descarte completo sem confirmação técnica.

**[PREENCHER/VALIDAR]:** prazo de retenção por categoria, critérios de descarte, ciclo de vida de backups e procedimento para atender pedidos de eliminação.

## 6. Segurança e acesso

O acesso deve considerar identidade autenticada, perfil, unidade, autoria e sessão. As instruções da Manu não substituem controles implementados no código. O FIXA deve restringir o acesso a dados ao necessário e evitar registrar credenciais ou conteúdo pessoal desnecessário em logs.

Nenhuma medida de segurança elimina todos os riscos. **[VALIDAR]:** controles técnicos e organizacionais que podem ser descritos publicamente, processo de resposta a incidentes e canal para comunicar suspeita de exposição de dados.

## 7. Decisões automatizadas e limitações da IA

A Manu pode gerar respostas e preparar operações com base em ferramentas e modelos. Uma resposta conversacional, uma proposta ou uma aprovação automática interna não comprova que uma operação foi gravada. A confirmação depende do resultado do serviço executor.

O fluxo atual trata imagens por URL e não comprova análise visual pelo modelo. A Manu também não deve ser tratada como serviço de emergência nem como fonte garantida de informações operacionais em tempo real.

O titular pode solicitar informações e, quando aplicável, revisão de decisões tomadas unicamente por tratamento automatizado que afetem seus interesses. Os direitos e canais são descritos pela [ANPD](https://www.gov.br/anpd/pt-br/assuntos/titular-de-dados-1/direito-dos-titulares); a aplicação concreta deve ser avaliada pelo controlador responsável.

## 8. Direitos dos titulares

Nos termos aplicáveis da legislação de proteção de dados, o titular pode solicitar, entre outros direitos previstos em lei, confirmação e acesso ao tratamento, correção de dados, informação sobre compartilhamentos e revisão de decisões automatizadas. O atendimento deve ser feito pelo controlador responsável, após validação de identidade e conforme as regras legais.

**Canal para solicitações e contato de privacidade:** [fixai.inter2026@gmail.com](mailto:fixai.inter2026@gmail.com).  
**Controlador responsável por responder:** [PREENCHER/VALIDAR por operação/unidade].

Veja a [Lei Geral de Proteção de Dados Pessoais — Lei nº 13.709/2018](https://www2.camara.leg.br/legin/fed/lei/2018/lei-13709-14-agosto-2018-787077-normaatualizada-pl.html) e as orientações oficiais da ANPD.

## 9. Crianças e adolescentes

**[VALIDAR]:** confirmar se o FIXA permite contas de menores e quais medidas são adotadas. Esta minuta não presume público etário nem define procedimento de consentimento ou melhor interesse.

## 10. Atualizações e contato

Esta minuta só passa a ser uma política publicada quando o controlador preencher as informações pendentes, aprovar o conteúdo e disponibilizar a versão vigente nos canais do produto. Alterações de finalidade, ferramentas, fornecedores, dados tratados ou retenção exigem revisão.

**Contato de privacidade:** [fixai.inter2026@gmail.com](mailto:fixai.inter2026@gmail.com).  
**Versão publicada e vigência:** [PREENCHER após aprovação].
