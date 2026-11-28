package br.edu.scoa.almoxarifado.infraestrutura.persistencia;

import br.edu.scoa.almoxarifado.aplicacao.RepositorioMovimentacoesEstoque;
import br.edu.scoa.almoxarifado.dominio.*;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoBanco;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public final class RepositorioMovimentacoesEstoqueMysql implements RepositorioMovimentacoesEstoque {
    private final ConfiguracaoBanco banco;
    public RepositorioMovimentacoesEstoqueMysql(ConfiguracaoBanco banco) { this.banco = java.util.Objects.requireNonNull(banco); }

    @Override public MovimentacaoEstoque movimentar(long produtoId, TipoMovimentacao tipo, int quantidade,
                                                     long autor, Instant instanteUtc, String origem, String operacaoId) {
        var solicitada = new MovimentacaoEstoque(0, produtoId, tipo, quantidade, autor, instanteUtc, origem, operacaoId, 0);
        try (var conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                var existente = porOperacao(conexao, operacaoId);
                if (existente != null) {
                    if (existente.produtoId() != produtoId || existente.tipo() != tipo || existente.quantidade() != quantidade
                            || existente.usuarioAutorId() != autor || !existente.origem().equals(origem))
                        throw new IllegalStateException("Identificador de operação já utilizado com dados diferentes.");
                    conexao.commit();
                    return existente;
                }
                int saldo;
                int minimo;
                int maximo;
                try (var comando = conexao.prepareStatement("SELECT saldo, estoque_minimo, estoque_maximo FROM produto WHERE id = ? AND ativo = TRUE FOR UPDATE")) {
                    comando.setLong(1, produtoId);
                    try (var resultado = comando.executeQuery()) {
                        if (!resultado.next()) throw new IllegalArgumentException("Produto ativo não encontrado.");
                        saldo = resultado.getInt("saldo"); minimo = resultado.getInt("estoque_minimo"); maximo = resultado.getInt("estoque_maximo");
                    }
                }
                long saldoNovo = tipo == TipoMovimentacao.ENTRADA ? (long) saldo + quantidade : (long) saldo - quantidade;
                if (saldoNovo < 0) throw new IllegalStateException("Saldo insuficiente para a saída.");
                if (saldoNovo > Integer.MAX_VALUE) throw new IllegalArgumentException("Saldo excede o limite suportado.");
                try (var comando = conexao.prepareStatement("UPDATE produto SET saldo = ? WHERE id = ?")) {
                    comando.setInt(1, (int) saldoNovo); comando.setLong(2, produtoId); comando.executeUpdate();
                }
                long id;
                try (var comando = conexao.prepareStatement("INSERT INTO movimentacao_estoque (produto_id, tipo, quantidade, usuario_autor_id, instante_utc, origem, operacao_id, saldo_resultante) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    comando.setLong(1, produtoId); comando.setString(2, tipo.name()); comando.setInt(3, quantidade);
                    comando.setLong(4, autor); comando.setTimestamp(5, Timestamp.from(instanteUtc));
                    comando.setString(6, origem); comando.setString(7, operacaoId); comando.setInt(8, (int) saldoNovo);
                    comando.executeUpdate();
                    try (var chaves = comando.getGeneratedKeys()) { if (!chaves.next()) throw new SQLException("ID da movimentação ausente."); id = chaves.getLong(1); }
                }
                conexao.commit();
                return new MovimentacaoEstoque(id, produtoId, tipo, quantidade, autor, instanteUtc, origem, operacaoId, (int) saldoNovo);
            } catch (Exception e) {
                conexao.rollback();
                if (e instanceof SQLException sql && violacaoUnicaOperacao(sql)) {
                    var concorrente = consultarOperacao(operacaoId);
                    if (mesmaOperacao(concorrente, solicitada)) return concorrente;
                    if (concorrente != null) throw new IllegalStateException("Identificador de operação já utilizado com dados diferentes.", e);
                }
                if (e instanceof RuntimeException runtime) throw runtime;
                throw falha((SQLException) e);
            }
        } catch (SQLException e) { throw falha(e); }
    }

    @Override public SituacaoEstoque consultarSituacao(long produtoId) {
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement("SELECT saldo, estoque_minimo, estoque_maximo FROM produto WHERE id = ?")) {
            comando.setLong(1, produtoId);
            try (var resultado = comando.executeQuery()) {
                if (!resultado.next()) throw new IllegalArgumentException("Produto não encontrado.");
                int saldo = resultado.getInt(1), minimo = resultado.getInt(2), maximo = resultado.getInt(3);
                return new SituacaoEstoque(produtoId, saldo, minimo, maximo, SituacaoEstoque.calcularAlerta(saldo, minimo, maximo));
            }
        } catch (SQLException e) { throw falha(e); }
    }

    private static MovimentacaoEstoque porOperacao(java.sql.Connection conexao, String operacaoId) throws SQLException {
        try (var comando = conexao.prepareStatement("SELECT id, produto_id, tipo, quantidade, usuario_autor_id, instante_utc, origem, operacao_id, saldo_resultante FROM movimentacao_estoque WHERE operacao_id = ? FOR UPDATE")) {
            comando.setString(1, operacaoId);
            try (var r = comando.executeQuery()) {
                return r.next() ? new MovimentacaoEstoque(r.getLong(1), r.getLong(2), TipoMovimentacao.valueOf(r.getString(3)),
                        r.getInt(4), r.getLong(5), r.getTimestamp(6).toInstant(), r.getString(7), r.getString(8), r.getInt(9)) : null;
            }
        }
    }

    private MovimentacaoEstoque consultarOperacao(String operacaoId) throws SQLException {
        try (var conexao = banco.abrir()) { return porOperacao(conexao, operacaoId); }
    }

    private static boolean mesmaOperacao(MovimentacaoEstoque existente, MovimentacaoEstoque solicitada) {
        return existente != null && existente.produtoId() == solicitada.produtoId()
                && existente.tipo() == solicitada.tipo() && existente.quantidade() == solicitada.quantidade()
                && existente.usuarioAutorId() == solicitada.usuarioAutorId()
                && existente.instanteUtc().equals(solicitada.instanteUtc())
                && existente.origem().equals(solicitada.origem());
    }

    private static boolean violacaoUnicaOperacao(SQLException e) {
        return e.getErrorCode() == 1062 && e.getSQLState() != null && e.getSQLState().startsWith("23")
                && e.getMessage() != null && e.getMessage().contains("uq_movimentacao_operacao");
    }

    private static IllegalStateException falha(SQLException e) {
        if ("23000".equals(e.getSQLState())) return new IllegalStateException("Identificador de operação duplicado ou conflito de integridade.", e);
        return new IllegalStateException("Falha na persistência de estoque.", e);
    }
}
