> Fluxo vigente: a demonstração foi dividida em menu, cadastro guiado, consulta, detalhes e alteração. Consulte [guia das telas](GUIA_TELAS_ALUNOS.md). O conteúdo abaixo documenta a versão anterior.

# ACA-01 — Cadastro de aluno por CPF

Responsável: Dan/A. Branch: codex/aca-01-cadastro-cpf. Pedido de UX aprovado na conversa em 10/11/2026. Não altera RF, pontos ou aceite final.

## Executar

Na raiz C:\Users\Dan\SCOA, com Java 21 e Maven:

```powershell
mvn test
java -cp "target/classes;target/test-classes" br.edu.scoa.academico.apresentacao.alunos.DemonstracaoAlunos
```

Se Java/Maven não estiverem no PATH, os caminhos portáteis usados nesta máquina são:

```powershell
$env:JAVA_HOME = 'C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\java-tools\jdk-21.0.13+9'
& 'C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\java-tools\apache-maven-3.9.11\bin\mvn.cmd' test
& "$env:JAVA_HOME\bin\java.exe" -cp "target/classes;target/test-classes" br.edu.scoa.academico.apresentacao.alunos.DemonstracaoAlunos
```

## Roteiro

Dados exclusivamente sintéticos, sem consulta a cadastros externos:

| CPF de teste | Pessoa fictícia | Uso |
|---|---|---|
| 529.982.247-25 | Pessoa Exemplo A | Busca de pessoa existente |
| 111.444.777-35 | Pessoa Exemplo B | Segundo cadastro |
| 123.456.789-09 | Não existe inicialmente | Cadastro de nova pessoa e vínculo |

1. Digite um CPF e clique Buscar CPF. Com ou sem pontuação; dígitos verificadores são validados. Isso verifica formato, não existência oficial do documento.
2. Pessoa existente: confira nome/e-mail/telefone somente para leitura. O formulário não sobrescreve dados da Fundação.
3. Pessoa ausente: preencha nome e e-mail obrigatórios; telefone é opcional. Escolha situação e confirme Cadastrar aluno.
4. Cancelar a confirmação não grava pessoa nem aluno. Repetir cadastro do aluno é recusado.
5. Alterar CPF limpa a seleção e exige nova busca. CPF inválido ou indisponibilidade não libera cadastro indevido.
6. O ID de aluno gerado permite consulta e atualização de situação. Consulta por ID limpa os dados pessoais anteriores, evitando associá-los visualmente ao aluno errado.
7. Limpar não exclui registros. Fechar a demonstração perde os dados em memória.

## Arquitetura e fronteiras

PainelCadastroCpf chama CadastroAlunoPorCpf, que usa PessoasPorCpf (porta consumidora local proposta) e o caso de uso CadastrarAluno existente. A autorização precede a consulta. pessoaId segue interno, sem campo para digitação. A persistência acadêmica continua usando UNIQUE/FK de pessoa_id. Nenhuma tabela da Fundação é consultada diretamente pelo Acadêmico.

PessoasPorCpf recebe o operador em consulta e criação; o adaptador institucional deve verificar permissão e âmbito. cadastrarOuObter exige unicidade por CPF em concorrência/repetição e nunca sobrescreve dados de pessoa existente. O DTO PessoaParaCadastro é uma resposta de contrato, não entidade persistida pelo Acadêmico. Seu toString omite dados pessoais.

O único adaptador de pessoas implementado está em src/test (CenarioCpf). Não há migração para acrescentar CPF à fixture pessoa do MySQL. A tela não está conectada ao MySQL. O painel antigo por IDs permanece disponível para seus testes anteriores, mas não é mais usado pelo inicializador DemonstracaoAlunos.

Se a Fundação criar a pessoa e o cadastro acadêmico falhar, a identidade pode permanecer: não há exclusão compensatória destrutiva. Nova tentativa deve reutilizar a identidade. Transação integrada e política de recuperação devem ser acordadas com B/D antes da integração real.

## Evidências

- TDD do caso de uso: RED com 12 novos testes falhando; GREEN com 67 testes unitários/de apresentação aprovados.
- 13 testes existentes de integração MySQL aprovados. Isso é regressão da persistência acadêmica, não prova do novo contrato institucional real.
- Testes do novo painel cobrem consulta, troca de CPF, campos somente para leitura, confirmação, cancelamento, criação simulada e CPF inválido. Foram adicionados junto à implementação do painel.
- Prévia target/previa-cpf.png inspecionada sem cortes. Diálogos nativos e fluxo completo real aguardam homologação humana.
- Evidências versionáveis: documentos/evidencias/ACA-01-CPF/*.txt.

## Solicitação aos responsáveis externos

Incremento: ACA-01 | Área: Fundação e contratos B/D | Motivo: fluxo por CPF aprovado pelo usuário | Impacto: dados pessoais, autorização, concorrência e unicidade | Proposta: consultar pessoa por CPF e cadastrarOuObter, com operador e retorno de pessoaId | Testes: acesso indevido, CPF duplicado, concorrência, indisponibilidade e recuperação de falha parcial | Responsáveis: B e D | Decisão: pendente. Nenhum arquivo da Fundação ou catálogo compartilhado alterado.

Pronto para revisão da UX e proposta consumidora; ainda não para aceite institucional completo.
