# Orientação para agentes de IA

Este documento define como entender e ampliar o aplicativo Android FIXA. Antes de alterar código, leia este guia e inspecione os arquivos envolvidos: a implementação existente é a referência para o comportamento atual, enquanto este documento define as fronteiras arquiteturais esperadas.

## Objetivo e limites

- Preserve as funcionalidades existentes e a composição visual das telas, exceto quando a solicitação pedir explicitamente uma mudança funcional ou visual.
- Não introduza APIs Android ou dependências que exijam nível de SDK superior ao limite definido pelo produto (SDK 31). Confira a compatibilidade antes de adicionar chamadas ou bibliotecas.
- Prefira mudanças pequenas, coesas e restritas ao fluxo solicitado. Não faça uma migração ampla incidental.
- Não registre tokens, senhas, códigos de recuperação, credenciais ou dados pessoais em logs.
- Não coloque segredos em código, documentação, recursos versionados ou `BuildConfig`. `local.properties` e `google-services.json` são configuração local/integração e não devem ter seus valores copiados para respostas ou exemplos.
- Não afirme que o build ou testes passaram sem executá-los e observar o resultado.

## Mapa do projeto

O código Android fica em `app/src/main/java/com/aula/fixa`. A arquitetura combina Clean Architecture nas camadas internas e MVVM na fronteira com a interface:

```text
ui/view  -> adapter (ViewModels) -> application -> domain
                                      ^               ^
                                      |               |
infrastructure ----------------------+---------------+
```

A dependência de código aponta para dentro: `domain` não conhece Android, Retrofit, Firebase, Cloudinary ou outras implementações externas. `application` depende de `domain`. `infrastructure` implementa contratos do `domain` e adapta serviços externos. `adapter` conecta a UI aos casos de uso. `ui/view` não chama infraestrutura nem APIs diretamente.

### `domain/`

É o núcleo independente de framework e fornecedor.

- `model/`: modelos de negócio e de apresentação compartilhados entre camadas (por exemplo, `Problema`, `Perfil`, `Evento`, `NovaSolicitacao`, `FotoProblema`). Use tipos explícitos; não transporte `JSONObject`, `JsonObject`, `Map<String, Object>` ou DTOs da API como modelo de domínio.
- `repository/`: contratos que descrevem operações necessárias ao negócio, com parâmetros e retornos de `domain/model` e tipos de `domain/enums`. Não coloque implementação HTTP/SQL nesta pasta.
- `enums/`: estados e classificações usados pelo domínio.
- Regras puras de validação e transformação podem estar nos modelos/objetos de domínio quando forem invariantes do negócio. Não use Android nesse código.

### `application/`

Contém casos de uso, organizados por responsabilidade: `auth/`, `cadastro/`, `evento/`, `foto/`, `perfil/` e `solicitacao/`.

- Cada classe representa uma ação de negócio clara (por exemplo, `ListarSolicitacoes`, `CriarSolicitacao`, `AtualizarPerfil`).
- Recebe contratos do domain por construtor e coordena a operação.
- Trabalha com modelos de domínio; não conhece Retrofit, DTOs, `Context`, Activity, Fragment, Firebase ou Cloudinary.
- Decisões e validações de negócio pertencem aqui ou ao domain, nunca em `infrastructure/.../repository` nem em uma Activity.

### `infrastructure/`

Implementa detalhes externos e converte seus formatos para contratos internos.

- `remote/api/`: interfaces Retrofit por recurso (`SolicitacaoApi`, `PerfilApi`, `EventoApi`, `AuthApi` etc.) e configuração do cliente em `ApiClient`.
- `remote/dto/`: contratos de entrada/saída da API. Crie DTOs tipados para cada payload; não deixe telas ou casos de uso dependerem diretamente deles.
- `remote/repository/`: implementações dos contratos de `domain/repository`; fazem chamadas remotas e mapeiam DTOs para modelos do domínio.
- `auth/`: integração e implementação da autenticação. `auth/session/SessionStorage` mantém a sessão protegida com Android Keystore e AES-GCM; não mova tokens para preferências sem criptografia nem duplique armazenamento de sessão.
- `cloudinary/`: integração de upload e implementação do repositório de fotos.
- `remote/ApiException`: erros de comunicação/adaptação. Converta erros para uma mensagem apropriada na fronteira de apresentação, sem propagar dependências de rede para as views.
- Se surgir uma persistência local, crie uma implementação em infrastructure e faça-a implementar um contrato de domain. Atualmente, a sessão é local; não assuma que exista banco SQLite/Room para os demais dados.

