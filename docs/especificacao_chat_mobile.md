# Especificação de integração do chat FIXA no mobile

## 1. Objetivo, escopo e convenções

Integrar a assistente Manu ao aplicativo para solicitantes, técnicos e gestores, permitindo listar conversas, recuperar mensagens, iniciar uma conversa, continuar uma conversa e excluir uma conversa. Incluir encerramento, pois essa operação gera o título e o resumo usados no histórico.

As expressões abaixo distinguem o estado atual do trabalho a executar:

| Expressão | Significado |
| --- | --- |
| **Existente na IA** | Comportamento verificado neste repositório. |
| **Obrigatório no mobile** | Regra que o agente deve implementar no backend mobile ou comunicar à equipe do aplicativo. |
| **Proposta de implementação** | Solução indicada para o destino; não é um recurso já existente na IA. |
| **Pendente de ambiente/produto** | Informação não disponível neste repositório, com responsável indicado na seção 15. |

O termo **adaptador mobile** designa a camada do backend mobile que chama a API de IA. A aplicação móvel deve consumir esse adaptador. O adaptador não deve reimplementar o grafo de agentes nem acessar diretamente as bases internas da IA.

O escopo desta integração não inclui alterar o código da IA, criar autenticação paralela, implementar dashboards mobile, renomear conversas, editar mensagens ou acrescentar áudio/documentos. Alterações futuras na origem exigem contrato separado.

### 1.1 Verificação prévia obrigatória das imagens da Manu

**Instrução ao agente do mobile: antes de iniciar a implementação, localize no projeto mobile as imagens da Manu e informe ao usuário onde elas estão.** Faça essa verificação durante o levantamento inicial das configurações e recursos do projeto.

1. Procure as imagens e os arquivos de registro/importação de assets já usados pelo aplicativo. Não presuma diretório, extensão ou nome de arquivo.
2. Verifique se os nomes das imagens correspondem às emoções retornadas pela IA: `NEUTRO`, `CURIOSO`, `PENSATIVO`, `CONFIRMACAO`, `SUCESSO`, `EMPATICO`, `ALERTA`, `ERRO`, `BLOQUEADO`, `CONSELHEIRO`, `AGUARDANDO` e `CELEBRACAO`. Confira diferenças de maiúsculas/minúsculas, acentuação, prefixos e extensões.
3. Antes de implementar, apresente o diretório encontrado e um mapeamento **emoção → caminho real da imagem → identificador/importação no aplicativo**. Indique também imagens ou correspondências ausentes e qual imagem está disponível para `NEUTRO`.
4. Se não localizar os arquivos ou não conseguir determinar a correspondência, peça ao usuário: **“Em qual pasta do projeto mobile estão as imagens da Manu? Indique o caminho e quais arquivos correspondem às emoções da IA.”** Se encontrou apenas parte das imagens, informe os caminhos encontrados e pergunte somente pelas correspondências restantes.

Use os recursos existentes e o mecanismo de assets do projeto. Não invente caminhos nem crie/renomeie imagens automaticamente para preencher lacunas. A localização e o mapeamento precisam estar esclarecidos antes de implementar a renderização da Manu; trabalho independente do backend pode continuar enquanto essa informação estiver pendente.

## 2. Arquitetura e responsabilidades

O caminho de uma mensagem é: aplicativo → backend mobile → API de IA → processamento multiagente → API de IA → backend mobile → aplicativo.

Durante o processamento, a IA também consulta o backend FIXA para validar o perfil e pode criar registros operacionais nele. O backend deve continuar atendendo essas chamadas de domínio diretamente, sem encaminhá-las novamente ao chat.

| Componente | Responsabilidade |
| --- | --- |
| Aplicativo | Seleção da conversa, composição de mensagem, envio de fotos pelo fluxo de upload, apresentação do histórico e estados de carregamento. |
| Adaptador mobile | Autenticação do chamador, encaminhamento do Bearer correto, validação dos dados, imposição de `mobile`, filtragem de visualizações, tratamento de falhas e resposta ao app. |
| API de IA | Validação autenticada, criação de sessão, execução dos agentes, persistência de mensagens/propostas, consulta de histórico, encerramento e exclusão lógica. |
| Backend FIXA | Perfil autenticado e operações de manutenção chamadas pela IA; regras de autorização dessas operações. |
| Serviço de upload | Recebimento/armazenamento de imagens e geração de URLs compatíveis. O provedor e seu contrato são pendentes de ambiente. |

**Existente na IA:** FastAPI com prefixo `/api/v1`; grafo LangGraph com proteção de entrada, supervisor, especialista, julgamento JEV, proteção de saída e classificação de emoção. A resposta HTTP contém o resultado completo do turno. Não existe streaming, SSE, WebSocket ou endpoint de acompanhamento de processamento exposto para esse chat.

MongoDB guarda conversas e propostas; PostgreSQL sustenta consultas/mutações operacionais; Qdrant atende buscas, FAQ e resumos; Databricks atende análises. Modelos/JEV e Pechincha MCP são dependências internas. O aplicativo não precisa das credenciais dessas dependências.

Fontes: `app/main.py`, `app/api/router.py`, `app/services/chat.py`, `app/graph/fluxo.py`, `app/graph/executor.py`, `app/core/clients/`.

## 3. Requisitos e regras invariantes

| ID | Obrigação da integração |
| --- | --- |
| RF-01 | Listar conversas do usuário autenticado e usar `session_id` como chave estável; mostrar título ou o rótulo substituto definido na seção 8. |
| RF-02 | Recuperar uma conversa específica e preservar a ordem das mensagens. |
| RF-03 | Iniciar conversa no primeiro envio sem identificador; armazenar o ID retornado. |
| RF-04 | Continuar usando esse mesmo ID como `thread_id`. |
| RF-05 | Excluir pelo endpoint de exclusão do histórico, sem confundir com encerramento. |
| RF-06 | Enviar `plataforma: "mobile"` em todo turno e impedir entrega/renderização de dashboards. |
| RF-07 | Preservar identidade do usuário, propriedade da sessão e autorização do perfil. |
| RF-08 | Suportar URLs de imagens e aceite por mensagem, conforme os contratos atuais. |
| RF-09 | Disponibilizar encerramento para geração de título/resumo, mantendo possibilidade de retomada. |
| RF-10 | Tratar falhas de rede e efeitos incertos sem repetir mensagens automaticamente. |
| RF-11 | Renderizar a imagem local da Manu correspondente ao campo `emocao` da resposta da IA, após localizar e documentar os assets no projeto mobile. |

**Obrigatório no mobile:** o adaptador deve reconstruir o body enviado à IA a partir dos campos permitidos, fixar `plataforma` no servidor e usar o token do usuário autenticado. Não encaminhar corpos ou headers arbitrários para uma URL fornecida pelo cliente. A URL de destino deve ser configuração de servidor.

Um HTTP 200 significa que existe uma resposta conversacional válida, inclusive de recusa ou indisponibilidade. Não comprova que uma solicitação/ocorrência foi criada. `emocao`, `intencao` e `confirmacao` também não são recibos de uma operação de negócio.

## 4. Perfis e capacidades do chatbot

O perfil vem da autenticação. Não existe seletor de perfil no body do chat.

| Capacidade | Solicitante | Técnico | Gestor |
| --- | --- | --- | --- |
| Conversar, consultar FAQ e enviar feedback | Sim | Sim | Sim |
| Listar/recuperar/excluir as próprias conversas | Sim | Sim | Sim |
| Criar solicitação de reparo | Sim | Sim | Não disponibilizada pelas ferramentas de criação |
| Criar ocorrência de serviço já realizado | Não | Sim | Sim |
| Consultar minhas solicitações por ferramenta dedicada | Sim | Sim | Não disponibilizada nessa forma; há consulta gerencial |
| Consultar solicitações pendentes da unidade por ferramenta gerencial | Não | Não | Sim |
| Consultar OS, tarefas, equipamentos, ocorrências e eventos por ferramentas especializadas | Acesso restrito às ferramentas do perfil | Sim, conforme autorização | Sim, conforme autorização |
| Consultar disponibilidade de técnicos por ferramenta gerencial | Não | Não | Sim |
| Alterar solicitação própria pendente/anexar foto | Sim, conforme autorização | Sim, conforme autorização | Ferramentas presentes, sujeitas à propriedade e ao estado do registro |
| Anexar foto a ocorrência/observação de conclusão | Não | Conforme propriedade/responsabilidade | Conforme unidade e autorização |
| Indicadores gerenciais textuais | Não | Não | Sim |
| Pesquisa/comparação de ofertas de compra | Não | Sim | Sim |
| Gráficos/dashboards no mobile | Não | Não | Não |

Consultas de registros e análise agregada são caminhos diferentes: “quais solicitações estão pendentes?” pode listar registros; “qual a taxa de aprovação?” é análise gerencial. A IA resolve a intenção; o app não deve escolher agentes pelo texto ou por parâmetros extras.

Uma **solicitação** pede reparo ainda necessário. Uma **ocorrência** registra manutenção já realizada. Os nomes de intenção não concedem permissão: as ferramentas e os serviços verificam acesso.

O código também aceita `administrador`, com capacidades gerenciais semelhantes às do gestor. Sua exposição no mobile está fora dos três perfis solicitados e deve ser decidida pelo produto; não converter esse perfil em gestor nem conceder acesso por inferência no adaptador.

Prompts descrevem objetivos gerais dos perfis, mas não comprovam que o chat execute todas as ações do aplicativo. Não anunciar aprovação de solicitações, distribuição de tarefas ou execução de checklist sem uma ferramenta/contrato que implemente a operação.

Fontes: `app/agents/registro.py::AGENTES_POR_PERFIL`, `app/agents/solicitacoes.py::criar_agente_solicitacoes`, `app/agents/ajuda_geral.py::criar_agente_ajuda_geral`, `app/agents/analista_compras.py`, `app/services/ajuda_geral/edicoes.py`, `app/graph/nos.py`.

## 5. Autenticação e contexto confiável

