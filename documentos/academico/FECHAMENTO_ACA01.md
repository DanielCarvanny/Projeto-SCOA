> Atualização de responsabilidade — 13/11/2026: por determinação explícita de Dan nesta conversa, Pessoa, conta, login, sessão e permissões do Acadêmico são implementados por A. A atribuição anterior dessa identidade a D e a espera pela Fundação estão superadas para este escopo. D mantém as demais responsabilidades de infraestrutura; contratos consumidos por outros módulos continuam sujeitos ao acordo com seus responsáveis. Não há alteração de pontos nem aceite automático do incremento.

# Fechamento do ACA-01 — checklist, casos de uso e dependências

Data: 12/11/2026. Responsável acadêmico: Dan/A. Estado: implementação local validada; fechamento integrado bloqueado pela Fundação. Não é aceite final nem autorização para publicar.

## Execução do plano

| Etapa | Resultado nesta revisão |
|---|---|
| 1. Critérios e casos de uso | Revisados; divergências documentadas abaixo. Regra de CPF e relato manual registrados. |
| 2. Disponibilidade da Fundação | Não há implementação em src/main; Dan confirmou nesta conversa que ainda não foi entregue em outro local. |
| 3. Integração dos casos de uso | Bloqueada até entrega do provedor e contratos de B/D. |
| 4. Interface persistente com sessão | Bloqueada pela mesma dependência; demonstração continua em memória. |
| 5. Testes | Regressão local executada nesta revisão; resultado anexado às evidências. Contratos contra provedor real continuam pendentes. |
| 6. Homologação integrada | Roteiro preparado; não executada. O relato manual anterior refere-se à demonstração. |
| 7. Versão completa | Não publicar nem declarar conclusão antes dos itens bloqueados. Sem commit/push nesta revisão. |

## Checklist de aceite

| Critério | Evidência/estado | Condição restante |
|---|---|---|
| Criar e reutilizar Pessoa pelo CPF | Fluxos explícitos implementados e testados com dublê | Fundação persistente, unicidade concorrente e política de recuperação |
| Cadastrar/consultar/editar aluno | Aplicação, Swing e repositório MySQL implementados | Fluxo integrado com sessão/Pessoa reais |
| Cadastrar/consultar/editar professor | Implementado; catálogo de seis títulos aprovado na conversa | Mesmo fluxo integrado |
| Recusar vínculo duplicado | UNIQUE e testes MySQL | Confirmar comportamento pela aplicação integrada |
| CPF/ID na consulta do aluno | Implementado, IDs de pessoa e aluno distintos | Provedor institucional real |
| Dados pessoais compartilhados | Implementado no consumidor e demonstração | Persistência e auditoria institucionais |
| Situações e títulos independentes | Testes locais | Homologação integrada |
| Autorização e identidade do autor | Portas testadas; operador de demonstração fixo | Sessão confiável e políticas de D/B |
| Auditoria acadêmica | Transação junto com vínculo no MySQL | Vínculo do autor com conta institucional e política aprovada |
| Migrações | V001/V002 acadêmicas; dependem de pessoa(id INT) | Migrações institucionais e instalação/atualização integrada |
| Persistir após fechar/reabrir interface | Repositórios persistem; tela atual usa memória | Composição persistente de D e teste pela interface |
| Documentação consistente | Revisão aponta divergências UC01/UC07 | Decisão de responsáveis e atualização da fonte documental |

Regra de CPF vigente por decisão de Dan: aceitar 11 números ou XXX.XXX.XXX-XX, normalizar pontuação e rejeitar sequências repetidas; não calcular dígitos verificadores. 12345678901 é aceito por essa regra de formato. Isso não é comprovação de validade cadastral/titularidade. A mudança não remove unicidade do CPF.

Dan informou que executou testes manuais, encontrou e alterou a regra de CPF. As demais funcionalidades foram relatadas como funcionando. Não registrar esse relato como homologação de autenticação ou persistência pela tela, que continuam simuladas.

## Revisão visual dos casos de uso

Fonte: [PDF original](../requisitos/Trabalho%20AMS%202026.pdf), Figura 1 na página 55, UC01 na 56 e UC07 na 59. Páginas renderizadas e inspecionadas, não somente extração textual. O PDF original não foi alterado.

