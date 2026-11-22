# ACA-02 — evidências de matrizes e componentes

Execução: 15/11/2026. Conferência do código e documentação: 16/11/2026.
Responsável: A/Dan. Escopo: matriz em elaboração e componentes, sem publicação, pré-requisitos ou períodos.

## Resultado

- 181 testes unitários/de interface aprovados.
- 53 testes de integração MySQL aprovados.
- Zero falhas, erros ou testes ignorados na execução final.
- Instalação limpa e atualização desde V003 verificadas.
- Captura da ficha gerada em target/aca02-matriz.png e inspecionada visualmente.
- Git diff --check sem erros.
- Os 19 arquivos de implementação/testes/migração desta etapa foram comparados por SHA-256 entre a cópia testada e o checkout em 16/11/2026, sem diferenças.

## Execução reproduzível

A suíte usa Java 21, Maven e o perfil integracao já existente. Banco permitido pelos testes: jdbc:mysql://127.0.0.1:3308/scoa_test, usuário scoa_test e SCOA_TEST_CONFIRM=CONFIRMO_BANCO_DESCARTAVEL. A senha vem exclusivamente do ambiente local de testes.

Na máquina de desenvolvimento foi executado:

```powershell
docker compose --env-file .env.test -f compose.test.yaml up -d --wait mysql-testes
& 'C:\Users\Dan\Documents\ChatGPT\SCOA\executar_integracao.ps1' -Fase matriz-final
```

O script configura as ferramentas locais, lê a credencial de .env.test sem imprimi-la e executa Maven com -Pintegracao verify. Não aponta para o banco padrão. Log integral local: C:\Users\Dan\Documents\ChatGPT\SCOA\tmp\tdd-matriz-final.log.

## TDD e comportamentos verificados

Os primeiros testes de domínio, persistência e tela falharam por ausência das classes esperadas. Após a implementação, uma colisão de nomes entre página e campo foi identificada pelos testes de tela e corrigida; as telas foram executadas novamente com sucesso.

Verificados: composição não vazia, disciplina repetida, dados inválidos, autorização, cancelamento, gravação única fora da thread gráfica, conteúdo histórico, atualização explícita de revisão, preservação da carga considerada, FK para revisão inexistente, unicidade, rollback, IDs estáveis dos componentes mantidos, estado persistido não editável e duas remoções concorrentes.

Regressão incluiu Pessoa, Aluno, Professor, identidade e catálogo. A suíte final levou aproximadamente 45 segundos.

## Limitações

Homologação interativa pelo responsável ainda pendente. Os testes de interface exercitam componentes Swing programaticamente; a captura não substitui essa homologação. A demonstração em memória não comprova autenticação ou persistência reais. Não houve aplicação da V004 ao banco padrão nesta tarefa, commit, push ou aceite final do ACA-02.