### 5.1 Headers e obtenção de identidade

Todas as seis operações de chat/sessões deste documento exigem:

```http
Authorization: Bearer <TOKEN_DO_USUARIO>
Accept: application/json
```

Operações com body usam `Content-Type: application/json`. GET e DELETE não precisam de body.

**Existente na IA:** a dependência `obter_usuario_atual` consulta `GET {FIXA_API_BASE_URL}/api/v1/perfil` com o mesmo Bearer. A IA não emite tokens nem implementa login/renovação. Um token de serviço compartilhado não substitui a identidade de cada pessoa.

O perfil externo é um objeto JSON, sem envelope intermediário esperado por esse código:

```json
{
  "id": 123,
  "nomeCompleto": "Pessoa de Exemplo",
  "email": "pessoa@example.com",
  "tipoAcesso": "Técnico",
  "cnpjUnidade": "12.345.678/0001-90",
  "cargo": "Técnico de manutenção",
  "dataNascimento": null
}
```

| Campo externo | Necessidade para a IA | Conversão interna |
| --- | --- | --- |
| `id` | Obrigatório, compatível com inteiro | `usuario_id` |
| `nomeCompleto` | Obrigatório, texto | `nome_completo` |
| `email` | Obrigatório, texto | `email` |
| `tipoAcesso` | Obrigatório, texto | Trim/minúsculas; `técnico` e `tecnico` tornam-se `tecnico` |
| `cnpjUnidade` | Obrigatório, texto | Remove não dígitos; exige 14 dígitos; vira `cnpj_endereco` |
| `cargo` | Opcional, texto ou null | `cargo` |
| `dataNascimento` | Opcional, data compatível com `YYYY-MM-DD` ou null | `data_nascimento` |

O body do chat não deve conter `usuario_id`, `user_id`, email, perfil, CNPJ, unidade ou token. O adaptador deve obter identidade da sessão autenticada e encaminhar somente os campos públicos documentados.

### 5.2 Isolamento e acesso

As consultas públicas de histórico usam `user_id` e `esta_ativo=True`. Uma conversa alheia, inexistente ou excluída não fica disponível pelo ID informado. Os caminhos correspondentes retornam 404 sem revelar o dono.

O ID interno do checkpoint é `usuario_id:thread_id`; o cliente utiliza apenas `thread_id`/`session_id`, nunca esse valor composto. Nas operações de manutenção, a IA também resolve o vínculo ativo do usuário à unidade por PostgreSQL e aplica as permissões do domínio.

O histórico atual é separado por usuário, sem filtro de plataforma/unidade. A mesma conta pode ver conversas da web no mobile. Não apresentar “histórico exclusivo do mobile” nem “histórico restrito à unidade atual” sem uma mudança específica na origem. Em troca de conta, limpar seleção/cache local; em mudança de unidade/perfil, invalidar caches operacionais e seguir a decisão de produto sobre conversas antigas, sem alterar sua propriedade.

Fontes: `app/api/dependencies/auth.py`, `app/services/gerais/perfil.py::consultar_perfil`, `app/schemas/UsuarioContexto.py`, `app/services/gerais/unidade.py`, `app/services/sessoes/historico.py`.

## 6. Contratos HTTP

### 6.1 Endereçamento e mapeamento

`IA_BASE_URL` neste documento é um placeholder de configuração do adaptador, sem o sufixo `/api/v1`. Seu valor real por ambiente é pendente. Não é uma variável já exigida pelo serviço de IA. `FIXA_API_BASE_URL`, usada pela própria IA, aponta no sentido inverso para o backend FIXA.

O host descrito em `docs/api/swagger.json` pertence à API FIXA/mobile; não deve ser assumido como host da IA.

**Proposta de implementação:** reservar `/api/v1/ia` no backend mobile para evitar conflito com rotas de manutenção. Esse namespace é novo e não existe neste repositório. Se o backend de destino já tiver convenção equivalente, preservar a convenção e publicar o mapeamento escolhido junto da implementação. Os caminhos da coluna IA são obrigatoriamente os existentes abaixo.

| Operação | Rota proposta no adaptador mobile | Rota existente na IA |
| --- | --- | --- |
| Nova conversa/continuar | `POST /api/v1/ia/chat` | `POST /api/v1/chat` |
| Listar | `GET /api/v1/ia/sessoes` | `GET /api/v1/sessoes` |
| Recuperar | `POST /api/v1/ia/sessoes/conversa` | `POST /api/v1/sessoes/conversa` |
| Encerrar | `DELETE /api/v1/ia/chat/sessions/{thread_id}` | `DELETE /api/v1/chat/sessions/{thread_id}` |
| Excluir | `DELETE /api/v1/ia/sessoes/conversa/{session_id}` | `DELETE /api/v1/sessoes/conversa/{session_id}` |

Os exemplos a seguir mostram a chamada enviada **à IA**. A proposta do adaptador preserva os bodies e envelopes desses exemplos, com as transformações obrigatórias da seção 10 e as regras de erros da seção 12. Nenhuma transformação pode mudar o ID da conversa.

### 6.2 Schemas compartilhados

#### Entrada `ConversaEntrada`

| Campo | Tipo | Obrigatório na IA | Nulo | Regra |
| --- | --- | --- | --- | --- |
| `mensagem` | string | Sim | Não | `strip` antes da validação; entre 1 e 4.000 caracteres após normalização |
| `plataforma` | string enum | Sim | Não | Exatamente `web` ou `mobile`; nesta integração sempre `mobile` |
| `thread_id` | string | Não | Sim | Ausente/null cria sessão; quando presente, `strip`, 1 a 100 caracteres e sessão existente |
| `imagens` | array de strings | Não | Não | Padrão `[]`; até três URLs por mensagem; validação da seção 11 |

String vazia não equivale a `null`. Não enviar `thread_id: ""` para criar sessão. Não criar um UUID no app para enviá-lo como sessão existente: o servidor gera o ID no primeiro envio.

O schema de chat da IA usa o comportamento padrão do Pydantic para extras, que são ignorados; isso não lhes dá efeito. **Proposta do adaptador:** rejeitar campos desconhecidos com 422 e aceitar `plataforma` apenas como `mobile` ou ausente; se ausente, preenchê-la. `web`, `null`, diferenças de capitalização ou outros valores devem ser recusados no adaptador. Toda chamada efetivamente encaminhada deve conter o literal `mobile` definido no servidor.

#### Saída `ConversaSaida`

| Campo | Tipo | Presença na saída da IA | Significado |
| --- | --- | --- | --- |
| `thread_id` | string | Sempre | Identificador da sessão usada/criada |
| `intencao` | enum ou null | Sempre; pode ser null | Encaminhamento do turno, sem garantia de efeito de negócio |
| `resposta` | string | Sempre | Texto final para exibir; resposta vazia é tratada como erro pelo serviço |
| `emocao` | enum | Sempre | Padrão `NEUTRO`; determina qual imagem local da Manu renderizar, conforme seção 13.3.1 |
| `confirmacao` | enum `sim`, `nao` | Sempre | Padrão `nao`; apresentação de proposta conforme seção 9 |
| `visualizacao` | objeto | Opcional; omitido quando nulo | Contrato web. Deve ser removido pelo adaptador mobile |

Valores de `intencao`: `ajuda_geral`, `faq`, `solicitacao`, `ocorrencia`, `analytics`, `visualizacao`, `feedback`, `analista_compras`, `fora_de_escopo`; também pode ser `null`.

Valores de `emocao`: `NEUTRO`, `CURIOSO`, `PENSATIVO`, `CONFIRMACAO`, `SUCESSO`, `EMPATICO`, `ALERTA`, `ERRO`, `BLOQUEADO`, `CONSELHEIRO`, `AGUARDANDO`, `CELEBRACAO`. O aplicativo deve ter apresentação neutra para metadado desconhecido em versões futuras, sem inferir sucesso/permissão por emoção.

#### Objeto `MensagemHistorico`

| Campo | Tipo | Comportamento público atual |
| --- | --- | --- |
| `role` | `human` ou `assistant` | Obrigatório; distinguir pessoa e Manu |
| `mensagem` | string | Obrigatório; corresponde ao `content` interno persistido |
| `confirmacao` | `sim` ou `nao` | Presente; padrão `nao`; humano sempre formatado como `nao` |
| `emocao` | enum ou null no schema | Opcional; omitida quando nula. Assistente é normalizado para emoção conhecida, padrão `NEUTRO` |
| `imagens` | array de strings ou null no schema | Opcional; omitida quando nula; sem imagens pode estar ausente, não exigir `[]` |
| `visualizacao` | objeto ou null no schema | Opcional e somente de assistente; a IA pode incluir por perfil. Remover no adaptador |

Não há ID público, timestamp por mensagem, intenção por mensagem nem estado atual da proposta nesse objeto. Não fabricar esses campos como se viessem da IA. O adaptador não precisa validar/interpretar o contrato de gráfico: deve descartá-lo antes de construir a saída mobile.

#### Objeto `ConversaHistorico`

| Campo | Tipo | Regra |
| --- | --- | --- |
| `session_id` | string | Mesmo identificador chamado `thread_id` no chat |
| `iniciada_em` | string date-time | Data de início; política temporal na seção 8 |
| `atualizada_em` | string date-time | Última atualização persistida; usada na ordenação |
| `titulo` | string | Pode ser `""`; gerado no encerramento |
| `resumo` | string | Pode ser `""`; gerado no encerramento |
| `mensagens` | array de `MensagemHistorico` | Histórico completo formatado, inclusive array vazio |

IDs são strings de até 100 caracteres nos schemas de entrada/caminho. Novas sessões usam UUID v4 gerado pela IA, mas a validação pública não exige formato UUID. Tratar o ID como opaco e codificá-lo como segmento de URL ao usá-lo em DELETE; não concatenar valores não codificados na URL.

Fontes dos schemas: `app/schemas/chat.py`, `sessao.py`, `id_sessao.py`, `imagem.py`, `app/core/emocoes.py`.