| Achado | Evidência | Avaliação e proposta |
|---|---|---|
| Estrutura gráfica básica presente | Atores externos, elipses e fronteira Módulo Acadêmico | A apresentação identifica a fronteira e os participantes. Isso não basta para validar a semântica dos relacionamentos. |
| Login inclui cadastro | Seta tracejada include parte de Fazer Login para Cadastrar Usuário Acadêmico; UC01 também lista esse subcaso apesar de exigir usuário já cadastrado | Incompatível com login de usuário existente como fluxo normal. Propor login independente e sessão autenticada como pré-condição das operações protegidas. Não inverter a seta automaticamente. |
| Herança dos atores | Professor/Coordenador apontam para Aluno na generalização | Sugere herança de todas as participações de Aluno; não constitui uma matriz de permissões segura. Propor ator genérico Usuário autenticado, com papéis especializados, ou associações explícitas. Não presumir que todo professor é aluno. |
| Cadastro acadêmico e conta misturados | UC07 cria registro de professor/aluno/coordenador, atribui login/senha e perfil; ator indicado é Aluno | Conflita com separação atual Pessoa/conta em Fundação e vínculos em Acadêmico. Definir operador autorizado; separar manter vínculos de provisionar conta. Credenciais não podem ser geradas implicitamente pelo Acadêmico. |
| Matrícula solicitada antes do vínculo com curso | UC07 pede matrícula no cadastro; modelo da página 141 reserva o número a MatriculaCurso, detalhada na 144 | Resolver divergência documental. Não criar matrícula no cadastro de Aluno para satisfazer esse campo do UC07. |
| Cobertura incompleta do ACA-01 | Figura destaca cadastrar usuário; consulta/edição de aluno e professor não aparecem de forma inequívoca | Propor manter Alunos e manter Professores, com fluxos de cadastrar/consultar/editar e rastreabilidade RF-01/RF-02. Não exigir um caso por botão. |
| Fluxos alternativos e exceções ausentes no UC07 | Campos Branch Sequence e Exception Sequence vazios | Documentar pessoa nova/existente, duplicidade, campo inválido, acesso negado, indisponibilidade e falha parcial. |
| Linhas entre login e outras funções | Ligações simples para disciplina, turma, documentos e relatórios | Não interpretar como navegação de telas ou sequência temporal. Definir associações aos atores ou relações entre casos com semântica explícita. |
| Extensões de matrícula e sala | Processar rematrícula e Cadastrar sala ligados por extend | Necessitam condições/pontos de extensão para justificar o comportamento; fora do fechamento estrito do ACA-01, registrar para seus incrementos. |

Conclusão: o desenho tem formato visual de DCU, mas não está suficientemente coerente com o ACA-01 e a separação institucional para servir sozinho como critério de aceite. A/B/D devem decidir as divergências; nenhuma regra foi modificada unilateralmente.

### Especificação proposta para revisão (não substitui UC07 sem aprovação)

- Objetivos: manter vínculo de aluno (RF-01) e manter vínculo de professor (RF-02).
- Ator: operador com permissão específica; o perfil institucional exato será definido por D/B com A.
- Pré-condições: sessão válida, permissão na operação e provedor institucional disponível.
- Cadastro: escolher nova pessoa ou existente; validar entrada; obter/criar identidade institucional pelo contrato; registrar vínculo acadêmico e auditoria; confirmar resultado.
- Consulta: localizar por identificador acadêmico; em Aluno também por CPF; autorizar antes de devolver a ficha.
- Edição: separar alteração acadêmica da atualização de dados pessoais compartilhados. CPF e pessoaId permanecem imutáveis nesse fluxo.
- Pós-condição: vínculo persistido, identidade única reutilizável e auditoria consistente. Conta/login não são pós-condições automáticas do cadastro acadêmico.
- Alternativas: CPF inexistente no caminho existente não abre criação automática; pessoa encontrada no caminho novo solicita voltar; conflito de edição exige reconsulta.
- Exceções: entrada inválida, vínculo duplicado, pessoa ausente, não autorizado, indisponibilidade e falha de gravação. Falha não representa sucesso.

## O que D precisa implementar na Fundação

1. **Pessoa institucional persistente.** Identificador único, CPF normalizado e único, nome, e-mail, telefone. Migração versionada e compatibilidade com pessoa(id INT) usada pelas FKs acadêmicas, ou adaptação coordenada. Criação concorrente não duplica CPF nem sobrescreve o cadastro de outro operador.
2. **Serviço público de Pessoa.** Consultar por CPF/ID; cadastrar ou obter a identidade; atualizar nome/e-mail/telefone preservando CPF/ID, comparando versão ou retrato esperado atomicamente para detectar conflito. Não expor repositórios internos aos outros módulos.
3. **Conta e autenticação.** Conta vinculada à Pessoa, login único, hash de senha com mecanismo aprovado da stack, situação da conta e verificação das credenciais. Não reutilizar a senha do MySQL como senha institucional. Definir provisionamento de conta e perfis com os responsáveis; sem cadastro autônomo de privilégios.
4. **Sessão e autorização.** Fornecer usuarioId autenticado de fonte confiável, encerramento de sessão e política de validade. Verificar cadastro/consulta/edição de aluno e professor e edição de Pessoa por perfil e âmbito. Não confiar em botão oculto, CPF informado ou ID fixo da demonstração.
5. **Auditoria institucional.** Registrar criação/edição de Pessoa e eventos definidos da conta com autor, operação, entidade e instante UTC, sem senhas ou CPF integral em logs. Acordar com A o vínculo da auditoria acadêmica ao autor institucional.
6. **Composição persistente.** Inicializar sessão, serviços institucionais e repositórios acadêmicos reais; configurar banco por ambiente. Criar credenciais sintéticas de homologação por procedimento local, sem versionar segredos. Separar demonstração em memória do modo persistente.
7. **Migrações e instalação.** Publicar ordem de instalação (Pessoa antes das FKs acadêmicas), comando de inicialização e atualização. Não editar migrações já compartilhadas; testar instalação limpa e evolução preservando dados.

