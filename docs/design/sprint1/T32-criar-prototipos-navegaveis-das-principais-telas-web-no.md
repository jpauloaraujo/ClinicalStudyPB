# T32 · Criar protótipos navegáveis das principais telas web no Figma

> **Sprint 1** · PBI-08 · Protótipos navegáveis das interfaces  
> **Responsável:** I6 · **Estimativa:** 5h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T23  
> **Situação:** 📋 Entrega fora do código (Figma ou processo): este documento especifica tudo o que precisa ser produzido, e a entrega continua sendo obrigatória

## 1. Objetivo

Produzir no Figma o protótipo navegável da aplicação web, em modo apresentação e sem backend, cobrindo os fluxos do administrador, do pesquisador e do coordenador. A entrega é o arquivo Figma com link compartilhado; o documento abaixo é a especificação completa do que desenhar. As telas já existem no código e servem de referência fiel (cores, campos, textos e regras).

## 2. Contexto: o que já existe e deve ser reaproveitado

- Matriz de permissões: `docs/sprint1/arquitetura.md`, seção 4. Modelo de protótipos mobile (T33): `docs/design/sprint1/T33-criar-prototipos-navegaveis-das-telas-do-participante-no.md`.
- O protótipo deve refletir o que a web faz hoje: se desenhar algo diferente, registrar o ajuste na revisão da T35.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `Figma → arquivo "ClinicalStudyPB — Web"` | Criar | Páginas: 00 Capa e links, 01 Tokens e componentes, 02 Telas, 03 Fluxos (protótipo), 04 Estados |
| `README.md`, seção 10 (Documentos de referência) | Alterar | Registrar o link do protótipo |
| `Azure Boards → T32` | Atualizar | Colar o link e anexar um print da página de fluxos |

## 4. O que fazer em cada arquivo

### 1. Tokens (criar como estilos/variáveis do Figma)

- Cores: primária `#1d5fa8` (escura `#164a84`, fundo `#e6eef8`); fundo `#f4f6f9`; superfície `#ffffff`; texto `#1f2a37`, texto suave `#5b6675`; borda `#dde2e9`; perigo `#b42318` (fundo `#fef3f2`); sucesso `#067647` (fundo `#ecfdf3`).
- Espaçamentos 4, 8, 12, 16, 24, 32 px; raio 8 px; sombra `0 1px 2px / 0 1px 3px`; fonte do sistema (Segoe UI/Roboto), texto base 16 px.
- Header 56 px de altura; menu lateral com 220 px de largura; telas em 1440 px (desktop) e 375 px (celular).

### 2. Componentes reutilizáveis

- Botão (primário, secundário, perigo, pequeno, desabilitado); campo de texto com rótulo e mensagem de erro; seletor; caixa de marcação; área de texto.
- Tabela com cabeçalho e ações; etiqueta de status (Planejamento, Recrutamento, Execução, Encerrado); alerta (erro, informação, sucesso); diálogo de confirmação; abas; paginação.
- Layout base: header (nome do sistema, "Nome · Perfil", Sair), menu lateral com item ativo, área de conteúdo e rodapé "Sistema acadêmico — dados fictícios".

### 3. Telas a desenhar (uma moldura por tela)

