package br.edu.scoa.academico.infraestrutura.persistencia;

import br.edu.scoa.academico.aplicacao.avaliacoes.AutorizacaoAvaliacoes;
import br.edu.scoa.academico.aplicacao.avaliacoes.GestaoAvaliacoes;
import br.edu.scoa.academico.aplicacao.boletins.FalhaBoletimAluno;
import br.edu.scoa.academico.aplicacao.boletins.GestaoBoletimAluno;
import br.edu.scoa.academico.aplicacao.diarios.AutorizacaoAuditoriaDiario;
import br.edu.scoa.academico.aplicacao.diarios.ConsultaDiarioAulas;
import br.edu.scoa.academico.aplicacao.diarios.GestaoAuditoriaDiario;
import br.edu.scoa.academico.aplicacao.diarios.ResumoDiarioAulas;
import br.edu.scoa.academico.dominio.avaliacoes.AvaliacaoAcademica;
import br.edu.scoa.academico.dominio.avaliacoes.FalhaAvaliacao;
import br.edu.scoa.academico.dominio.avaliacoes.Nota;
import br.edu.scoa.academico.dominio.avaliacoes.TipoAvaliacao;
import br.edu.scoa.academico.dominio.avaliacoes.SituacaoResultadoFinal;
import br.edu.scoa.academico.dominio.matriculas.SituacaoInscricao;
import br.edu.scoa.academico.dominio.diarios.EstadoPautaFinal;
import br.edu.scoa.academico.dominio.diarios.FalhaAuditoriaDiario;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;
import br.edu.scoa.academico.dominio.professores.SituacaoFuncional;
import br.edu.scoa.academico.dominio.alunos.SituacaoCadastral;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.Instant;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import br.edu.scoa.academico.dominio.avaliacoes.RegistroNota;
import br.edu.scoa.academico.dominio.avaliacoes.ResultadoExameFinal;
import static org.junit.jupiter.api.Assertions.*;

/** Valida a atualização da base oficial V011 e os fluxos ACA-07 a ACA-09. */
class FluxosAcademicosMysqlIT {
    private static ConfiguracaoBanco banco;
    @BeforeAll static void preparar() {
        var ambiente = System.getenv();
        Assumptions.assumeTrue("CONFIRMO_BANCO_DESCARTAVEL".equals(ambiente.get("SCOA_TEST_CONFIRM")));
        assertEquals("jdbc:mysql://127.0.0.1:3308/scoa_test", ambiente.get("SCOA_TEST_URL"));
        assertEquals("scoa_test", ambiente.get("SCOA_TEST_USER"));
        banco = ConfiguracaoBanco.doAmbiente(ambiente, "SCOA_TEST_");
        MigracoesIdentidade.executar(banco);
        var flyway = MigracoesAcademico.configurar(banco);
        if (flyway.info().applied().length == 0) MigracoesAplicacao.inicializarHistoricoAcademico(banco);
        org.flywaydb.core.Flyway.configure().configuration(flyway.getConfiguration()).target("11").load().migrate();
        assertEquals("011", flyway.info().current().getVersion().getVersion());
        flyway.migrate();
        assertEquals("015", flyway.info().current().getVersion().getVersion());
    }

