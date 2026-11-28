package br.edu.scoa.academico.infraestrutura.persistencia;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoBanco;

import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.dominio.curriculo.FalhaCurriculo;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import static br.edu.scoa.academico.dominio.curriculo.FalhaCurriculo.Codigo.*;

/** Nomes SQL vêm apenas do enum interno; valores de entrada sempre parametrizados. */
public final class RepositorioCatalogoMysql implements RepositorioCatalogo {
    private final ConfiguracaoBanco banco;
    public RepositorioCatalogoMysql(ConfiguracaoBanco banco) { this.banco = banco; }
    private static String tabela(TipoCatalogo tipo) { return switch (tipo) { case CURSO -> "curso"; case DISCIPLINA -> "disciplina"; }; }
    private static String carga(TipoCatalogo tipo) { return tipo == TipoCatalogo.CURSO ? "carga_horaria_total" : "carga_horaria"; }
    private static String campos(TipoCatalogo tipo) {
        return "codigo,nome," + (tipo == TipoCatalogo.DISCIPLINA ? "ementa," : "") + carga(tipo) + ",situacao,versao_registro";
    }
    private static String situacao(ItemCatalogo item) {
        return item.tipo() == TipoCatalogo.CURSO ? (item.estaAtivo() ? "ATIVO" : "INATIVO") : (item.estaAtivo() ? "ATIVA" : "INATIVA");
    }
    private static int preencher(PreparedStatement comando, ItemCatalogo item, long versao) throws SQLException {
        int indice = 1;
        comando.setString(indice++, item.codigo()); comando.setString(indice++, item.nome());
        if (item.tipo() == TipoCatalogo.DISCIPLINA) comando.setString(indice++, item.ementa());
        comando.setInt(indice++, item.cargaHoraria()); comando.setString(indice++, situacao(item)); comando.setLong(indice++, versao);
        return indice;
    }
    public ItemCatalogo salvar(ItemCatalogo item, long autorId, Instant instante) {
        String tabela = tabela(item.tipo());
        boolean novo = item.id() == 0;
        long versao = item.versao() + 1;
        try (var conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                long id = item.id();
                if (novo) {
                    String marcadores = item.tipo() == TipoCatalogo.CURSO ? "?,?,?,?,?" : "?,?,?,?,?,?";
                    try (var comando = conexao.prepareStatement("INSERT INTO " + tabela + "(" + campos(item.tipo()) + ") VALUES(" + marcadores + ")", Statement.RETURN_GENERATED_KEYS)) {
                        preencher(comando, item, versao); comando.executeUpdate();
                        try (var chaves = comando.getGeneratedKeys()) { if (!chaves.next()) throw new SQLException("Sem identificador."); id = chaves.getLong(1); }
                    }
                } else {
                    String atribuicoes = "codigo=?,nome=?," + (item.tipo() == TipoCatalogo.DISCIPLINA ? "ementa=?," : "") + carga(item.tipo()) + "=?,situacao=?,versao_registro=?";
                    try (var comando = conexao.prepareStatement("UPDATE " + tabela + " SET " + atribuicoes + " WHERE id=? AND versao_registro=?")) {
                        int indice = preencher(comando, item, versao); comando.setLong(indice++, id); comando.setLong(indice, item.versao());
                        if (comando.executeUpdate() != 1) throw new FalhaCurriculo(CONFLITO_EDICAO);
                    }
                }
                // Snapshot da versão salva; versões anteriores nunca são atualizadas.
                try (var comando = conexao.prepareStatement("INSERT INTO " + tabela + "_historico(item_id," + campos(item.tipo()) + ",autor_id,instante,operacao_id) SELECT id," + campos(item.tipo()) + ",?,?,? FROM " + tabela + " WHERE id=?")) {
                    comando.setLong(1, autorId); comando.setTimestamp(2, Timestamp.from(instante));
                    comando.setString(3, UUID.randomUUID().toString()); comando.setLong(4, id); comando.executeUpdate();
                }
                conexao.commit();
                return new ItemCatalogo(id, item.tipo(), item.codigo(), item.nome(), item.ementa(), item.cargaHoraria(), item.estaAtivo(), versao);
            } catch (SQLException | RuntimeException erro) {
                try { conexao.rollback(); } catch (SQLException falhaRollback) { erro.addSuppressed(falhaRollback); }
                if (erro instanceof FalhaCurriculo falha) throw falha;
                if (erro instanceof SQLException sql && sql.getErrorCode() == 1062) throw new FalhaCurriculo(CODIGO_DUPLICADO);
                throw new FalhaCurriculo(INDISPONIVEL);
            }
        } catch (SQLException erro) { throw new FalhaCurriculo(INDISPONIVEL); }
    }
    private static ItemCatalogo ler(ResultSet resultado, TipoCatalogo tipo) throws SQLException {
        return new ItemCatalogo(resultado.getLong("id"), tipo, resultado.getString("codigo"), resultado.getString("nome"),
                tipo == TipoCatalogo.DISCIPLINA ? resultado.getString("ementa") : "",
                resultado.getInt(carga(tipo)), resultado.getString("situacao").startsWith("ATIV"), resultado.getLong("versao_registro"));
    }
    public List<ItemCatalogo> pesquisar(TipoCatalogo tipo, String termo) {
        String tabela = tabela(tipo);
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(
                "SELECT id," + campos(tipo) + " FROM " + tabela + " WHERE LOCATE(UPPER(?),UPPER(codigo))>0 OR LOCATE(UPPER(?),UPPER(nome))>0 ORDER BY nome,id")) {
            comando.setString(1, termo); comando.setString(2, termo);
            var itens = new ArrayList<ItemCatalogo>();
            try (var resultado = comando.executeQuery()) { while (resultado.next()) itens.add(ler(resultado, tipo)); }
            return List.copyOf(itens);
        } catch (SQLException erro) { throw new FalhaCurriculo(INDISPONIVEL); }
    }
    public List<RevisaoCatalogo> historico(TipoCatalogo tipo, long id) {
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(
                "SELECT item_id AS id," + campos(tipo) + ",autor_id,instante,operacao_id FROM " + tabela(tipo) + "_historico WHERE item_id=? ORDER BY versao_registro")) {
            comando.setLong(1, id); var revisoes = new ArrayList<RevisaoCatalogo>();
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) revisoes.add(new RevisaoCatalogo(ler(resultado, tipo), resultado.getLong("autor_id"),
                        resultado.getTimestamp("instante").toInstant(), resultado.getString("operacao_id")));
            }
            return List.copyOf(revisoes);
        } catch (SQLException erro) { throw new FalhaCurriculo(INDISPONIVEL); }
    }
}

