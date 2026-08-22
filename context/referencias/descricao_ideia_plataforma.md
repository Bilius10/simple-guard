# Relatório de Descoberta — Hub de Segurança e Recuperação de Dispositivos

## 1. Resumo Executivo

O projeto consiste no desenvolvimento de uma plataforma de segurança pessoal voltada para o monitoramento e recuperação de dispositivos (celulares, computadores e outros equipamentos) em casos de perda ou roubo.

Diferente das soluções comerciais tradicionais, o sistema prioriza a soberania do usuário, permitindo o gerenciamento da própria infraestrutura e a geração de evidências técnicas confiáveis para apresentação às autoridades competentes.

A interface será inspirada em sistemas de monitoramento tático, oferecendo rápida interpretação das informações durante situações de emergência.

---

## 2. Contexto do Negócio

O objetivo é disponibilizar uma solução centralizada que permita ao proprietário agir rapidamente após um roubo ou perda de um dispositivo.

A plataforma funcionará como um **Hub Global de Segurança**, permitindo:

- Monitorar diversos dispositivos simultaneamente;
- Visualizar todos os equipamentos em um mapa único;
- Executar bloqueio remoto;
- Preservar os dados do dispositivo para auditoria;
- Gerar evidências para seguradoras e autoridades.

---

## 3. Objetivos Estratégicos

### Recuperação Eficaz

- Localização precisa do dispositivo;
- Histórico completo de deslocamento.

### Preservação de Dados

- Bloqueio imediato do aparelho;
- Nenhum apagamento automático dos dados.

### Geração de Evidências

- Relatórios em PDF;
- Trajetória geográfica;
- Registro cronológico dos eventos;
- Informações utilizáveis em processos policiais ou de seguro.

---

## 4. Principais Dores Identificadas

- Vulnerabilidade dos dados pessoais após um roubo.
- Dificuldade de agir rapidamente quando o celular utilizado para autenticação também é o dispositivo roubado.
- Ausência de histórico de permanência ("paradas") em soluções tradicionais.
- Necessidade de uma visão única de todos os dispositivos.

---

## 5. Oportunidades de Melhoria

- Implementação de um **Modo Vigia** ativado manualmente.
- Visualização contínua da trajetória no mapa.
- Interface intuitiva para usuários leigos.
- Melhor destaque visual para eventos críticos.

---

## 6. Perfis dos Usuários

### Administrador (Proprietário)

Responsável por:

- Gerenciar dispositivos;
- Ativar bloqueio remoto;
- Acompanhar o rastreamento;
- Gerar relatórios;
- Administrar a senha mestra;
- Manter a chave física de recuperação.

---

## 7. Processos Atuais

- Recebimento de localização a cada **1 minuto**.
- Exibição do status técnico:
  - bateria;
  - tipo de conexão;
  - estado geral do dispositivo.

---

## 8. Processos que Precisam Evoluir

### Rastreamento em Tempo Real

Transformar a atualização pontual em uma animação contínua semelhante à utilizada em aplicativos de transporte.

### Detecção Inteligente de Paradas

Registrar automaticamente quando um dispositivo permanecer imóvel por determinado período.

---

## 9. Regras de Negócio

### Atualização de Localização

- Intervalo: **1 minuto**

Objetivo:

- equilíbrio entre precisão e consumo de bateria.

---

### Definição de Parada

Um dispositivo será considerado parado quando permanecer na mesma posição por:

**10 minutos**

---

### Bloqueio Remoto

O bloqueio deve:

- impedir o uso do aparelho;
- preservar todos os dados armazenados;
- não realizar limpeza ("wipe") automática.

---

### Segurança de Acesso

A autenticação deverá utilizar:

- senha mestra;
- biometria (quando disponível);
- chave física de recuperação.

---

## 10. Necessidades Funcionais

### Mapa Interativo

- Visualização de todos os dispositivos.
- Zoom automático para nível de rua.
- Centralização automática ao selecionar um alerta.

### Painel Lateral

Exibir:

- endereço;
- coordenadas;
- bateria;
- nível de sinal;
- tipo de rede;
- horário da última atualização.

### Relatórios

Exportação em PDF contendo:

- mapa;
- trajetória completa;
- horários;
- pontos de parada;
- tempo de permanência;
- dados técnicos do dispositivo.

---

## 11. Necessidades Não Funcionais

### Segurança

- Aplicação Tipo Aplicativo Web.
- Controle local pelo usuário.
- Redundância através de chave física.

### Interface

Estilo:

- Retro-Tech;
- Pixel Art;
- Alta legibilidade;
- Cores fortes para situações críticas.

### Persistência

Os alertas deverão permanecer ativos até confirmação manual do administrador.

---

## 12. Indicadores Gerenciais

### Status Atual

- 🟢 Seguro
- 🔴 Em Alerta

### Histórico

Registro consolidado contendo:

- movimentações;
- bloqueios;
- alertas;
- eventos do sistema.

---

## 13. Restrições e Premissas

- Plataforma hospedada e administrada pelo próprio usuário.
- Acesso principal via aplicativos.
- O rastreamento depende da conectividade ativa do dispositivo monitorado.

---

## 14. Riscos Identificados

- Perda simultânea da chave física e do dispositivo.
- Desligamento do aparelho antes da ativação do modo de monitoramento.
- Ausência de conexão com a internet.

---

## 15. Pontos que Precisam de Esclarecimento

Definir:

- usuários adicionais autorizados a receber alertas;
- níveis de permissão;
- procedimentos para recuperação de acesso.

---

## 16. Ideias e Insights

### Sinalização Visual

Utilizar cores para indicar o estado de vigilância.

| Cor | Significado |
|------|-------------|
| 🟢 Verde | Seguro |
| 🟡 Amarelo | Atenção |
| 🔴 Vermelho | Alerta |
| 🔵 Azul | Monitoramento |

---

## 17. Funcionalidades Potenciais

### Zonas Seguras

Possibilidade futura de criação de perímetros automáticos.

### Diagnóstico Remoto

Permitir verificar:

- GPS;
- rede móvel;
- Wi-Fi;
- bateria;
- sensores de localização.

---

## 18. Benefícios Esperados

- Redução do tempo de resposta após um roubo.
- Maior probabilidade de recuperação do dispositivo.
- Evidências técnicas para processos policiais.
- Apoio a processos de seguro.
- Maior controle do patrimônio digital.

---

## 19. Priorização Inicial

| Prioridade | Funcionalidades |
|------------|-----------------|
| 🔴 Alta | Bloqueio remoto, rastreamento, mapa, alertas persistentes |
| 🟡 Média | Relatórios PDF, painel técnico, histórico de paradas |
| 🟢 Baixa | Pixel Art avançada, animações e sons personalizados |

---

## 20. Conclusão

O projeto evoluiu de uma simples solução de rastreamento para uma plataforma completa de segurança e recuperação de dispositivos.

A combinação entre:

- regras de negócio bem definidas;
- rastreamento contínuo;
- bloqueio remoto;
- preservação de evidências;
- geração de relatórios técnicos;

forma uma base sólida para a construção de um ecossistema de segurança voltado à rápida resposta em situações de perda ou roubo.

A arquitetura proposta prioriza a autonomia do usuário, a preservação dos dados e a produção de informações confiáveis para apoiar tanto a recuperação do dispositivo quanto processos administrativos, policiais e de seguro.

Com funcionalidades planejadas para evolução gradual — como zonas seguras, diagnósticos remotos e monitoramento inteligente —, a plataforma apresenta potencial para se tornar um hub completo de gestão e proteção de dispositivos pessoais.