    @Test void avaliacoesNotasAutorizacaoAuditoriaIdempotenciaElegibilidadeEResultadoFinal() throws Exception {
        long[] ids = new long[15];
        long autorProfessor = 910_000_101L;
        long autorCoordenador = 910_000_102L;
        Instant agora = Instant.parse("2026-11-24T12:00:00Z");
        var clock = Clock.fixed(agora, ZoneId.of("America/Sao_Paulo"));
        var autorizacao = new AutorizacaoAvaliacoesTeste(ids, autorProfessor, autorCoordenador);
        RepositorioAvaliacoesMysql repositorio = new RepositorioAvaliacoesMysql(banco);
        var gestao = new GestaoAvaliacoes<>(autorizacao, repositorio,
                new RepositorioTurmasMysql(banco), clock);
        long provaUmId = 0, provaDoisId = 0, exameId = 0;
        try {
            prepararFixtureAvaliacoes(banco, ids, autorCoordenador, autorProfessor);
            LocalDate dataAplicacao = LocalDate.ofInstant(agora, ZoneId.of("America/Sao_Paulo")).plusDays(7);
            var provaUm = new AvaliacaoAcademica(0, ids[8], "Prova 1", "Teste de integração", dataAplicacao, TipoAvaliacao.PROVA, 0);
            UUID operacaoProvaUm = UUID.randomUUID();
            var provaUmSalva = gestao.cadastrar("professor", provaUm, operacaoProvaUm); provaUmId = provaUmSalva.id();
            assertEquals(provaUmId, gestao.cadastrar("professor", provaUm, operacaoProvaUm).id(), "Repetição do cadastro retorna a mesma avaliação.");
            assertEquals(1, contarAvaliacoesAuditadas(banco, provaUmId));

            var provaDois = new AvaliacaoAcademica(0, ids[8], "Prova 2", "Teste de integração", dataAplicacao, TipoAvaliacao.PROVA, 0);
            provaDoisId = gestao.cadastrar("professor", provaDois, UUID.randomUUID()).id();
            assertThrows(FalhaAvaliacao.class, () -> gestao.cadastrar("outro-professor", provaUm, UUID.randomUUID()));
            assertThrows(FalhaAvaliacao.class, () -> gestao.cadastrar("aluno", provaUm, UUID.randomUUID()));
            assertEquals(4, gestao.diario("professor", ids[8]).size(), "O diário lista duas provas para cada uma das duas inscrições ativas.");
            assertTrue(gestao.minhasNotas("aluno").stream().allMatch(registro -> registro.alunoId() == ids[2]),
                    "A consulta do aluno não pode incluir a matrícula de outro aluno da mesma turma.");

            var exame = new AvaliacaoAcademica(0, ids[8], "Exame Final", "Teste de integração", dataAplicacao, TipoAvaliacao.EXAME_FINAL, 0);
            exameId = gestao.cadastrar("coordenador", exame, UUID.randomUUID()).id();
            final long avaliacaoExameId = exameId;
            var linhas = gestao.diario("professor", ids[8]);
            var linhaUm = localizar(linhas, provaUmId, ids[10]);
            assertEquals(FalhaAvaliacao.Codigo.NOTAS_PROVAS_PENDENTES,
                    assertThrows(FalhaAvaliacao.class, () -> gestao.lancarNota("professor", avaliacaoExameId, ids[10],
                            new Nota(new BigDecimal("5.0")), 0, UUID.randomUUID())).codigo());

            UUID operacaoNotaUm = UUID.randomUUID();
            var notaUm = gestao.lancarNota("professor", provaUmId, ids[10], new Nota(new BigDecimal("4.0")),
                    linhaUm.revisaoNota(), operacaoNotaUm);
            assertEquals(notaUm.revisaoNota(), gestao.lancarNota("professor", provaUmId, ids[10],
                    new Nota(new BigDecimal("4.0")), linhaUm.revisaoNota(), operacaoNotaUm).revisaoNota(),
                    "A mesma operação de nota não cria nova revisão.");
            assertEquals(1, contarAuditoriaNota(banco, provaUmId, ids[10]));
            var linhaDois = localizar(gestao.diario("professor", ids[8]), provaDoisId, ids[10]);
            var notaDois = gestao.lancarNota("professor", provaDoisId, ids[10], new Nota(new BigDecimal("4.0")),
                    linhaDois.revisaoNota(), UUID.randomUUID());
            assertEquals(Optional.of(false), gestao.elegivelParaExame(gestao.diario("professor", ids[8]).stream()
                    .filter(registro -> registro.inscricaoId() == ids[10]).toList()));
            assertEquals(FalhaAvaliacao.Codigo.ALUNO_NAO_ELEGIVEL_EXAME,
                    assertThrows(FalhaAvaliacao.class, () -> gestao.lancarNota("professor", avaliacaoExameId, ids[10],
                            new Nota(new BigDecimal("5.0")), 0, UUID.randomUUID())).codigo());

            var segundaNotaCorrigida = gestao.lancarNota("coordenador", provaDoisId, ids[10], new Nota(new BigDecimal("6.0")),
                    notaDois.revisaoNota(), UUID.randomUUID());
            var notasTurma = gestao.diario("professor", ids[8]);
            var minhasNotas = gestao.minhasNotas("aluno");
            assertEquals(Optional.of(true), gestao.elegivelParaExame(notasTurma.stream()
                    .filter(registro -> registro.inscricaoId() == ids[10]).toList()));
            var notaExame = gestao.lancarNota("professor", avaliacaoExameId, ids[10], new Nota(new BigDecimal("4.9")), 0, UUID.randomUUID());
            List<RegistroNota> registrosAluno = gestao.minhasNotas("aluno");
            Optional<ResultadoExameFinal> resultadoAprovado = gestao.resultadoAposExame(registrosAluno.stream()
                    .filter(registro -> registro.inscricaoId() == ids[10]).toList());
            assertEquals(new BigDecimal("5.0"), resultadoAprovado.orElseThrow().mediaFinal().valor(), "MP=5,0 e EF=4,9 dão 4,95, arredondado HALF_UP para 5,0.");
            assertTrue(resultadoAprovado.orElseThrow().aprovado());

            gestao.lancarNota("coordenador", avaliacaoExameId, ids[10], new Nota(new BigDecimal("4.8")),
                    notaExame.revisaoNota(), UUID.randomUUID());
            var resultadoReprovado = gestao.resultadoAposExame(gestao.minhasNotas("aluno").stream()
                    .filter(registro -> registro.inscricaoId() == ids[10]).toList()).orElseThrow();
            assertEquals(new BigDecimal("4.9"), resultadoReprovado.mediaFinal().valor());
            assertFalse(resultadoReprovado.aprovado());
            assertTrue(contarAuditoriaNota(banco, avaliacaoExameId, ids[10]) >= 2, "Correções do exame devem preservar a auditoria.");
            assertEquals(1, contarAvaliacoesAuditadas(banco, provaUmId));
            assertEquals(1, contarAuditoriaNota(banco, provaUmId, ids[10]));
            var tentativaTipo = new AvaliacaoAcademica(provaUmSalva.id(), provaUmSalva.turmaId(), provaUmSalva.titulo(),
                    provaUmSalva.descricao(), provaUmSalva.dataAplicacao(), TipoAvaliacao.EXAME_FINAL, provaUmSalva.revisao());
            assertEquals(FalhaAvaliacao.Codigo.TIPO_AVALIACAO_COM_NOTAS,
                    assertThrows(FalhaAvaliacao.class, () -> gestao.atualizar("coordenador", tentativaTipo, UUID.randomUUID())).codigo());
            assertTrue(minhasNotas.stream().noneMatch(registro -> registro.alunoId() == ids[12]));
            assertEquals(segundaNotaCorrigida.revisaoNota(), localizar(gestao.diario("coordenador", ids[8]), provaDoisId, ids[10]).revisaoNota());
        } finally {
            limparFixtureAvaliacoes(banco, ids, provaUmId, provaDoisId, exameId);
        }
    }

