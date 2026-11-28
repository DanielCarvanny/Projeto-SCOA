package br.edu.scoa.almoxarifado.infraestrutura.persistencia;

import br.edu.scoa.almoxarifado.aplicacao.RepositorioAlmoxarifado;
import br.edu.scoa.almoxarifado.dominio.Localizacao;
import br.edu.scoa.almoxarifado.dominio.Produto;
import br.edu.scoa.almoxarifado.dominio.TipoProduto;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoBanco;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RepositorioAlmoxarifadoMysql implements RepositorioAlmoxarifado {
    private final ConfiguracaoBanco banco;

    public RepositorioAlmoxarifadoMysql(ConfiguracaoBanco banco) {
        this.banco = java.util.Objects.requireNonNull(banco);
    }

    @Override public Localizacao salvar(Localizacao localizacao) {
        String sql = "INSERT INTO localizacao (nome, descricao, ativa) VALUES (?, ?, ?)";
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, localizacao.nome());
            comando.setString(2, localizacao.descricao());
            comando.setBoolean(3, localizacao.ativa());
            comando.executeUpdate();
            try (var chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) throw new SQLException("Banco não retornou o identificador da localização.");
                return new Localizacao(chaves.getLong(1), localizacao.nome(), localizacao.descricao(), localizacao.ativa());
            }
        } catch (SQLException e) { throw falha(e); }
    }

    @Override public List<Localizacao> listarLocalizacoes() {
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(
                "SELECT id, nome, descricao, ativa FROM localizacao ORDER BY nome, id");
             var resultado = comando.executeQuery()) {
            var itens = new ArrayList<Localizacao>();
            while (resultado.next()) itens.add(new Localizacao(resultado.getLong("id"), resultado.getString("nome"),
                    resultado.getString("descricao"), resultado.getBoolean("ativa")));
            return List.copyOf(itens);
        } catch (SQLException e) { throw falha(e); }
    }

    @Override public Optional<Localizacao> consultarLocalizacao(long id) {
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(
                "SELECT id, nome, descricao, ativa FROM localizacao WHERE id = ?")) {
            comando.setLong(1, id);
            try (var resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(new Localizacao(resultado.getLong("id"), resultado.getString("nome"),
                        resultado.getString("descricao"), resultado.getBoolean("ativa"))) : Optional.empty();
            }
        } catch (SQLException e) { throw falha(e); }
    }

    @Override public Produto salvar(Produto produto) {
        String sql = "INSERT INTO produto (nome, tipo, unidade, codigo_barras, localizacao_id, estoque_minimo, estoque_maximo, ativo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, produto.nome()); comando.setString(2, produto.tipo().name());
            comando.setString(3, produto.unidade()); comando.setString(4, produto.codigoBarras());
            comando.setLong(5, produto.localizacaoId()); comando.setInt(6, produto.estoqueMinimo());
            comando.setInt(7, produto.estoqueMaximo()); comando.setBoolean(8, produto.ativo());
            comando.executeUpdate();
            try (var chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) throw new SQLException("Banco não retornou o identificador do produto.");
                return new Produto(chaves.getLong(1), produto.nome(), produto.tipo(), produto.unidade(),
                        produto.codigoBarras(), produto.localizacaoId(), produto.estoqueMinimo(),
                        produto.estoqueMaximo(), produto.ativo());
            }
        } catch (SQLException e) { throw falha(e); }
    }

    @Override public List<Produto> listarProdutos() {
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(
                "SELECT id, nome, tipo, unidade, codigo_barras, localizacao_id, estoque_minimo, estoque_maximo, ativo FROM produto ORDER BY nome, id");
             var resultado = comando.executeQuery()) {
            var itens = new ArrayList<Produto>();
            while (resultado.next()) itens.add(produto(resultado));
            return List.copyOf(itens);
        } catch (SQLException e) { throw falha(e); }
    }

    @Override public Optional<Produto> consultarProduto(long id) {
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(
                "SELECT id, nome, tipo, unidade, codigo_barras, localizacao_id, estoque_minimo, estoque_maximo, ativo FROM produto WHERE id = ?")) {
            comando.setLong(1, id);
            try (var resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(produto(resultado)) : Optional.empty();
            }
        } catch (SQLException e) { throw falha(e); }
    }

    private static Produto produto(java.sql.ResultSet resultado) throws SQLException {
        return new Produto(resultado.getLong("id"), resultado.getString("nome"),
                TipoProduto.valueOf(resultado.getString("tipo")), resultado.getString("unidade"),
                resultado.getString("codigo_barras"), resultado.getLong("localizacao_id"),
                resultado.getInt("estoque_minimo"), resultado.getInt("estoque_maximo"), resultado.getBoolean("ativo"));
    }

    private static IllegalStateException falha(SQLException e) {
        if ("23000".equals(e.getSQLState())) return new IllegalStateException("Conflito de dados: código de barras ou nome de localização já cadastrado, ou localização inexistente.", e);
        return new IllegalStateException("Falha ao acessar persistência do Almoxarifado.", e);
    }
}
