# ACA-01 — Telas guiadas de Alunos

Responsável: Dan/A. Branch: codex/aca-01-telas-guiadas. Data: 10/11/2026.

## Executar

Na pasta C:\Users\Dan\SCOA, com Java 21 e Maven configurados:

```powershell
mvn test
java -cp "target/classes;target/test-classes" br.edu.scoa.academico.apresentacao.alunos.DemonstracaoAlunos
```

Ferramentas portáteis desta máquina, se necessário:

```powershell
$env:JAVA_HOME = 'C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\java-tools\jdk-21.0.13+9'
& 'C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\java-tools\apache-maven-3.9.11\bin\mvn.cmd' test
& "$env:JAVA_HOME\bin\java.exe" -cp "target/classes;target/test-classes" br.edu.scoa.academico.apresentacao.alunos.DemonstracaoAlunos
```

## Roteiro por funcionalidade

Menu: escolha Cadastrar aluno ou Consultar aluno. Apenas uma tarefa aparece por vez, na mesma janela.

Cadastro:
1. Para um novo cadastro, informe CPF sintético 123.456.789-09 e clique Buscar CPF e continuar.
2. Preencha nome fictício, e-mail e situação. Pessoas já existentes são exibidas somente para leitura.
3. Continue para revisão. Voltar permite corrigir dados de pessoa nova; Cancelar encerra sem gravar.
4. Confirme o cadastro. A tela de resultado apresenta o ID do aluno; anote-o.
5. Para pessoa nova, use CPF sintético 123.456.789-09, nome fictício e e-mail teste@example.invalid. Telefone é opcional.

Consulta e alteração:
1. Volte ao menu, escolha Consultar aluno e informe o ID gerado.
2. Confira os detalhes acadêmicos. Nome/contato ainda não estão disponíveis nessa consulta: não são inferidos de outra busca.
3. Clique Alterar situação; confira o aluno e a situação atual, escolha a nova e confirme o diálogo.
4. Cancelar ou Voltar sem alterar preserva o registro. Ao salvar, os detalhes são atualizados.
5. Voltar à busca limpa a seleção anterior. Um aluno inexistente não deixa uma ficha antiga disponível.

## Verificações

- Base: 67 testes aprovados antes da alteração.
- TDD: cinco novos cenários falharam no esqueleto da navegação (RED); implementação passou (GREEN).
- Suíte final: 76 testes unitários/de apresentação aprovados, incluindo nove testes do novo fluxo.
- Cobertura nova: isolamento de telas, confirmação final, cancelamento, nova busca de CPF, CPF inválido, campos obrigatórios, correção antes de confirmar, duplicidade, seleção do aluno para alteração e execução fora da thread gráfica com bloqueio de repetição/navegação.
- Oito prévias geradas em target/tela-*.png e inspecionadas; alinhamento corrigido. Diálogos nativos ainda requerem homologação manual.
- Regressão MySQL: primeira tentativa falhou porque o Docker estava desligado. Após localizar e iniciar Docker Desktop na instalação do usuário, os 13 testes MySQL passaram, junto aos 76 unitários/de apresentação. O aviso conhecido de compatibilidade Flyway/MySQL 8.4 permanece.
- Evidências em documentos/evidencias/ACA-01-TELAS/.

## Escopo e limites

Novo FluxoAlunos usa CardLayout e os casos de uso existentes; DemonstracaoAlunos abre esse fluxo. Painéis antigos não foram removidos e mantêm seus testes, mas não são usados pela demonstração atual. Nenhuma regra de domínio, dependência, contrato ou migração foi alterada.

A demonstração continua em memória com identidade simulada, aviso visível, operador e CPFs sintéticos. Fechar perde os cadastros. Busca de aluno continua por ID; busca de pessoa no cadastro usa CPF. Nome/contato na ficha de aluno, busca acadêmica por CPF e integração real dependem de contratos institucionais. Não há cadastro real de pessoas no MySQL por esta tela.

Pronto para revisão humana da UX inicial. Aceite final depende da homologação e integração real da Fundação. Sem alteração de pontos, prazo ou status final do cartão; sem commit/merge automático.

## Carga inicial da demonstração em memória

A demonstração agora inicia com Pessoa Exemplo A (CPF sintético 529.982.247-25, aluno 1 ATIVO) e Pessoa Exemplo B (CPF sintético 111.444.777-35, aluno 2 INATIVO). Abra Consultar aluno e informe 1 ou 2 para consultar e alterar imediatamente. Tentar cadastrá-los novamente deve ser recusado por duplicidade.

DadosDemonstracaoAlunos.java, em src/test, prepara os registros usando o mesmo caso de uso de cadastro da tela. O cenário padrão dos outros testes continua vazio de alunos. Cada abertura restaura os dois exemplos: alterações e novos cadastros não persistem ao fechar. O CPF 123.456.789-09 permanece disponível para experimentar pessoa nova, cujo primeiro aluno terá ID 3.

Validação da carga inicial: dois testes novos executados primeiro em RED (alunos ausentes), depois GREEN; 78 testes unitários/de apresentação aprovados. MySQL não foi reexecutado nesta alteração porque somente a composição de demonstração e dados em memória foram alterados; nenhum adaptador, contrato ou migração mudou. Dados pessoais na ficha de consulta continuam pendentes do contrato institucional.