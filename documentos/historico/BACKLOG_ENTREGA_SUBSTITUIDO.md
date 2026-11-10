> HISTÓRICO — SUBSTITUÍDO EM 09/11/2026. Não usar como instrução vigente. Pacotes e governança: documentos/entrega_modular/. QA/REL: documentos/PLANO_TESTES_ENTREGA.md. Conteúdo original preservado abaixo.

# SCOA — backlog para reunião e entrega em 30/11/2026

Proposta de PO. Quatro integrantes, implementação ainda não iniciada. Premissa: as 14–21 horas semanais informadas são por integrante; confirmar na reunião. Pontos são estimativas iniciais de tamanho relativo, não compromisso nem conversão em horas.

## Capacidade e decisão de escopo

Entre 07 e 30/11 há 24 dias corridos: aproximadamente 192–288 horas-pessoa brutas para quatro pessoas nessa disponibilidade. Reservando 30% para integração, revisão, testes finais e entrega, restam aproximadamente 134–202 horas para construção. Disponibilidade real, feriados e estágio podem reduzir esse total. Se as horas informadas forem do grupo inteiro, dividir a capacidade por quatro.

Não é responsável prometer todos os 72 RF a partir do zero com esses dados. O backlog abaixo preserva sua cobertura, mas a reunião deve definir o subconjunto que será compromisso de entrega com o professor. Prioridade não significa autorização para excluir requisitos. Antes de validar esse recorte, a entrega completa permanece em risco.

P0 = candidato ao fluxo mínimo integrado; P1 = complemento de alta relevância; P2 = restante do escopo documentado. A seleção de P0 considera os cinco módulos, mas deve ser ajustada à rubrica de avaliação. Não iniciar P1/P2 enquanto o compromisso P0 não estiver protegido.

## Kanban e regras de trabalho

Listas propostas: Backlog → Pronto para iniciar → Em implementação → Em revisão → Em teste → Concluído. Bloqueios usam etiqueta e motivo/data, mantendo o cartão na etapa em que parou.

Limites iniciais: no máximo um cartão em implementação por pessoa (quatro no total), dois em revisão e dois em teste. Quando revisão/teste lotar, ajudar a finalizar antes de começar outra tarefa. Cada cartão tem um responsável e um revisor diferente. O responsável acompanha até a conclusão.

Usar IDs e SP no título, por exemplo `[ACA-04][5 SP] Inscrever aluno em turma`. Etiquetas: módulo, prioridade e bloqueio. Na descrição: RF, resultado esperado, dependências, critérios de aceite, evidências, responsável e revisor. Não depender de recursos pagos do Trello para registrar esses dados.

Entrada em Pronto: critérios entendidos, dependências disponíveis, parâmetros decididos, SP consensual e responsável definido. Concluído: código integrado, persistência funcional, validações e acesso verificados, testes do cartão aprovados, revisão por colega e evidência anexada. Tela pronta ou código local não bastam.

## Pontuação

1 = alteração pequena e conhecida; 2 = operação simples com validação; 3 = cadastro/consulta completo; 5 = fluxo com regras ou integração; 8 = fluxo complexo a decompor antes de iniciar. Acima de 8: dividir. SP incluem implementação, testes do cartão e correção até o aceite; cartões QA abaixo cobrem verificação sistêmica adicional, sem contar novamente o mesmo teste.

Referências para votação: ACA-01 (3), ACA-04 (5), FIN-02 (5). Cada pessoa vota 1/2/3/5/8; extremos explicam hipóteses; esclarecer e votar novamente. Não tirar média automática nem comparar produtividade individual por pontos. Para prazo, estimar também esforço restante em horas/faixas por cartão após decomposição, usando a disponibilidade real; não criar taxa universal SP/hora.

## Backlog de implementação

Cada linha é um candidato a cartão. Todos começam no Backlog até validação da reunião. A coluna de aceite contém a verificação específica; a definição comum de Concluído também se aplica.

