# Task 8.1: Criar Snapshot de Evidencia do Incidente

Fase: MVP

Como administrador, quero congelar os dados usados no relatorio, para que o PDF represente exatamente o estado do incidente no momento da geracao.

Referencias:
- `docs/arquitetura/simpleguard-arquitetura.md`
- `docs/produto/simpleguard-prd.md`

Escopo:
- Criar snapshot com IDs de eventos, janela de telemetria, paradas, dados tecnicos e limitacoes.
- Garantir imutabilidade do snapshot.

Testes unitarios:
- Backend: snapshot completo, sem telemetria e com eventos fora de ordem.

Cenarios de validacao manual:
- Gerar snapshot.
- Adicionar novo evento e confirmar que snapshot nao muda.

Criterio de conclusao:
- Snapshot preserva a base factual do relatorio.