- W01 Login: e-mail, senha, botão Entrar, mensagem "Credenciais inválidas" (variante de erro). Perfil Participante: aviso de que usa o aplicativo.
- W02 Início: "Olá, <nome>", texto de boas-vindas e cartões de atalho conforme o perfil.
- W03 Usuários (administrador): tabela Nome, E-mail, Perfil, Situação, ações Editar/Excluir; botão "Novo usuário"; estado vazio.
- W04 Formulário de usuário: Nome, E-mail, Senha (na edição, "Nova senha (opcional)"), Perfil, Ativo; erros por campo; botões Salvar e Cancelar.
- W05 Estudos: tabela Título, Responsável, Período, Vagas, Status e ações (só o pesquisador vê Novo, Editar, Excluir); coordenador vê o aviso "Seu perfil tem acesso apenas para consulta".
- W06 Formulário de estudo: Título, Objetivo, Responsável (o próprio pesquisador), Data de início, Data de término, Limite de participantes, Status (desabilitado: o estado muda no detalhe).
- W07 Detalhe do estudo: título, botão Voltar, indicador dos quatro estados com o atual destacado, botão "Avançar para <próximo estado>" (só pesquisador) e diálogo "não é possível voltar"; alerta de erro de regra (ex.: falta critério de inclusão).
- W08 Aba Resumo: objetivo, responsável, datas, limite e estado.
- W09 Aba Critérios: duas listas (Inclusão com peso e "Determinante"; Exclusão), formulário (Tipo, Título, Descrição, Peso, Determinante), edição, exclusão com confirmação, aviso "Adicione ao menos um critério de inclusão para avançar" e a variante somente leitura.
- W10 Aba Visitas: visitas ordenadas pelo dia como cartões expansíveis com as atividades; formulários de visita (Nome, Dia previsto, Tolerância 0 a 30) e de atividade (Nome, Descrição, Categoria); variante somente leitura.
- W11 Aba Candidatos (coordenador): tabela Código, Nome fictício, Situação, Elegibilidade, Consentimento; filtro por situação; paginação; formulário de cadastro (Nome fictício, Data de nascimento, Contato); botão desabilitado com explicação se o estudo não está em recrutamento.
- W12 Ficha do candidato: dados, e três seções: Avaliação de elegibilidade (critérios em dois grupos com "Se aplica / Não se aplica" e observação, Salvar desabilitado até responder tudo, resultado e histórico), Consentimento (termo rolável, aviso de consentimento simulado, confirmação, data e hora, Registrar, comprovante) e Inclusão (4 pré-condições, botão "Incluir no estudo" com confirmação).
- W13 Aba Participantes: lista (Código, Nome, Data de inclusão, Incluído por) e contador "X de N vagas".
- W14 Acesso negado; W15 Página não encontrada (404).

### 4. Estados e variações

- Para cada tela de lista e formulário: carregando, vazio, erro de carregamento e erro de validação.
- Versões por perfil: o mesmo estudo visto pelo pesquisador (com botões de escrita) e pelo coordenador (somente leitura).
- Mensagens de erro de regra de recrutamento: vagas esgotadas, candidato duplicado, sem consentimento, não elegível.

### 5. Fluxos navegáveis (conexões do protótipo)

- Administrador: W01 → W02 → W03 → W04 (cadastrar) → W03.
- Pesquisador: W01 → W02 → W05 → W06 (cadastrar estudo) → W07 → W09 (critérios) → W10 (visitas) → W07 (avançar estado, com confirmação) → W07 em recrutamento.
- Coordenador: W01 → W05 → W07 → W11 (cadastrar candidato) → W12 (avaliar, consentir) → W12 (incluir) → W13 (participante e contador "1 de N").
- Em todos: Sair volta a W01; rota proibida leva a W14.

### 6. Organização e entrega

- Nomear molduras `W01 Login`, `W02 Início`...; usar Auto Layout e componentes com variantes; usar só dados fictícios.
- Ativar o modo apresentação com ponto de partida em W01; compartilhar com "qualquer pessoa com o link pode ver".
- Registrar o link no README, no Azure Boards e na revisão da T35.

## 7. Critérios de aceite

- [ ] Os três fluxos podem ser percorridos clicando, sem backend.
- [ ] Todas as telas W01 a W15 existem, com as variações de estado.
- [ ] Tokens e componentes batem com os da web.
- [ ] Link compartilhado e registrado.

## 8. Cuidados e fora de escopo

- Esta task é obrigatória, mesmo sendo fora do código: sem o link, a T35 não pode ser concluída.
- Se o Figma divergir do código, registre o ajuste em vez de alterar a tela já funcional.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
