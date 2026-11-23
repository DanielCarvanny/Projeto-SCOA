package br.edu.scoa.academico.dominio.turmas;

import java.nio.charset.StandardCharsets;
import static br.edu.scoa.academico.dominio.turmas.FalhaTurma.Codigo.SALA_INVALIDA;

public record Sala(long id, String identificacao, String localizacao, int capacidade, String recursos, SituacaoSala situacao, long revisao) {
    public Sala {
        identificacao=limpar(identificacao); localizacao=limpar(localizacao); recursos=limpar(recursos);
        if(id<0 || revisao<0 || (id==0)!=(revisao==0) || identificacao.isBlank() || localizacao.isBlank()
            || identificacao.getBytes(StandardCharsets.UTF_8).length>100 || localizacao.getBytes(StandardCharsets.UTF_8).length>255
            || recursos.getBytes(StandardCharsets.UTF_8).length>2000 || capacidade<=0 || situacao==null) throw new FalhaTurma(SALA_INVALIDA);
    }
    private static String limpar(String valor){ return valor==null?"":valor.strip(); }
    public Sala identificado(long novoId,long novaRevisao){return new Sala(novoId,identificacao,localizacao,capacidade,recursos,situacao,novaRevisao);}
}
