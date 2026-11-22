package br.edu.scoa.academico.infraestrutura.persistencia;
import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.dominio.curriculo.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class MatrizesMysqlIT {
    static ConfiguracaoBanco banco;
    RepositorioMatrizesMysql repo;
    RepositorioCatalogoMysql catalogo;
    ItemCatalogo curso, disciplina, segunda;
    static final Instant AGORA=Instant.parse("2026-11-15T18:00:00Z");
    @BeforeAll static void preparar() {
        var e=System.getenv();
        if(!"CONFIRMO_BANCO_DESCARTAVEL".equals(e.get("SCOA_TEST_CONFIRM"))
                || !"jdbc:mysql://127.0.0.1:3308/scoa_test".equals(e.get("SCOA_TEST_URL"))
                || !"scoa_test".equals(e.get("SCOA_TEST_USER"))) throw new IllegalStateException("Banco exclusivo de testes obrigatório.");
        banco=ConfiguracaoBanco.doAmbiente(e,"SCOA_TEST_");
        MigracoesIdentidade.executar(banco);
        var f=MigracoesAcademico.configurar(banco);
        if(f.info().applied().length==0) MigracoesAplicacao.inicializarHistoricoAcademico(banco);
        f.migrate();
    }
    @BeforeEach void iniciar() throws Exception {
        limpar();repo=new RepositorioMatrizesMysql(banco);catalogo=new RepositorioCatalogoMysql(banco);
        curso=catalogo.salvar(GestaoCatalogo.validar(TipoCatalogo.CURSO,null,"ADS","Sistemas","",2400),7,AGORA);
        disciplina=catalogo.salvar(GestaoCatalogo.validar(TipoCatalogo.DISCIPLINA,null,"POO","Objetos","Conteúdo original",80),7,AGORA);
        segunda=catalogo.salvar(GestaoCatalogo.validar(TipoCatalogo.DISCIPLINA,null,"BD","Banco","SQL",60),7,AGORA);
    }
    @AfterAll static void limpar() throws Exception {
        if(banco==null)return;
        try(var c=banco.abrir();var s=c.createStatement()){
            s.execute("DROP TRIGGER IF EXISTS teste_matriz_auditoria");
            s.executeUpdate("UPDATE matriz_curricular SET matriz_origem_id=NULL");
            for(String tabela:List.of("pre_requisito_historico","pre_requisito","componente_historico","matriz_historico","componente_curricular","matriz_curricular",
                    "disciplina_historico","curso_historico","disciplina","curso"))s.executeUpdate("DELETE FROM "+tabela);
        }
    }
    ComponenteCurricular componente(ItemCatalogo item) {return new ComponenteCurricular(item.id(),item.versao(),TipoComponente.OBRIGATORIO,1,80);}
    MatrizCurricular nova(List<ComponenteCurricular> itens) {
        return new MatrizCurricular(0,curso.id(),"M-ADS","2027.1",LocalDate.of(2027,1,1),null,SituacaoMatriz.EM_ELABORACAO,0,itens);
    }
    MatrizCurricular salvar(MatrizCurricular matriz) {return repo.salvar(matriz,7,AGORA);}
    @Test void gravaReabreConsultaEReferenciaConteudoHistorico() {
        var salva=salvar(nova(List.of(componente(disciplina))));
        catalogo.salvar(GestaoCatalogo.validar(TipoCatalogo.DISCIPLINA,disciplina,"POO","Objetos novos","Conteúdo novo",100),7,AGORA);
        assertEquals(salva,new RepositorioMatrizesMysql(banco).consultar(salva.id()));
        assertEquals(1,repo.consultar(salva.id()).componentes().getFirst().disciplinaRevisao());
        assertEquals(1,repo.pesquisar(curso.id(),"2027").size());
        assertTrue(repo.pesquisar(curso.id(),"' OR 1=1 --").isEmpty());
    }
    @Test void recusaDuplicidadeEReferenciaInexistenteSemCabecalhoOrfao() throws Exception {
        salvar(nova(List.of(componente(disciplina))));
        assertEquals(FalhaMatriz.Codigo.MATRIZ_DUPLICADA,assertThrows(FalhaMatriz.class,
            ()->salvar(nova(List.of(componente(segunda))))).codigo());
        var invalida=new MatrizCurricular(0,curso.id(),"OUTRA","1",LocalDate.of(2027,1,1),null,SituacaoMatriz.EM_ELABORACAO,0,
            List.of(new ComponenteCurricular(disciplina.id(),999,TipoComponente.OBRIGATORIO,1,80)));
        assertEquals(FalhaMatriz.Codigo.REFERENCIA_INEXISTENTE,assertThrows(FalhaMatriz.class,()->salvar(invalida)).codigo());
        assertEquals(1,repo.pesquisar(curso.id(),"").size());
    }
    @Test void falhaNaAuditoriaDesfazTudo() throws Exception {
        try(var c=banco.abrir();var s=c.createStatement()){
            s.execute("CREATE TRIGGER teste_matriz_auditoria BEFORE INSERT ON matriz_historico FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='teste'");
        }
        assertThrows(FalhaMatriz.class,()->salvar(nova(List.of(componente(disciplina)))));
        assertTrue(repo.pesquisar(curso.id(),"").isEmpty());
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*) FROM componente_curricular")){
            r.next();assertEquals(0,r.getInt(1));
        }
    }
    @Test void edicaoPreservaIdComponenteEAuditaRemocao() throws Exception {
        var salva=salvar(nova(List.of(componente(disciplina),componente(segunda))));
        long idAntes;
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT id FROM componente_curricular WHERE disciplina_id="+disciplina.id())){
            r.next();idAntes=r.getLong(1);
        }
        var alterada=salvar(salva.remover(segunda.id()));
        assertEquals(2,alterada.revisao());assertEquals(1,alterada.componentes().size());
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT id FROM componente_curricular WHERE disciplina_id="+disciplina.id())){
            r.next();assertEquals(idAntes,r.getLong(1));
        }
        try(var c=banco.abrir();var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*) FROM componente_historico")){
            r.next();assertEquals(3,r.getInt(1));
        }
    }
    @Test void duasRemocoesConcorrentesNaoDeixamMatrizVazia() throws Exception {
        var salva=salvar(nova(List.of(componente(disciplina),componente(segunda))));
        var largada=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(2)){
            var a=executor.submit(()->editarDepois(largada,salva.remover(disciplina.id())));
            var b=executor.submit(()->editarDepois(largada,salva.remover(segunda.id())));
            largada.countDown();assertNotEquals(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));
        }
        assertEquals(1,repo.consultar(salva.id()).componentes().size());
    }
    boolean editarDepois(CountDownLatch largada,MatrizCurricular matriz)throws Exception {
        largada.await();
        try{salvar(matriz);return true;}catch(FalhaMatriz erro){assertEquals(FalhaMatriz.Codigo.CONFLITO_EDICAO,erro.codigo());return false;}
    }
    @Test void estadoPersistidoImpedeEdicaoMesmoComEntradaForjada() throws Exception {
        var salva=salvar(nova(List.of(componente(disciplina))));
        try(var c=banco.abrir();var s=c.createStatement()){s.executeUpdate("UPDATE matriz_curricular SET situacao='VIGENTE' WHERE id="+salva.id());}
        assertEquals(FalhaMatriz.Codigo.MATRIZ_NAO_EDITAVEL,assertThrows(FalhaMatriz.class,()->salvar(salva)).codigo());
    }
    @Test void permiteAtualizacaoExplicitaDaRevisaoDaDisciplina() {
        var salva=salvar(nova(List.of(componente(disciplina))));
        var novaDisciplina=catalogo.salvar(GestaoCatalogo.validar(TipoCatalogo.DISCIPLINA,disciplina,"POO","Objetos","Nova ementa",100),7,AGORA);
        var atualizada=new MatrizCurricular(salva.id(),salva.cursoId(),salva.codigo(),salva.versao(),salva.inicioVigencia(),
            salva.fimVigencia(),salva.situacao(),salva.revisao(),List.of(componente(novaDisciplina)));
        assertEquals(2,salvar(atualizada).componentes().getFirst().disciplinaRevisao());
    }
}
