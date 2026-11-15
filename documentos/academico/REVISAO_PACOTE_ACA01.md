# Revisão do pacote ACA-01

Data: 15/11/2026. Responsável: A — Dan. Escopo desta revisão: etapa 4, conferência do pacote e preparação da mensagem de commit. Não foi executado commit, push, merge ou alteração de dados do banco.

## Resultado

- README atualizado para refletir identidade real, fluxos vigentes e resultados atuais; corrigido link do seed.
- Guia antigo de telas identificado como histórico para evitar uso de instruções superadas.
- Comandos conferidos no script: Preparar compila e monta classpath; Migrar aplica os históricos; Inicializar cria somente a primeira conta; Abrir inicia a aplicação. Java 21/Maven são pré-requisitos. A alternativa automática de ferramentas locais é específica do ambiente de Dan, não acompanha o repositório.
- Demonstração em memória conferida no código: alunos/professores de exemplo e Pessoa sintética CPF 12345678901 sem vínculos. Não popula o banco padrão e não substitui a aplicação persistente com login.
- Migrações do pacote: db/academico/V002__criar_professor_e_auditoria.sql e db/identidade/V001__pessoa_e_acesso.sql. A V001 acadêmica existente permanece preservada. O refinamento de vínculos não exige migração adicional.
- Git diff --check sem erros de whitespace; avisos LF/CRLF são conversão de fim de linha.
- PDF original e novo caminho possuem o mesmo hash Git c10e3bce2be3395246fdb5420f1f3f91b9d0e4fd: movimentação sem mudança do conteúdo.
- Alterações já existentes nos pacotes Financeiro/Biblioteca são correções do caminho do plano de testes. Não foram alteradas regras desses módulos nesta revisão.
- .env, .env.test e guia privado GUIA_LOCAL_ACESSO_MYSQL.md estão ignorados. O guia privado usa exclusão local do Git; não deve ser adicionado com force. target/ e logs brutos também não integram a entrega.

## Testes e limites

Evidência conferida: documentos/evidencias/ACA-01-VINCULOS/vinculos-integracao.txt, execução de 13/11/2026, 153 testes unitários e 37 MySQL aprovados. A revisão de 15/11 alterou somente documentação; não repetiu a suíte nem apresentou os testes anteriores como uma execução nova. Dan relatou funcionamento correto dos testes manuais na conversa.

Permanece sem confirmação de regularização a conta antiga de Professor sem vínculo identificada em 13/11. O banco não foi consultado ou modificado nesta etapa. Ajustes de mensagens e acabamento visual mencionados pelo usuário não foram implementados. Nenhum status final de aceite foi alterado.

## Composição do commit proposto

Incluir código/testes do Acadêmico, migrações novas, executar-academico.ps1, AGENTS.md e documentação correspondente. Incluir as remoções dos caminhos antigos junto às adições nos novos diretórios: o status ainda mostra D/?? porque a reorganização não foi adicionada ao índice. O diff --stat sozinho não inclui os arquivos novos.

Não incluir ferramentas Java locais, cache Maven, diretório target, credenciais, guia privado, dumps do banco ou arquivos temporários do ambiente de trabalho. Revisar o diff staged antes do commit, especialmente os arquivos novos. Não usar git add --force.

Pronto para revisão humana do pacote: sim. Mensagem preparada separadamente; criação do commit e publicação permanecem fora desta etapa.
