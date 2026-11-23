package br.edu.scoa.academico.dominio.turmas;

import java.time.LocalTime;
import static br.edu.scoa.academico.dominio.turmas.FalhaTurma.Codigo.HORARIO_INVALIDO;

public record HorarioTurma(long id, DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFim, Long salaId, long revisao) {
    public HorarioTurma {
        if(id<0 || revisao<0 || (id==0)!=(revisao==0) || diaSemana==null || horaInicio==null || horaFim==null || !horaInicio.isBefore(horaFim) || (salaId!=null&&salaId<=0)) throw new FalhaTurma(HORARIO_INVALIDO);
    }
    public boolean sobrepoe(HorarioTurma outro){return outro!=null&&diaSemana==outro.diaSemana&&horaInicio.isBefore(outro.horaFim)&&outro.horaInicio.isBefore(horaFim);}
    public HorarioTurma identificado(long novoId,long novaRevisao){return new HorarioTurma(novoId,diaSemana,horaInicio,horaFim,salaId,novaRevisao);}
}