| ID | Entrega | RF | Prioridade | SP | Depende de | Aceite específico |
|---|---|---|---|---:|---|---|
| BAS-01 | Aplicação executável e banco versionado | Técnico | P0 | 3 | — | Colega instala a partir do README, aplica migrações e executa verificação básica. |
| BAS-02 | Identidade, login e permissões | ADM-09 | P0 | 5 | BAS-01 | Pessoa comum aos módulos; acesso indevido recusado também no servidor; papéis necessários disponíveis. |
| BAS-03 | Auditoria e dados de demonstração | RnF transversais | P0 | 3 | BAS-02 | Alteração relevante registra autor/data/origem; ambiente de demonstração pode ser reconstruído. |
| ACA-01 | Alunos e professores | ACA-01,02 | P0 | 3 | BAS-02 | Cadastrar/editar/consultar; identidade duplicada recusada. |
| ACA-02 | Cursos, disciplinas, matriz e período | ACA-04,22,30 | P0 | 5 | BAS-01 | Matriz versionada; período com datas válidas; pré-requisitos sem ciclos. |
| ACA-03 | Turmas, salas e alocação | ACA-03,05,20 | P0 | 5 | ACA-01,02 | Professor, disciplina e período vinculados; capacidade e conflitos de sala/professor validados. |
| ACA-04 | Matrícula e inscrição em turma | ACA-18,19 | P0 | 5 | ACA-03,05 | Distinguir vínculo e inscrição; recusar duplicidade, pré-requisito, conflito e falta de vaga, inclusive concorrente. |
| ACA-05 | Envio e validação documental | ACA-14 | P0 | 3 | ACA-01 | Envio não aprova documento; responsável registra aceite/recusa com justificativa. |
| ACA-06 | Aulas, conteúdo e frequência | ACA-10,21 | P0 | 5 | ACA-04 | Só professor autorizado registra; conteúdo exigido; sem duplicação de presença ou divisão por zero. |
| ACA-07 | Avaliações, notas e resultados | ACA-11,12 | P0 | 5 | ACA-06 | Escala, antecedência e fórmula aprovadas aplicadas; nota ausente permanece pendente. |
| ACA-08 | Auditoria do diário e pauta final | ACA-16,24 | P1 | 5 | ACA-07 | Coordenador registra análise/homologação; pendências impedem fechamento. |
| ACA-09 | Boletim, histórico e horários | ACA-08,09,25 | P1 | 5 | ACA-07 | Aluno consulta apenas dados próprios; resultado confere com lançamentos e plano. |
| ACA-10 | Rematrícula e impedimentos | ACA-13 | P1 | 5 | ACA-07,FIN-03,BIB-04 | Cada origem de impedimento identificada; regularizar uma não libera as demais; reexecução sem duplicação. |
| ACA-11 | Solicitações e documentos acadêmicos | ACA-17 | P2 | 3 | ACA-09 | Solicitação rastreável; documento emitido apenas com condições e dados completos. |
| ACA-12 | Transferência e equivalência | ACA-27,28 | P2 | 8 | ACA-02,04 | Decisão autorizada preserva origem/histórico; indeferimento não altera currículo. Dividir por processo. |
| ACA-13 | Avaliação institucional e manifestações | ACA-07,26 | P2 | 5 | BAS-02 | Respostas e manifestações registradas; consulta limitada ao responsável; política de participação definida. |
| ACA-14 | Ponto, prazos e metas docentes | ACA-15,23 | P2 | 5 | ACA-01 | Sequência de ponto validada; metas/pr azo vinculados ao docente e coordenador autorizado. |
| ACA-15 | Relatórios acadêmicos e institucionais | ACA-06,29 | P2 | 5 | ACA-07 | Filtros e totais conferidos; formato institucional acordado; emissão não declara transmissão externa. |
| FIN-01 | Contratos e consulta de cobranças | FIN-01 | P0 | 3 | ACA-01,BAS-02 | Modalidade/vigência registradas; aluno consulta apenas suas cobranças; devedor de multa pode não ser aluno. |
| FIN-02 | Mensalidades e documento de cobrança | FIN-02,03,04 | P0 | 5 | FIN-01,ACA-04 | Fórmula e proporcionalidade acordadas; memória de cálculo; reexecução sem duplicidade; natureza demonstrativa identificada quando aplicável. |
| FIN-03 | Pagamentos e saldo | FIN-08 | P0 | 5 | FIN-02 | Comprovante não duplica pagamento; parcial mantém saldo; quitação apenas após recebimento registrado. |
| FIN-04 | Multa integrada ao financeiro | FIN-10 | P0 | 3 | FIN-03,BIB-04 | Mesma multa gera um débito; quitação/cancelamento retorna à biblioteca; atende todos os devedores elegíveis. |
| FIN-05 | Ajuste por alteração acadêmica | FIN-09 | P1 | 5 | FIN-02,03 | Alteração de carga recalcula conforme contrato; cobrança paga preserva pagamento e histórico. |
| FIN-06 | Bolsas e sua aplicação | FIN-05 | P2 | 5 | FIN-02 | Vigência/cobertura/limites respeitados; benefício não equivale a recebimento. |
| FIN-07 | Financiamentos e repasses | FIN-06,07 | P2 | 8 | FIN-06 | Contrato e repasse rastreáveis; previsto separado de recebido; reprocessamento sem duplicação. Dividir público/privado e repasses. |
| FIN-08 | Relatórios financeiros | FIN-11 | P1 | 3 | FIN-03 | Totais de saldo/recebimento conferem com dados e filtros; acesso restrito. |
| BIB-01 | Acervo, exemplares e pesquisa | BIB-01,08 | P0 | 3 | BAS-01 | Obra distinta de exemplar; busca e disponibilidade corretas; tipos exigidos representados. |
| BIB-02 | Usuários e empréstimos | BIB-02,03 | P0 | 5 | BIB-01,BAS-02 | Identidade institucional, atendente e prazo registrados; exemplar tem no máximo um empréstimo aberto. |
| BIB-03 | Devolução do exemplar | BIB-03 | P0 | 3 | BIB-02 | Data e condição registradas; disponibilidade atualizada; multa não impede recebimento físico. |
| BIB-04 | Multas e pendências | BIB-05,06,10 | P0 | 5 | BIB-03 | Cálculo parametrizado e origem preservados; devolução não quita multa; pendência disponível ao Acadêmico. |
| BIB-05 | Reservas e renovações | BIB-11,12 | P1 | 5 | BIB-02 | Até duas renovações; reserva alheia bloqueia; datas e contagem persistidas. |
| BIB-06 | Avisos, relatórios e ajuda | BIB-04,07,09 | P2 | 5 | BIB-02 | Aviso com antecedência definida e resultado registrado; relatórios conferidos; instruções acessíveis. |
| ALM-01 | Produtos e localizações | ALM-01,02,05 | P0 | 3 | BAS-01 | Tipos e unidade definidos; local e limites coerentes; regra de código de barras decidida. |
| ALM-02 | Movimentações e níveis de estoque | ALM-04,09 | P0 | 5 | ALM-01,BAS-02 | Entrada/saída com autor; impedir saldo negativo inclusive concorrente; alertar mínimo/máximo. |
| ALM-03 | Retirada e devolução de material | ALM-10 | P1 | 3 | ALM-02 | Responsável e prazo registrados; devolução vinculada à retirada; disponibilidade coerente. |
| ALM-04 | Tombamento, transferência e baixa | ALM-06,07,08 | P1 | 5 | ALM-01,ADM-01 | Tombo único; histórico por setor; baixa justificada torna bem indisponível. |
| ALM-05 | Relatórios de estoque e patrimônio | ALM-03 | P2 | 3 | ALM-02,04 | Saldos, movimentações e bens por setor conferem com registros. |
| ADM-01 | Funcionários, setores e centros de custo | ADM-01,05 | P0 | 3 | BAS-02 | Identidade comum, vínculo e recursos registrados; acesso pessoal restrito. |
| ADM-02 | Distribuição e autorização de recursos | ADM-02,03 | P0 | 5 | ADM-01 | Origem/destino e decisão auditáveis; limite aplicado; aprovação distinta de execução. |
| ADM-03 | Cálculo e emissão da folha | ADM-06,07 | P1 | 8 | ADM-01 | Rubricas e competência rastreáveis; cálculo validado antes de emitir; reexecução sem duplicação. Dividir cálculo e emissão. |
| ADM-04 | Relatórios por centro de custo | ADM-08 | P2 | 3 | ADM-02 | Filtros e totais conferidos; acesso limitado. |
| INT-01 | Contratos de integração e identidade | ADM-04 | P0 | 3 | BAS-01 | Interfaces e IDs comuns acordados antes do trabalho paralelo; integrações executáveis verificadas em QA-01/02. |