### 6.3 Iniciar conversa — `POST /api/v1/chat`

Gatilho: primeiro envio da pessoa em uma tela de nova conversa. A sessão só passa a existir na origem quando esse POST é processado. Não há rota de criação vazia.

Requisição mínima enviada à IA:

```http
POST /api/v1/chat
Authorization: Bearer <TOKEN_DO_USUARIO>
Content-Type: application/json
```

```json
{
  "mensagem": "Como acompanho minhas solicitações?",
  "plataforma": "mobile"
}
```

A forma explícita e completa equivalente é:

```json
{
  "mensagem": "Como acompanho minhas solicitações?",
  "plataforma": "mobile",
  "thread_id": null,
  "imagens": []
}
```

Resposta HTTP 200, exemplo compatível com `ConversaSaida`:

```json
{
  "thread_id": "65b27f9d-2243-4bfe-8898-0cedc447fbd1",
  "intencao": "faq",
  "resposta": "Acesse a área Minhas solicitações no aplicativo para acompanhar o andamento.",
  "emocao": "NEUTRO",
  "confirmacao": "nao"
}
```

O adaptador deve retornar o ID recebido e o app deve guardá-lo antes de permitir o próximo envio. A resposta não contém título/resumo ou ID da mensagem. Recarregar a lista após sucesso; a sessão recém-criada pode ter título vazio.

Efeitos: criação do documento da sessão, gravação da mensagem humana e, quando o processamento termina com sucesso, da resposta. Essas etapas não são uma transação única. Falha no primeiro envio pode deixar sessão/mensagem salva sem o cliente conhecer o ID. Ver seção 12.4.

Erros aplicáveis: autenticação/perfil, 422 de entrada, 502 de resposta vazia e falhas internas/dependências. Não presumir que todo erro significa que nada foi persistido.

Fontes: `app/api/routes/chat.py::conversar`, `app/services/chat.py::_processar_chat`, `app/memory/persistencia/sessoes/inicio.py`, `app/graph/executor.py::executar_grafo`.

### 6.4 Continuar conversa — `POST /api/v1/chat`

Gatilho: mensagem, pergunta, correção, aceite ou envio de foto em conversa existente. Método, headers e saída são os mesmos da criação; `thread_id` deve conter o identificador selecionado.

Exemplo mínimo de continuidade:

```json
{
  "mensagem": "Quais informações preciso fornecer?",
  "plataforma": "mobile",
  "thread_id": "65b27f9d-2243-4bfe-8898-0cedc447fbd1"
}
```

Exemplo completo com imagem, enviada previamente por um serviço de upload:

```json
{
  "mensagem": "Segue a foto para a solicitação que estamos preparando.",
  "plataforma": "mobile",
  "thread_id": "65b27f9d-2243-4bfe-8898-0cedc447fbd1",
  "imagens": ["https://cdn.example.com/manutencao/foto-1.jpg"]
}
```

A URL é ilustrativa, não um serviço de upload real. A resposta mantém o mesmo `thread_id`; o texto e a intenção dependem do estado. Não reenviar `imagens` automaticamente nos turnos seguintes: anexos já persistidos ficam sob controle do fluxo da IA.

Se a sessão não estiver acessível ao usuário, o serviço não cria outra com o ID recebido:

```json
{
  "detail": "Conversa não encontrada."
}
```

Essa resposta usa HTTP 404. Aplica-se a sessão inexistente, alheia ou excluída. O cliente deve recarregar a lista, manter o texto ainda não enviado como rascunho local e permitir selecionar/iniciar outra conversa explicitamente.

Um turno deve continuar na conversa que o originou, mesmo que a pessoa troque de tela antes da chegada da resposta. O app não deve inserir a resposta na conversa atualmente visível por conveniência.

Fontes: `app/services/chat.py::_processar_chat`, `app/memory/persistencia/utilitarios.py::sessao_acessivel`, `app/graph/executor.py`.

### 6.5 Listar conversas — `GET /api/v1/sessoes`

Gatilho: abrir/atualizar lista, retornar ao app, concluir primeiro envio, encerrar ou excluir. Não há body, query de plataforma nem paginação declarada.

```http
GET /api/v1/sessoes
Authorization: Bearer <TOKEN_DO_USUARIO>
Accept: application/json
```

Resposta HTTP 200 sem conversas:

```json
{
  "status": "SEM_DADOS",
  "mensagem": "Você ainda não possui conversas salvas.",
  "conversas": []
}
```

Resposta HTTP 200 com dados, após eventual encerramento:

```json
{
  "status": "SUCESSO",
  "mensagem": "Conversas carregadas com sucesso.",
  "conversas": [
    {
      "session_id": "65b27f9d-2243-4bfe-8898-0cedc447fbd1",
      "iniciada_em": "2026-10-31T15:00:00Z",
      "atualizada_em": "2026-10-31T15:05:00Z",
      "titulo": "Acompanhamento de solicitações",
      "resumo": "A pessoa perguntou como acompanhar suas solicitações e recebeu orientação sobre a área correspondente do aplicativo.",
      "mensagens": [
        {
          "role": "human",
          "mensagem": "Como acompanho minhas solicitações?",
          "confirmacao": "nao"
        },
        {
          "role": "assistant",
          "mensagem": "Acesse a área Minhas solicitações no aplicativo para acompanhar o andamento.",
          "emocao": "NEUTRO",
          "confirmacao": "nao"
        }
      ]
    }
  ]
}
```

O exemplo usa ISO 8601 com UTC explícito para leitura; a origem pode devolver datas sem offset, conforme seção 8. `titulo` e `resumo` também podem vir como strings vazias.

Schema do envelope: `status` obrigatório `SUCESSO|SEM_DADOS`, `mensagem` obrigatória string e `conversas` obrigatório array de `ConversaHistorico`.

**Existente na IA:** todas as conversas ativas do usuário, ordenadas por `atualizada_em` decrescente, com mensagens completas. Não há limite/paginação, contagem total ou filtro por título/plataforma. Não prometer desempate específico entre datas iguais.

**Obrigatório no mobile:** filtrar visualizações de cada mensagem em cada item antes de responder/cachear. Na primeira versão, preservar o envelope e o array completo. Se a interface paginar a lista em memória, isso não reduz o volume buscado na origem. Paginação real exige evolução de contrato separada.

Falhas específicas tratadas: 500 de configuração/interpretação e 503 de persistência indisponível, além dos erros de autenticação. Falha não deve ser convertida em `SEM_DADOS`.

Fontes: `app/api/routes/sessoes.py::pegar_todas_conversas_usuario`, `app/services/sessoes/historico.py::buscar_conversas_usuario`.

### 6.6 Recuperar conversa — `POST /api/v1/sessoes/conversa`

É uma consulta feita por POST. Não substituir por um GET inventado nem enviar plataforma no body.

```http
POST /api/v1/sessoes/conversa
Authorization: Bearer <TOKEN_DO_USUARIO>
Content-Type: application/json
```

```json
{
  "session_id": "65b27f9d-2243-4bfe-8898-0cedc447fbd1"
}
```

`session_id` é obrigatório, não nulo, normalizado com `strip` e deve ter 1 a 100 caracteres. **Campos extras são proibidos na IA**, incluindo `plataforma`. Esse endpoint não executa o grafo nem gera nova mensagem/título.

Resposta HTTP 200, mostrando também o caso de sessão com título ainda vazio:

```json
{
  "status": "SUCESSO",
  "mensagem": "Conversa carregada com sucesso.",
  "conversa": {
    "session_id": "65b27f9d-2243-4bfe-8898-0cedc447fbd1",
    "iniciada_em": "2026-10-31T15:00:00Z",
    "atualizada_em": "2026-10-31T15:01:00Z",
    "titulo": "",
    "resumo": "",
    "mensagens": [
      {
        "role": "human",
        "mensagem": "Como acompanho minhas solicitações?",
        "confirmacao": "nao"
      },
      {
        "role": "assistant",
        "mensagem": "Acesse a área Minhas solicitações no aplicativo para acompanhar o andamento.",
        "emocao": "NEUTRO",
        "confirmacao": "nao"
      }
    ]
  }
}
```

Schema do envelope: `status` obrigatório e igual a `SUCESSO`, `mensagem` string obrigatória e `conversa` objeto `ConversaHistorico` obrigatório.

Sessão ausente/alheia/excluída retorna HTTP 404:

```json
{
  "detail": "A conversa solicitada não foi encontrada."
}
```

Não existe `SEM_DADOS` para uma conversa inexistente. Uma conversa existente pode ter `mensagens: []`. Aplicam-se também 422, 500 e 503 nos caminhos tratados, além de autenticação.

**Obrigatório no mobile:** filtrar visualizações antes da saída e usar `conversa.session_id` como `thread_id` no próximo envio. Recarregar essa consulta ao abrir a conversa, mesmo que o item da lista contenha mensagens, para reconciliar alterações recentes.

Fontes: `app/api/routes/sessoes.py::pegar_conversa_especifica`, `app/schemas/sessao.py::ConversaEspecificaEntrada`, `app/services/sessoes/historico.py::buscar_conversa`.

### 6.7 Encerrar conversa — `DELETE /api/v1/chat/sessions/{thread_id}`

Gatilho proposto para a primeira versão: ação explícita **Finalizar conversa**. Não chamar automaticamente ao suspender o app, a cada mensagem ou em todo retorno da tela. Isso evita gerar resumos repetidos e depender do ciclo de vida do sistema operacional para salvar metadados.

```http
DELETE /api/v1/chat/sessions/65b27f9d-2243-4bfe-8898-0cedc447fbd1
Authorization: Bearer <TOKEN_DO_USUARIO>
Accept: application/json
```

Sem body. O ID do caminho recebe `strip` e validação de 1 a 100 caracteres.

Resposta HTTP 200:

