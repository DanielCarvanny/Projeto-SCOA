> Atualização de 13/11/2026: o cadastro de Pessoa é exclusivo de Pessoas; contas Aluno/Professor exigem o vínculo correspondente. Consulte [fluxos, homologação e contas antigas](ENTREGA_PESSOA_VINCULOS_CONTA.md).

# ACA-01 — Pessoa, conta e acesso persistente

Data: 13/11/2026. Responsável: Dan/A — Acadêmico. Implementação disponível para homologação; publicação e aceite final dependem de revisão humana.

## Decisão e responsabilidade

Dan esclareceu em 12/11/2026 que Pessoa, login e permissões descritos no módulo Acadêmico são responsabilidade integral de A. Autorizou o plano e aprovou a matriz de acesso e os parâmetros abaixo. Esta decisão substitui a atribuição anterior dessas funcionalidades a D/Fundação. Não concede permissão para implementar Financeiro, Biblioteca, Almoxarifado ou Administrativo nem aprova automaticamente contratos de consumo desses módulos.

Pessoa é única e compartilhada por pessoaId. Conta (usuario) e vínculos Aluno/Professor têm IDs independentes. O cadastro acadêmico não gera credenciais silenciosamente; conta é criada em ação específica de coordenador. Referências do PDF: Pessoa/Usuario nas páginas 140–143, RF-01/RF-02, segurança RnF-06 e RI-02. A referência a RN-17 nas classes é inconsistente: na lista de regras, RN-17 trata de multas da Biblioteca. Não foi usada como fundamento de autenticação.

## Funcionalidades

- Pessoa: cadastro, consulta por CPF/ID, edição de nome/contato, CPF único, conflito de edição e auditoria transacional.
- Conta: login único normalizado em minúsculas, no máximo uma conta por Pessoa, perfil, situação ativa/inativa e versão para revogar sessões antigas.
- Login: senha protegida por PBKDF2-HMAC-SHA256 do Java, salt aleatório de 16 bytes, 600.000 iterações, resultado de 256 bits. Sem senha em texto aberto no banco ou logs. APIs: [SecretKeyFactory/algoritmos Java 21](https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html) e [PBEKeySpec](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/crypto/spec/PBEKeySpec.html).
- Sessão: objeto opaco mantido pelo serviço, prazo absoluto de 30 minutos desde login; sair invalida a sessão. A cada operação, a conta é reconsultada para recusar inativação ou alteração de versão/perfil.
- Tentativas: cinco falhas bloqueiam a conta por cinco minutos. Contadores ficam no MySQL; reabrir aplicação não remove bloqueio. Mensagem genérica para login inexistente, senha errada, bloqueio e conta inativa.
- Senha: 8–128 caracteres, sem composição obrigatória. Login: 3–100 caracteres ASCII, letras/números e . _ @ -, começando por letra/número.
- Situação/perfil da conta: atualização revoga sessões anteriores; não permite remover ou inativar o último coordenador ativo.
- Primeira conta: comando local explícito, apenas quando nenhuma conta existe; operação serializada impede inicializações concorrentes. Senha digitada no terminal sem eco, sem valor padrão.

### Matriz aprovada

| Perfil | Permissões nesta versão |
|---|---|
| Coordenador | Gerenciar Pessoas e contas; cadastrar, consultar e editar alunos/professores |
| Aluno | Consultar a própria Pessoa/vínculo de aluno e editar somente seu e-mail/telefone |
| Professor | Consultar a própria Pessoa/vínculo de professor e editar somente seu e-mail/telefone |

Aluno/Professor não alteram nome, CPF, perfil, situação acadêmica, situação docente ou titulação. No menu de acesso desta primeira versão, esses perfis recebem Meu contato e Sair; as portas de consulta acadêmica também restringem o acesso ao próprio vínculo. Coordenador recebe Pessoas e contas, Alunos e Professores. As verificações estão nos serviços, além da apresentação.

Não há perfil Financeiro/Biblioteca/RH implementado por inferência. O contrato de permissões desses módulos será definido com seus responsáveis. A sessão é local ao processo desktop; não há API HTTP, SSO entre processos ou credenciais de acesso compartilhadas com outros sistemas.

## Executar no banco padrão

Pré-requisitos: Docker, Java 21 e Maven. Abra PowerShell na raiz do SCOA. O arquivo .env local fornece SCOA_DB_PASSWORD e, opcionalmente, SCOA_DB_PORT; não é versionado. Defaults do script: jdbc:mysql://127.0.0.1:3307/scoa, usuário scoa_app. É possível substituir por SCOA_DB_URL e SCOA_DB_USER no ambiente ou .env.

```powershell
docker compose up -d --wait mysql
.\executar-academico.ps1 -Acao Migrar
.\executar-academico.ps1 -Acao Inicializar
.\executar-academico.ps1 -Acao Abrir
```

Inicializar pede CPF, nome, e-mail, login e senha do primeiro coordenador em terminal interativo. A senha da conta é definida nesse momento; não é a senha do MySQL. Repetir Inicializar depois da primeira conta é recusado. Não existe login padrão escondido. Para abrir novamente, somente Abrir é necessário; o script prepara o classpath e compila a aplicação. Preparar compila sem acessar o banco.

Se Maven/Java não estiverem no PATH desta máquina:

```powershell
$env:JAVA_HOME = 'C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\java-tools\jdk-21.0.13+9'
$ferramentas = @{
  Maven = 'C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\java-tools\apache-maven-3.9.11\bin\mvn.cmd'
  RepositorioMaven = 'C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\maven-repository'
}
.\executar-academico.ps1 -Acao Migrar @ferramentas
.\executar-academico.ps1 -Acao Inicializar @ferramentas
.\executar-academico.ps1 -Acao Abrir @ferramentas
```

