# ACA-02 — publicação e nova versão de matrizes

20/11/2026. Responsável: A/Dan. Entrega para revisão humana; não representa aceite final do incremento.

## Adições

Na ficha da matriz em elaboração, **Publicar matriz** abre uma tela para revisar a composição e confirmar. Publicar impede novas alterações nessa matriz. Cancelar não grava.

Na ficha publicada, **Criar nova versão** pede código, versão e vigência. Depois de revisar, **Confirmar e criar nova versão** salva uma cópia em elaboração, com outro identificador. A origem permanece intacta. Os componentes e pré-requisitos são copiados com as mesmas revisões históricas de disciplinas: mudanças posteriores no catálogo não atualizam esse conteúdo automaticamente.

O estado documentado VIGENTE representa uma matriz publicada. A operação `estaVigente(data)` também verifica o intervalo inclusivo de vigência: uma matriz publicada com início futuro ainda não se aplica à data atual. Publicar outra versão não encerra as anteriores. A seleção por matrícula pertence à etapa de matrículas.

Somente Coordenador pode executar as operações. Datas usam DD/MM/AAAA, com fim opcional e não anterior ao início. A versão continua sendo uma identificação textual; nenhuma regra nova de numeração foi presumida. A combinação curso/código/versão é única.

## Rodar com MySQL

Em C:\Users\Dan\SCOA, com Docker iniciado:

```powershell
docker compose up -d --wait mysql
.\executar-academico.ps1 -Acao Migrar
.\executar-academico.ps1 -Acao Abrir
```

A migração **V006__publicacao_e_origem_matriz.sql** registra origem e eventos de auditoria. As migrações anteriores são preservadas. Entre como Coordenador e consulte uma matriz em elaboração não vazia.

## Demonstração em memória

A demonstração existente de pré-requisitos também oferece as novas ações:

```powershell
.\executar-academico.ps1 -Acao Preparar
$javaMatrizes = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
& $javaMatrizes -cp "target\test-classes;target\classes" br.edu.scoa.academico.curriculo.DemonstracaoPreRequisitos
```

Consulte ADS-PRE / 2027.1. Essa demonstração usa identidade e persistência em memória; seus dados desaparecem ao fechar e não chegam ao MySQL.

## Homologação

| Ação | Resultado esperado |
|---|---|
| Abrir publicação e cancelar | Matriz continua em elaboração |
| Confirmar publicação | Ficha indica publicada; edição de dados, componentes e pré-requisitos fica bloqueada |
| Clicar duas vezes em confirmar | Apenas uma gravação |
| Criar nova versão com 31/02/2028 | Mensagem de data inválida; nada gravado |
| Usar mesmo código/versão da origem | Duplicidade recusada |
| Revisar cópia e cancelar | Não cria outra matriz |
| Criar versão 2028.1 e vigência válida | Nova matriz em elaboração com origem identificada e composição preservada |
| Editar a cópia | Original continua inalterada |
| Publicar a cópia | Ambas permanecem publicadas; vigências independentes |
| Alterar uma matriz em outra janela antes de publicar | Operação desatualizada recusada; consultar novamente |
| Fechar e reabrir usando MySQL | Publicação, origem e composição persistem |

## Limites

Calendário visual, reorganização de botões, apresentação de revisões e análise da numeração da versão continuam no [backlog de UX](ACA02_REFINAMENTOS_UX.md). Não houve implementação de período letivo, matrícula ou encerramento automático de matrizes nesta entrega. A homologação manual anterior não substitui a homologação destas novas ações.

## Validação técnica executada

Em 20/11/2026: 198 testes unitários/interface e 80 testes de integração MySQL aprovados, sem falhas ou testes ignorados. Incluem migração limpa e V005 → V006, autorização, limites de vigência, cópias concorrentes, disputa entre publicação e edição, histórico, coexistência de versões e rollback de publicação/cópia quando a auditoria falha. Tela de confirmação renderizada e inspecionada. Evidências em documentos/evidencias/aca02-publicacao. Homologação manual das novas telas pendente.
