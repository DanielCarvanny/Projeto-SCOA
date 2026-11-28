package br.edu.scoa.almoxarifado.aplicacao;

/** Porta para a sessão institucional. Implementação deve verificar validade, conta ativa e perfil COORDENADOR. */
public interface IdentidadeParaMovimentacao {
    long exigirCoordenadorAtivo(Object sessaoInstitucional);
}
