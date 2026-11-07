# Como usar as regras e os pacotes do SCOA

Este guia explica a execução por quatro integrantes e suas equipes de agentes. Prazo final: 30/11/2026. As regras obrigatórias de desenvolvimento estão no `AGENTS.md` da raiz. A organização e os contratos propostos estão em `entrega_modular/GOVERNANCA.md`.

## 1. O que cada arquivo faz

| Arquivo | Uso |
|---|---|
| `AGENTS.md` | Padrão de código, nomes, arquitetura, permissões, dados, testes e revisão para todos os agentes. |
| `entrega_modular/GOVERNANCA.md` | Responsáveis técnicos, interfaces entre módulos, marcos e coordenação assíncrona. |
| `entrega_modular/01_ACADEMICO.md` | Incrementos do responsável A. |
| `entrega_modular/02_FINANCEIRO.md` | Incrementos de B, incluindo contratos compartilhados. |
| `entrega_modular/03_BIBLIOTECA.md` | Incrementos de C; também consolida evidências e roteiro final. |
| `entrega_modular/04_ALMOXARIFADO_ADMINISTRATIVO.md` | Incrementos de D, mantendo os dois módulos separados; inclui fundação. |
| `BACKLOG_ENTREGA.md` | Referência de QA/REL e rastreabilidade anterior; sua pauta de reuniões foi substituída pela execução assíncrona. |
| PDF do projeto | Fonte dos requisitos e regras do produto. Os pacotes não substituem sua leitura nas seções relacionadas. |

Os IDs de cartões e os IDs de requisitos não são equivalentes: o cartão ACA-04 implementa os RF acadêmicos 18 e 19. Sempre conferir o campo “Referência” do incremento antes de buscar o requisito.

## 2. Preparação única da equipe

1. Preencher A/B/C/D com os nomes no quadro e nos pacotes, sem mudar a propriedade por iniciativa de um agente.
2. Manter uma cópia comum versionada destes arquivos na raiz do repositório real. Se o repositório já tiver AGENTS.md, incorporar as regras após revisão de D, preservando instruções existentes pertinentes.
3. D registra linguagem, framework, versões, estrutura real de diretórios, comandos de instalação/build/teste/lint e configuração do banco em uma decisão técnica. Registra também os caminhos pertencentes a cada módulo.
4. B publica contratos versionados com provedores e consumidores. Definir assinaturas e exemplos na mesma stack; não basta distribuir descrições em linguagem natural.
5. Todos partem do mesmo commit-base e ambiente. Os arquivos de regras podem ser copiados para outras ferramentas de IA, mantendo uma única fonte de verdade no repositório.

Até essas definições, os pacotes são especificações de trabalho, não autorização para cada integrante criar uma stack diferente. O responsável pode decidir e comunicar por escrito; não é necessária reunião.

## 3. Iniciar uma sessão de agente

Coloque AGENTS.md na raiz do repositório. Ferramentas que reconhecem esse nome podem carregá-lo automaticamente; confirme o comportamento da ferramenta utilizada. Para qualquer ferramenta, forneça explicitamente as regras, a governança e o pacote na primeira instrução. Não dependa apenas de memória de outra conversa. Agentes sem acesso aos arquivos precisam receber seu conteúdo e os requisitos relevantes.

Prompt pronto para adaptar:

```text
Você trabalhará no SCOA como agente do integrante [nome / A-B-C-D].
Leia AGENTS.md, entrega_modular/GOVERNANCA.md e [caminho do pacote].
Leia a decisão de stack e os contratos existentes.
Incremento desta sessão: [ID e título].
Diretórios autorizados: [caminhos reais do meu módulo e testes].
Fonte dos requisitos: [PDF/seções ou transcrição pertinente].
Critérios adicionais e parâmetros já decididos: [referências].
Primeiro resuma o escopo, dependências, arquivos previstos e testes.
Depois execute o incremento autorizado. Não altere outros módulos ou
arquivos compartilhados sem a autorização específica exigida no AGENTS.md.
Se houver bloqueio, registre-o e continue o trabalho independente.
Entregue o relatório final no formato do AGENTS.md para minha revisão.
```

## 4. Como executar um pacote

O pacote é a fila do integrante, não uma ordem para implementar tudo em uma única conversa. Cada título `[ID][SP]` vira um cartão no Trello. Copie referência, dependências, aceite, testes e evidências para a descrição; atribua o responsável humano.

