# ACA-01 — Pessoa, vínculos acadêmicos e conta

Data: 13/11/2026. Responsável: Dan/A — Acadêmico. Implementação disponível para revisão humana; sem alteração de pontos ou aceite final.

## Decisão aprovada

O usuário aprovou nesta conversa manter Pessoas, Alunos e Professores como menus separados e criar Pessoa somente no menu Pessoas. Perfil de acesso e vínculo acadêmico continuam distintos. Pessoa pode ter ambos os vínculos, mas a conta mantém um perfil por vez.

Ordem: **Pessoa → vínculo acadêmico → conta de acesso**. Coordenador conserva a regra administrativa própria e o bootstrap da primeira conta, sem exigir vínculo acadêmico. Matrícula em curso permanece no ACA-04; não foi criado requisito de documento profissional para Professor.

## Comportamento entregue

- Alunos/Professores: cadastro começa diretamente pela busca de uma Pessoa existente. CPF aceita XXXXXXXXXXX ou XXX.XXX.XXX-XX; formato inválido não é consultado. A regra aprovada de formato foi preservada, sem cálculo de dígitos verificadores.
- Pessoa ausente: orientar cadastro no menu Pessoas. Nome/e-mail/telefone são somente para conferência durante a criação do vínculo; cancelar não grava.
- Professor: consulta por CPF ou ID interno, com somente o campo selecionado visível. CPF resolve pessoaId e depois o vínculo, sem igualar IDs. Pessoa ausente, vínculo ausente, acesso negado e indisponibilidade não são confundidos.
- Ficha de Pessoa: nome, CPF, e-mail, telefone; login, perfil e situação ou Sem conta; vínculos Aluno/Professor/ambos/nenhum. Erro de consulta não é apresentado como ausência.
- Conta: criar com perfil Aluno exige vínculo de Aluno; perfil Professor exige vínculo de Professor com titulação no catálogo e situação preenchida. A mesma regra vale ao trocar perfil. Não se exige situação ATIVO do vínculo: isso seria uma regra adicional não aprovada.
- Regras verificadas na aplicação e novamente na transação MySQL de criação/troca de perfil. Leitura dos vínculos usa bloqueio compartilhado nessa transação. Alterar perfil continua revogando sessões anteriores.
- Alterar somente situação da conta sem trocar perfil permanece possível, inclusive para inativar uma conta antiga irregular. Nenhum login antigo foi bloqueado automaticamente por esta mudança.

## Arquivos e contratos

Alteradas as aplicações CadastroAlunoPorCpf, GestaoProfessores e ServicoIdentidade; telas FluxoAlunos, FluxoProfessores e PainelIdentidade; adaptadores RepositorioProfessoresMysql e RepositorioIdentidadeMysql; testes e demonstração em memória. PainelCadastroCpf antigo também foi alinhado para não oferecer criação de Pessoa.

Contratos internos: RepositorioProfessores.consultarPorPessoa; RepositorioIdentidade.vinculos; novos DTOs VinculosAcademicos e FichaPessoa. Novos códigos VINCULO_ALUNO_NECESSARIO e VINCULO_PROFESSOR_NECESSARIO. Assinaturas antigas de cadastro com dados pessoais foram mantidas por compatibilidade, mas esses parâmetros não criam nem alteram Pessoa.

Não houve nova migração ou dependência. Os índices UNIQUE(pessoa_id) existentes em aluno e professor atendem às buscas. Nenhum código de outro módulo foi alterado. Autorização utilizada: execução expressa do plano por Dan nesta conversa.

## Roteiro manual

Na raiz C:\Users\Dan\SCOA, feche a aplicação antiga e execute:

```powershell
.\executar-academico.ps1 -Acao Abrir
```

Não repetir Inicializar. Não há migração adicional desta etapa.

1. Entre como Coordenador. Em Pessoas, crie uma Pessoa e consulte seu CPF: conferir todos os dados, Sem conta e Nenhum vínculo.
2. Tente criar conta Aluno antes do vínculo: deve orientar cadastro em Alunos e não gravar conta. Repetir para Professor.
3. Em Alunos → Cadastrar, verificar que a primeira tela pede CPF. CPF malformado é recusado; CPF inexistente orienta Pessoas. CPF existente mostra os dados somente para conferência.
4. Revisar e cancelar: não deve criar vínculo. Repetir e confirmar; retornar à ficha de Pessoa e conferir vínculo Aluno.
5. Criar conta com perfil Aluno; entrar com ela e conferir acesso restrito ao próprio contato.
6. Como Coordenador, tentar trocar o perfil para Professor sem vínculo: recusar. Cadastrar o Professor para a mesma Pessoa com titulação válida; consultar por CPF formatado e sem pontuação.
7. Conferir ambos os vínculos na ficha e trocar perfil para Professor. A sessão anterior deve ser recusada na próxima operação.
8. Consultar Pessoa existente sem Professor e depois CPF inexistente: as mensagens devem distinguir os casos. Alternar CPF/ID e conferir que somente o campo selecionado aparece.
9. Encerrar e reabrir para verificar persistência. Falhas de banco não podem aparecer como Sem conta ou Nenhum vínculo.

Na demonstração em memória, foi acrescentada a Pessoa sintética CPF **12345678901**, sem vínculos, para exercitar os novos cadastros. Ela não é inserida no MySQL. Essa demonstração permanece limitada aos fluxos acadêmicos; o menu completo de Pessoas/contas está na aplicação persistente.

## Contas anteriores

Consulta somente de leitura no banco padrão em 13/11/2026 encontrou: 0 contas Aluno sem vínculo; **1 conta Professor sem vínculo**; 0 contas Professor com vínculo e titulação fora do catálogo. Nenhum dado foi modificado por essa avaliação.

A ficha da Pessoa vinculada à conta apresenta Regularização pendente. O coordenador deve conferir se o perfil está correto e cadastrar o vínculo real de Professor com sua titulação, ou corrigir o perfil para outro permitido pela situação real. Não criar vínculos fictícios. Para localizar a conta sem consultar dados pessoais:

```sql
SELECT u.id AS conta_id, u.pessoa_id, u.perfil
FROM usuario u
LEFT JOIN professor p ON p.pessoa_id = u.pessoa_id
WHERE u.perfil = 'PROFESSOR' AND p.id IS NULL;
```

## Evidências e limites

- TDD vermelho: testes novos comprovaram que a versão anterior criava Pessoa implicitamente e concedia perfil sem vínculo (vinculos-red.txt).
- Validação final: **153 testes unitários + 37 testes de integração MySQL**, zero falhas/erros/ignorados, BUILD SUCCESS (vinculos-integracao.txt).
- Incluídos testes de telas, CPF, IDs distintos, recusa de troca de perfil, revogação, titulação legada, acesso, falha de consulta e combinações de vínculos.
- Prévia da ficha inspecionada visualmente; screenshots nos arquivos de evidência deste incremento.
- Aguarda homologação manual por Dan e regularização da conta antiga. Não houve commit, push ou publicação.

Evidências: ../evidencias/ACA-01-VINCULOS/. Resultados anteriores em outros documentos permanecem como histórico.