    @Test void pautaFinalBloqueiaAlteracoesExigeDiarioCompletoEPermiteReabertura() throws Exception {
        long[] ids = new long[15];
        long autorCoordenador = 910_000_202L;
        long autorProfessor = 910_000_201L;
        var repositorioAvaliacoes = new RepositorioAvaliacoesMysql(banco);
        var repositorioPautas = new RepositorioPautasFinaisMysql(banco);
        var turmas = new RepositorioTurmasMysql(banco);
        var autorizacaoAvaliacoes = new AutorizacaoAvaliacoesTeste(ids, autorProfessor, autorCoordenador);
        var gestaoAvaliacoes = new GestaoAvaliacoes<>(autorizacaoAvaliacoes, repositorioAvaliacoes, turmas,
                Clock.fixed(Instant.parse("2026-11-24T12:00:00Z"), ZoneOffset.UTC));
        AutorizacaoAuditoriaDiario<String> autorizacaoAuditoria = new AutorizacaoAuditoriaDiario<>() {
            public long usuarioId(String sessao) {
                if ("coordenador".equals(sessao)) return autorCoordenador;
                if ("professor".equals(sessao)) return autorProfessor;
                throw new FalhaAuditoriaDiario(FalhaAuditoriaDiario.Codigo.NAO_AUTORIZADO);
            }
            public boolean ehCoordenador(String sessao) { return "coordenador".equals(sessao); }
        };
        var pendenciasAulas = new AtomicReference<List<br.edu.scoa.academico.aplicacao.diarios.PendenciaDiarioAula>>(
                List.of(new br.edu.scoa.academico.aplicacao.diarios.PendenciaDiarioAula("FREQUENCIA", "Frequência obrigatória pendente.")));
        ConsultaDiarioAulas diarioCompleto = turmaId -> new ResumoDiarioAulas(turmaId,
                repositorioPautas.versaoDiario(turmaId), pendenciasAulas.get());
        var gestaoAuditoria = new GestaoAuditoriaDiario<>(autorizacaoAuditoria, repositorioPautas,
                repositorioAvaliacoes, turmas, diarioCompleto,
                Clock.fixed(Instant.parse("2026-11-24T12:00:00Z"), ZoneOffset.UTC), gestaoAvaliacoes);
        long provaId = 0;
        long exameId = 0;
        try {
            prepararFixtureAvaliacoes(banco, ids, autorCoordenador, autorProfessor);
            assertThrows(FalhaAuditoriaDiario.class, () -> gestaoAuditoria.turmas("professor"));
            var prova = new AvaliacaoAcademica(0, ids[8], "Prova para homologação", "Fixture ACA-08",
                    LocalDate.of(2026, 12, 1), TipoAvaliacao.PROVA, 0);
            provaId = gestaoAvaliacoes.cadastrar("professor", prova, UUID.randomUUID()).id();
            var pauta = gestaoAuditoria.registrarAnalise("coordenador", ids[8], "Conferência inicial", UUID.randomUUID());
            var resumoInicial = gestaoAuditoria.consultar("coordenador", ids[8]);
            assertTrue(resumoInicial.pendencias().stream().anyMatch(p -> p.codigo() == br.edu.scoa.academico.dominio.diarios.CodigoPendenciaPauta.NOTAS_PROVAS_PENDENTES));
            assertTrue(resumoInicial.pendencias().stream().anyMatch(p -> p.codigo() == br.edu.scoa.academico.dominio.diarios.CodigoPendenciaPauta.DIARIO_AULAS_PENDENTE));
            final long revisaoPautaInicial = pauta.revisao();
            assertEquals(FalhaAuditoriaDiario.Codigo.PENDENCIAS_PARA_HOMOLOGAR,
                    assertThrows(FalhaAuditoriaDiario.class, () -> gestaoAuditoria.homologar("coordenador", ids[8], revisaoPautaInicial, UUID.randomUUID())).codigo());
            for (long inscricaoId : new long[]{ids[10], ids[14]}) {
                var registro = localizar(gestaoAvaliacoes.diario("professor", ids[8]), provaId, inscricaoId);
                String valor = inscricaoId == ids[10] ? "6.0" : "4.0";
                gestaoAvaliacoes.lancarNota("professor", provaId, inscricaoId,
                        new Nota(new BigDecimal(valor)), registro.revisaoNota(), UUID.randomUUID());
            }
            assertTrue(gestaoAuditoria.consultar("coordenador", ids[8]).pendencias().stream()
                    .anyMatch(p -> p.codigo() == br.edu.scoa.academico.dominio.diarios.CodigoPendenciaPauta.EXAME_FINAL_NAO_CADASTRADO));
            var exame = new AvaliacaoAcademica(0, ids[8], "Exame final", "Fixture ACA-08",
                    LocalDate.of(2026, 12, 1), TipoAvaliacao.EXAME_FINAL, 0);
            exameId = gestaoAvaliacoes.cadastrar("coordenador", exame, UUID.randomUUID()).id();
            var registroExame = localizar(gestaoAvaliacoes.diario("professor", ids[8]), exameId, ids[10]);
            gestaoAvaliacoes.lancarNota("professor", exameId, ids[10], new Nota(new BigDecimal("5.0")),
                    registroExame.revisaoNota(), UUID.randomUUID());
            pendenciasAulas.set(List.of());
            var resumo = gestaoAuditoria.consultar("coordenador", ids[8]);
            assertTrue(resumo.pendencias().isEmpty(), "Provas e diário obrigatórios completos permitem análise.");
            pauta = gestaoAuditoria.homologar("coordenador", ids[8], pauta.revisao(), UUID.randomUUID());
            assertEquals(EstadoPautaFinal.HOMOLOGADA, pauta.estado());
            assertEquals(2, gestaoAuditoria.consultar("coordenador", ids[8]).historicoPauta().size());
            final long avaliacaoHomologadaId = provaId;
            var registroHomologado = localizar(gestaoAvaliacoes.diario("professor", ids[8]), provaId, ids[10]);
            var falhaAlteracao = assertThrows(FalhaAvaliacao.class, () -> gestaoAvaliacoes.lancarNota("professor",
                    avaliacaoHomologadaId, ids[10], new Nota(new BigDecimal("6.0")), registroHomologado.revisaoNota(), UUID.randomUUID()));
            assertEquals(FalhaAvaliacao.Codigo.PAUTA_HOMOLOGADA, falhaAlteracao.codigo());
            var provaHomologada = gestaoAvaliacoes.porTurma("coordenador", ids[8]).stream()
                    .filter(avaliacao -> avaliacao.id() == avaliacaoHomologadaId).findFirst().orElseThrow();
            assertEquals(FalhaAvaliacao.Codigo.PAUTA_HOMOLOGADA,
                    assertThrows(FalhaAvaliacao.class, () -> gestaoAvaliacoes.atualizar("coordenador",
                            new AvaliacaoAcademica(provaHomologada.id(), provaHomologada.turmaId(),
                                    "Prova editada", provaHomologada.descricao(), provaHomologada.dataAplicacao(),
                                    provaHomologada.tipo(), provaHomologada.revisao()), UUID.randomUUID())).codigo());
            pauta = gestaoAuditoria.reabrir("coordenador", ids[8], pauta.revisao(), "Correção autorizada", UUID.randomUUID());
            assertEquals(EstadoPautaFinal.ABERTA, pauta.estado());
            var registroReaberto = localizar(gestaoAvaliacoes.diario("professor", ids[8]), provaId, ids[10]);
            gestaoAvaliacoes.lancarNota("professor", provaId, ids[10], new Nota(new BigDecimal("6.0")),
                    registroReaberto.revisaoNota(), UUID.randomUUID());
            assertEquals(3, gestaoAuditoria.consultar("coordenador", ids[8]).historicoPauta().size());
        } finally {
            limparFixtureAvaliacoes(banco, ids, provaId, exameId);
        }
    }

