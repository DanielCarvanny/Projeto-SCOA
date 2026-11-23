package br.edu.scoa.academico.infraestrutura.persistencia;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
/** Reconstrução restrita ao banco descartável, nunca executada na aplicação. */
class InstalacaoIdentidadeMysqlIT {
    static ConfiguracaoBanco banco;
    @BeforeAll static void configurar(){
        var e=System.getenv();if(!"CONFIRMO_BANCO_DESCARTAVEL".equals(e.get("SCOA_TEST_CONFIRM"))||!"jdbc:mysql://127.0.0.1:3308/scoa_test".equals(e.get("SCOA_TEST_URL"))||!"scoa_test".equals(e.get("SCOA_TEST_USER")))throw new IllegalStateException("Exige banco exclusivo de teste");banco=ConfiguracaoBanco.doAmbiente(e,"SCOA_TEST_");
    }
    @BeforeEach void reconstruir()throws Exception{
        try(var c=banco.abrir();var s=c.createStatement()){
            assertEquals("scoa_test",c.getCatalog());
            for(String tabela:new String[]{"inscricao_auditoria","inscricao_turma","matricula_auditoria","matricula_curso","turma_auditoria","horario_turma","turma","sala_auditoria","sala","periodo_historico","periodo_letivo","pre_requisito_historico","pre_requisito","componente_historico","matriz_historico","componente_curricular","matriz_curricular","disciplina_historico","curso_historico","disciplina","curso","professor_auditoria","professor","aluno_auditoria","aluno","identidade_auditoria","usuario","controle_acesso","pessoa","flyway_identidade_schema_history","flyway_academico_schema_history"})s.execute("DROP TABLE IF EXISTS "+tabela);
        }
    }
    @AfterAll static void deixarEsquemaPronto(){MigracoesIdentidade.executar(banco);var f=MigracoesAcademico.configurar(banco);if(f.info().applied().length==0)MigracoesAplicacao.inicializarHistoricoAcademico(banco);f.migrate();}
    @Test void instalaLimpoSemFixtureDePessoa()throws Exception{
        MigracoesIdentidade.executar(banco);MigracoesAplicacao.inicializarHistoricoAcademico(banco);MigracoesAcademico.configurar(banco).migrate();
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*) FROM usuario")){r.next();assertEquals(0,r.getInt(1));}
    }
    @Test void atualizaAca01ParaCatalogoSemPerderAluno()throws Exception{
        MigracoesIdentidade.executar(banco);
        MigracoesAplicacao.inicializarHistoricoAcademico(banco);
        org.flywaydb.core.Flyway.configure().configuration(MigracoesAcademico.configurar(banco).getConfiguration()).target("2").load().migrate();
        try(var c=banco.abrir();var s=c.createStatement()){
            s.executeUpdate("INSERT INTO pessoa(id,cpf,nome,email) VALUES(456,'12345678901','Pessoa sintética','pessoa@example.invalid')");
            s.executeUpdate("INSERT INTO aluno(pessoa_id,situacao_cadastral) VALUES(456,'ATIVO')");
        }
        MigracoesAcademico.configurar(banco).migrate();
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT a.pessoa_id,p.nome FROM aluno a JOIN pessoa p ON p.id=a.pessoa_id")){
            assertTrue(r.next());assertEquals(456,r.getLong(1));assertEquals("Pessoa sintética",r.getString(2));
        }
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*) FROM disciplina_historico")){
            r.next();assertEquals(0,r.getInt(1));
        }
    }
    @Test void atualizaCatalogoParaMatrizesPreservandoHistorico()throws Exception{
        MigracoesIdentidade.executar(banco);
        MigracoesAplicacao.inicializarHistoricoAcademico(banco);
        org.flywaydb.core.Flyway.configure().configuration(MigracoesAcademico.configurar(banco).getConfiguration()).target("3").load().migrate();
        var catalogo=new RepositorioCatalogoMysql(banco);
        var disciplina=catalogo.salvar(br.edu.scoa.academico.aplicacao.curriculo.GestaoCatalogo.validar(
            br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo.DISCIPLINA,null,"POO","Objetos","Conteúdo preservado",80),7,java.time.Instant.parse("2026-11-15T12:00:00Z"));
        MigracoesAcademico.configurar(banco).migrate();
        assertEquals("Conteúdo preservado",catalogo.historico(br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo.DISCIPLINA,disciplina.id()).getFirst().item().ementa());
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*) FROM matriz_curricular")){
            r.next();assertEquals(0,r.getInt(1));
        }
    }
    @Test void atualizaV004SemPerderComposicao()throws Exception{
        MigracoesIdentidade.executar(banco);MigracoesAplicacao.inicializarHistoricoAcademico(banco);
        org.flywaydb.core.Flyway.configure().configuration(MigracoesAcademico.configurar(banco).getConfiguration()).target("4").load().migrate();
        try(var c=banco.abrir();var s=c.createStatement()){
            s.executeUpdate("INSERT INTO curso(id,codigo,nome,carga_horaria_total,situacao,versao_registro) VALUES(800,'CURSO','Curso teste',2400,'ATIVO',1)");
            s.executeUpdate("INSERT INTO disciplina(id,codigo,nome,ementa,carga_horaria,situacao,versao_registro) VALUES(900,'DISC','Disciplina teste','Conteúdo',80,'ATIVA',1)");
            s.executeUpdate("INSERT INTO disciplina_historico(item_id,codigo,nome,ementa,carga_horaria,situacao,versao_registro,autor_id,instante,operacao_id) VALUES(900,'DISC','Disciplina teste','Conteúdo',80,'ATIVA',1,7,'2026-11-16 12:00:00',UUID())");
            s.executeUpdate("INSERT INTO matriz_curricular(id,curso_id,codigo,versao,inicio_vigencia,situacao,versao_registro) VALUES(700,800,'M','2027','2027-01-01','EM_ELABORACAO',1)");
            s.executeUpdate("INSERT INTO componente_curricular(matriz_curricular_id,disciplina_id,disciplina_revisao,tipo,periodo_ideal,carga_horaria_considerada) VALUES(700,900,1,'OBRIGATORIO',1,80)");
        }
        MigracoesAcademico.configurar(banco).migrate();
        var matriz=new RepositorioMatrizesMysql(banco).consultar(700);
        assertEquals(1,matriz.componentes().size());assertEquals(900,matriz.componentes().getFirst().disciplinaId());
        assertTrue(matriz.preRequisitos().isEmpty());assertEquals(1,matriz.revisao());
    }
    @Test void atualizaV005SemPerderComposicao()throws Exception{
        MigracoesIdentidade.executar(banco);MigracoesAplicacao.inicializarHistoricoAcademico(banco);
        org.flywaydb.core.Flyway.configure().configuration(MigracoesAcademico.configurar(banco).getConfiguration()).target("5").load().migrate();
        try(var c=banco.abrir();var s=c.createStatement()){
            s.executeUpdate("INSERT INTO curso(id,codigo,nome,carga_horaria_total,situacao,versao_registro) VALUES(800,'CURSO','Curso teste',2400,'ATIVO',1)");
            s.executeUpdate("INSERT INTO disciplina(id,codigo,nome,ementa,carga_horaria,situacao,versao_registro) VALUES(900,'DISC','Disciplina teste','Conteúdo',80,'ATIVA',1)");
            s.executeUpdate("INSERT INTO disciplina_historico(item_id,codigo,nome,ementa,carga_horaria,situacao,versao_registro,autor_id,instante,operacao_id) VALUES(900,'DISC','Disciplina teste','Conteúdo',80,'ATIVA',1,7,'2026-11-16 12:00:00',UUID())");
            s.executeUpdate("INSERT INTO matriz_curricular(id,curso_id,codigo,versao,inicio_vigencia,situacao,versao_registro) VALUES(700,800,'M','2027','2027-01-01','EM_ELABORACAO',1)");
            s.executeUpdate("INSERT INTO componente_curricular(matriz_curricular_id,disciplina_id,disciplina_revisao,tipo,periodo_ideal,carga_horaria_considerada) VALUES(700,900,1,'OBRIGATORIO',1,80)");
        }
        MigracoesAcademico.configurar(banco).migrate();
        var matriz=new RepositorioMatrizesMysql(banco).consultar(700);
        assertEquals(1,matriz.componentes().size());assertEquals(900,matriz.componentes().getFirst().disciplinaId());
        assertNull(matriz.matrizOrigemId());assertTrue(matriz.preRequisitos().isEmpty());assertEquals(1,matriz.revisao());
    }
    @Test void atualizaV006ParaPeriodosSemPerderComposicao()throws Exception{
        MigracoesIdentidade.executar(banco);MigracoesAplicacao.inicializarHistoricoAcademico(banco);
        org.flywaydb.core.Flyway.configure().configuration(MigracoesAcademico.configurar(banco).getConfiguration()).target("6").load().migrate();
        try(var c=banco.abrir();var s=c.createStatement()){
            s.executeUpdate("INSERT INTO curso(id,codigo,nome,carga_horaria_total,situacao,versao_registro) VALUES(800,'CURSO','Curso teste',2400,'ATIVO',1)");
            s.executeUpdate("INSERT INTO disciplina(id,codigo,nome,ementa,carga_horaria,situacao,versao_registro) VALUES(900,'DISC','Disciplina teste','Conteúdo',80,'ATIVA',1)");
            s.executeUpdate("INSERT INTO disciplina_historico(item_id,codigo,nome,ementa,carga_horaria,situacao,versao_registro,autor_id,instante,operacao_id) VALUES(900,'DISC','Disciplina teste','Conteúdo',80,'ATIVA',1,7,'2026-11-16 12:00:00',UUID())");
            s.executeUpdate("INSERT INTO matriz_curricular(id,curso_id,codigo,versao,inicio_vigencia,situacao,versao_registro) VALUES(700,800,'M','2027','2027-01-01','EM_ELABORACAO',1)");
            s.executeUpdate("INSERT INTO componente_curricular(matriz_curricular_id,disciplina_id,disciplina_revisao,tipo,periodo_ideal,carga_horaria_considerada) VALUES(700,900,1,'OBRIGATORIO',1,80)");
        }
        MigracoesAcademico.configurar(banco).migrate();
        var matriz=new RepositorioMatrizesMysql(banco).consultar(700);
        assertEquals(1,matriz.componentes().size());assertEquals(900,matriz.componentes().getFirst().disciplinaId());
        assertNull(matriz.matrizOrigemId());assertTrue(matriz.preRequisitos().isEmpty());assertEquals(1,matriz.revisao());
    }
    @Test void atualizaLegadoPreservandoIdsEFks()throws Exception{
        try(var c=banco.abrir();var s=c.createStatement()){s.execute("CREATE TABLE pessoa(id INT PRIMARY KEY) ENGINE=InnoDB");s.executeUpdate("INSERT INTO pessoa VALUES(123)");}
        MigracoesAplicacao.inicializarHistoricoAcademico(banco);MigracoesAcademico.configurar(banco).migrate();
        try(var c=banco.abrir();var s=c.createStatement()){s.executeUpdate("INSERT INTO aluno(pessoa_id,situacao_cadastral) VALUES(123,'ATIVO')");}
        MigracoesIdentidade.executar(banco);
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT a.pessoa_id,p.cpf FROM aluno a JOIN pessoa p ON p.id=a.pessoa_id")){assertTrue(r.next());assertEquals(123,r.getLong(1));assertNull(r.getString(2));}
    }
}
