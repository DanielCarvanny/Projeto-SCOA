# ACA-05 — Documentos acadêmicos

Responsável: A/Dan. Status: implementação iniciada em 23/11/2026.

O incremento separa tipos de documento, metadados e armazenamento físico. Arquivos ficam fora de `src`, `target` e diretórios públicos, em caminho definido por `SCOA_DOCUMENTOS_DIR`; UUID é o nome físico e o nome original é apenas metadado. São aceitos inicialmente PDF, PNG e JPEG, até 10 MB.

Estados: `PENDENTE`, `APROVADO`, `RECUSADO`. Coordenador administra tipos e decide; Aluno somente envia/consulta seus próprios registros. Recusa exige justificativa. Todo envio ou julgamento recebe auditoria, revisão técnica e UUID de operação. Nova tentativa após recusa cria outro registro; não altera o aprovado.

O fluxo será: Tipo de documento → Enviar → Revisar → Confirmar; e Pendências → Ficha → Aprovar/Recusar. Persistência, arquivo e auditoria participam de compensação para não deixar arquivo órfão se a gravação falhar.