INT-01 é pré-condição para implementar integrações entre módulos, além das dependências específicas listadas. Fechar decisões sobre fórmulas, precisão monetária, permissões e simulações antes dos cartões afetados. Resolver divergências do modelo físico como tarefas técnicas desses cartões, sem reabrir indiscriminadamente a fase de requisitos.

## Backlog de testes e entrega

| ID | Entrega | Prioridade | SP | Dependência/aceite |
|---|---|---|---:|---|
| QA-01 | Fluxo acadêmico-financeiro integrado | P0 | 5 | Após ACA-04 e FIN-03: matrícula → inscrição → cobrança → recebimento; negativos e totais documentados. |
| QA-02 | Fluxo biblioteca-financeiro integrado | P0 | 5 | Após FIN-04: empréstimo → devolução/multa → cobrança → pagamento; sem dupla cobrança nem quitação implícita. |
| QA-03 | Estoque e autorização administrativa | P0 | 3 | Após ALM-02 e ADM-02: concorrência de saídas, limites e autorizações; nenhuma perda de saldo/histórico. |
| QA-04 | Segurança, validação e auditoria | P0 | 5 | Após fluxos P0: acesso entre usuários/papéis, alteração indevida e entradas inválidas; evidências de recusa. |
| QA-05 | Regressão, interface e desempenho | P0 | 5 | Versão candidata: fluxos do compromisso, navegadores acordados; consultas/cadastros selecionados abaixo de 3 s na carga registrada. |
| QA-06 | Homologação da equipe | P0 | 3 | Após QA-01 a 05: matriz de aceite com resultado e evidência; falhas impedidoras corrigidas e retestadas. |
| REL-01 | Instalação, dados e manual de execução | P0 | 3 | Colega instala versão limpa; restaura dados; executa roteiro sem apoio do autor. |
| REL-02 | Demonstração e pacote final | P0 | 2 | Após QA-06/REL-01: versão identificada, repositório, instruções, evidências e limitações; ensaio dos cinco módulos. |