    @Test void boletimHistoricoEHorarioConsultamSomenteAlunoDaSessao() throws Exception {
        long[] ids = new long[15];
        long autorCoordenador = 910_000_302L;
        long autorProfessor = 910_000_301L;
        long avaliacaoId = 0;
        var autorizacao = new AutorizacaoAvaliacoesTeste(ids, autorProfessor, autorCoordenador);
        var repositorioAvaliacoes = new RepositorioAvaliacoesMysql(banco);
        var repositorioTurmas = new RepositorioTurmasMysql(banco);
        var relogio = Clock.fixed(Instant.parse("2026-11-24T12:00:00Z"), ZoneOffset.UTC);
        var gestaoAvaliacoes = new GestaoAvaliacoes<>(autorizacao, repositorioAvaliacoes, repositorioTurmas, relogio);
        var gestaoBoletim = new GestaoBoletimAluno<>(autorizacao, gestaoAvaliacoes, repositorioAvaliacoes,
                new RepositorioMatriculasMysql(banco), new RepositorioInscricoesMysql(banco), repositorioTurmas,
                new RepositorioMatrizesMysql(banco), new RepositorioCatalogoMysql(banco), new RepositorioPeriodosMysql(banco));
        try {
            prepararFixtureAvaliacoes(banco, ids, autorCoordenador, autorProfessor);
            prepararPlanoMatriz(banco, ids, autorCoordenador);
            assertEquals(FalhaBoletimAluno.Codigo.NAO_AUTORIZADO,
                    assertThrows(FalhaBoletimAluno.class, () -> gestaoBoletim.consultar("professor")).codigo());

            var inicial = gestaoBoletim.consultar("aluno");
            assertEquals(1, inicial.matriculas().size(), "A consulta não deve mostrar a matrícula do segundo aluno da turma.");
            assertEquals(ids[9], inicial.matriculas().getFirst().matricula().id());
            assertEquals(1, inicial.matriculas().getFirst().componentesPlano().size());
            assertEquals(1, inicial.matriculas().getFirst().historicoDisciplinas().size());
            assertTrue(inicial.matriculas().getFirst().historicoDisciplinas().getFirst().resultadoFinal().isEmpty());
            assertEquals(1, inicial.horarios().size());
            assertNull(inicial.horarios().getFirst().sala());

            var prova = new AvaliacaoAcademica(0, ids[8], "Prova boletim ACA-09", "Teste sintético",
                    LocalDate.of(2026, 12, 1), TipoAvaliacao.PROVA, 0);
            avaliacaoId = gestaoAvaliacoes.cadastrar("professor", prova, UUID.randomUUID()).id();
            var linhaAluno = localizar(gestaoAvaliacoes.diario("professor", ids[8]), avaliacaoId, ids[10]);
            gestaoAvaliacoes.lancarNota("professor", avaliacaoId, ids[10], new Nota(new BigDecimal("6.0")),
                    linhaAluno.revisaoNota(), UUID.randomUUID());
            var comNota = gestaoBoletim.consultar("aluno");
            var resultado = comNota.matriculas().getFirst().historicoDisciplinas().getFirst().resultadoFinal().orElseThrow();
            assertTrue(comNota.matriculas().getFirst().historicoDisciplinas().getFirst().componentePlano().isPresent());
            assertEquals(SituacaoResultadoFinal.PENDENTE_EXAME, resultado.situacao());
            assertEquals(new BigDecimal("6.0"), resultado.mediaProvas().orElseThrow().valor());

            var inscricoes = new RepositorioInscricoesMysql(banco);
            var inscricao = inscricoes.porMatricula(ids[9]).getFirst();
            inscricoes.cancelar(inscricao, UUID.randomUUID(), autorCoordenador, Instant.parse("2026-11-24T12:00:00Z"));
            var aposCancelamento = gestaoBoletim.consultar("aluno");
            assertEquals(SituacaoInscricao.CANCELADA,
                    aposCancelamento.matriculas().getFirst().historicoDisciplinas().getFirst().inscricao().situacao());
            assertTrue(aposCancelamento.horarios().isEmpty(), "Inscrição cancelada não aparece na grade semanal.");
            assertEquals(new BigDecimal("6.0"), aposCancelamento.matriculas().getFirst().historicoDisciplinas()
                    .getFirst().lancamentos().getFirst().nota().orElseThrow().valor(), "Nota histórica permanece no histórico acadêmico.");
        } finally {
            limparFixtureAvaliacoes(banco, ids, avaliacaoId);
        }
    }

