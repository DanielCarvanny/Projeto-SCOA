# ACA-01 — primeiro ciclo TDD de Alunos

Responsável: Dan, módulo Acadêmico. Data: 10/11/2026. Estado: fatia de aplicação implementada; ACA-01 ainda incompleto. Professores está fora desta fatia.

## O que foi implementado

CadastrarAluno valida IDs positivos e situação informada, exige permissão, consulta existência de pessoa institucional e chama a escrita do repositório. Identidade, pessoa, matrícula em curso e inscrição em turma continuam conceitos separados. Situação é explícita (ATIVO/INATIVO), sem inventar regra de ativação automática.

O requisito acadêmico RF-01 orienta o cadastro e preservação da identidade; o modelo físico distingue aluno(id, pessoa_id, situacao_cadastral). Este primeiro recorte não cadastra nome/CPF diretamente: dados pessoais pertencem à identidade comum e dependem da integração com a fundação.

## Evidência TDD executada

1. RED: contratos e assinatura mínima criados com execução não implementada. Os testes foram executados: 12, com 9 falhas e 3 erros; build falhou pelo comportamento ausente.
2. GREEN: implementação mínima adicionada; mesmos 12 testes executados, zero falhas/erros, build aprovado.
3. Revisão: sem necessidade de refatoração adicional nesta fatia; regras permanecem no caso de uso e detalhes externos atrás de portas.

Logs das execuções em evidencias/ACA-01/. Ambiente de execução: Windows, Temurin 21.0.13+9, Maven 3.9.11. Nenhum teste foi ignorado.

## Cenários

- Cadastro preserva pessoa, situação e autor.
- Situação inativa informada é preservada.
- Duplicidade retorna conflito e mantém cadastro anterior.
- Usuário sem permissão não consulta pessoa nem grava.
- Pessoa inexistente não é gravada.
- IDs zero/negativos são recusados (quatro casos parametrizados).
- Situação ausente é recusada.
- Falha da identidade impede gravação.
- Falha de persistência não retorna sucesso.

Os dublês ficam exclusivamente em src/test. O cenário de duplicidade comprova a reação do caso de uso ao conflito; a garantia de concorrência será verificada no adaptador MySQL real. Não há implementação em memória em produção.

## Executar

Com JDK 21 e Maven 3.9.11 instalados/configurados, na raiz:

```powershell
java -version
mvn -version
mvn test
```

Maven pode baixar dependências no primeiro uso. Resultado esperado: 12 testes aprovados. Relatórios locais: target/surefire-reports/ (ignorados pelo Git). O projeto ainda não possui tela ou comando de inicialização da aplicação.

Na máquina desta sessão, foram baixadas ferramentas portáteis fora do repositório, sem instalação global. Para repetir exatamente nesse ambiente:

```powershell
$env:JAVA_HOME = 'C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\java-tools\jdk-21.0.13+9'
& 'C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\java-tools\apache-maven-3.9.11\bin\mvn.cmd' -f 'C:\Users\Dan\SCOA\pom.xml' '-Dmaven.repo.local=C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\maven-repository' test
```

## Próximos ciclos, sem declarar o incremento concluído

1. Validar porta de identidade e mapa de diretórios com responsáveis.
2. Testar e implementar consulta/atualização de Alunos, incluindo acesso e preservação de identidade.
3. Testar MySQL real: migração, FK, unicidade concorrente, rollback e auditoria.
4. Integrar identidade/autorização real e tela desktop após definição da biblioteca.
5. Verificar fluxo completo e critérios do pacote antes de aceite humano.

Nenhum outro módulo foi implementado. Configurações de build foram criadas com autorização explícita do usuário para proposta técnica. Alterações documentais anteriores foram preservadas. Não houve commit/push.
