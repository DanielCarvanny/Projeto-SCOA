package br.edu.scoa.academico.infraestrutura.persistencia;
import org.flywaydb.core.Flyway;
public final class MigracoesAplicacao {
    private MigracoesAplicacao(){}
    public static void inicializarHistoricoAcademico(ConfiguracaoBanco banco){
        try(var c=banco.abrir()){
            for(String tabela:new String[]{"aluno","professor","aluno_auditoria","professor_auditoria"})
                try(var r=c.getMetaData().getTables(c.getCatalog(),null,tabela,new String[]{"TABLE"})){if(r.next())throw new IllegalStateException("Esquema acadêmico sem histórico: requer revisão manual antes de baseline.");}
        }catch(java.sql.SQLException e){throw new PersistenciaAcademicaException(e);}
        Flyway.configure().configuration(MigracoesAcademico.configurar(banco).getConfiguration()).baselineVersion("0").load().baseline();
    }
}
