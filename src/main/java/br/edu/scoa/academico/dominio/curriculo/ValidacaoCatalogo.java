package br.edu.scoa.academico.dominio.curriculo;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import static br.edu.scoa.academico.dominio.curriculo.FalhaCurriculo.Codigo.*;

final class ValidacaoCatalogo {
    private ValidacaoCatalogo() {}
    static String codigo(String valor) { return texto(valor == null ? null : valor.strip().toUpperCase(Locale.ROOT), 40, CODIGO_INVALIDO); }
    static String nome(String valor) { return texto(valor, 200, NOME_INVALIDO); }
    static String texto(String valor, int limite, FalhaCurriculo.Codigo erro) {
        String limpo = valor == null ? "" : valor.strip();
        if (limpo.isBlank() || limpo.codePointCount(0, limpo.length()) > limite) throw new FalhaCurriculo(erro);
        return limpo;
    }
    static String ementa(String valor) {
        String limpo = texto(valor, 65535, EMENTA_INVALIDA);
        if (limpo.getBytes(StandardCharsets.UTF_8).length > 65535) throw new FalhaCurriculo(EMENTA_INVALIDA);
        return limpo;
    }
    static int carga(int valor) { if (valor <= 0) throw new FalhaCurriculo(CARGA_HORARIA_INVALIDA); return valor; }
}

