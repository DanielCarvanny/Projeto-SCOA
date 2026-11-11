> Atualização de 10/11/2026: a demonstração agora começa pelo CPF. Consulte [fluxo vigente e roteiro](GUIA_CADASTRO_CPF.md). O conteúdo abaixo registra a primeira versão por IDs.

# Interface inicial de Alunos — Swing

Incremento: ACA-01, interface inicial. Responsável: Dan / A. Branch: `codex/aca-01-interface-swing`.

## O que foi entregue

Formulário para cadastrar um aluno vinculado a uma pessoa institucional existente, consultar pelo ID do aluno e atualizar a situação ATIVO/INATIVO. Não cadastra nome/CPF nem substitui a identidade da fundação. O identificador do operador vem da composição do controlador; não é digitado no formulário.

`JOptionPane` é a classe do Java usada para mensagens e confirmação. `JPanel` organiza os campos. O nome solicitado como “JOptionPanel” corresponde aqui a essas duas peças.

Fluxo: `PainelAlunos → ControladorAlunos → casos de uso → contratos de persistência/identidade`. `DialogosJOptionPane` apresenta mensagens. `SwingWorker` executa operações fora da thread gráfica e devolve o resultado à tela. Os botões ficam desabilitados durante o processamento. A autorização continua nos casos de uso, independentemente da interface.

## Executar a demonstração

Na raiz do repositório, com JDK 21 e Maven configurados:

```powershell
mvn test
java -cp "target/classes;target/test-classes" br.edu.scoa.academico.apresentacao.alunos.DemonstracaoAlunos
```

A demonstração está exclusivamente em `src/test`: operador 7, pessoas sintéticas 42 e 43, repositório em memória. Os dados desaparecem ao encerrar. Não é um acesso autenticado ao sistema nem uma demonstração de persistência MySQL. Não usar com dados reais.

1. Informe pessoa 42 e ATIVO; clique Cadastrar. O ID gerado aparece no campo de aluno.
2. Consulte pelo ID gerado. Repetir cadastro da pessoa 42 deve mostrar duplicidade.
3. Selecione INATIVO e Atualizar situação. Cancele uma vez; depois confirme e consulte o resultado.
4. Tente pessoa 99, ID inválido e consulta de aluno inexistente. O formulário deve preservar a entrada em caso de erro.
5. Limpar apaga apenas o formulário, nunca os registros.

## Testes e evidências

Controlador: entradas, operador fixado pela composição, recusas de autorização/duplicidade, tradução dos códigos e ocultação de detalhes técnicos.

Painel: cadastro, consulta/atualização, preservação da entrada em erro, cancelamento sem gravação, execução fora da thread gráfica e bloqueio de duplo clique. Os diálogos são substituídos por um adaptador controlável. A suíte gera `target/previa-alunos.png`; a prévia foi inspecionada e apresenta campos e botões sem cortes.

Resultados executados em 10/11/2026:

- RED do controlador: 44 testes, 9 falhas e 8 erros esperados nas 17 verificações novas; implementação ainda era um esqueleto.
- GREEN: 50 testes unitários/de apresentação, zero falhas/erros.
- Regressão `mvn -Pintegracao verify`: os mesmos 50 testes mais 13 testes MySQL, todos aprovados. Configuração do banco isolado no guia `EXECUCAO_TDD_PERSISTENCIA_ALUNOS.md`.
- O ciclo RED foi registrado para o controlador; os testes do painel foram adicionados junto à implementação da tela.
- A suíte de banco permanece independente da demonstração em memória. Não foi executado fluxo completo janela → identidade real → MySQL.

Logs em `documentos/evidencias/ACA-01-SWING/`. Flyway mantém o aviso já conhecido de versão MySQL 8.4 mais recente que a versão verificada por esse adaptador; a suíte passou.

## Próximas etapas

1. Revisão humana desta tela e homologação dos diálogos reais do JOptionPane (confirmar, cancelar e fechar a janela de confirmação).
2. D implementar identidade, sessão e autenticação; B/D validar os contratos propostos. Não substituir essa dependência por identidade duplicada.
3. Compor a janela com o operador autenticado, o adaptador real da fundação e `RepositorioAlunosMysql`; preparar o banco por migrações aprovadas.
4. Executar o fluxo integrado real, verificar auditoria/permissões e persistência após reinício. Somente então avaliar aceite integral de ACA-01.
5. Prosseguir pelos incrementos do pacote Acadêmico vigente, repetindo teste que falha → implementação mínima → melhoria do código, sem alterar pontos ou prioridades unilateralmente.

Nenhum contrato compartilhado, dependência, migração ou módulo dos colegas foi alterado por esta interface. O plano detalhado está em `PLANO_INTERFACE_ALUNOS.md`. A primeira interface está pronta para revisão; o aceite final do cadastro continua condicionado às integrações e à homologação.
