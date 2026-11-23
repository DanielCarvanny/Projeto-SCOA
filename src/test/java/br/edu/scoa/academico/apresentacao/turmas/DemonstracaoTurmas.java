package br.edu.scoa.academico.apresentacao.turmas;

import br.edu.scoa.academico.aplicacao.curriculo.*;
import br.edu.scoa.academico.aplicacao.turmas.CenarioTurmas;
import br.edu.scoa.academico.apresentacao.alunos.DialogosJOptionPane;
import br.edu.scoa.academico.dominio.curriculo.*;
import br.edu.scoa.academico.dominio.turmas.*;
import java.awt.Dimension;
import java.time.*;
import java.util.List;
import java.util.UUID;
import javax.swing.*;

/**
 * Demonstração interativa de Salas, Turmas e Alocações com dados sintéticos pré-carregados em memória.
 */
public final class DemonstracaoTurmas {
    private DemonstracaoTurmas() {}

    public static void main(String[] args) {
        var cenario = new CenarioTurmas();
        var agora = cenario.cenarioCatalogo.relogio.instant();

        // 1. Período letivo
        var periodo = new PeriodoLetivo(0, "2027.1 — Graduação",
                LocalDate.of(2027, 2, 1), LocalDate.of(2027, 6, 30),
                Instant.parse("2027-01-01T12:00:00Z"), Instant.parse("2027-01-31T21:00:00Z"),
                Instant.parse("2027-01-15T12:00:00Z"), Instant.parse("2027-02-10T21:00:00Z"),
                SituacaoPeriodoLetivo.PLANEJADO, 0);
        periodo = cenario.cenarioPeriodos.salvar(periodo, UUID.randomUUID(), 7, agora);
        periodo = cenario.cenarioPeriodos.transicionar(periodo.id(), periodo.revisao(), SituacaoPeriodoLetivo.ABERTO, 7, agora);

        // 2. Disciplinas
        var calc = cenario.cenarioCatalogo.salvar(
                new ItemCatalogo(0, TipoCatalogo.DISCIPLINA, "MAT101", "Cálculo Diferencial e Integral I", "Limites e Derivadas", 80, true, 0),
                7, agora
        );
        var algo = cenario.cenarioCatalogo.salvar(
                new ItemCatalogo(0, TipoCatalogo.DISCIPLINA, "CC102", "Algoritmos e Estruturas de Dados", "Lógica e Estruturas", 80, true, 0),
                7, agora
        );

        // 3. Salas
        var sala101 = cenario.gestao.salvarSala(new Sala(0, "SALA-101", "Bloco Didático 1 - Térreo", 45, "Projetor, Ar Condicionado", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID());
        var lab01 = cenario.gestao.salvarSala(new Sala(0, "LAB-01", "Bloco de Informática - 2º Andar", 35, "35 Computadores, Quadro Branco", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID());
        var auditorio = cenario.gestao.salvarSala(new Sala(0, "AUDITORIO", "Prédio Central", 120, "Sistema de Som, Projetor 4K", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID());
        cenario.gestao.salvarSala(new Sala(0, "LAB-MANUT", "Bloco B", 25, "Bancadas em manutenção elétrica", SituacaoSala.INDISPONIVEL, 0), UUID.randomUUID());

        // 4. Turmas
        // Turma 1: Cálculo I (Prof 100), Segunda 08:00 às 10:00, alocada na SALA-101
        var t1 = cenario.gestao.salvarTurma(
                new Turma(0, periodo.id(), calc.id(), 100L, "T01-CALC", 40, SituacaoTurma.PLANEJADA,
                        List.of(new HorarioTurma(0, DiaSemana.SEGUNDA, LocalTime.of(8, 0), LocalTime.of(10, 0), sala101.id(), 0)), 0),
                UUID.randomUUID()
        );

        // Turma 2: Algoritmos (Prof 200), Quarta 14:00 às 16:00, alocada no LAB-01, Aberta
        var t2 = cenario.gestao.salvarTurma(
                new Turma(0, periodo.id(), algo.id(), 200L, "T02-ALGO", 30, SituacaoTurma.PLANEJADA,
                        List.of(new HorarioTurma(0, DiaSemana.QUARTA, LocalTime.of(14, 0), LocalTime.of(16, 0), lab01.id(), 0)), 0),
                UUID.randomUUID()
        );
        cenario.gestao.transicionar(t2, SituacaoTurma.ABERTA, UUID.randomUUID());

        // Turma 3: Cálculo I (Prof 100), Terça 19:00 às 21:00, sem sala (para demonstrar alocação)
        cenario.gestao.salvarTurma(
                new Turma(0, periodo.id(), calc.id(), 100L, "T03-CALC-NOTURNO", 35, SituacaoTurma.PLANEJADA,
                        List.of(new HorarioTurma(0, DiaSemana.TERCA, LocalTime.of(19, 0), LocalTime.of(21, 0), null, 0)), 0),
                UUID.randomUUID()
        );

        SwingUtilities.invokeLater(() -> {
            var janela = new JFrame("SCOA — Turmas e Salas — demonstração em memória");
            janela.setContentPane(new FluxoTurmas(cenario.gestao, new DialogosJOptionPane()));
            janela.setSize(880, 780);
            janela.setMinimumSize(new Dimension(820, 720));
            janela.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
        });
    }
}
