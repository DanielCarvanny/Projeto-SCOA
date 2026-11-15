> Atualização vigente de 11/11/2026: cadastro com escolha de pessoa nova/existente, consulta de aluno por CPF e catálogo de seis títulos. Use o [roteiro atualizado](ENTREGA_UX_CADASTROS.md). As etapas e contagens anteriores abaixo registram a entrega da época.

# ACA-01 — Professores: execução e entrega

Responsável: Dan/A. Data: 11/11/2026. Branch: codex/aca-01-professores.

## Plano executado

1. Revisar e commitar a ficha/edição pessoal de Alunos: commit 6a637c5. A validação manual do usuário continua em andamento; não equivale a aceite final.
2. Conferir RF-02, pré/pós-condições, classe Professor (p.142) e tabela professor (p.229) no PDF do projeto.
3. Escrever e executar testes RED dos casos de uso; implementar cadastro, consulta, edição docente/pessoal e erros estáveis.
4. Criar V002 e repositório MySQL acadêmico: unicidade de pessoa_id, FK, titulação obrigatória, transação com auditoria, conflito de edição e preservação de histórico.
5. Implementar telas guiadas equivalentes às de Alunos e dados iniciais em memória; testar navegação, recusas e confirmação.
6. Executar regressão, inspeção visual, instalação limpa e atualização V001 → V002; documentar limitações.

## Requisitos e escolhas

RF-02 exige manter dados docentes, titulações e contato. O modelo detalhado usa uma maior titulação, String/VARCHAR(255), e situação funcional ATIVO/INATIVO. A implementação respeita esse campo, sem impor mestrado/doutorado ou criar catálogo institucional não aprovado. A auditoria preserva titulação e situação anterior/nova; não representa coleção de diplomas/comprovantes. Pessoa é única por vínculo de professor. A mesma pessoa pode ter vínculos de aluno e professor com IDs independentes.

Não implementa alocação em turmas, aulas, folha ou regras de aptidão docente: pertencem aos outros incrementos. Não há exclusão física de professor.

## Como abrir

Na raiz C:\Users\Dan\SCOA, com Java 21 e Maven:

```powershell
mvn test
java -cp "target/classes;target/test-classes" br.edu.scoa.academico.apresentacao.alunos.DemonstracaoAlunos
```

O inicializador existente abre uma única janela com abas Alunos e Professores. Na aba Professores, escolha Cadastrar ou Consultar. O nome da classe foi mantido para preservar o comando conhecido.

Se Java/Maven não estiverem no PATH:

```powershell
$env:JAVA_HOME = 'C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\java-tools\jdk-21.0.13+9'
& 'C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\java-tools\apache-maven-3.9.11\bin\mvn.cmd' test
& "$env:JAVA_HOME\bin\java.exe" -cp "target/classes;target/test-classes" br.edu.scoa.academico.apresentacao.alunos.DemonstracaoAlunos
```

## Dados e roteiro

| Professor | Pessoa fictícia | CPF sintético | Titulação | Situação |
|---|---|---|---|---|
| 1 | Pessoa Exemplo A | 529.982.247-25 | Mestrado | ATIVO |
| 2 | Pessoa Exemplo B | 111.444.777-35 | Doutorado | INATIVO |

Consultar: informe ID 1 ou 2, confira ficha completa e selecione Editar titulação e situação ou Editar dados pessoais. Cada função usa tela própria, confirmação e Voltar sem salvar. Uma busca nova limpa o resultado anterior.

Cadastrar: CPF → conferir/preencher pessoa e maior titulação/situação → revisão → confirmar → resultado com ID. Para nova pessoa, use 123.456.789-09, nome fictício e e-mail novo@example.invalid. É esperado receber o ID de professor 3 se nenhum outro foi cadastrado. Os CPFs dos professores 1/2 devem gerar recusa de duplicidade.

Trocar o CPF exige nova busca. Voltar/cancelar antes da confirmação não grava. Durante operações, botões e navegação são bloqueados; SwingWorker mantém os acessos fora da thread gráfica. Edição desatualizada exige nova consulta.

A demonstração compartilha a mesma identidade de Alunos: alterar nome/contato de uma pessoa também aparece ao consultar novamente seu outro vínculo. Situação e titulação são atributos separados. São dados em memória; fechar restaura os exemplos. O histórico completo de auditoria é validado no repositório MySQL, não armazenado pelo dublê da demonstração.

## Arquivos e contratos

- dominio/professores: Professor e SituacaoFuncional.
- aplicacao/professores: GestaoProfessores, FichaProfessor, RepositorioProfessores, AcessoProfessores e FalhaProfessor.
- apresentacao/professores: FluxoProfessores.
- infraestrutura/persistencia: RepositorioProfessoresMysql.
- db/academico/V002__criar_professor_e_auditoria.sql: nova migração; V001 não alterada.
- Testes/demonstração em src/test; não entram como identidade de produção.

Reutiliza DTO/portas consumidoras PessoasPorCpf e DadosPessoaisInstitucionais já existentes no Acadêmico. AcessoProfessores propõe permissões distintas por cadastrar/consultar/atualizar e âmbito professorId. B/D devem validar o contrato e autorização institucional também por pessoa. Não se deve autorizar professores automaticamente só porque o usuário pode editar alunos.

## Persistência e validação

Testes reais usam somente scoa_test na porta 3308, com confirmação explícita. O fixture pessoa(id) pertence exclusivamente aos testes. RepositorioProfessoresMysql não acessa dados pessoais diretamente. Auditoria local contém autor, instante UTC e valores antes/depois; validade institucional do autor e centralização continuam pendentes.

A suíte cobre cadastro/consulta em nova conexão, atualização, duplicidade, FK, recusas CHECK, concorrência, conflito de edição, rollback quando auditoria falha e reaplicação de migração. Na preparação de banco limpo, instala V001, insere aluno sentinela, aplica V002 e verifica preservação. Para mais de uma migração, a preparação de teste faz baseline apenas quando não há versões aplicadas; não altera o comportamento de produção.

Casos de uso cobrem entradas, autorização, falha de identidade, nova tentativa após falha parcial e identidade compartilhada. O RED inicial teve 13 testes novos falhando. Testes de UI foram acrescentados junto à implementação da interface; evidências em documentos/evidencias/ACA-01-PROFESSORES. Prévias em target/professor-*.png.

Avisos conhecidos: MySQL 8.4 acima da versão verificada por essa versão de Flyway e seed SQL não nomeado como migração (é ignorado, conforme esperado). Nenhuma dependência nova ou configuração compartilhada foi introduzida.

## Limites do aceite

Implementação acadêmica pronta para revisão. Fundação, autenticação, contratos compartilhados e aplicação desktop ligada aos adaptadores reais continuam pendentes com B/D, conforme decisão do usuário. As simulações não comprovam integração institucional. Homologação manual, revisão e integração na branch principal ainda necessárias. Nenhum ponto, prazo ou status de aceite foi alterado unilateralmente.

Resultados finais: 110 testes unitários/de apresentação e 21 testes MySQL aprovados (13 Alunos + 8 Professores). Instalação limpa V001→V002 com aluno sentinela preservado executada com sucesso. Nove prévias de telas inspecionadas. O banco descartável foi reiniciado para esse teste; o banco de desenvolvimento não foi alterado.
