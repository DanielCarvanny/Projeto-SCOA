# Mensagem para B e D — integração de Pessoa e Aluno

Pessoal, avançamos no ACA-01 (Pessoa/Aluno) do Acadêmico. Temos cadastro por CPF, vínculo com pessoaId, ficha e edição de nome/contato, alteração de situação e persistência acadêmica MySQL. A interface ainda usa identidade simulada em memória. Precisamos integrar a Fundação para concluir o fluxo real.

**D — Fundação:** precisamos de migrações de Pessoa/conta, sessão autenticada e operações para consultar pessoa por CPF e por ID, cadastrar pessoa sem duplicar CPF e atualizar nome/e-mail/telefone. CPF e pessoaId não são alterados por essa edição. Favor informar os caminhos, contratos implementados e ordem das migrações.

**B — contratos, junto com D:** precisamos validar as portas consumidoras propostas em `academico/aplicacao/alunos`: `PessoasPorCpf`, `DadosPessoaisInstitucionais`, `IdentidadeInstitucional` e `AcessoAlunos`. São propostas locais, não contratos compartilhados aprovados.

Precisamos garantir:
- CPF único, inclusive com duas requisições concorrentes; repetição reutiliza a identidade sem sobrescrever dados.
- Operador obtido da sessão, com autorização por ação e pessoa/aluno antes de consultar ou escrever dados.
- Erros distinguíveis para entrada inválida, inexistência, acesso negado, conflito e indisponibilidade. Falha não pode retornar “pessoa inexistente”.
- Edição atômica com detecção de alteração concorrente (versão institucional ou equivalente), sem sobrescrever dados novos; auditoria com autor, instante UTC e operação.
- Recuperação de falha parcial: se a pessoa foi criada e o vínculo de aluno falhou, a nova tentativa reutiliza a pessoa. Não remover a identidade automaticamente.

Após a disponibilização, o Acadêmico conecta os adaptadores e valida o fluxo real: pessoa existente/nova → aluno → consulta/edição → reabertura da aplicação, além de testes de autorização, duplicidade, concorrência e auditoria.

Por decisão de Dan em 11/11/2026, a integração permanece pendente com vocês. Não alteramos a Fundação nem criamos identidade institucional paralela. Podem responder de forma assíncrona com contratos/caminhos disponíveis e previsão de entrega.

Referência: ACA-01 depende de BAS-02. Detalhamento local: `documentos/CONTRATO_ACADEMICO_FUNDACAO_PROPOSTA.md`.
