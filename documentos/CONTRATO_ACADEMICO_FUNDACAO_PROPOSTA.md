# Contrato consumidor do Acadêmico com a fundação

Data: 10/11/2026. Status: proposta pendente de validação por B e D. Não há implementação institucional real disponível, conforme informação do usuário.

## Responsabilidades

Dan/A mantém aluno, situação cadastral e regras acadêmicas. D mantém pessoa, conta, autenticação e autorização institucionais. B coordena os contratos compartilhados. As portas Java deste documento são internas ao Acadêmico e não substituem um contrato comum aprovado.

## Operações solicitadas

| Porta | Operação | Resultado e comportamento |
|---|---|---|
| IdentidadeInstitucional | podeCadastrarAluno(usuarioId) | Permissão explícita de cadastro. Indisponibilidade deve lançar falha; nunca retornar true por padrão. |
| IdentidadeInstitucional | pessoaExiste(pessoaId) | Existência da pessoa comum; pessoaId não é alunoId nem usuarioId. |
| AcessoAlunos | podeConsultarAluno(usuarioId, alunoId) | Permissão inclui o âmbito do aluno solicitado, inclusive consulta aos próprios dados. |
| AcessoAlunos | podeAtualizarAluno(usuarioId, alunoId) | Permissão de edição acadêmica com âmbito explícito. |

usuarioId deve ser fornecido por contexto autenticado confiável. Não receber identidade do operador de um campo livre na futura interface. O servidor de banco ou a conexão não equivalem à autenticação institucional do operador.

## Persistência proposta

Pré-condição da migração acadêmica: pessoa(id INT) fornecida pela fundação, conforme tipo do modelo físico. A tabela aluno usa FK e UNIQUE(pessoa_id); o Acadêmico não lê/escreve dados pessoais diretamente. O SQL de teste cria somente uma fixture pessoa(id), no banco scoa_test; não é uma implementação alternativa da fundação.

Auditoria desta fatia é local em aluno_auditoria: aluno, autor, operação, situação anterior/nova e instante UTC, gravados na mesma transação. usuario_id permanece referência sem FK enquanto o contrato da conta não existe. Isso não comprova validade institucional do autor: a autorização real continua pendente. D/B devem definir integração/centralização futura; não foi criada nem alterada tabela institucional de auditoria.

## Decisões para fechamento

- Validar assinaturas, tipos de IDs e fonte do usuário autenticado.
- Publicar migração da identidade e ordem relativa às migrações acadêmicas.
- Definir política de acesso por perfil e âmbito.
- Aprovar ou ajustar auditoria local e seu vínculo institucional.
- Definir como criar/alterar os dados pessoais pela interface de Alunos, sem duplicação.

Sem essas decisões, podem ser aprovados testes unitários e persistência com fixture; a integração entre módulos permanece pendente. Interface Swing demonstrativa iniciada por pedido posterior do usuário; integração institucional real permanece pendente.


## Extensão consumidora por CPF

Proposta local PessoasPorCpf: consultar(usuarioId, cpf) e cadastrarOuObter(usuarioId, cpf, nome, email, telefone). Retorna PessoaParaCadastro com pessoaId, cpf e dados de conferência. Aprovação B/D pendente. Contrato, recuperação de falha parcial e roteiro em [cadastro por CPF](GUIA_CADASTRO_CPF.md).
