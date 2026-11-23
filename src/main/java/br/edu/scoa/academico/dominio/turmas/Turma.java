package br.edu.scoa.academico.dominio.turmas;

import java.util.*;
import static br.edu.scoa.academico.dominio.turmas.FalhaTurma.Codigo.*;

public record Turma(long id,long periodoId,long disciplinaId,long professorId,String codigo,int capacidade,SituacaoTurma situacao,List<HorarioTurma> horarios,long revisao) {
    public Turma {
        codigo=codigo==null?"":codigo.strip().toUpperCase(Locale.ROOT); horarios=horarios==null?List.of():List.copyOf(horarios);
        if(id<0||revisao<0||(id==0)!=(revisao==0)||periodoId<=0||disciplinaId<=0||professorId<=0||codigo.isBlank()||codigo.length()>50||capacidade<=0||situacao==null||horarios.isEmpty())throw new FalhaTurma(TURMA_INVALIDA);
        for(int i=0;i<horarios.size();i++)for(int j=i+1;j<horarios.size();j++)if(horarios.get(i).sobrepoe(horarios.get(j)))throw new FalhaTurma(HORARIO_INVALIDO);
    }
    public Turma identificado(long novoId,long novaRevisao,List<HorarioTurma> novosHorarios){return new Turma(novoId,periodoId,disciplinaId,professorId,codigo,capacidade,situacao,novosHorarios,novaRevisao);}
}