```json
{
  "thread_id": "65b27f9d-2243-4bfe-8898-0cedc447fbd1",
  "status": "ENCERRADA",
  "mensagem": "Sessão encerrada com sucesso.",
  "titulo": "Acompanhamento de solicitações",
  "resumo": "A pessoa perguntou como acompanhar suas solicitações e recebeu orientação sobre a área correspondente do aplicativo."
}
```

Schema: `thread_id`, `status` e `mensagem` são strings obrigatórias; o serviço retorna `status: ENCERRADA`, embora o schema desse campo seja string livre. `titulo` e `resumo` são opcionais/anuláveis no modelo e retornados como string ou `null` por essa rota. Sessão sem mensagens pode retornar ambos como `null`; não há API pública para criar deliberadamente uma sessão vazia.

Efeitos existentes: gera título/resumo com modelo, atualiza o documento e `atualizada_em`, indexa resumo no Qdrant quando há resumo e remove checkpoint do grafo após o serviço de encerramento concluir. **Não marca `esta_ativo=False` e não apaga mensagens.** A sessão continua listável e pode receber novos turnos com o mesmo ID. Propostas persistidas não são automaticamente todas canceladas.

O app deve atualizar título/resumo, recarregar a lista e manter a opção de reabrir. O encerramento não fornece um campo persistente “encerrada” nas consultas de histórico.

404 usa `detail: "Conversa não encontrada."`. ID inválido retorna 422. A rota também converte `ValueError` em 422, inclusive erros de validação que possam surgir ao gerar resumo; esse caso não deve ser apresentado automaticamente como erro de digitação da pessoa. Outras falhas podem ocorrer antes/depois da atualização no MongoDB. Repetir encerramento pode gerar outro resumo e nova data, portanto não há garantia de resultado idêntico.

Fontes: `app/api/routes/chat.py::encerrar_conversa`, `app/services/sessao.py::finalizar_sessao_chat`, `app/memory/persistencia/sessoes/encerramento.py`, `app/memory/persistencia/gerar_resumo.py`.

### 6.8 Excluir conversa — `DELETE /api/v1/sessoes/conversa/{session_id}`

Gatilho: exclusão explícita de um item do histórico. O aplicativo deve informar o efeito e solicitar confirmação de exclusão pela própria interface; isso é uma regra de UX proposta, independente do campo `confirmacao` do chat.

```http
DELETE /api/v1/sessoes/conversa/65b27f9d-2243-4bfe-8898-0cedc447fbd1
Authorization: Bearer <TOKEN_DO_USUARIO>
Accept: application/json
```

Sem body. ID com `strip`, entre 1 e 100 caracteres. Resposta HTTP 200:

```json
{
  "session_id": "65b27f9d-2243-4bfe-8898-0cedc447fbd1",
  "status": "EXCLUIDA",
  "mensagem": "Conversa removida do histórico."
}
```

Os três campos são obrigatórios; `status` é enum literal `EXCLUIDA`. O adaptador deve preservar a distinção entre `session_id` dessa resposta e o nome `thread_id` usado no chat, mantendo o mesmo valor.

Efeito existente: atualiza logicamente documentos ativos da sessão/usuário para `esta_ativo=False` e remove a entrada do cache de sessões do processo. Não gera resumo nem executa o encerramento. Não apaga fisicamente fotos, vetores, checkpoints ou registros de manutenção já criados.

Após sucesso, remover o item do cache/lista e limpar a seleção se era a conversa aberta. Próximas consultas/continuações por esse ID retornam 404. A segunda exclusão retorna 404 com `detail: "A conversa solicitada não foi encontrada."`; não é erro 200 idempotente. O app pode atualizar a lista e mostrar que o item já não está disponível, sem concluir que uma sessão alheia foi apagada.

Aplicam-se 422, erros de autenticação, 500/503 de histórico. Não excluir enquanto houver uma chamada de chat conhecida em andamento na mesma tela; isso não elimina a possibilidade de corrida com outro dispositivo. Em resposta tardia de sessão localmente excluída, não recriar o item no cache nem reapresentar sua resposta como conversa ativa.

Fontes: `app/api/routes/sessoes.py::excluir_conversa_historico`, `app/services/sessoes/historico.py::excluir_conversa`.

## 7. Ciclo de vida e sincronização

### 7.1 Fluxo operacional do aplicativo

1. Ao abrir a área de chat, autenticar e listar conversas. Distinguir carregamento, falha e lista vazia.
2. Ao selecionar item, recuperar histórico pelo `session_id`; substituir a cópia local confirmada pelos dados da origem após a filtragem mobile.
3. Ao acionar **Nova conversa**, limpar a seleção e preparar um rascunho sem ID. Nenhuma sessão é criada até o primeiro envio.
4. Ao enviar, capturar a conversa de origem da requisição e impedir novo envio pela mesma tela enquanto houver um turno pendente.
5. Na criação bem-sucedida, associar o `thread_id` recebido ao rascunho. Em continuidade, conferir que a resposta corresponde à sessão enviada antes de apresentá-la.
6. Após cada sucesso, atualizar a lista e reconciliar o histórico da conversa. Evitar simplesmente concatenar a resposta em uma lista já recarregada, duplicando mensagens.
7. Ao finalizar explicitamente, aguardar o turno pendente terminar, chamar encerramento e atualizar metadados. Finalizar não precisa abrir outra conversa automaticamente.
8. Ao excluir, aguardar operações locais conhecidas, confirmar a ação e aplicar o fluxo da seção 6.8.
9. Ao retornar de suspensão/reconexão, recarregar lista e conversa selecionada; não reenviar a última mensagem pendente automaticamente.

### 7.2 Estados locais da primeira versão

**Proposta de implementação, sem novos campos na IA:** `rascunho`, `enviando`, `carregada`, `erro_de_leitura`, `resultado_incerto` e `excluida`. São estados do app/adaptador, não enums retornados pelo serviço de IA.

Uma chave temporária local pode identificar o rascunho antes de receber a sessão. Ela não deve ser enviada como `thread_id`. Mensagens locais pendentes podem ter IDs locais para renderização, sem fingir que a IA reconhece esses IDs.

O histórico da IA não fornece ID/timestamp por mensagem. Portanto, igualdade de texto ou contagem de mensagens não é prova de deduplicação: duas mensagens iguais podem ser legítimas. Em reconciliação, usar a lista recuperada como histórico confirmado, conservar separadamente o rascunho incerto e pedir decisão explícita quando houver ambiguidade.

O checkpointer da IA é `MemorySaver`, no processo. Em ausência de estado em memória, o executor recupera mensagens/propostas do MongoDB. Isso permite retomada, mas não garante continuidade idêntica de checkpoint entre réplicas/reinícios. O adaptador não deve depender de afinidade de processo como solução de integridade.

Fontes: `app/graph/fluxo.py::construir_grafo`, `app/graph/executor.py::executar_grafo`, `app/memory/persistencia/recuperar_mensagens.py`.

## 8. Histórico, títulos e datas

**Existente na IA:** `titulo` e `resumo` começam como `""`. A geração ocorre no encerramento; título validado tem uma linha e até dez palavras, e deve ser distinto do resumo. Não há endpoint público de renomear conversa nem geração automática no primeiro turno.

**Proposta de apresentação para a primeira versão:** usar `titulo.trim()` quando não vazio; caso contrário, exibir **Nova conversa** com a data de atualização como informação secundária. Não gravar esse rótulo como título persistido nem acrescentar `titulo_exibicao` ao JSON da IA sem versionar o contrato do adaptador.

Quando a pessoa continuar uma conversa já finalizada, o título/resumo anteriores podem ficar desatualizados até novo encerramento. Não tratar resumo como descrição de todos os turnos mais recentes. Encerrar atualiza `atualizada_em`, podendo reposicionar o item na lista.

O histórico público contém apenas papéis `human`/`assistant` e a ordem do array persistido. Não são expostas mensagens de ferramentas, raciocínio interno ou estado completo de proposta. A anonimização de entrada/saída pode alterar o texto persistido em relação ao digitado. Preservar a versão retornada pela origem como histórico confirmado.

Datas de sessão são geradas em UTC. O cliente MongoDB atual não declara `tz_aware`; a serialização pode retornar ISO 8601 sem offset. O consumidor deve aceitar timestamps com offset e, **somente nos campos de sessão `iniciada_em`/`atualizada_em` desta origem**, interpretar ausência de offset como UTC. Exibir no fuso do aplicativo/usuário. Não anexar UTC por suposição a datas de negócio de outros domínios. Confirmar esses formatos em homologação com uma leitura real.

Referências temporais conversacionais de negócio usam `America/Sao_Paulo`; isso não muda a base UTC dos timestamps da sessão. Não há timestamp por mensagem ou indicador persistente de sessão encerrada no histórico.

Fontes: `app/memory/persistencia/sessoes/inicio.py`, `encerramento.py`, `app/memory/persistencia/gerar_resumo.py`, `utilitarios.py::agora`, `app/core/clients/mongo.py`, `app/core/tempo.py`, `app/services/sessoes/historico.py`.

## 9. Confirmações, propostas e efeitos de negócio

### 9.1 Significado do campo público

`confirmacao` aceita somente as strings `sim` e `nao`. `sim` significa que uma proposta de solicitação/ocorrência foi apresentada e seu registro de apresentação foi confirmado pelo fluxo. Não significa aceite da pessoa nem criação concluída.

O executor salva a resposta do assistente e depois marca a apresentação/flag conforme a proposta. Existem janelas de falha entre etapas; não presumir transação única. `confirmacao: "nao"` também não prova que não exista uma operação pendente: edições/anexos de registros existentes seguem confirmação textual sem usar esse sinal universalmente.

**Regra da primeira versão:** a interface deve permitir confirmação por texto. Não reativar botões de confirmação a partir de flags históricas. Um eventual atalho de confirmar só pode corresponder à última resposta recebida ao vivo naquela sessão, deve ser desabilitado após o próximo envio/recarga e precisa funcionar como envio de texto normal.

### 9.2 Exemplo de proposta e aceite

Exemplo ilustrativo de resposta HTTP 200 após coleta de dados:

```json
{
  "thread_id": "65b27f9d-2243-4bfe-8898-0cedc447fbd1",
  "intencao": "solicitacao",
  "resposta": "Confira os dados:\n- Título: Torneira com vazamento\n- Problema: A torneira continua pingando após ser fechada.\n- Detalhes do local: Pia ao lado da porta de entrada.\n- Local: Banheiro do térreo\n- Categoria do equipamento: Torneira\nEssas informações estão corretas? Se precisar, indique somente o que devo corrigir.",
  "emocao": "CONFIRMACAO",
  "confirmacao": "sim"
}
```

Aceite é outra chamada a `POST /api/v1/chat`:

```json
{
  "mensagem": "Confirmo os dados",
  "plataforma": "mobile",
  "thread_id": "65b27f9d-2243-4bfe-8898-0cedc447fbd1",
  "imagens": []
}
```

Não enviar `confirmacao: true`, IDs de proposta ou versão do rascunho; esses campos não são entrada pública. Se faltar foto, o fluxo pode pedir o anexo em vez de criar o registro, mesmo após o aceite. Exibir a próxima resposta sem substituir seu resultado por uma mensagem fixa de “cadastrado”.

### 9.3 Regras de continuidade

- Perguntas e correções não autorizam envio. “Sim, mas troque o local” não deve ser convertido pelo app em “sim”. Encaminhar o texto completo.
- O sistema pode aceitar somente campos identificados no aceite e reapresentar os restantes; não confirmar tudo automaticamente pelo frontend.
- Fotos enviadas após aceite podem permitir retomar o cadastro sem exigir novamente campos inalterados. O app deve manter a sessão e não iniciar outra.
- Pausa, retomada e cancelamento de proposta são mensagens na mesma sessão, como “pausar a solicitação”, “retomar a solicitação” e “cancelar a solicitação”. Não chamar DELETE de sessão para cancelar proposta.
- Uma resposta isolada “sim” não retoma automaticamente proposta pausada. Com múltiplas propostas possíveis, a IA pode esclarecer com `intencao: null`.
- Trocar de assunto pode pausar o fluxo anterior. Não prender o app a um formulário rígido por causa de intenção anterior.

As ferramentas usam rascunhos/versionamento e bloqueios de alguns estados de envio. Isso não fornece idempotência geral de HTTP. A API pública não devolve recibo estruturado de criação, estado completo da proposta ou ID público de turno. Não extrair recibos por regex de texto gerado.

Uma ferramenta pode criar registro antes de o julgamento/saída falhar. A mensagem final pode orientar consultar registros antes de repetir. Os estados internos `RESULTADO_INDETERMINADO`, `ERRO_API`, `AGUARDANDO_IMAGEM` e semelhantes não são campos universais da saída pública; não programar o app esperando esses campos.

Fontes: `app/core/confirmacoes.py`, `app/core/propostas.py`, `app/graph/continuacoes.py`, `app/graph/propostas.py`, `app/graph/executor.py`, `app/tools/ajuda_geral/edicoes.py`, `app/tools/solicitacoes/criarSolicitacao.py`, `app/tools/ocorrencias/criarOcorrencia.py`.

## 10. Plataforma mobile e bloqueio de dashboards

### 10.1 Imposição da plataforma

**Obrigatório:** reconstruir a chamada de chat com `plataforma: "mobile"` em primeiro envio, continuidade, aceite, foto, correção e retomada. Nenhum caminho de reenvio pode usar `web` ou omitir esse campo ao chamar a origem.

Na IA, a plataforma pertence à requisição e ao contexto de execução; não é propriedade persistida da sessão. O executor tem default interno `web`, embora o HTTP exija a plataforma. Essa diferença torna necessário defini-la explicitamente em todos os caminhos do adaptador.

Histórico, encerramento e exclusão não aceitam parâmetro de plataforma. O adaptador aplica sua política de saída localmente; não adicionar campo ao body de recuperação de histórico.

Quando o roteamento escolhe visualização em um turno mobile, o nó de visualização bloqueia antes de executar o especialista/consultas. A resposta pode usar HTTP 200 com `intencao: "visualizacao"`, `confirmacao: "nao"`, sem `visualizacao`, e texto atual:

> A criação de gráficos não está disponível no aplicativo para celular. Acesse o site e faça a mesma solicitação.

O app deve exibir a orientação e não criar área de dashboard. Indicadores textuais autorizados continuam disponíveis para gestor; não bloquear toda intenção `analytics`.

### 10.2 Filtragem de saída obrigatória no adaptador

**Lacuna existente:** as duas consultas de histórico incluem gráficos conforme o perfil gestor/administrador, sem conhecer plataforma. Uma conversa produzida na web pode trazer `visualizacao` ao mobile.

A integração deve remover o payload, além de impedir renderização:

| Saída | Caminho a remover |
| --- | --- |
| Chat | `visualizacao` na raiz |
| Listagem | `conversas[*].mensagens[*].visualizacao` |
| Recuperação | `conversa.mensagens[*].visualizacao` |

Aplicar projeção por campos permitidos antes de serializar/cachear a resposta mobile: saída do chat com `thread_id`, `intencao`, `resposta`, `emocao`, `confirmacao`; mensagens históricas com `role`, `mensagem`, `emocao`, `confirmacao`, `imagens`. Preservar a omissão dos campos opcionais. Preservar IDs, título/resumo, datas, quantidade/ordem das mensagens e texto permitido.

Não remover a mensagem inteira que continha gráfico, nem executar nova análise para substituí-lo. Textos históricos podem referir-se a gráficos ausentes; a interface pode mostrar aviso local “Gráficos disponíveis na versão web”, sem alterar a mensagem persistida ou acrescentar um campo não acordado ao contrato.

Caches existentes no destino também devem ser sanitizados/invalidados ao ativar a integração. O app não deve usar um renderizador de dashboards mesmo que receba um campo indevido. Se a origem falhar ao validar uma visualização antiga antes de responder o histórico, o adaptador só recebe o erro: deve tratá-lo como erro de leitura e encaminhar correção à equipe da IA. Não acessar MongoDB para contornar a falha.

Fontes: `app/graph/nos.py::executar_visualizacao`, `app/tools/visualizacoes/contexto.py::MENSAGEM_BLOQUEIO_MOBILE`, `app/api/routes/sessoes.py`, `app/services/sessoes/historico.py::_formatar_mensagens`.

## 11. Imagens e anexos

### 11.1 Fluxo obrigatório

1. A pessoa seleciona/captura foto no aplicativo.
2. O aplicativo usa o serviço de upload definido pelo backend mobile.
3. O upload devolve URL que atenda ao contrato abaixo.
4. O app envia essa URL em `imagens` com texto não vazio e a sessão correta.
5. A IA persiste a referência na mensagem humana e pode associá-la à proposta/registro pertinente.

Não há upload, multipart, base64, diagnóstico visual, áudio ou documento genérico no endpoint do chat. A IA recebe referências para anexo, não o conteúdo visual para analisar defeitos. Não afirmar que uma foto foi vinculada a um cadastro só porque a mensagem foi enviada: o fluxo de negócio ainda pode estar coletando dados.

### 11.2 Validação de URLs

| Item | Regra existente na IA |
| --- | --- |
| Quantidade | Zero a três URLs em cada mensagem; não é limite total da conversa |
| Tamanho | Cada URL, após trim, até 255 caracteres |
| Protocolo | HTTP ou HTTPS; preferir HTTPS no armazenamento mobile |
| Credenciais/fragmento | Não pode conter usuário/senha embutidos nem fragmento |
| Caracteres | Sem barra invertida, caracteres de controle, espaços internos ou caracteres com código 127 |
| Origem IP | IP deve ser global; IP privado, loopback e outras classes não globais são rejeitados |
| Origem DNS | Nome com ponto e rótulos DNS válidos; nomes locais são rejeitados, inclusive sufixos `.local`, `.localhost`, `.internal`, `.test`, `.invalid` |

Essa validação não comprova resolução DNS, existência do arquivo, MIME, tamanho em bytes ou conteúdo seguro. O serviço de upload deve aplicar suas validações próprias. URLs assinadas longas podem exceder 255 caracteres; resolver essa compatibilidade no upload antes de enviar. A política de expiração e privacidade deve considerar que a origem exige referência pública; não substituir por endereço privado ou credenciais embutidas para contornar o contrato.

O schema HTTP exige `mensagem` não vazia mesmo com imagem. A interface deve solicitar texto ou oferecer uma legenda explícita que a pessoa possa revisar, como “Segue a foto para o cadastro em andamento”. Não inventar descrição visual da imagem. Testes/caminhos internos para imagem sem texto não alteram a validação pública.

### 11.3 Requisitos de negócio e falhas

Criar solicitação/ocorrência exige pelo menos uma foto no fluxo atual. A pessoa pode começar a conversa sem foto, mas o cadastro pode ficar aguardando imagem. IDs de categoria/local são resolvidos pelo agente e não precisam virar campos do formulário HTTP do chat.

Se o upload falhar, preservar o texto e não encaminhar caminho local/base64. Se a URL expirar no histórico, mostrar falha de carregamento do anexo sem ocultar as mensagens. Não reenviar fotos antigas automaticamente nem repetir a mensagem de chat porque a miniatura falhou.

Anexar a registro existente exige autorização própria: solicitação própria pendente; ocorrência conforme perfil/propriedade/unidade; observação de conclusão conforme responsabilidade/autorização. Esses vínculos são determinados pela IA/serviço operacional, não pela mera escolha de sessão.

Fontes: `app/schemas/imagem.py::validar_url_imagem`, `app/schemas/chat.py`, `app/memory/persistencia/salvar_mensagem.py`, `rascunho_solicitacao.py`, `app/services/ajuda_geral/edicoes.py`, serviços de criação de solicitação/ocorrência.

## 12. Erros e recuperação

### 12.1 Formatos de erro

Erros tratados por `HTTPException` retornam, em geral, `detail` textual. Exemplo HTTP 401 sem token:

```json
{
  "detail": "Token de acesso não informado."
}
```

Nesse caminho a IA inclui `WWW-Authenticate: Bearer`. Não exigir esse header em toda resposta 401: os caminhos de recusa do perfil não o definem todos explicitamente.

Erros de validação do FastAPI usam `detail` em lista. Exemplo representativo de envio de campo extra à consulta de conversa:

```json
{
  "detail": [
    {
      "type": "extra_forbidden",
      "loc": ["body", "plataforma"],
      "msg": "Extra inputs are not permitted",
      "input": "mobile"
    }
  ]
}
```

`msg`, `input`, `ctx` e campos adicionais variam conforme validador/versão. O app deve reconhecer status/estrutura e apresentar descrição adequada em português, sem depender de texto exato. A IA não usa um envelope genérico `status/mensagem/dados` para todos os erros; exceções não tratadas podem produzir resposta não JSON.

**Proposta do adaptador:** preservar status HTTP e `detail` seguro dos erros conhecidos. Em listas de validação, projetar somente `type`, `loc` e `msg`, removendo `input`, `ctx` e demais dados que possam refletir mensagem/credenciais. Para falhas não JSON ou detalhes internos inadequados, devolver mensagem genérica em `detail`, mantendo o status aplicável. Não devolver stack trace, body HTML de proxy ou configuração interna ao app.

### 12.2 Catálogo por origem

| HTTP/resultado | Origem/condição | Ação do consumidor |
| --- | --- | --- |
| 401 | Token ausente/inválido/expirado ou perfil externo recusa autenticação | Seguir autenticação/renovação existente do mobile; sem reenviar mutação automaticamente |
| 403 | Perfil sem chat, recusa externa ou política local do destino | Informar acesso negado; não trocar token/perfil para contornar |
| 404 | Sessão alheia, inexistente ou inativa | Atualizar lista/seleção; preservar rascunho ainda não confirmado |
| 422 | Corpo/caminho inválido, limites, enum/extra proibido; também `ValueError` no encerramento | Usar contexto da operação; no encerramento investigar geração/validação do resumo |
| 500 | Falta de configuração, histórico incompatível ou falha não traduzida | Exibir falha, registrar metadados seguros e não substituir histórico por vazio |
| 502 | Perfil externo incompatível ou resposta final inválida/vazia; outros caminhos de gateway | Reconciliar histórico/registro, principalmente após mensagem ou aceite |
| 503 | Consulta de perfil/histórico indisponível em caminhos tratados | Permitir nova leitura; não repetir chat automaticamente |
| 200 com texto de recusa/fallback | Pedido de gráfico mobile, autorização de especialista, indisponibilidade recuperada | Mostrar texto; não gerar indicador artificial de cadastro concluído |
| Timeout/desconexão | Resposta pode ter sido perdida depois de persistência ou efeito de negócio | Marcar localmente resultado incerto e reconciliar |

Além de autenticação, a associação principal é: chat → 403/404/422/502 e falhas internas; consultas de histórico → 404 quando específica, 422 quando há body inválido, 500/503; exclusão → 404/422/500/503; encerramento → 404/422 e falhas de resumo/indexação/checkpoint. Erros 5xx de perfil podem ocorrer em todas as rotas autenticadas, pois a dependência consulta o perfil externo.

Não presumir sucesso de persistência ou falha de persistência apenas pelo HTTP. A mensagem de 502 por resposta vazia informa, no caminho atual, que a mensagem foi preservada na conversa. Isso não generaliza para qualquer 502 de perfil/proxy.

### 12.3 Erros produzidos pelo adaptador

**Proposta de implementação, não comportamento já existente na IA:** falha de conexão/protocolo entre adaptador e IA → HTTP 502 com `detail` genérico; prazo de espera do adaptador excedido → HTTP 504 com `detail` indicando que não foi possível confirmar o resultado e orientando atualizar a conversa antes de repetir.

Esses códigos não devem garantir que a origem parou de processar. O adaptador precisa distinguir suas falhas das respostas da origem em métricas/logs internos, sem alterar `intencao` ou inventar uma resposta da Manu.

### 12.4 Política de repetição e resultado incerto

**Obrigatório:** desabilitar retry automático de `POST /chat` no app, no cliente HTTP, no adaptador e em gateways controlados pela equipe. Uma única mensagem pode gerar escrita no sistema operacional. Não acrescentar um header de idempotência esperando suporte da IA: a origem não implementa esse contrato.

| Situação | Procedimento obrigatório |
| --- | --- |
| Sessão conhecida e resposta perdida | Recuperar a conversa, mostrar histórico confirmado e manter rascunho incerto separado. Ausência imediata da resposta não prova que o processamento acabou |
| Primeiro envio sem ID recebido | Recarregar a lista; a sessão pode aparecer. A pessoa pode abri-la para conferir. Não escolher automaticamente uma sessão por texto/data, nem garantir recuperar o ID original |
| Aceite/cadastro com resultado incerto | Consultar registros operacionais pelas funcionalidades existentes do mobile antes de permitir repetição explícita. O histórico isolado não é recibo de cadastro |
| Falha de encerramento | Recuperar metadados para verificar atualização parcial; não repetir em loop. Novo encerramento explícito pode gerar título/resumo novamente |
| Falha de exclusão | Recarregar a lista/consulta. Se não está disponível, atualizar estado local; não recriar uma sessão com o ID antigo |
| Falha de leitura | Nova tentativa de leitura é permitida; manter o último estado visível como desatualizado, sem afirmar “nenhuma conversa” |

O contrato atual não consegue resolver de forma determinística o primeiro envio perdido, deduplicação entre dispositivos ou comprovação estruturada de todo efeito de negócio. Essas limitações devem constar da entrega e dos testes. Suporte a recibos/idempotência exige evolução na IA; não simular uma garantia com heurística no adaptador.

Fontes: `app/api/dependencies/auth.py`, `app/services/gerais/perfil.py`, `app/services/chat.py`, `app/api/routes/chat.py`, `app/services/sessoes/historico.py`, `app/services/solicitacoes/criarSolicitacao.py`, `app/services/ocorrencias/criarOcorrencia.py`.

## 13. Requisitos de implementação e operação

### 13.1 Sequência mínima do adaptador

Para chat: autenticar → validar campos permitidos → definir `plataforma=mobile` → chamar origem com token do usuário → tratar HTTP/protocolo → projetar saída sem `visualizacao` → responder ao app. Fazer uma chamada de chat por envio; não chamar novamente para “melhorar” a resposta.

Para histórico: autenticar → chamar a rota existente, com body exato quando aplicável → verificar sucesso → remover visualizações de todas as mensagens → responder com envelope correto. Na listagem, a filtragem deve percorrer todos os itens, não apenas a conversa selecionada.

Para encerrar/excluir: autenticar → validar/codificar ID → chamar o DELETE correto sem body → retornar resultado/erro → invalidar caches associados. Não chamar os dois DELETEs em conjunto para cumprir uma única ação.

O backend pode usar os padrões de clients/services/controllers já existentes no repositório mobile. A interface de destino proposta deve ser documentada no OpenAPI daquele backend e compartilhar os mesmos exemplos de contrato.

### 13.2 Configuração e disponibilidade

- Configurar host da IA por ambiente, TLS, autorização de rede e encaminhamento do token de usuário. Não embutir host/secrets no app.
- Usar cliente HTTP com pool de conexões e timeouts configuráveis. Medir o tempo total do grafo antes de fixar limites de produção.
- Timeouts internos de 15 segundos para perfil/cadastro e outros prazos de fornecedores não são SLA do chat. App, proxy e adaptador precisam de orçamento compatível; valores finais dependem de homologação/operação.
- `GET /api/v1/health` da IA é uma verificação simples de vida, sem comprovar todos os bancos/modelos disponíveis. A inicialização de MongoDB/Qdrant/índices também pode impedir o serviço de subir.
- Não condicionar a UI a acesso a métricas, bases ou endpoints de fornecedores. Indisponibilidade de um especialista pode aparecer como texto no chat.
- Não enviar turnos offline automaticamente ao reconectar. A primeira versão preserva rascunho local e exige envio explícito após reconciliação.

### 13.3 Concorrência, cache e apresentação

- Na interface, permitir um turno pendente por conversa e impedir duplo toque. Isso reduz duplicação local, mas não cria serialização global entre dispositivos/réplicas.
- Não inventar um TTL fixo ou limite de tamanho de chat como se existisse na IA. Medir volumes de lista/histórico; não truncar mensagens silenciosamente.
- Cache deve ser privado por usuário, sanitizado antes de armazenamento e invalidado em envio/encerramento/exclusão/troca de conta. Não compartilhar resposta autenticada em cache público.
- Conteúdo pode conter Markdown, listas e links, inclusive ofertas de compra. Usar renderizador que não execute HTML/scripts e abertura de links conforme política do aplicativo. Não interpretar texto gerado como comando de navegação.
- Estados de carregamento, falha, anexo e resposta devem ter indicação textual/acessível; não comunicar estado apenas por emoção/cor/animação.
- Guardar o rascunho local quando houver falha, distinguindo-o de mensagem confirmada pela origem. Não limpar a tela inteira por uma falha de leitura.

### 13.3.1 Renderização da Manu a partir de `emocao`

**Obrigatório no mobile:** ao receber uma resposta da IA, ler `emocao` e renderizar a imagem correspondente da Manu, que deve estar disponível entre os assets do projeto mobile. A API fornece o identificador da emoção; não fornece a imagem, seu caminho local ou uma URL para baixá-la.

