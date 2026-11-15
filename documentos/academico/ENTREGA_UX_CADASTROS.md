> Os caminhos de Pessoa nova dentro de Alunos/Professores descritos abaixo foram substituídos. Veja [fluxo vigente](ENTREGA_PESSOA_VINCULOS_CONTA.md), de 13/11/2026. Este registro permanece como histórico da etapa anterior.

# ACA-01 — Cadastro explícito, consulta por CPF e titulações

Data: 11/11/2026. Responsável: Dan/A — Acadêmico. Revisão humana pendente.

Este roteiro atualiza os fluxos de cadastro descritos nas entregas anteriores. A implementação segue o plano aprovado na conversa: escolha inicial, validação de CPF, edição compartilhada, consulta de aluno por CPF e catálogo docente. Não altera pontos, prazo ou aceite final do pacote.

## Comportamento entregue

Ao cadastrar aluno ou professor, escolha **Cadastrar uma nova pessoa** ou **Usar uma pessoa já cadastrada**. No primeiro caminho, preencha CPF e dados pessoais diretamente. No segundo, localize pelo CPF e confira os dados somente para leitura. Pessoa não encontrada permanece na busca; não ocorre criação automática nem mudança de caminho. Volte à escolha para cadastrar uma nova pessoa.

São aceitos exatamente os formatos XXXXXXXXXXX e XXX.XXX.XXX-XX (espaços externos são ignorados). Letras, pontuação fora do formato, quantidade errada e sequências repetidas são recusados antes da consulta institucional. Os dois formatos são normalizados para os mesmos 11 números. Por decisão de Dan, o cálculo de dígitos verificadores foi removido: valida-se o formato, sem comprovar validade cadastral ou titularidade.

A confirmação reconsulta a identidade: a escolha Nova pessoa recusa CPF já encontrado; a escolha Pessoa existente recusa pessoa ausente. Nada é gravado durante revisão ou cancelamento. Uma pessoa não recebe dois vínculos de aluno nem dois vínculos de professor. Aluno e professor podem compartilhar a mesma pessoa.

Na consulta de aluno, escolha ID interno ou CPF; só o campo correspondente é apresentado. A pesquisa por CPF obtém pessoaId pelo contrato institucional e consulta o vínculo acadêmico por esse ID. IDs de pessoa e aluno não são intercambiáveis. Sem pessoa, sem vínculo, falta de permissão e indisponibilidade são tratados sem apresentar uma ficha anterior.

As fichas separam dados pessoais compartilhados dos dados do aluno/professor. As edições continuam em telas próprias. Nome e contato atualizados aparecem nos outros vínculos da pessoa; situação cadastral, situação funcional e titulação permanecem específicas. CPF e identificadores não são editáveis.

## Titulações aprovadas nesta conversa

Tecnólogo, Bacharel, Licenciado, Especialista, Mestre e Doutor. Não há ordem de superioridade entre os três títulos de graduação. A seleção registra uma maior titulação concluída, não um histórico completo de diplomas. Pós-doutorado, livre-docência e siglas internacionais não pertencem ao catálogo deste campo.

O cadastro exige escolha explícita no JComboBox; a aplicação e o adaptador MySQL recusam novos valores fora do catálogo. Na edição, uma titulação reconhecida é pré-selecionada. Um valor antigo ou desconhecido continua visível como **Titulação registrada**, sem conversão automática, e exige seleção explícita para atualizar os dados docentes. A auditoria registra o valor anterior e o novo. Editar somente dados pessoais não obriga a converter a titulação.

Os exemplos em memória agora usam Mestre e Doutor. Não houve conversão em lote, alteração de migrações existentes nem alteração do banco padrão. O VARCHAR existente mantém compatibilidade com registros antigos; não foi acrescentada restrição SQL de catálogo que invalidaria esses registros.

## Executar a demonstração

Na raiz do projeto, com Java 21 e Maven:

```powershell
mvn test
java -cp "target/classes;target/test-classes" br.edu.scoa.academico.apresentacao.alunos.DemonstracaoAlunos
```

Para executar integração MySQL, use as variáveis e os comandos do [guia MySQL](../Guias/MYSQL_DOCKER.md). Os testes exigem explicitamente scoa_test na porta 3308 e confirmação de banco descartável. Não use a base padrão para os testes automatizados.

## Roteiro manual