P0/P1/P2 determinam ordem inicial, não retirada de requisitos. Dependências podem alterar a ordem: por exemplo, ACA-05 precisa anteceder ACA-04 apesar da numeração. “BAS-02” refere-se à fundação de D; “INT-01”, aos contratos mantidos por B. Cartões divididos A/B/C substituem seus pais antigos; não criar ambos como trabalho duplicado.

Leia a descrição completa, identifique entradas/saídas e confirme que existem contratos e parâmetros suficientes. Trabalhe um incremento principal por vez, entregue uma parte funcional com persistência e testes e passe à revisão antes de acumular novos cartões. SP já incluem testes locais; não contar pontos novamente por cada agente ou subtask.

Exemplo: para BIB-02, C entrega empréstimo com pessoa institucional, exemplar, atendente e prazo; testa duplicidade e concorrência; integra com a identidade de D. Se faltar um campo na identidade, C solicita a mudança a D com evidência. O agente de C não edita o núcleo diretamente para desbloquear sua tarefa.

## 5. Usar vários agentes com segurança

O humano define o escopo de cada agente e permanece responsável pelo pacote. Uma divisão possível: um agente implementa caso de uso/persistência, outro implementa interface em arquivos distintos e outro revisa/testa depois da integração. Paralelizar somente tarefas com contratos definidos e conjuntos de arquivos separados.

Cada agente recebe as mesmas regras, o mesmo incremento e limites explícitos. Use branches/worktrees próprios; não dar a dois agentes edição simultânea do mesmo arquivo. O integrante consolida alterações e resolve conflitos conscientemente. O teste do trabalho consolidado é obrigatório mesmo se cada agente declarar sucesso isoladamente.

## 6. Solicitar alteração fora do módulo

Registre no cartão/PR: área, arquivos, problema, alteração desejada, impacto e testes. Direcione ao responsável humano da tabela do AGENTS.md. Aguarde aprovação apenas para a parte dependente; continue testes e componentes independentes. A aprovação pode ocorrer de forma assíncrona e deve ser vinculada à tarefa.

A pessoa que autoriza define se fará a alteração ou se permite ao solicitante editar os arquivos indicados. Permissão para corrigir um contrato não autoriza refatorar o módulo inteiro. Se o contrato for incompatível, envolver os consumidores antes de integrar. Não manter uma cópia divergente como atalho.

## 7. Aceitar e integrar

O agente entrega relatório, testes e evidências; o integrante revisa o diff, confere critérios e reproduz o fluxo. Use revisão cruzada assíncrona para alterações de integração. Um PR deve indicar o incremento, comportamento, migrações, execução de testes e pendências. Só mover a Concluído após cumprir a definição de conclusão.

Integração é contínua: usar simulações contratuais temporárias quando necessário e substituí-las pelo provedor real. Executar QA-01 a QA-06 e REL-01/02 conforme a governança; os testes por pacote não substituem a validação dos cinco módulos juntos.

## 8. Controle sem reuniões

No cartão, atualizar: “entregue + evidência”, “próximo passo” e “bloqueio + responsável solicitado”. Ajustes de pontos são propostas documentadas e decididas pelo humano; não é necessário fazer votação. Pontos não se convertem automaticamente em horas nem garantem o prazo.

Até 08/11: base/stack/contratos. Até 12/11: primeiro fluxo por pacote. Até 15/11: primeiras integrações. Até 22/11: funcionalidades planejadas prontas e pendências explícitas. De 23 a 26/11: testes sistêmicos e correções. De 27 a 29/11: homologação/ensaio. Em 30/11: entrega. Se um marco falhar, registrar o impacto imediatamente e obter decisão escrita sobre escopo/prazo; não ocultar RF faltantes.

## 9. Checklist de início do integrante

- [ ] Meu nome e meu pacote estão definidos.
- [ ] Meus agentes receberam regras, governança, pacote e requisitos pertinentes.
- [ ] A stack, os caminhos autorizados e os contratos foram publicados.
- [ ] Meu cartão tem critérios de aceite, dependências e parâmetros suficientes.
- [ ] Estou trabalhando sobre a base comum, sem sobrescrever trabalho de terceiros.
- [ ] Sei quais testes e evidências comprovam a conclusão.

As convenções de nomes e formatos do AGENTS.md são decisões técnicas propostas para unificar a implementação; não se deve atribuí-las ao PDF como se fossem requisitos originais. Mudanças posteriores ficam versionadas e são comunicadas aos quatro integrantes.
