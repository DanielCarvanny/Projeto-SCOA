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
            for(String tabela:new String[]{"professor_auditoria","professor","aluno_auditoria","aluno","identidade_auditoria","usuario","controle_acesso","pessoa","flyway_identidade_schema_history","flyway_academico_schema_history"})s.execute("DROP TABLE IF EXISTS "+tabela);
        }
    }
    @AfterAll static void deixarEsquemaPronto(){MigracoesIdentidade.executar(banco);var f=MigracoesAcademico.configurar(banco);if(f.info().applied().length==0)MigracoesAplicacao.inicializarHistoricoAcademico(banco);f.migrate();}
    @Test void instalaLimpoSemFixtureDePessoa()throws Exception{
        MigracoesIdentidade.executar(banco);MigracoesAplicacao.inicializarHistoricoAcademico(banco);MigracoesAcademico.configurar(banco).migrate();
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*) FROM usuario")){r.next();assertEquals(0,r.getInt(1));}
    }
    @Test void atualizaLegadoPreservandoIdsEFks()throws Exception{
        try(var c=banco.abrir();var s=c.createStatement()){s.execute("CREATE TABLE pessoa(id INT PRIMARY KEY) ENGINE=InnoDB");s.executeUpdate("INSERT INTO pessoa VALUES(123)");}
        MigracoesAplicacao.inicializarHistoricoAcademico(banco);MigracoesAcademico.configurar(banco).migrate();
        try(var c=banco.abrir();var s=c.createStatement()){s.executeUpdate("INSERT INTO aluno(pessoa_id,situacao_cadastral) VALUES(123,'ATIVO')");}
        MigracoesIdentidade.executar(banco);
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT a.pessoa_id,p.cpf FROM aluno a JOIN pessoa p ON p.id=a.pessoa_id")){assertTrue(r.next());assertEquals(123,r.getLong(1));assertNull(r.getString(2));}
    }
}