1. Abra Cadastrar aluno: verifique os dois botões antes de qualquer campo de CPF. Repita na aba Professores.
2. Escolha pessoa existente e informe 5299822472; deve aparecer apenas a orientação de CPF inválido. Em Professores, teste 4299822472. Nenhuma consulta institucional deve ocorrer nesses casos, conforme teste automatizado.
3. Ainda em pessoa existente, procure 123.456.789-09 antes de criá-la: deve permanecer na busca e orientar voltar à escolha.
4. Volte e escolha nova pessoa. Use 123.456.789-09, nome fictício e novo@example.invalid; no professor, selecione um título. Revise, volte para corrigir e confirme. Cancelar antes de confirmar não deve criar pessoa ou vínculo.
5. Tente criar nova pessoa usando 529.982.247-25: deve orientar usar a pessoa já cadastrada, sem alterar nome ou contato.
6. Os CPFs 529.982.247-25 e 111.444.777-35 já possuem aluno e professor na carga de demonstração. Tentar criar novamente o mesmo vínculo deve informar duplicidade.
7. Consulte aluno por CPF com e sem pontuação; ambos devem abrir a mesma ficha que o ID 1. Troque o tipo de busca e confira que apenas um campo fica visível.
8. Edite contato do aluno 1 e consulte novamente o professor 1: o contato deve estar atualizado. A situação de um vínculo não deve mudar a do outro.
9. Confira os seis títulos no cadastro e na edição. O cadastro começa sem título escolhido. Salvar sem selecionar deve mostrar mensagem específica.
10. Feche e reabra: os exemplos originais retornam, pois a demonstração é em memória.

Para testar pessoa existente sem vínculo em uma das abas: crie uma nova pessoa como professor e depois use seu CPF em Cadastrar aluno → pessoa existente (ou faça o inverso).

## Escopo técnico e contratos

- Reutiliza Java 21, Swing, JUnit e MySQL, sem novas dependências ou mudanças na configuração raiz.
- Acrescenta consultarPorPessoa à porta interna RepositorioGestaoAlunos e consulta parametrizada ao adaptador MySQL; o índice UNIQUE(pessoa_id) existente atende essa busca.
- Validação de CPF e de campos pessoais é centralizada no Acadêmico; mensagens usam códigos estáveis.
- Os métodos antigos de cadastro continuam disponíveis para compatibilidade com testes e composição anteriores. As telas atuais utilizam a escolha explícita; painéis históricos não são a entrada da demonstração.
- Nenhuma migração nova foi necessária. V001 e V002 foram preservadas.
- A autorização institucional continua no provedor e a autorização acadêmica por aluno continua antes da entrega da ficha. Não foi introduzida permissão de professor por possuir vínculo de aluno.

## Limites e integração pendente

Pessoa, autenticação, autorização institucional e sua persistência reais continuam sob responsabilidade da Fundação. Os contratos locais permanecem propostas pendentes de B/D. Não houve implementação de identidade institucional nem acesso às tabelas desse módulo.

O contrato cadastrarOuObter precisa garantir atomicamente CPF único, sem sobrescrever dados em corrida. Entre a consulta prévia e a criação, outro operador pode criar a pessoa: o contrato atual reutiliza essa identidade. D/B devem validar esse comportamento e, caso se exija distinguir criação de reutilização concorrente na resposta, acordar sua evolução. Não alegamos integração real concluída.

Se a criação de Pessoa tiver sucesso e o vínculo falhar, a pessoa poderá existir sem o vínculo. A recuperação é voltar à escolha e usar pessoa já cadastrada. A interface não apaga uma identidade para desfazer falha acadêmica.

Busca por matrícula permanece planejada no ACA-04: número pertence a MatriculaCurso, não ao ID de Aluno. Deve apresentar aluno e curso e contemplar nenhum ou múltiplos vínculos.

## Evidências

Regressão inicial: três falhas esperadas comprovam ausência da escolha e aceitação de título arbitrário. A integração intermediária identificou fixtures com títulos antigos; elas foram ajustadas e um teste específico preserva a leitura e auditoria de conversão explícita.

Resultados finais e logs estão em [evidências ACA-01-UX](../evidencias/ACA-01-UX/). Prévias das telas estão em target/tela-*.png e target/professor-*.png; inspecionadas as telas de escolha, cadastro novo, ficha e edição pessoal. Diálogos nativos ainda dependem da homologação manual do responsável.

Validação final: **147 testes unitários/de interface e 23 testes de integração MySQL aprovados, sem falhas ou testes ignorados**. Comando local: executar_integracao.ps1 -Fase ux-final. Banco de teste já em V002; não houve migração nova nem necessidade de reiniciar o banco para esta alteração. Permanecem os avisos conhecidos do Flyway sobre versão MySQL 8.4 e o seed que não é migração.

## Revisão de fechamento — 12/11/2026

Dan relatou validação manual das funcionalidades e ajustou a regra de CPF. A regressão após essa alteração passou com 143 testes unitários/interface. O aceite integrado permanece pendente; consulte o [checklist e revisão dos casos de uso](FECHAMENTO_ACA01.md). As contagens anteriores acima são registros das execuções da época.