- O backend mobile deve preservar `emocao` no contrato entregue ao aplicativo. A seleção e a renderização da imagem são responsabilidade da interface mobile.
- Usar um mapeamento explícito entre cada valor do enum e o recurso real identificado na verificação da seção 1.1. Se os arquivos já tiverem o nome das emoções, registrá-los pelo mecanismo de assets do projeto; se tiverem outro nome, mapear os nomes reais sem alterar o enum da API.
- Atualizar a imagem junto da apresentação da resposta correspondente, mantendo sua associação com a conversa que originou a chamada. Uma resposta tardia de outra sessão não pode alterar a Manu da conversa atualmente aberta.
- Na recuperação do histórico, quando a Manu for exibida para uma mensagem do assistente, usar a `emocao` dessa mensagem. Se a tela tiver uma única imagem representando a conversa, usar a emoção da última mensagem do assistente carregada.
- Para emoção ausente, nula ou desconhecida, ou recurso específico indisponível, usar a imagem de `NEUTRO` previamente localizada. Uma conversa sem respostas também pode usar essa imagem. Se o próprio recurso neutro não existir, registrar a pendência e solicitar seu caminho ao usuário; não inventar um asset substituto.
- Manter o texto da resposta disponível mesmo se houver falha ao carregar a imagem. A emoção e a imagem são apresentação e não comprovam sucesso de cadastro, autorização ou estado atual de uma proposta.

Critério visual: uma resposta com `emocao: "SUCESSO"` deve selecionar o recurso mapeado para `SUCESSO`; uma resposta com `emocao: "EMPATICO"` deve selecionar o recurso mapeado para `EMPATICO`. Os nomes/caminhos exatos serão os encontrados no projeto mobile, sem presumir arquivos como `SUCESSO.png` ou qualquer extensão específica.

### 13.4 Observabilidade e privacidade

A IA coleta métricas agregadas e desabilita tracing LangSmith. Isso não é uma garantia de ausência de conteúdo em todo log da aplicação. O adaptador deve registrar operação lógica, status, duração, origem da falha e uso de retry de leitura, sem registrar Bearer, corpo do chat, URLs assinadas ou perfil completo por padrão.

Não há request ID público no contrato atual. Se o backend mobile já usa correlação, aplicá-la ao seu fluxo sem supor que a IA reconhece ou devolve o mesmo ID. Chaves de modelos, JEV, Databricks, MongoDB, Qdrant e PostgreSQL permanecem com a equipe da IA.

### 13.5 Retenção e exclusão

Excluir conversa é ocultação lógica do histórico, sem prazo de retenção definido nesse código. Não prometer eliminação definitiva ou remoção de registros de manutenção. A busca semântica de resumos consulta o estado ativo no MongoDB antes de utilizar resultados do Qdrant; isso impede uso de sessão excluída nesse caminho, mas não elimina os vetores fisicamente.

Políticas de retenção física, remoção de fotos e tratamento de exclusão concorrente dependem do produto/operação. Não executar limpeza diretamente nos bancos como parte desta integração.

Fontes: `app/main.py`, `app/observability/`, `app/guardrails/validacao.py`, `app/memory/vectorSearch/recuperar_historico.py`, `app/memory/persistencia/utilitarios.py`, `Dockerfile`.

## 14. Informações de apoio e limites de escopo

| Item | Uso nesta integração |
| --- | --- |
| `GET /api/v1/health` | Apoio operacional; retorna indicador simples de vida, não prontidão completa |
| `GET /api/v1/metrics` | Prometheus; não integrar ao app nem repassar a usuários |
| `POST /api/v1/classificar/classificar-problema` | Endpoint separado, sem Bearer declarado na rota atual; não é pré-requisito do chat |
| FAQ/RAG e busca em outras conversas | Executadas por ferramentas internas; não substituem as rotas de histórico da interface |
| Indicadores e catálogo web de gráficos | Conhecer a separação entre análise textual e visualização; não implementar contrato de gráfico no mobile |
| Pesquisa de compras | Saída textual com ofertas/links; não há schema público de cards nem garantia de disponibilidade/preço de todo o mercado |
| Listagens operacionais | Podem limitar resultados e orientar usar telas do app; esses limites não são paginação das conversas |
| SQL/enums | Referências de domínio, sem necessidade de replicar banco no adaptador |
| Diretórios `a2a` e `rag` | Inicializadores no checkout; não comprovam protocolos públicos adicionais |
| `docs/api/swagger.json` | OpenAPI do backend FIXA, não da IA. Usar rotas/schemas da IA para este contrato |

Não há contrato público para editar/apagar mensagem individual, renomear/exportar conversa, feedback por ID de mensagem, áudio/transcrição, arquivos genéricos, push de novos turnos ou polling de processamento. Não expor esses recursos no adaptador como se a origem os implementasse.

### 14.1 Chamadas da IA de volta ao backend FIXA

Estas chamadas já são feitas pelos serviços da IA. O agente do mobile deve conferir compatibilidade dos endpoints de domínio existentes no destino; não são novas rotas do adaptador de chat. Todas usam o Bearer da pessoa e a base `FIXA_API_BASE_URL` configurada na IA.

| Operação chamada pela IA | Método/caminho FIXA | Contrato necessário |
| --- | --- | --- |
| Perfil | `GET /api/v1/perfil` | HTTP 200 e objeto descrito na seção 5; sem envelope adicional |
| Criar solicitação | `POST /api/v1/solicitacoes` | JSON com `titulo`, `descricaoProblema`, `localUnidadeID`, `descricaoLocal`, `categoriaEquipamentoId`, `urlsFoto` |
| Criar ocorrência | `POST /api/v1/ocorrencias` | JSON com `localUnidadeId`, `categoriaEquipamentoId`, `categoriaProblemaId`, `titulo`, `descricaoOcorrencia`, `descricaoLocal`, `prioridade`, `urlsFoto` |

Preservar a capitalização existente: a solicitação usa **`localUnidadeID`**, enquanto a ocorrência usa **`localUnidadeId`**. IDs de local/categoria de equipamento são inteiros positivos, `urlsFoto` é array não vazio de URLs, título tem até 100 caracteres e descrições têm até 255 caracteres nos modelos de ferramenta. Na ocorrência, `categoriaProblemaId` é string de categoria, apesar do sufixo `Id`; `prioridade` é `ALTA|MEDIA|BAIXA`. As categorias atuais são `ELETRICA`, `HIDRAULICA`, `MECANICA`, `INFORMATICA`, `MOBILIARIO`, `CLIMATIZACAO`, `REDE_CONECTIVIDADE`, `SEGURANCA`, `ILUMINACAO`, `LIMPEZA_CONSERVACAO`.

A identidade é obtida pelo Bearer, não por um `usuario_id` incluído nesses bodies. O endpoint de domínio deve verificar unidade, propriedade, autorização e existência dos IDs; não confiar na seleção do agente como autorização suficiente.

Na interpretação atual dos serviços da IA, resposta 2xx diferente de 202/204 precisa conter objeto JSON com `titulo` não vazio; `id` é aproveitado quando presente. HTTP 204 é considerado sucesso sem objeto de retorno. HTTP 202, 5xx, erro de transporte ou resposta 2xx incompatível são tratados como resultado indeterminado; 4xx vira recusa da API. Esses resultados internos alimentam a resposta conversacional, sem se tornarem automaticamente o status HTTP do chat. Conferir essa compatibilidade em homologação antes de afirmar sucesso de cadastro pelo chatbot.

Fontes: `app/services/solicitacoes/criarSolicitacao.py::adicionar_solicitacao`, `app/services/ocorrencias/criarOcorrencia.py::adicionar_ocorrencia`, `app/tools/toolsRequest/CriarSolicitacaoEntrada.py`, `app/tools/toolsRequest/CriarOcorrenciaEntrada.py`. Edições/anexos existentes e feedback também podem usar PostgreSQL diretamente nos serviços da IA; o adaptador não deve duplicar essas escritas ao receber a resposta.

## 15. Decisões da primeira versão e pendências externas

### 15.1 Soluções especificadas para implementação

| Tema | Solução desta especificação | Natureza |
| --- | --- | --- |
| Plataforma | Valor `mobile` definido no adaptador em todo turno | Obrigatório mobile |
| Gráficos no histórico | Remover payload antes de enviar/cachear e impedir renderização | Obrigatório mobile |
| Imagem da Manu | Mapear `emocao` para imagem local verificada e usar `NEUTRO` como fallback | Obrigatório mobile |
| Título vazio | Rótulo de apresentação “Nova conversa”, sem alterar campo persistido | Proposta de UX da primeira versão |
| Encerramento | Ação explícita “Finalizar conversa”; não atrelada a suspensão da tela | Proposta de UX da primeira versão |
| Confirmações | Texto; sem botões históricos ativos | Regra de integração da primeira versão |
| Histórico web/mobile | Compartilhar o histórico que a origem retorna para o usuário, com sanitização mobile | Compatibilidade com origem atual |
| Lista | Preservar lista completa e envelope; sem simular paginação na IA | Compatibilidade com origem atual |
| Falha de chat | Sem retry automático; reconciliação e decisão explícita | Obrigatório mobile |
| Namespace de destino | Proposta `/api/v1/ia`, ajustável à convenção já existente e documentada | Proposta de interface, não rota da IA |

### 15.2 Dados a confirmar sem inventar valores

| Pendência | Responsável sugerido | Impacto e encaminhamento |
| --- | --- | --- |
| URLs de IA e rede/TLS por ambiente | Operação/IA | Necessário para homologar chamadas reais; manter configuração externa |
| Token mobile compatível com `GET /perfil` e renovação existente | Backend mobile/autenticação | Necessário para acesso real; não implementar conversão improvisada de identidade |
| Serviço de upload, privacidade e validade de URLs ≤255 caracteres | Backend mobile/armazenamento | Necessário para cadastros com foto; não criar rota de upload da IA |
| Caminhos das imagens da Manu e correspondência com todas as emoções | Agente do mobile/equipe do aplicativo | Localizar e informar os caminhos antes de implementar; conferir nomes e pedir ao usuário os caminhos/correspondências ausentes, conforme seção 1.1 |
| Orçamento de timeout, volume e disponibilidade | Operação/mobile/IA | Medir em homologação e configurar cliente/proxy/app sem prometer SLA inexistente |
| Inclusão de administrador | Produto | Manter escopo de três perfis até decisão; não alterar permissões do perfil na IA |
| Histórico após mudança de unidade/perfil | Produto/IA | Origem filtra por usuário; se exigir novo isolamento, precisa evolução específica |
| Retenção física e remoção de anexos | Produto/operação | Exclusão atual é lógica; comunicar esse efeito sem promessa de purga |
| Necessidade de títulos gerados antes do encerramento | Produto/IA | Primeira versão usa rótulo substituto; geração antecipada exige evolução da IA |
| Exigência de recuperação determinística do primeiro envio e recibos de cadastro | Produto/IA | Não suportada integralmente pelo contrato atual; projetar evolução de idempotência/recibos se for requisito de lançamento |