    private static void prepararPlanoMatriz(ConfiguracaoBanco banco, long[] ids, long autor) throws SQLException {
        try (var conexao = banco.abrir();
             var historico = conexao.prepareStatement("INSERT INTO disciplina_historico(item_id,codigo,nome,ementa,carga_horaria,situacao,versao_registro,autor_id,instante,operacao_id) SELECT id,codigo,nome,ementa,carga_horaria,situacao,versao_registro,?,?,? FROM disciplina WHERE id=?")) {
            historico.setLong(1, autor);
            historico.setTimestamp(2, Timestamp.from(Instant.parse("2026-11-24T12:00:00Z")));
            historico.setString(3, UUID.randomUUID().toString());
            historico.setLong(4, ids[5]);
            assertEquals(1, historico.executeUpdate());
            try (var componente = conexao.prepareStatement("INSERT INTO componente_curricular(matriz_curricular_id,disciplina_id,disciplina_revisao,tipo,periodo_ideal,carga_horaria_considerada) VALUES(?, ?,1,'OBRIGATORIO',1,60)")) {
                componente.setLong(1, ids[6]); componente.setLong(2, ids[5]);
                assertEquals(1, componente.executeUpdate());
            }
        }
    }
    private static void prepararFixtureAvaliacoes(ConfiguracaoBanco banco,long[] ids,long autorAluno,long autorProfessor)throws SQLException{
        ids[0]=criarPessoaAvaliacoes(banco,"Aluno ACA07 Principal");ids[1]=criarPessoaAvaliacoes(banco,"Professor ACA07 Sintético");ids[11]=criarPessoaAvaliacoes(banco,"Aluno ACA07 Secundário");
        var alunos=new RepositorioAlunosMysql(banco);ids[2]=alunos.cadastrar(ids[0],SituacaoCadastral.ATIVO,autorAluno).id();ids[12]=alunos.cadastrar(ids[11],SituacaoCadastral.ATIVO,autorAluno).id();
        ids[3]=new RepositorioProfessoresMysql(banco).cadastrar(ids[1],"Doutor",SituacaoFuncional.ATIVO,autorProfessor).id();
        String sufixo=UUID.randomUUID().toString().replace("-","").substring(0,12);
        try(var c=banco.abrir()){
            try(var q=c.prepareStatement("INSERT INTO curso(codigo,nome,carga_horaria_total,situacao,versao_registro) VALUES(?,? ,60,'ATIVO',1)",Statement.RETURN_GENERATED_KEYS)){q.setString(1,"ACA07-CURSO-"+sufixo);q.setString(2,"Curso sintético ACA07");q.executeUpdate();try(var r=q.getGeneratedKeys()){r.next();ids[4]=r.getLong(1);}}
            try(var q=c.prepareStatement("INSERT INTO disciplina(codigo,nome,ementa,carga_horaria,situacao,versao_registro) VALUES(?,?,?,60,'ATIVA',1)",Statement.RETURN_GENERATED_KEYS)){q.setString(1,"ACA07-DISC-"+sufixo);q.setString(2,"Disciplina sintética ACA07");q.setString(3,"Conteúdo exclusivamente de teste de integração.");q.executeUpdate();try(var r=q.getGeneratedKeys()){r.next();ids[5]=r.getLong(1);}}
            try(var q=c.prepareStatement("INSERT INTO matriz_curricular(curso_id,codigo,versao,inicio_vigencia,fim_vigencia,situacao,versao_registro) VALUES(?,?,?,? ,NULL,'VIGENTE',1)",Statement.RETURN_GENERATED_KEYS)){q.setLong(1,ids[4]);q.setString(2,"ACA07-MAT-"+sufixo);q.setString(3,"1");q.setDate(4,Date.valueOf("2026-01-01"));q.executeUpdate();try(var r=q.getGeneratedKeys()){r.next();ids[6]=r.getLong(1);}}
            try(var q=c.prepareStatement("INSERT INTO periodo_letivo(descricao,data_inicio,data_fim,inicio_matricula,fim_matricula,inicio_inscricao,fim_inscricao,situacao,versao_registro,operacao_criacao) VALUES(?,?,?,?,?,?,?,'ABERTO',1,?)",Statement.RETURN_GENERATED_KEYS)){q.setString(1,"Período sintético ACA07 "+sufixo);q.setDate(2,Date.valueOf("2026-01-01"));q.setDate(3,Date.valueOf("2026-12-31"));q.setTimestamp(4,Timestamp.from(Instant.parse("2025-12-01T00:00:00Z")));q.setTimestamp(5,Timestamp.from(Instant.parse("2026-12-15T23:59:59Z")));q.setTimestamp(6,Timestamp.from(Instant.parse("2025-12-01T00:00:00Z")));q.setTimestamp(7,Timestamp.from(Instant.parse("2026-12-15T23:59:59Z")));q.setString(8,UUID.randomUUID().toString());q.executeUpdate();try(var r=q.getGeneratedKeys()){r.next();ids[7]=r.getLong(1);}}
            try(var q=c.prepareStatement("INSERT INTO turma(periodo_id,disciplina_id,professor_id,codigo,capacidade,situacao,versao_registro,operacao_criacao) VALUES(?,?,?, ?,20,'EM_ANDAMENTO',1,?)",Statement.RETURN_GENERATED_KEYS)){q.setLong(1,ids[7]);q.setLong(2,ids[5]);q.setLong(3,ids[3]);q.setString(4,"T-"+sufixo);q.setString(5,UUID.randomUUID().toString());q.executeUpdate();try(var r=q.getGeneratedKeys()){r.next();ids[8]=r.getLong(1);}}
            try(var q=c.prepareStatement("INSERT INTO horario_turma(turma_id,sala_id,dia_semana,hora_inicio,hora_fim,versao_registro) VALUES(?,NULL,'SEGUNDA','08:00:00','09:00:00',1)")){q.setLong(1,ids[8]);q.executeUpdate();}
            ids[9]=criarMatricula(c,ids,ids[2],sufixo+"-1");ids[10]=criarInscricao(c,ids[9],ids[8]);
            ids[13]=criarMatricula(c,ids,ids[12],sufixo+"-2");ids[14]=criarInscricao(c,ids[13],ids[8]);
        }
    }
    private static long criarMatricula(Connection c,long[] ids,long alunoId,String sufixo)throws SQLException{
        try(var q=c.prepareStatement("INSERT INTO matricula_curso(numero_matricula,aluno_id,curso_id,matriz_id,data_ingresso,situacao,versao_registro,operacao_criacao) VALUES(?,?,?,?,?,'ATIVA',1,?)",Statement.RETURN_GENERATED_KEYS)){q.setString(1,"ACA07-"+sufixo);q.setLong(2,alunoId);q.setLong(3,ids[4]);q.setLong(4,ids[6]);q.setDate(5,Date.valueOf("2026-01-01"));q.setString(6,UUID.randomUUID().toString());q.executeUpdate();try(var r=q.getGeneratedKeys()){r.next();return r.getLong(1);}}
    }
    private static long criarInscricao(Connection c,long matriculaId,long turmaId)throws SQLException{
        try(var q=c.prepareStatement("INSERT INTO inscricao_turma(matricula_id,turma_id,situacao,inscrita_em,versao_registro,operacao_criacao) VALUES(?,?,'ATIVA',?,1,?)",Statement.RETURN_GENERATED_KEYS)){q.setLong(1,matriculaId);q.setLong(2,turmaId);q.setTimestamp(3,Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")));q.setString(4,UUID.randomUUID().toString());q.executeUpdate();try(var r=q.getGeneratedKeys()){r.next();return r.getLong(1);}}
    }
    private static long criarPessoaAvaliacoes(ConfiguracaoBanco banco,String nome)throws SQLException{
        try(var c=banco.abrir()){long id;try(var q=c.prepareStatement("SELECT COALESCE(MAX(id),0)+1 FROM pessoa");var r=q.executeQuery()){r.next();id=r.getLong(1);}String sufixo=UUID.randomUUID().toString().replace("-","").substring(0,10);try(var q=c.prepareStatement("INSERT INTO pessoa(id,cpf,nome,email,telefone) VALUES(?,?,?,?,?)")){q.setLong(1,id);q.setString(2,String.format("%011d",Math.floorMod(UUID.randomUUID().hashCode(),100_000_000_000L)));q.setString(3,nome);q.setString(4,"aca07-"+sufixo+"@example.invalid");q.setString(5,"");q.executeUpdate();}return id;}
    }
    private static RegistroNota localizar(List<RegistroNota> registros,long avaliacaoId,long inscricaoId){return registros.stream().filter(registro->registro.avaliacao().id()==avaliacaoId&&registro.inscricaoId()==inscricaoId).findFirst().orElseThrow();}
    private static int contarAvaliacoesAuditadas(ConfiguracaoBanco banco,long avaliacaoId)throws SQLException{try(var c=banco.abrir();var q=c.prepareStatement("SELECT COUNT(*) FROM avaliacao_academica_auditoria WHERE avaliacao_id=?")){q.setLong(1,avaliacaoId);try(var r=q.executeQuery()){r.next();return r.getInt(1);}}}
    private static int contarAuditoriaNota(ConfiguracaoBanco banco,long avaliacaoId,long inscricaoId)throws SQLException{try(var c=banco.abrir();var q=c.prepareStatement("SELECT COUNT(*) FROM nota_avaliacao_auditoria a JOIN nota_avaliacao n ON n.id=a.nota_avaliacao_id WHERE n.avaliacao_id=? AND n.inscricao_id=?")){q.setLong(1,avaliacaoId);q.setLong(2,inscricaoId);try(var r=q.executeQuery()){r.next();return r.getInt(1);}}}
    private static final class AutorizacaoAvaliacoesTeste implements AutorizacaoAvaliacoes<String>{
        private final long[] ids;private final long autorProfessor;private final long autorCoordenador;
        private AutorizacaoAvaliacoesTeste(long[] ids,long autorProfessor,long autorCoordenador){this.ids=ids;this.autorProfessor=autorProfessor;this.autorCoordenador=autorCoordenador;}
        public long usuarioId(String sessao){return switch(sessao){case "professor"->autorProfessor;case "coordenador"->autorCoordenador;case "aluno"->910_000_103L;case "outro-professor"->910_000_104L;default->throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.NAO_AUTORIZADO);};}
        public boolean ehCoordenador(String sessao){return "coordenador".equals(sessao);}
        public boolean ehProfessor(String sessao){return "professor".equals(sessao)||"outro-professor".equals(sessao);}
        public boolean ehAluno(String sessao){return "aluno".equals(sessao);}
        public long professorId(String sessao){if(!ehProfessor(sessao))throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.NAO_AUTORIZADO);return "professor".equals(sessao)?ids[3]:ids[3]+100_000;}
        public long alunoId(String sessao){if(!ehAluno(sessao))throw new FalhaAvaliacao(FalhaAvaliacao.Codigo.NAO_AUTORIZADO);return ids[2];}
    }
    private static void limparFixtureAvaliacoes(ConfiguracaoBanco banco,long[] ids,long... avaliacaoIds){try(var c=banco.abrir()){c.setAutoCommit(false);try{
        for(long avaliacaoId:avaliacaoIds)if(avaliacaoId>0){try(var q=c.prepareStatement("DELETE a FROM nota_avaliacao_auditoria a JOIN nota_avaliacao n ON n.id=a.nota_avaliacao_id WHERE n.avaliacao_id=?")){q.setLong(1,avaliacaoId);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM nota_avaliacao WHERE avaliacao_id=?")){q.setLong(1,avaliacaoId);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM avaliacao_academica_auditoria WHERE avaliacao_id=?")){q.setLong(1,avaliacaoId);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM avaliacao_academica WHERE id=?")){q.setLong(1,avaliacaoId);q.executeUpdate();}}
        for(long inscricaoId:new long[]{ids[10],ids[14]})if(inscricaoId>0)try(var q=c.prepareStatement("DELETE FROM inscricao_auditoria WHERE inscricao_id=?")){q.setLong(1,inscricaoId);q.executeUpdate();}
        for(long matriculaId:new long[]{ids[9],ids[13]})if(matriculaId>0)try(var q=c.prepareStatement("DELETE FROM matricula_auditoria WHERE matricula_id=?")){q.setLong(1,matriculaId);q.executeUpdate();}
        for(long inscricaoId:new long[]{ids[10],ids[14]})if(inscricaoId>0)try(var q=c.prepareStatement("DELETE FROM inscricao_turma WHERE id=?")){q.setLong(1,inscricaoId);q.executeUpdate();}
        for(long matriculaId:new long[]{ids[9],ids[13]})if(matriculaId>0)try(var q=c.prepareStatement("DELETE FROM matricula_curso WHERE id=?")){q.setLong(1,matriculaId);q.executeUpdate();}
        if(ids[8]>0){try(var q=c.prepareStatement("DELETE FROM pauta_final_auditoria WHERE turma_id=?")){q.setLong(1,ids[8]);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM pauta_final WHERE turma_id=?")){q.setLong(1,ids[8]);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM horario_turma WHERE turma_id=?")){q.setLong(1,ids[8]);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM turma_auditoria WHERE turma_id=?")){q.setLong(1,ids[8]);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM turma WHERE id=?")){q.setLong(1,ids[8]);q.executeUpdate();}}
        if(ids[7]>0)try(var q=c.prepareStatement("DELETE FROM periodo_letivo WHERE id=?")){q.setLong(1,ids[7]);q.executeUpdate();}
        if(ids[6]>0){try(var q=c.prepareStatement("DELETE FROM componente_curricular WHERE matriz_curricular_id=?")){q.setLong(1,ids[6]);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM matriz_curricular WHERE id=?")){q.setLong(1,ids[6]);q.executeUpdate();}}
        if(ids[5]>0){try(var q=c.prepareStatement("DELETE FROM disciplina_historico WHERE item_id=?")){q.setLong(1,ids[5]);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM disciplina WHERE id=?")){q.setLong(1,ids[5]);q.executeUpdate();}}
        if(ids[4]>0)try(var q=c.prepareStatement("DELETE FROM curso WHERE id=?")){q.setLong(1,ids[4]);q.executeUpdate();}
        if(ids[3]>0){try(var q=c.prepareStatement("DELETE FROM professor_auditoria WHERE professor_id=?")){q.setLong(1,ids[3]);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM professor WHERE id=?")){q.setLong(1,ids[3]);q.executeUpdate();}}
        for(long alunoId:new long[]{ids[2],ids[12]})if(alunoId>0){try(var q=c.prepareStatement("DELETE FROM aluno_auditoria WHERE aluno_id=?")){q.setLong(1,alunoId);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM aluno WHERE id=?")){q.setLong(1,alunoId);q.executeUpdate();}}
        for(long pessoaId:new long[]{ids[0],ids[1],ids[11]})if(pessoaId>0)try(var q=c.prepareStatement("DELETE FROM pessoa WHERE id=?")){q.setLong(1,pessoaId);q.executeUpdate();}
        c.commit();
    }catch(SQLException erro){c.rollback();throw erro;}}catch(SQLException erro){throw new AssertionError("Falha ao remover somente a fixture sintética ACA07.",erro);}}
}