### `adapter/` (ViewModels)

É a camada MVVM que serve às telas. Os subpacotes atuais são `auth/`, `cadastro/`, `evento/`, `foto/`, `newrequest/`, `perfil/` e `solicitacao/`.

- Um ViewModel coordena casos de uso e expõe estado/resultado adequado à tela. Ele não deve conhecer views concretas nem montar componentes visuais.
- Use factories existentes (`*ViewModelFactory`) e o padrão de callbacks adotado no fluxo próximo, sempre entregando callbacks à UI na thread principal e executando I/O fora dela.
- ViewModels não devem construir chamadas Retrofit, acessar `ApiClient`, DTOs, SharedPreferences ou Cloudinary diretamente.
- Reaproveite um ViewModel quando ele tiver a mesma responsabilidade; crie um novo quando o fluxo representar uma responsabilidade independente.

### `ui/`

- `view/`: Activities e Fragments, agrupados por fluxo (`auth/`, `home/`, `requests/`, `profile/`, `events/`, `newrequest/` etc.). Fazem binding de views, navegação, permissões/hardware e apresentação de estados. Chamam ViewModels; não repositórios ou APIs.
- `adapter/`: adapters visuais de listas/RecyclerViews, atualmente em `solicitacao/` (por exemplo, `RequestAdapter` e `TimelineAdapter`). Recebem modelos apropriados e encaminham eventos por callbacks; não fazem chamadas de rede nem regras de negócio.
- `config/`: componentes e utilitários de UI reutilizáveis, como diálogos, loading, navegação auxiliar, notificadores e comportamento compartilhado. Não use esta pasta como depósito de regras de negócio ou acesso a dados.

### Recursos Android

- `app/src/main/res/layout/`: layouts. Preserve dimensões, hierarquia e composição visual em refatorações arquiteturais.
- `app/src/main/res/values/strings.xml` e demais arquivos `strings*.xml`: textos mostrados ao usuário. Não deixe textos fixos novos em Java/Kotlin; adicione recursos com nomes claros.
- `app/src/main/res/navigation/nav_graph_app.xml`: destinos, ações e argumentos do Navigation Component. Ao mover/renomear uma view, atualize aqui e no Manifest quando aplicável.
- `app/src/main/AndroidManifest.xml`: Activities exportadas, launcher, permissões e providers. Declare apenas o necessário e mantenha `exported` correto.
- Drawables, dimensões, cores e temas ficam em seus diretórios de `res`; reutilize os recursos existentes.

## Caminho esperado de uma funcionalidade

Para adicionar, por exemplo, uma consulta de recurso:

1. Defina ou ajuste um modelo em `domain/model` e, se necessário, enum em `domain/enums`.
2. Declare uma operação em uma interface coesa em `domain/repository`.
3. Implemente um caso de uso em `application/<responsabilidade>` que dependa apenas dessa interface e de modelos do domínio.
4. Se a operação for remota, crie/ajuste DTOs em `infrastructure/remote/dto`, endpoint em `remote/api` e implementação/mapeamento em `remote/repository`. Para armazenamento ou serviço externo, mantenha detalhes em seu pacote de infraestrutura.
5. Conecte os casos de uso a um ViewModel em `adapter/<responsabilidade>` e sua factory.
6. Atualize `ui/view` para observar/acionar o ViewModel e renderizar estados de sucesso, vazio, carregamento e erro. Use `ui/adapter` para listas e `ui/config` apenas para componentes reutilizáveis.
7. Atualize strings, navegação e Manifest somente se necessário. Preserve os layouts quando o pedido for apenas arquitetural.
8. Verifique as dependências entre camadas, procure referências antigas e execute a verificação/build adequado se o ambiente permitir.

Não pule diretamente da view para `infrastructure`. A interface do repositório em `domain` é a fronteira neutra entre o caso de uso e a implementação externa.

## Configuração e dados do aplicativo