## O que B deve acordar com D e A

- Assinaturas, IDs, campos e erros dos contratos. Portas consumidoras atuais: PessoasPorCpf, DadosPessoaisInstitucionais, IdentidadeInstitucional, AcessoAlunos e AcessoProfessores.
- Semântica de consultar/criar/atualizar Pessoa e permissões de cada operação, inclusive consulta por CPF sem exigir permissão de cadastro por engano.
- Unicidade concorrente: cadastrarOuObter atualmente pode reutilizar Pessoa criada entre a consulta e a escrita. Definir se a resposta deve distinguir criada/existente e como a tela reage.
- Recuperação da falha parcial: Pessoa criada, vínculo recusado. Proposta atual: preservar Pessoa e repetir pelo caminho existente; não apagar identidade institucional.
- Sessão, identidade do autor e contrato de erro: ausência, duplicidade, acesso negado, conflito, entrada inválida e indisponibilidade não podem se confundir.
- Resolver UC01/UC07 com A e o responsável documental. A aprovação de contrato não é inferida deste relatório.

## Solicitação preparada para encaminhamento (não enviada)

Incremento: ACA-01 | Área: Fundação/identidade/autenticação/composição (D) e contratos (B com D/A) | Motivo: fechar o fluxo real de aluno/professor | Impacto: elimina dublês institucionais do modo persistente, preservando pessoaId e histórico | Alteração proposta: entregar os sete itens de Fundação acima e validar as cinco portas consumidoras; informar branch, caminhos, comandos e ordem de migrações | Testes: unicidade concorrente, autorização, conflito, auditoria, instalação/atualização e fluxo Swing–serviço–MySQL | Decisão: pendente. Dan confirmou em 12/11/2026 que a Fundação ainda não foi entregue.

## Roteiro de homologação integrada — executar após a entrega

Registrar versão/commit, ambiente de teste, operador sintético, data, esperado/observado e evidência por cenário. Não registrar senhas.

| Cenário | Aceite |
|---|---|
| Entrar/sair | Sessão real; credencial inválida recusada; sessão encerrada não autoriza operação |
| Pessoa nova + aluno | Uma identidade e um vínculo; autoria registrada |
| Pessoa existente + professor | Mesma pessoaId; nenhum dado pessoal sobrescrito |
| Duplicidade e concorrência | Um vínculo por pessoa/tipo; CPF institucional único |
| Consulta por CPF/ID | Mesmo aluno; pessoa sem vínculo distinguível de pessoa ausente |
| Editar contato | Reconsulta do outro vínculo reflete alteração; CPF/ID preservados |
| Editar situação/título | Somente vínculo específico muda; histórico anterior preservado |
| Edição simultânea | Atualização desatualizada recusada, sem perda silenciosa |
| Operador sem permissão | Recusa na aplicação mesmo fora do botão da tela |
| Falha da Fundação/banco | Sem confirmação indevida; repetição segura e recuperação explícita |
| Fechar e reabrir | Registros e alterações persistem na aplicação real |
| Instalar/atualizar | Migrações ordenadas, sem perda dos registros preexistentes |

## Condição de publicação

Não subir como incremento completo enquanto houver item integrado pendente. Após a entrega de B/D: adaptar consumidores, executar testes reais, concluir homologação, resolver divergências documentais, revisar diff/segredos e organizar commits. Publicação e aceite somente após aprovação humana da versão concreta. Nenhum push/PR foi realizado.

## Resultado de testes desta revisão

143 testes unitários/interface e 23 MySQL aprovados em 12/11/2026, sem falhas ou ignorados. A primeira tentativa de integração falhou por Docker desligado; após iniciar Docker e o serviço de testes, a execução final passou. Evidências em [ACA-01-FECHAMENTO](../evidencias/ACA-01-FECHAMENTO/). Isso comprova persistência acadêmica com fixture, não integração institucional real. Nenhum código, contrato compartilhado ou migração foi alterado nesta revisão.
