> Atualização de 10/11/2026: a demonstração agora começa pelo CPF. Consulte [fluxo vigente e roteiro](GUIA_CADASTRO_CPF.md). O conteúdo abaixo registra a primeira versão por IDs.

# Plano de execução — interface inicial de Alunos

Responsável: Dan / Acadêmico. Data: 10/11/2026. O pedido atual autoriza iniciar Swing e JOptionPane; substitui a decisão anterior de deixar toda a interface pendente. Não altera os pontos ou o escopo de outros módulos.

## Ordem e critérios

| Etapa | Trabalho | Aceite |
|---|---|---|
| 1 | Controlador de apresentação com TDD | Converte entradas, preserva autorização, chama casos de uso e traduz erros sem expor detalhes. |
| 2 | Painel Swing e adaptador JOptionPane | Cadastrar por pessoaId, consultar por alunoId e atualizar situação; mensagens e confirmação de alteração. |
| 3 | Testes sem janelas | Diálogos substituíveis; testes no thread de eventos verificam botões, estado, cancelamento e recusa. |
| 4 | Operações em segundo plano | SwingWorker executa casos de uso fora do thread de eventos; comandos bloqueados enquanto a operação está em andamento. |
| 5 | Demonstração isolada | Inicializador exclusivo em src/test, banner explícito, pessoa/operador sintéticos; não criar login fictício em produção. |
| 6 | Regressão | Executar unitários e MySQL; registrar RED/GREEN e limitações. |
| 7 | Fundação real | B/D validam portas, identidade, sessão e auditoria; adaptar composição sem SQL na tela. Pendente de implementação externa. |
| 8 | Homologação | Confirmar comportamento visual e fluxo com identidade real e banco de desenvolvimento preparado. Não declarar integração institucional a partir da demonstração. |

## Limites de edição

Código e testes em br/edu/scoa/academico/apresentacao/alunos. Sem mudanças nos módulos dos colegas. Swing pertence ao JDK; nenhuma biblioteca gráfica adicional é necessária. Modelos/casos de uso existentes continuam responsáveis pelas regras. Não haverá cadastro de CPF/nome paralelo à fundação, exclusão de alunos ou matrícula automática.

## Testabilidade

JOptionPane abre diálogos e não torna o programa automaticamente testável. Uma interface DialogosAluno permite testar as mesmas ações com respostas controladas, sem cliques manuais. JPanel pode ser exercitado em modo headless; JFrame e diálogos reais exigem avaliação visual posterior. Nenhuma operação de banco deve bloquear a Event Dispatch Thread (EDT), o thread que processa os eventos visuais.

## Saída esperada

Tela inicial simples, testes reproduzíveis e guia de demonstração. Pronto para revisão da interface inicial, não para aceite completo de ACA-01. Contratos reais e dados pessoais permanecem condicionados à fundação.
