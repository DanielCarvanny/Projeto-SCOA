# ACA-01 — Ficha e edição de dados pessoais

Data: 11/11/2026. Responsável: Dan/A. Branch: codex/aca-01-ficha-pessoa.

## Escopo executado

GestaoFichaAluno reúne o vínculo acadêmico com a pessoa obtida pela porta consumidora DadosPessoaisInstitucionais. A autorização acadêmica precede a exposição de dados; o provedor institucional deve verificar também sua autorização por pessoa. O código não consulta tabelas da Fundação.

A demonstração usa o adaptador de CenarioCpf, em src/test. A ficha exibe nome, CPF, e-mail e telefone. A tela Editar dados pessoais altera nome/e-mail/telefone com confirmação; CPF e pessoaId permanecem imutáveis. A edição de situação continua separada. Cancelar não grava. Falha mantém o formulário para correção.

A porta de edição recebe o retrato esperado da pessoa; o provedor deve comparar e escrever atomicamente ou rejeitar conflito. O adaptador de teste faz isso em memória. Uma futura versão institucional pode substituir esse retrato por versão explícita acordada com B/D. FalhaDadosPessoais distingue CONFLITO_EDICAO e RESPOSTA_INCONSISTENTE sem comparar mensagens.

## Testar manualmente

```powershell
cd C:\Users\Dan\SCOA
mvn test
java -cp "target/classes;target/test-classes" br.edu.scoa.academico.apresentacao.alunos.DemonstracaoAlunos
```

1. Consulte aluno 1 ou 2. Confira nome, CPF, e-mail e situação.
2. Abra Editar dados pessoais; CPF aparece somente para leitura.
3. Altere nome/e-mail e volte sem salvar: os dados permanecem iguais.
4. Repita, confirme o salvamento e confira a ficha atualizada.
5. Tente nome vazio ou e-mail inválido: deve recusar e manter a tela.
6. Alterar situação segue sendo outra ação. Fechar e reabrir restaura os exemplos em memória.

As ferramentas portáteis e instruções de execução continuam em GUIA_TELAS_ALUNOS.md.

## Validação

Sete testes do caso de uso foram escritos e executados em RED antes da implementação. Testes adicionais cobrem UI e recuperação após criação de pessoa seguida de falha do vínculo: nova tentativa reutiliza a pessoa, sem exclusão compensatória.

Testes verificam: consulta por pessoaId correto, edição sem mudança de identidade, autorização, entrada inválida, inexistência, indisponibilidade, resposta institucional inconsistente, concorrência e recuperação de falha parcial. Prévias da ficha e edição inspecionadas sem cortes. Homologação de diálogos nativos ainda é humana.

## Integração pendente por decisão explícita

Dan orientou nesta conversa manter a integração real pendente com B/D. Não foram implementados identidade, login, sessão, migração de Pessoa, auditoria institucional ou persistência de dados pessoais. Nenhum contrato compartilhado foi aprovado unilateralmente. A demonstração não comprova persistência de Pessoa após reinício nem testes de contrato contra um provedor real.

O plano foi executado nas partes independentes do Acadêmico: proposta consumidora, ficha, edição, cenários e evidências. Dependem de B/D: contratos reais, migrações institucionais, sessão/permissões e adaptadores. Depois disso: conexão da aplicação completa ao MySQL, testes integrados de criação/edição/reabertura, instalação/atualização e homologação.

Mensagem pronta para encaminhar: MENSAGEM_FUNDACAO_PESSOA_ALUNO.md. Não foi enviada a terceiros pelo agente. ACA-01 permanece parcial e também exige Professores.

Resultados finais: 88 testes unitários/de apresentação e 13 testes MySQL aprovados. A primeira tentativa MySQL falhou com o container parado; após iniciá-lo, a regressão passou. Avisos existentes: versão MySQL 8.4 e seed SQL fora da convenção de migrações (não executado pelo Flyway). Testes MySQL usam fixture de identidade, não a Fundação real.