| Configuração | Local atual | Orientação |
|---|---|---|
| Módulo, namespace e identificador | `settings.gradle.kts`, `app/build.gradle.kts` | Projeto Gradle de módulo único `:app`; namespace/applicationId atuais `com.aula.fixa`. Não mude o identificador sem solicitação explícita. |
| SDK e linguagem | `app/build.gradle.kts` | Configuração atual: `minSdk 29`, `compileSdk 36`, `targetSdk 36`, Java 11. O requisito do produto é não adotar APIs/libs que ultrapassem SDK 31; confira a compatibilidade de cada novidade. Se o limite se referir também ao `compileSdk`/`targetSdk`, isso precisa ser alinhado como alteração de configuração separada. |
| Dependências | `gradle/libs.versions.toml` e `app/build.gradle.kts` | Prefira o version catalog (`libs.*`) e evite declarar versões dispersas. Verifique compatibilidade de SDK e versões transitivas antes de introduzir biblioteca. |
| Repositórios Gradle | `settings.gradle.kts` | Google, Maven Central e JitPack estão configurados. Não adicione repositório sem necessidade. |
| URL da API | `infrastructure/remote/api/ApiClient.java` | A URL base de produção está atualmente escrita no `ApiClient`. Use configuração centralizada por ambiente para novos ambientes; não replique a URL em cada endpoint. |
| Configuração Cloudinary | `local.properties` | O Gradle lê `cloudinary.cloud_name`, `cloudinary.api_key`, `cloudinary.upload_preset` (com aliases legados) e `cloudinary.solicitacoes_folder` e os injeta como campos `BuildConfig`. Configure localmente; nunca inclua valores reais em documentação, commits, logs ou exemplos. Credenciais embutidas em um app distribuído podem ser extraídas; use apenas credenciais/presets apropriados para upload público e restrito. |
| Firebase/Google | `app/google-services.json` e plugin Google Services | Arquivo de configuração local do Firebase associado ao `applicationId`. Não copie seu conteúdo para documentos ou mensagens. Para OAuth, mantenha os client IDs e a configuração do console alinhados com a aplicação e o ambiente. |
| Sessão | `infrastructure/auth/session/SessionStorage.java` | Token de acesso cifrado com chave Android Keystore; operações autenticadas adicionam `Authorization: Bearer` no cliente configurado em `ApiClient`. Não registre nem exponha esse token. |
| Textos | `app/src/main/res/values/strings*.xml` | Texto de interface deve ser recurso, para consistência e localização.
| Permissões e destinos | Manifest e `res/navigation/nav_graph_app.xml` | Atualize ao introduzir Activity, permissões ou destino de navegação; não exponha componentes sem necessidade. |

O arquivo `local.properties` contém caminhos/valores locais e é ignorado pelo Git. Não peça ao usuário que compartilhe os valores em texto quando basta confirmar a presença das chaves. Nunca reproduza valores de configuração encontrados durante a inspeção.

## Convenções de implementação

- Nomes de classes de domínio/processo em português, seguindo o padrão já adotado (`Problema`, `SolicitacaoRepository`, `CriarSolicitacao`). Preserve nomes obrigatórios de bibliotecas, protocolos e classes geradas.
- Uma API/Repository deve tratar de um recurso ou responsabilidade. Evite interfaces genéricas que misturam autenticação, perfil, eventos e solicitações.
- DTO de rede é contrato externo; modelo de domínio é contrato interno. Faça o mapeamento explicitamente na infraestrutura.
- Mantenha tratamento de erro e estado de carregamento coerentes com os ViewModels existentes.
- Use `strings.xml` para textos e `BuildConfig`/configuração de ambiente para valores configuráveis, sem colocar segredo real no cliente.
- Não introduza APIs Java/Android indisponíveis na versão mínima suportada. Confirme a API level, especialmente para criptografia, permissões, seleção de arquivos e notificações.
- Ao alterar pacote/classe de Activity ou Fragment, confira Manifest, Navigation graph, referências XML e imports.

## Verificação antes de concluir

1. Leia o diff e confirme que o comportamento e as telas não mudaram sem autorização.
2. Procure acesso direto a `infrastructure` dentro de `ui/view` e `ui/adapter`.
3. Confirme que `domain` e `application` não importam `android.*`, Retrofit, Gson, Firebase ou classes de `infrastructure`.
4. Confirme que nenhuma credencial/token foi adicionado ao diff ou aos logs.
5. Verifique recursos, Manifest e grafo de navegação quando aplicável.
6. Execute `git diff --check`; compile/build quando solicitado ou necessário e viável. Informe com precisão qualquer verificação que não pôde ser concluída.

## Escopo funcional conhecido

A base atual já contém fluxos de autenticação/sessão, perfil, cadastro de opções, fotos, eventos, criação/listagem/edição/detalhe de solicitações e navegação principal. O fluxo de IA/chatbot ainda não tem integração de negócio; não presuma que está conectado à API. Novos fluxos de gestor e técnico devem seguir as mesmas fronteiras, com seus próprios modelos/casos de uso/ViewModels quando forem implementados.