O script não inicia Docker nem inventa senha. O primeiro uso pode baixar o plugin Maven de geração do classpath; nenhuma dependência de aplicação nova foi adicionada ao pom.xml. Mensagens de inicialização não exibem credenciais. Em caso de erro de esquema, não executar repair ou apagar tabelas sem análise.

## Migrações e dados anteriores

Nova migração db/identidade/V001__pessoa_e_acesso.sql, com histórico flyway_identidade_schema_history. Instalar identidade antes do histórico acadêmico. V001/V002 de db/academico permanecem intactas.

Instalação limpa cria Pessoa, Usuario, controle e auditoria; depois as tabelas acadêmicas. Atualização de referência legada pessoa(id INT) acrescenta dados pessoais sem mudar o id ou remover suas FKs. IDs de Pessoa são gerados sob bloqueio transacional do controle de identidade, preservando o formato INT existente.

Se Pessoa existente tiver colunas não reconhecidas sem o histórico de identidade, a instalação interrompe para revisão, em vez de adotar ou sobrescrever o esquema automaticamente. O mesmo vale para tabelas institucionais preexistentes. Uma falha DDL exige avaliação antes de nova tentativa: não há repair automático no instalador.

Registros legados contendo apenas id permanecem com dados pessoais nulos. Não são apresentados como Pessoas completas e não podem receber conta. O saneamento requer dados legítimos e procedimento explícito; não inventamos CPF, nome ou e-mail. Antes de migrar uma base com dados relevantes, mantenha backup e valide a atualização no ambiente de teste.

O contrato cadastrarOuObter reutiliza a mesma Pessoa em concorrência e não sobrescreve seus dados. Se Pessoa for criada mas o vínculo acadêmico falhar, ela permanece: recuperar pelo caminho Pessoa já cadastrada. Não há exclusão automática de identidade.

Auditoria institucional registra autor, operação, entidade/ID, origem e UTC na mesma transação das alterações; não armazena senha ou CPF integral. Eventos de bootstrap antes de existir conta podem ter autor nulo, identificados pelo procedimento local; criação da conta inicial registra seu identificador. Auditoria acadêmica continua separada e recebe o usuarioId autenticado. Não foram acrescentadas FKs retroativas que invalidariam autores sintéticos antigos.

## Roteiro de homologação

1. Migrar, inicializar e entrar com o coordenador criado.
2. Pessoas e contas → cadastrar Pessoa → revisar → confirmar. Cancelar antes de confirmar não grava.
3. No menu Alunos, usar o CPF da Pessoa para criar seu vínculo. Conta Aluno exige esse vínculo prévio.
4. Consultar essa Pessoa pelo CPF → Gerenciar conta → definir login/senha e perfil Aluno. Conferir duplicidade e a ficha completa.
5. Criar vínculo Professor para a mesma Pessoa e conferir compartilhamento de contato; situações/titulação continuam independentes.
6. Fechar/reabrir e consultar registros e alterações. Este modo usa MySQL, diferentemente de DemonstracaoAlunos em memória.
7. Sair e entrar como Aluno: somente próprio contato editável; nome/CPF não editáveis; sem administração de contas/vínculos.
8. Como Coordenador, alterar perfil ou inativar uma conta; a sessão anterior deve ser recusada na próxima operação. Não inativar o último coordenador.
9. Repetir senha incorreta cinco vezes; mesmo com senha correta, aguardar cinco minutos. Os testes automatizados usam relógio controlado, sem esperas reais.
10. Conferir cancelamento, conflito de edição e histórico. Não registrar senhas nas evidências.

## Testes, limites e estado do backlog

Suíte inclui testes de hash/salt, sessão/expiração, papéis e telas; MySQL verifica persistência real, cadastro e edição compartilhada, autenticação/bloqueio, revogação, duplicidade, concorrência, rollback de auditoria, instalação limpa e atualização com FKs.

O modo persistente está implementado e os adaptadores simulados continuam somente na demonstração/testes. Ainda é necessária homologação manual desta nova versão por Dan e revisão da equipe antes de concluir/publicar o ACA-01. Não houve commit, push ou mudança de pontos. Integrações dos outros módulos e telas acadêmicas além do ACA-01 não fazem parte desta entrega.

Arquitetura atual é desktop com conexão JDBC direta. Credenciais de banco ficam no ambiente local confiável; não se trata de uma API de segurança acessível por clientes não confiáveis. Em uma implantação multiusuário com clientes não confiáveis, a fronteira de autorização deverá ser executada em serviço controlado pela instituição.

### Evidência de execução — 13/11/2026

Maven integracao/verify: **149 testes unitários + 35 de integração MySQL**, zero falhas/erros/ignorados, BUILD SUCCESS. Inclui fluxo Swing real de login, criação de Pessoa e conta, seguido de recuperação por nova instância do repositório. Preparar a aplicação pelo executar-academico.ps1 também resultou em BUILD SUCCESS. Log em ../evidencias/ACA-01-IDENTIDADE/identidade-final.txt.

O diagrama revisado está em CASOS_USO_IDENTIDADE_ACA01.puml (fonte PlantUML editável). O PDF original foi preservado. O diagrama não foi renderizado nesta etapa.

O banco padrão não foi migrado nem recebeu conta inicial automaticamente nesta validação; os testes usam exclusivamente o banco descartável scoa_test. Execute as etapas de instalação acima para iniciar a versão persistente.