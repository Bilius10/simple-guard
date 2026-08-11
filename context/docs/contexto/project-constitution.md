---
title: "Constituicao do Projeto SimpleGuard"
project_name: "SimpleGuard"
status: "draft"
created: "2026-08-08"
updated: "2026-08-10"
source_material:
  - "contexto/project-context.md"
  - "produto/simpleguard-prd.md"
  - "ux/simpleguard-ux-plataforma-web.md"
  - "ux/simpleguard-ux-app-celular.md"
  - "ux/simpleguard-ux-app-computador.md"
  - "arquitetura/simpleguard-arquitetura.md"
---

# Constituicao do Projeto SimpleGuard

Este documento define as regras permanentes de desenvolvimento, nomeacao, arquitetura e qualidade do SimpleGuard. Ele existe para reduzir interpretacao ambigua, manter consistencia entre tarefas e evitar correcoes tardias por desvio de padrao.

## 1. Prioridade Das Fontes

Quando houver conflito, siga esta ordem:

1. Constituicao do projeto.
2. `contexto/project-context.md`.
3. PRD, UX e arquitetura.
4. Tasks do epic.
5. Codigo existente e testes.

Se uma lacuna nao estiver coberta por estas fontes, registre a suposicao antes de implementar.

## 2. Regras Gerais

- Todo desenvolvimento deve ser orientado por tarefa aprovada.
- Nao criar comportamento futuro sem necessidade da task atual.
- Nao substituir lacuna por mock permanente.
- Toda mudanca relevante deve vir com testes unitarios adequados ao risco.
- Erros, estados vazios e falhas devem ser tratados explicitamente.
- A implementacao deve preservar o que ja foi validado, nao reescrever o sistema sem motivo.

## 3. Nomeacao

- Classes Java terminam com o papel real do componente.
- Classes de teste terminam com `Tests`.
- Metodos de teste terminam com `Tests`.
- DTOs recebem sufixo `Request`, `Response`, `Dto` ou `Payload` conforme o papel.
- Excecoes de sistema usam a base comum definida no backend.
- Pacotes seguem dominio funcional, nao camada generica solta.

## 4. Estrutura De Codigo

- Backend organiza por dominio e caso de uso: `config`, `domain`, `controller`, `service`, `repository`, `error`, `shared`.
- Frontend organiza por feature: `auth`, `session`, `critical-action`, `map`, `incident`, ou equivalente.
- Arquivos novos devem entrar no padrao existente antes de criar novos estilos.
- Nao misturar responsabilidade de UI, dominio e infraestrutura no mesmo arquivo quando houver alternativa clara.

## 5. Clean Code E Manutenibilidade

- Metodos devem ter uma responsabilidade clara e preferencialmente executar um unico nivel de abstracao.
- Fluxos de aplicacao nao devem concentrar busca, validacao, mutacao de dominio, montagem de entidade e persistencia no mesmo metodo quando isso tornar a leitura extensa.
- Ao perceber um metodo crescendo, extrair passos nomeados antes de adicionar nova regra de negocio.
- Um metodo de servico deve ler como uma orquestracao curta do caso de uso; detalhes repetitivos devem ficar em metodos privados bem nomeados ou em componentes dedicados.
- Validacoes de negocio devem ter metodo proprio quando possuirem erro, codigo HTTP ou mensagem especifica.
- Criacao de entidades complexas deve ficar em metodo factory privado ou factory de dominio, evitando construtores longos no meio do fluxo principal.
- Evitar funcoes gigantes. Como referencia pragmatica, metodo com mais de 30 linhas ou que exige rolagem deve ser tratado como suspeito e refatorado, salvo justificativa clara.
- Refatoracao local para preservar legibilidade faz parte da entrega quando a task tocar codigo ja degradado.

## 6. Contratos E Validacao

- Contratos de entrada devem ser claros e pequenos.
- DTOs de API devem validar forma com anotacoes de validacao padrao quando aplicavel.
- Regras de negocio devem ficar em servicos ou handlers, nao em componentes visuais.
- Excecoes de dominio devem ser convertidas pelo handler geral do sistema.
- O sistema deve padronizar mensagens de erro e formato de resposta.

## 7. Erros

- Toda excecao do sistema deve derivar da excecao base padrao do projeto.
- O handler geral da API deve ser a unica borda de conversao de excecoes de aplicacao.
- Erros de seguranca continuam tratados no handler especifico do Spring Security.
- Mensagens de erro devem ser internacionalizadas.

## 8. Testes

- Todo comportamento novo deve ser coberto por teste.
- Testes de backend devem seguir o padrao `*Tests`.
- Testes de frontend devem cobrir o fluxo real do componente ou servico.
- Backend deve manter cobertura JaCoCo de 100% para instrucoes, branches, linhas, metodos e classes.
- `mvn verify` deve ser a validacao padrao do backend quando houver mudanca Java, porque executa testes e regra de cobertura.
- Nao aceitar implementacao sem teste quando o risco for funcional ou de contrato.
- Validacoes manuais devem ser registradas quando a task pedir evidencia executavel.

## 9. Frontend

- A interface deve seguir o UX especificado para a superficie.
- Evitar layout generico de dashboard SaaS quando o produto pede leitura operacional.
- Componentes reutilizaveis devem ficar em feature propria.
- Dialogos criticos devem ser reutilizaveis e tratar cancelar, confirmar e falha.
- Textos de interface devem ser curtos, claros e coerentes com o tom operacional.

## 10. Backend

- APIs devem ser previsiveis, consistentes e auditaveis.
- Fluxos sensiveis precisam de validacao de autenticacao, autorizacao e contrato.
- Nao confiar em dados vindos do front para regras criticas sem checagem adicional.
- O backend deve preservar separacao entre autenticacao, dominio e resposta HTTP.
- Codigos sensiveis de pareamento ou confirmacao nao devem ser persistidos em texto claro.
- Respostas HTTP que exponham segredo temporario devem impedir cache explicito.

## 11. Auditoria E Estados

- Estados importantes devem ser nomeados de forma explicita.
- Eventos observados, comandos solicitados, comandos executados e confirmacoes nao devem ser misturados.
- Falhas de conectividade devem ser tratadas como estado esperado.
- O historico e a auditoria devem favorecer reconstrucao cronologica.
- Sessoes temporarias sensiveis devem registrar criacao, expiracao, uso e motivo de expiracao quando aplicavel.
- Sessoes temporarias substituidas devem encerrar explicitamente a sessao anterior antes de expor uma nova.

## 12. Documentacao

- Cada task relevante deve terminar atualizada.
- Quando uma decisao sair de lacuna para regra oficial, atualizar este documento ou o contexto mestre.
- A documentacao deve usar portugues do Brasil e manter consistencia terminologica.
- Nao duplicar regras em varios lugares sem motivo; preferir um documento mestre e referencias curtas.

## 13. Checklists Antes De Fechar Uma Task

- O escopo ficou limitado ao que foi aprovado.
- Os nomes seguem o padrao do projeto.
- Os testes passam.
- O contrato nao ficou mais amplo do que o necessario.
- A resposta de erro e o estado vazio foram considerados.
- A documentacao da task foi atualizada.
- Nao sobrou comportamento futuro implementado como se fosse definitivo.
- Metodos alterados foram revisados contra as regras de clean code e nao concentram responsabilidades desnecessarias.