Essas pendências não impedem implementar/validar o adaptador com mocks de contrato. Os itens de ambiente/autenticação/upload e os requisitos adicionais considerados obrigatórios pelo produto precisam estar resolvidos para homologar/liberar os fluxos correspondentes.

## 16. Critérios de aceite e verificação do agente implementador

Os cenários abaixo são requisitos de teste da implementação futura. Não representam testes funcionais executados durante a redação deste documento. Usar mocks da IA para testar contrato/adaptador e ambiente de homologação autorizado para integração real, sem depender de frase exata de LLM.

| ID | Pré-condição e ação | Resultado verificável |
| --- | --- | --- |
| CA-01 / RF-07 | Chamada sem autenticação ou com token recusado | Erro de autenticação, sem token alternativo e sem consulta de conversa de outra pessoa |
| CA-02 / RF-07 | IA recebe perfil externo sem campos obrigatórios | Falha tratada como erro de dependência/perfil; app não escolhe perfil manualmente |
| CA-03 / RF-01 | IA retorna `SEM_DADOS` e array vazio | App mostra estado vazio; falha 503 não produz esse mesmo estado |
| CA-04 / RF-01 | Listagem com duas sessões e títulos vazio/preenchido | IDs preservados, ordem da origem e rótulo substituto apenas na apresentação |
| CA-05 / RF-02 | Recuperar por `session_id` conhecido | POST correto, body somente com ID, mensagens na ordem, próximo chat usa mesmo valor como `thread_id` |
| CA-06 / RF-03 | Primeiro envio mínimo | Uma chamada de chat, sem ID inventado, com `mobile`; ID retornado armazenado |
| CA-07 / RF-04 | Envio seguinte, correção, aceite e foto | Todas as chamadas continuam a sessão correta e contêm literal `mobile` |
| CA-08 / RF-06 | Cliente tenta `plataforma=web` ou campo de identidade | Adaptador recusa entrada não permitida; nunca encaminha turno web ou identidade forjada |
| CA-09 / RF-06 | Cliente omite plataforma no adaptador proposto | Adaptador define `mobile`; omissão no HTTP direto da IA permanece erro de validação |
| CA-10 / RF-06 | Pedido de gráfico roteado como visualização | Texto de bloqueio com HTTP 200 pode ser exibido, sem dashboard; não transformar em erro de transporte |
| CA-11 / RF-06 | Fixtures de listagem/recuperação com gráficos web | Remoção de todos os campos de visualização, texto/IDs/ordem preservados e cache sanitizado |
| CA-12 / RF-06 | Gestor solicita indicador textual | Encaminhamento mobile normal; adaptador não bloqueia `analytics` por confundir com gráfico |
| CA-13 / RF-07 | Sessão alheia/inexistente/excluída | 404 da operação preservado; nenhum dado de dono exposto |
| CA-14 / RF-08 | Texto vazio/branco, texto >4.000, ID vazio/>100, extras proibidos | 422 conforme origem/adaptador; não cria continuidade em sessão fabricada |
| CA-15 / RF-08 | Uma/três fotos válidas; quatro fotos; URL privada/longa/base64 | Aceita limites válidos; rejeita inválidos; não supõe análise visual |
| CA-16 / RF-08 | Foto sem texto | Não encaminha body inválido; app solicita legenda explícita/revisável |
| CA-17 / RF-08 | Proposta com `confirmacao=sim`, seguida de aceite textual | Novo turno, sem campo booleano; nenhuma mensagem fixa de cadastro concluído antes da resposta |
| CA-18 / RF-08 | Pergunta/correção parcial, proposta pausada ou ambígua | Texto preservado; nenhuma confirmação automática; aceita intenção nula |
| CA-19 / RF-02/08 | Reabrir histórico com antiga flag `sim` | Não oferece botão histórico ativo nem presume proposta atual |
| CA-20 / RF-09 | Finalizar conversa e recuperar depois | DELETE de encerramento correto, título/resumo atualizados, sessão permanece listável/continuável |
| CA-21 / RF-09 | Encerramento falha antes/depois de atualizar metadados | Erro contextualizado, leitura de reconciliação e ausência de loop de resumo |
| CA-22 / RF-05 | Excluir conversa própria; depois listar, recuperar e conversar | Item desaparece; recuperação/continuação recebem 404; nenhum encerramento automático adicional |
| CA-23 / RF-05 | Segunda exclusão ou resposta tardia após excluir | Tratar 404/estado ausente e não ressuscitar item local |
| CA-24 / RF-10 | Timeout em sessão conhecida | Nenhum retry de chat, estado incerto local e leitura de histórico antes de repetição explícita |
| CA-25 / RF-10 | Primeiro envio processado sem resposta/ID recebido | Recarrega lista, não escolhe sessão por heurística nem cria duplicata automaticamente |
| CA-26 / RF-10 | Cadastro confirmado operacionalmente, mas saída do chat falha | Orienta conferir registros operacionais; não repete escrita por status 5xx/emoção |
| CA-27 / RF-04/10 | Duplo toque, troca de tela e retorno do app | Um envio local por vez; resposta vinculada à origem; reconexão sem replay automático |
| CA-28 / RF-02 | Histórico antigo sem metadados opcionais e com datas sem offset | Leitura tolerante, neutro quando apropriado na apresentação, UTC só para timestamps de sessão |
| CA-29 / RF-07 | Logout/troca de conta | Seleção/cache anteriores não aparecem para outra conta |
| CA-30 / RF-10 | Erro não JSON da origem ou detalhes com `input` sensível | Envelope seguro do adaptador, sem HTML/stack trace/reflexo de dados pessoais |
| CA-31 / RF-11 | Levantamento inicial dos assets da Manu | Agente informa diretório e mapeamento emoção/arquivo/importação antes de implementar; solicita ao usuário localização ou correspondências ausentes |
| CA-32 / RF-11 | Respostas com cada emoção conhecida e histórico de assistente | Backend preserva o valor e app seleciona a imagem local correspondente, sem URL de imagem fornecida pela IA |
| CA-33 / RF-11 | Emoção ausente/nula/desconhecida ou imagem específica indisponível | App utiliza a imagem neutra verificada e preserva o texto; ausência do asset neutro é registrada e seu caminho solicitado |

Evidências de origem úteis para construir fixtures: `app/tests/test_auth.py`, `test_chat.py`, `test_sessao.py`, `test_encerramento_resumo.py`, `test_memoria_negocio.py`, `test_graph_runner.py`, `test_visualizacoes_integracao.py`, `test_visualizacoes_persistencia.py`, `test_propostas_cadastro.py`, `test_confirmacoes_continuacoes.py`, `test_cadastro_solicitacao_contrato.py`, `test_permissoes_consultas.py` e `test_observabilidade_politica.py`. Mocks e testes internos não comprovam conectividade real nem substituem o schema HTTP.

## 17. Entrega esperada do agente do backend mobile

- Antes da implementação, localizar as imagens da Manu no projeto mobile, verificar sua correspondência com os nomes das emoções e informar diretório, caminhos e identificadores/importações. Solicitar ao usuário os caminhos ou mapeamentos que não forem encontrados.
- Implementar o adaptador nas convenções do projeto de destino, mantendo o mapeamento documentado e sem alterar os contratos existentes da IA por suposição.
- Publicar OpenAPI do backend mobile com autenticação, bodies, envelopes, campos opcionais, erros próprios e regra de ausência de `visualizacao`.
- Entregar testes de contrato para imposição de plataforma, filtragem de gráficos, isolamento autenticado, envelopes e falhas sem retry de mutações.
- Informar à equipe do aplicativo os fluxos de nova conversa, recuperação, encerramento, exclusão, anexos, confirmações e resultado incerto.
- Entregar à equipe do aplicativo o mapeamento `emocao` → asset da Manu e a regra de renderização/fallback da seção 13.3.1, preservando o campo no backend.
- Documentar configurações por ambiente, pendências ainda abertas e resultado da homologação, distinguindo teste com mock de chamada real.
- Registrar limitações que exigem evolução na IA, especialmente recuperação determinística de primeiro envio, idempotência/recibos, paginação real e título antecipado, se exigidas pelo produto.

## 18. Fontes e validação documental

O contrato foi redigido a partir das rotas/schemas e dos serviços do commit indicado no início, usando o molde como roteiro. Os caminhos de fonte ao longo do texto referem-se ao repositório da IA, não ao futuro backend mobile.

Fontes centrais: `app/api/routes/chat.py`, `app/api/routes/sessoes.py`, `app/api/dependencies/auth.py`, `app/schemas/chat.py`, `app/schemas/sessao.py`, `app/schemas/imagem.py`, `app/services/chat.py`, `app/services/sessao.py`, `app/services/sessoes/historico.py`, `app/graph/executor.py` e `app/memory/persistencia/sessoes/`.

A revisão documental conferiu regras e caminhos contra o código, a separação entre origem existente e propostas e as referências de arquivos. Os 18 blocos JSON foram verificados sintaticamente; os 13 exemplos que correspondem aos schemas públicos de chat/sessões também foram validados diretamente pelos modelos Pydantic existentes, sem executar o grafo ou iniciar o serviço.

Não foram feitas chamadas de rede nem testes funcionais da IA/mobile. Nenhum arquivo de código ou o molde original foi alterado nesta entrega. A validação não constitui homologação da IA ou do aplicativo; a implementação permanece como trabalho do agente destinatário.
