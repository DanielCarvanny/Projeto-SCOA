# ACA-02 — refinamentos após homologação

Data: 16/11/2026. Responsável: A/Dan. Origem: observações de Dan nesta conversa.
Status: planejamento para implementação futura. Nenhuma alteração funcional nesta revisão; sem mudança de pontos, prazo ou aceite final.

Os identificadores abaixo organizam observações dentro do ACA-02; não são novos incrementos estimados.

## UX-01 — calendário integrado à digitação

Problema: datas exigem digitação e não oferecem escolha visual.

Comportamento solicitado:

- Oferecer calendário para início e fim de vigência e manter entrada por teclado.
- Aceitar DDMMYYYY e DD/MM/YYYY, exemplificados por 08032006 e 08/03/2006.
- Ao concluir uma entrada válida, selecionar e exibir no calendário o dia informado, mesmo que esteja em outro mês/ano.
- Selecionar uma data no calendário preenche o texto no formato DD/MM/AAAA.
- Recusar datas impossíveis, sem normalização silenciosa: 31/02/2027 não vira uma data de março.
- Distinguir digitação incompleta de erro confirmado: validar ao concluir o campo ou avançar, sem alertas a cada tecla.
- Manter fim opcional, permitir limpá-lo e sincronizar o calendário sem seleção residual.
- Continuar recusando fim anterior ao início. Não impor limite institucional de ano sem regra aprovada.

Testes futuros: ida e volta entre texto/calendário, anos bissextos, formato compacto, formatos inválidos, cancelamento do seletor, fim vazio e intervalo inconsistente. Usar LocalDate; nenhuma conversão de fuso para data civil.

Avaliação técnica: preservar Java/Swing. Avaliar componente reutilizável antes de implementar; não escolher biblioteca nem adicionar dependência compartilhada sem a autorização aplicável.

## UX-02 — significado e validação da versão curricular

Problema: campo livre aceita identificadores como 1029, que podem ser um engano de digitação.

Decisão de negócio pendente: versão curricular representa ano/semestre, sequência de versões ou outro identificador institucional? Atualmente é um identificador textual, não uma data; portanto 1029 não é comprovadamente inválido pela regra vigente.

Antes de implementar restrições, definir:

1. Formato esperado e exemplos válidos.
2. Se existe relação obrigatória com o início de vigência.
3. Tratamento das versões já cadastradas fora do formato futuro.

Proposta para avaliar: se representar ano/semestre, usar campos separados e gerar uma identificação como 2027.1. Caso seja uma sequência, usar rótulo e ajuda compatíveis com essa finalidade. Não converter nem bloquear registros antigos silenciosamente.

Critérios de aceite e testes de formato serão fechados após a definição. Permanecem válidas a obrigatoriedade e a unicidade por curso/código/versão já implementadas.

## UX-03 — tornar explícito o caminho para salvar

Problema: Revisar não comunica claramente que é o próximo passo para salvar e disputa atenção com ações de componentes.

Proposta de UX:

- Separar ações locais de componentes (adicionar, editar, remover) das ações da matriz.
- Colocar ações de navegação e conclusão em uma área inferior consistente.
- Usar “Continuar para salvar” na montagem e “Salvar matriz” na confirmação final.
- Exibir etapas e a indicação “Alterações ainda não salvas”.
- Destacar a ação principal com texto e contraste; manter Voltar como secundária e Cancelar separado.
- Preservar a revisão do conjunto antes da gravação, sem adicionar confirmações redundantes.
- Não depender exclusivamente da cor; garantir acesso por teclado, foco visível e leitura em diferentes tamanhos de janela.

Aceite proposto: usuário identifica como concluir sem explorar todos os botões; nenhuma gravação ocorre ao montar/revisar; cancelar não salva; o resultado informa claramente o sucesso. Testar posição/visibilidade dos controles, navegação, cancelamento e duplo clique.

Aplicação futura nas demais telas deve respeitar o escopo autorizado e preservar o fluxo separado por funcionalidade.

## UX-04 — explicar a atualização de conteúdo da disciplina

Função atual: cada componente guarda a revisão da disciplina utilizada. Uma alteração posterior no catálogo não modifica silenciosamente a matriz. O botão atual permite escolher explicitamente a revisão mais recente, apenas enquanto a matriz está em elaboração; a alteração só é persistida ao salvar a matriz.

Exemplo: a matriz referencia uma ementa “Classes e objetos”. O catálogo recebe “Classes, objetos e interfaces”. Sem atualizar o componente, a matriz mantém a primeira ementa. Ao atualizar e salvar, passa a referenciar a segunda; os históricos anteriores permanecem preservados.

Problema de UX: o rótulo usa um conceito técnico sem mostrar o benefício ou a diferença, e a ação fica disponível mesmo quando não há novidade.

Proposta:

- Mostrar “Conteúdo da disciplina atualizado” quando a referência já for a atual.
- Somente oferecer “Comparar atualização da disciplina” quando houver revisão mais recente.
- Apresentar lado a lado nome, ementa, carga do catálogo e situação anterior/atual; destacar diferenças.
- Distinguir mudança de conteúdo de uma revisão gerada somente por inativação.
- Confirmar com “Aplicar conteúdo atualizado à matriz”, mantendo a gravação final explícita.
- Informar que a carga considerada na matriz não é substituída automaticamente pela carga do catálogo.
- Se não houver mais diferença ao confirmar, atualizar a apresentação sem criar uma alteração enganosa.

Testes futuros: ausência de novidade, comparação, cancelamento, aplicação explícita, preservação de carga, histórico e recusa de edição estrutural em matriz publicada.

## Ordem recomendada pelo PO

1. UX-03: clareza da gravação, para reduzir abandono ou perda de trabalho.
2. UX-04: compreensão da atualização, para evitar mudanças involuntárias de conteúdo.
3. UX-01: calendário e entrada flexível de datas.
4. UX-02: implementação após definir o significado institucional da versão.

Esta ordem é proposta para o refinamento; não altera a prioridade formal, estimativa ou prazo do backlog.
