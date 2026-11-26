# T161 · Execução em clone limpo e instruções para a Release 3

> **Sprint 4** · PBI-31 · Qualidade, regressão e preparação da Release 2  
> **Responsável:** I1 · **Estimativa:** 3h · **Prazo:** Sáb 05/12  
> **Depende de:** T157  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Provar que outra equipe instala e executa o sistema a partir de um clone limpo seguindo o README.

## 2. Contexto: o que já existe e deve ser reaproveitado

- README seção 5, `docker-compose.yml`, `.env.example`, `application-dev.properties.example`, README do app.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `README.md` | Alterar | Instalação, variáveis, portas e problemas comuns |
| `mobile/ClinicalStudyPB/README.md` | Alterar | Emulador e dispositivo |
| `.env.example` | Alterar | Variáveis que faltarem |
| `docs/sprint4/clone-limpo-release2.md` | Criar | Registro da execução |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T161-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T161-01** Usar máquina, VM ou usuário sem nada do projeto, caches ou variáveis anteriores.
- **RQ-T161-02** Seguir o README ao pé da letra: clonar, Docker Compose, banco, backend, web, app no emulador e login com cada perfil.
- **RQ-T161-03** Executar também os testes de cada camada pelos comandos documentados.
- **RQ-T161-04** Registrar cada passo que falhou, a causa e a correção.
- **RQ-T161-05** Conferir os comandos no Windows (PowerShell) e em Linux/macOS, indicando as diferenças.
- **RQ-T161-06** Tabela de variáveis de ambiente e portas (5433, 8081, 5173, 8082) e o endereço `10.0.2.2` do emulador.
- **RQ-T161-07** Seção de problemas comuns (porta ocupada, `JWT_SECRET`, senha do banco, volume antigo, Maven sem acesso).
- **RQ-T161-08** Revisar o texto para quem não conhece o projeto (Release 3).
- **RQ-T161-09** Confirmar que não há credencial real no repositório.
- **RQ-T161-10** Repetir os passos corrigidos em clone novo.

## 5. Contrato, regras e códigos de erro

Regra 9.7 da especificação.

## 6. Testes

A própria execução registrada; o job `Ambiente Docker` verde.

## 7. Critérios de aceite

- [ ] Clone limpo sobe sem passo não documentado.

## 8. Cuidados e fora de escopo

- Se não houver máquina limpa, declarar a limitação em vez de afirmar que foi testado.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend**”, “Testes automatizados de componentes nas interfaces web e mobile” e “Pelo menos um fluxo E2E automatizado na web e um no mobile”.
- Entregas (seção 10): “Os requisitos de releases anteriores devem continuar funcionando.” Vale para RF01 a RF22.

### 9.2 Critério de aceite do PBI (planejamento)

> RF01 a RF22 passam na regressão; há E2E automatizado na web e no app; a cobertura de linhas do código de negócio do backend está em 80% ou mais; o sistema sobe em um clone limpo seguindo o README; os defeitos críticos estão corrigidos e o relatório de testes está pronto para a documentação.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T161 · Execução em clone limpo e instruções para a Release 3.** Primeiro clone o repositório em uma máquina sem nada instalado do projeto e siga o README ao pé da letra: Docker Compose, banco, backend, web, app no emulador e credenciais fictícias. Depois corrija cada passo que falhar e acrescente variáveis de ambiente, portas e uma seção de problemas comuns. Por fim, revise o texto pensando na equipe que vai receber o projeto na Release 3, como pede a especificação.

### 9.5 Requisitos que atravessam esta task

- Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- Regra 9.7: “O projeto deve conter documentação de instalação e execução que permita a outra equipe assumir o desenvolvimento na Release 3.”
- Entregas: “Arquivos de configuração para instalar, executar e testar o sistema, **sem senhas ou credenciais reais**.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
