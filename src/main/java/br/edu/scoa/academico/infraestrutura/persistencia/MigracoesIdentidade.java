package br.edu.scoa.academico.infraestrutura.persistencia;
import org.flywaydb.core.Flyway;
import java.sql.*;
import java.util.*;
/** Histórico separado permite instalar Pessoa antes das FKs acadêmicas sem reescrever V001/V002. */
public final class MigracoesIdentidade {
    private MigracoesIdentidade(){}
    public static void executar(ConfiguracaoBanco banco){
        try(var c=banco.abrir()){
            boolean historico;
            try(var r=c.getMetaData().getTables(c.getCatalog(),null,"flyway_identidade_schema_history",new String[]{"TABLE"})){historico=r.next();}
            if(!historico){
                Set<String> colunas=new HashSet<>();
                try(var r=c.getMetaData().getColumns(c.getCatalog(),null,"pessoa",null)){while(r.next())colunas.add(r.getString("COLUMN_NAME"));}
                if(!colunas.isEmpty()&&!colunas.equals(Set.of("id")))throw new IllegalStateException("Esquema Pessoa preexistente não reconhecido. Revisão de migração obrigatória; nada foi adotado automaticamente.");
                for(String tabela:List.of("usuario","controle_acesso","identidade_auditoria"))
                    try(var r=c.getMetaData().getTables(c.getCatalog(),null,tabela,new String[]{"TABLE"})){if(r.next())throw new IllegalStateException("Tabela institucional preexistente: revisar migrações antes de instalar.");}
            }
        }catch(SQLException e){throw new PersistenciaAcademicaException(e);}
        var flyway=Flyway.configure().dataSource(banco.url(),banco.usuario(),banco.senha())
                .jdbcProperties(Map.of("sslMode","REQUIRED","connectionTimeZone","UTC"))
                .locations("classpath:db/identidade").table("flyway_identidade_schema_history")
                .baselineVersion("0").baselineOnMigrate(true).cleanDisabled(true).load();
        flyway.migrate();
    }
}