Falhas descobertas geram cartões BUG vinculados ao teste: passos, esperado/obtido, gravidade e evidência. Bloqueador = impede instalação/fluxo de aceite; crítico = perda/inconsistência de dados ou acesso indevido. Ambos devem estar zerados na entrega. Defeitos menores exigem registro e decisão explícita. Não inventar SP de bugs antes de diagnóstico; usar a reserva de capacidade e replanejar quando consumida.

## Marcos propostos — sujeitos à seleção viável do compromisso

| Data | Resultado esperado |
|---|---|
| 06–08/11 | Reunião, escopo de entrega validado, pontuação, responsáveis, stack e contratos de integração definidos. |
| 09–15/11 | Fundação e primeiros fluxos integrados executando; evidência de entrega por pessoa; rever previsão em 12/11. |
| 16–22/11 | Concluir construção do compromisso; testes integrados contínuos; em 22/11 congelar novas funcionalidades. |
| 23–26/11 | Testes sistêmicos, correções, regressão e instalação limpa; versão candidata até 26/11. |
| 27–29/11 | Homologação, ensaio e margem para imprevistos. |
| 30/11 | Entregar versão validada; confirmar horário e canal de submissão. |

Os testes de cada cartão acompanham a implementação; a fase formal de testes sistêmicos/homologação conserva o modelo em cascata. Kanban gerencia o fluxo dentro dessas etapas, sem exigir adoção de Scrum.

## Distribuição para decisão em reunião

Sugestão de quatro frentes, sem atribuir nomes antes de conhecer experiência: A — base e fluxo acadêmico; B — financeiro e integração acadêmica; C — biblioteca e integração de multas; D — almoxarifado e administrativo. A frente acadêmica é maior: após a fundação, redistribuir cartões entre as quatro pessoas. Cada frente tem revisor de outra frente; todos participam de testes. Não reservar toda a integração ou todo o teste a uma pessoa no fim.

Distribuir apenas os primeiros cartões prontos e manter fila priorizada. Comparar carga por esforço/disponibilidade e dependências, não apenas pela soma de SP. Verificação diária curta: o que terminou com evidência, qual bloqueio existe e quem ajuda a fechar trabalho em curso.

## Pauta da reunião (75 minutos)

1. 10 min — confirmar disponibilidade individual, critérios do professor e entrega em 30/11.
2. 15 min — selecionar compromisso viável e encaminhar eventuais exclusões para validação; registrar P1/P2 que ficam fora.
3. 25 min — votar SP dos selecionados, dividir itens 8 e explicitar esforço/dependências. Refinar restantes em outra sessão, se necessário.
4. 15 min — atribuir primeiro responsável/revisor e datas dos cartões selecionados; checar capacidade e caminho de dependências.
5. 10 min — confirmar critérios de conclusão, marcos e roteiro de demonstração.

Saída: compromisso nominal de cartões, pendências decididas, responsáveis, revisores, estimativas consensuais e próxima verificação. A proposta completa não deve ser confundida com compromisso automaticamente aprovado.

## Migração do quadro antigo

Guardar o ClickUp como histórico. O quadro fornecido tem tarefas amplas por módulo e testes até 06/12, incompatíveis com o novo limite de 30/11. Registrar a nova linha de base. Requisitos/documentação concluídos não voltam ao backlog de implementação só porque o status antigo está desatualizado.

Mapear Tarefa-16 → ACA; 17 → BIB; 18 → FIN; 19 → ALM/ADM; 20 → QA/REL; arquitetura/banco → BAS/INT e pendências técnicas específicas. Copiar apenas trabalho restante, com link do cartão antigo quando útil. Criar os cartões candidatos no Backlog; mover para Pronto só após refinamento. Este arquivo prepara a migração; nenhum quadro externo foi alterado.
