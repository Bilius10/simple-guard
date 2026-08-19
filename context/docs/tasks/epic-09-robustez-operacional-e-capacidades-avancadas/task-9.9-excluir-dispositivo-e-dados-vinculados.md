# Task 9.9: Excluir Dispositivo e Dados Vinculados

Fase: fase posterior

## Contexto e Objetivo

Como administrador, quero excluir definitivamente um dispositivo e todos os dados operacionais vinculados a ele, para remover ativos descontinuados da instancia sem deixar residuos funcionais ou visuais no sistema.

Esta task existe para entregar uma capacidade administrativa destrutiva, auditavel e testavel, alinhada ao ciclo de vida de dados e a robustez operacional do produto, sem expandir escopo para exclusao em massa ou politicas genericas fora do caso de uso.

Nao considerar pronta se houver apenas exclusao parcial, ausencia de confirmacao critica, remocao sem testes ou comportamento indefinido para dados relacionados.

## Criterios de Aceite (Definition of Done)

- [ ] Expor endpoint administrativo autenticado para exclusao definitiva de dispositivo.
- [ ] Excluir o `device` e todos os registros vinculados definidos pelo contrato da task.
- [ ] Revogar ou remover credenciais ativas de pareamento ligadas ao dispositivo excluido.
- [ ] Garantir que agente previamente pareado nao consiga mais enviar telemetria nem buscar comandos apos a exclusao.
- [ ] Exibir acao destrutiva na web com confirmacao explicita e texto de impacto irreversivel.
- [ ] Atualizar a listagem e o estado local da web apos exclusao com sucesso, sem exigir recarga manual da pagina.
- [ ] Tratar sucesso, falha, dispositivo inexistente e conflito de estado de forma explicita no backend e no front.
- [ ] Testes unitarios obrigatorios implementados e passando.
- [ ] Cenarios de validacao manual executados e evidenciaveis pelo desenvolvedor.
- [ ] Nenhum comportamento fora do escopo desta task foi implementado sem nova task aprovada.

## Detalhes Tecnicos e Links Uteis

Referencias obrigatorias:
- `context/docs/contexto/project-context.md`
- `context/docs/contexto/project-constitution.md`
- `context/docs/produto/simpleguard-prd.md`
- `context/docs/ux/simpleguard-ux-plataforma-web.md`
- Figma oficial: https://www.figma.com/design/xWz2JTC3lp1N2uUeJXkLy9/SimpleGuard-UX-Screens?node-id=0-1&p=f&t=ete3nHVxHZKwr5qj-0

Notas tecnicas:
- O backend deve definir explicitamente, no service dono do dominio, quais agregados e registros relacionados sao removidos junto do dispositivo.
- A exclusao deve contemplar ao menos os dados operacionais ja introduzidos pelo produto para aquele dispositivo, como chaves, pareamentos, localizacoes, telemetria tecnica, eventos offline, comandos, incidentes, alertas e evidencias vinculadas, quando essas entidades ja existirem no codigo.
- Se algum dado vinculado nao puder ser removido fisicamente por restricao arquitetural real, a task nao pode ser fechada sem registrar a decisao e ajustar o contrato de exclusao de forma explicita.
- O endpoint deve permanecer administrativo e protegido por OIDC.
- O front web deve tratar a acao como destrutiva e irreversivel, com confirmacao reutilizando o padrao de acoes criticas do projeto.
- Nao implementar exclusao em massa nesta task.

## Dependencias e Bloqueios

- Depende das tasks anteriores de cadastro, pareamento, despareamento e listagem administrativa do dispositivo.
- Depende dos epics de telemetria, comandos, incidentes e evidencias apenas no que diz respeito aos dados ja existentes e vinculados ao dispositivo no momento da implementacao.
- Bloqueio: se houver entidade vinculada sem regra clara de ownership pelo dispositivo, registrar a decisao antes de concluir a task.
- Bloqueio: se a UX da confirmacao destrutiva na web nao estiver suficientemente definida para o fluxo, solicitar ajuste antes de fechar a implementacao visual.

## Granularidade

- A task deve caber em poucos dias de desenvolvimento.
- Se a exclusao completa ultrapassar 3 a 5 dias por depender de muitos modulos, dividir em subtasks por conjunto de dados, preservando um contrato unico de exclusao.
- Nao agrupar melhorias de busca, filtros, bulk actions ou lixeira/logica de arquivamento nesta task.

## Testes Unitarios Obrigatorios

- [ ] Backend: testes para exclusao bem-sucedida do dispositivo com remocao dos relacionamentos previstos.
- [ ] Backend: testes para dispositivo inexistente, credencial antiga rejeitada apos exclusao e idempotencia ou erro definido pelo contrato.
- [ ] Frontend web: testes para confirmacao, sucesso, falha e atualizacao da lista sem reload manual.

## Cenarios de Validacao Manual

- [ ] Excluir dispositivo pareado com sucesso pela web.
- [ ] Confirmar que o dispositivo some da lista administrativa.
- [ ] Tentar reenviar telemetria com credencial antiga e validar rejeicao.
- [ ] Validar mensagem de falha ao excluir dispositivo inexistente ou ja removido.

## Criterio de Conclusao

- Dispositivo excluido deixa de existir na instancia junto dos dados operacionais vinculados definidos pelo contrato, e nao pode mais operar como agente confiavel.

## Evidencias de Implementacao

- Endpoint administrativo destrutivo implementado e protegido.
- Service de dominio do dispositivo orquestrando exclusao e limpeza dos vinculados sem violar fronteiras de dominio.
- Fluxo web com confirmacao critica, feedback de sucesso/falha e remocao imediata do item na interface.
- Testes e validacoes manuais registrados junto da entrega.
