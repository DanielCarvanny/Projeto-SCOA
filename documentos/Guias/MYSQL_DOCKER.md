# MySQL local em Docker — SCOA

Java permanece a linguagem da aplicação. compose.yaml é configuração de infraestrutura, não uma aplicação em outra linguagem.

## Configuração

MySQL 8.4, banco `scoa`, usuário de aplicação `scoa_app`, porta local padrão 3307 (porta interna 3306). Somente localhost acessa a porta publicada. Volume nomeado `scoa_mysql_dados` mantém os dados fora do ciclo de vida do contêiner. A tag 8.4 acompanha correções dessa linha; upgrades devem ser planejados e precedidos de backup.

Na raiz do repositório, copie `.env.example` para `.env` se ainda não existir e preencha duas senhas diferentes. A preparação assistida gera essas senhas localmente quando não existe `.env`, sem exibi-las. `.env` está ignorado pelo Git. Variáveis de ambiente são convenientes no desenvolvimento local, mas quem administra o Docker pode inspecioná-las; implantação compartilhada requer tratamento próprio de segredos.

```powershell
docker compose config --quiet
docker compose up -d --wait --wait-timeout 180
docker compose ps
```

Docker Desktop precisa estar iniciado e usando contêineres Linux.

## Conexão

- Host: `127.0.0.1`
- Porta: `3307` (ou SCOA_DB_PORT do .env)
- Banco: `scoa`
- Usuário: `scoa_app`
- Senha: SCOA_DB_PASSWORD do .env
- URL JDBC prevista: `jdbc:mysql://127.0.0.1:3307/scoa`

A aplicação Java ainda precisa do driver Connector/J e de um adaptador de persistência. O Java/Maven não lê automaticamente o `.env` do Compose: as credenciais da aplicação deverão ser carregadas explicitamente pela configuração de infraestrutura, sem valores secretos no código. Não desabilitar segurança da conexão indiscriminadamente para contornar erros do driver.

Para consultar no cliente MySQL dentro do contêiner, com senha solicitada interativamente:

```powershell
docker compose exec mysql mysql -u scoa_app -p scoa
```

## Persistência e operação

`docker compose stop` para o serviço; `docker compose start` retoma. `docker compose down` remove contêiner/rede e preserva o volume; `docker compose up -d --wait` recria o serviço usando os dados existentes. Não usar `down -v` para uma parada normal: essa opção remove o volume e apaga os dados. Volume não substitui backup.

As variáveis MYSQL_DATABASE, MYSQL_USER e senhas inicializam um volume vazio. Alterar `.env` posteriormente não troca credenciais do banco existente; faça uma alteração SQL autorizada e ajuste as configurações correspondentes. Não apague o volume como solução para erro de autenticação.

## Próximo ciclo TDD

O serviço cria apenas o banco e o usuário. Nenhuma tabela institucional foi criada: identidade comum, migrações e repositório Java continuam pendentes. Testes de integração devem usar banco/volume descartável separado do desenvolvimento. Verificar no MySQL real unicidade de pessoa_id, FK institucional, rollback, auditoria e concorrência antes de concluir cadastro de Alunos.

Fontes: https://docs.docker.com/guides/databases/ e https://github.com/docker-library/mysql/blob/master/8.4/docker-entrypoint.sh .
