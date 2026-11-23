package br.edu.scoa.academico.apresentacao.turmas;

import br.edu.scoa.academico.aplicacao.curriculo.ItemCatalogo;
import br.edu.scoa.academico.aplicacao.curriculo.TipoCatalogo;
import br.edu.scoa.academico.aplicacao.turmas.CenarioTurmas;
import br.edu.scoa.academico.apresentacao.alunos.DialogosAluno;
import br.edu.scoa.academico.curriculo.PeriodoLetivoTest;
import br.edu.scoa.academico.dominio.curriculo.PeriodoLetivo;
import br.edu.scoa.academico.dominio.turmas.*;
import java.awt.Component;
import java.awt.Container;
import java.util.UUID;
import javax.swing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FluxoTurmasTest {

    private CenarioTurmas cenario;
    private DialogosTeste dialogos;
    private PeriodoLetivo periodo;
    private ItemCatalogo disciplina;

    @BeforeEach
    void setUp() {
        cenario = new CenarioTurmas();
        dialogos = new DialogosTeste();
        periodo = cenario.cenarioPeriodos.salvar(PeriodoLetivoTest.novo(), UUID.randomUUID(), 7, cenario.cenarioCatalogo.relogio.instant());
        disciplina = cenario.cenarioCatalogo.salvar(
                new ItemCatalogo(0, TipoCatalogo.DISCIPLINA, "ENG101", "Engenharia", "Ementa", 60, true, 0),
                7, cenario.cenarioCatalogo.relogio.instant()
        );
    }

    static final class DialogosTeste implements DialogosAluno {
        String erro;
        int informacoes;
        @Override public void informar(Component origem, String mensagem) { informacoes++; }
        @Override public void erro(Component origem, String mensagem) { erro = mensagem; }
        @Override public boolean confirmar(Component origem, String mensagem) { return true; }
    }

    static Component encontrar(Container raiz, String nome) {
        for (var filho : raiz.getComponents()) {
            if (nome.equals(filho.getName())) return filho;
            if (filho instanceof Container grupo) {
                var achado = encontrar(grupo, nome);
                if (achado != null) return achado;
            }
        }
        return null;
    }

    static void clicar(Container raiz, String nome) {
        var comp = (JButton) encontrar(raiz, nome);
        assertNotNull(comp, "Botão não encontrado: " + nome);
        comp.doClick();
    }

    static void preencher(Container raiz, String nome, String valor) {
        var comp = (javax.swing.text.JTextComponent) encontrar(raiz, nome);
        assertNotNull(comp, "Campo de texto não encontrado: " + nome);
        comp.setText(valor);
    }

    static void aguardar(JPanel painel) {
        for (int i = 0; i < 50; i++) {
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            var estado = (JLabel) encontrar(painel, "estadoTurmas");
            if (estado != null && "Pronto.".equals(estado.getText())) break;
        }
    }

    @Test
    void cadastroSalasRevisaoECancelamentoNaoGravam() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var fluxo = new FluxoTurmas(cenario.gestao, dialogos);
            clicar(fluxo, "novaSala");
            preencher(fluxo, "identificacaoSala", "LAB-10");
            preencher(fluxo, "localizacaoSala", "Bloco A");
            preencher(fluxo, "capacidadeSala", "30");
            preencher(fluxo, "recursosSala", "Computadores");
            clicar(fluxo, "revisarSala");
            clicar(fluxo, "cancelarRevisaoSala");
        });
        assertEquals(0, cenario.gravacoes);
    }

    @Test
    void cadastroConsultaEdicaoEAlternanciaDeSala() throws Exception {
        var fluxo = new FluxoTurmas[1];
        SwingUtilities.invokeAndWait(() -> {
            fluxo[0] = new FluxoTurmas(cenario.gestao, dialogos);
            clicar(fluxo[0], "novaSala");
            preencher(fluxo[0], "identificacaoSala", "SALA-101");
            preencher(fluxo[0], "localizacaoSala", "Bloco Didático 1");
            preencher(fluxo[0], "capacidadeSala", "40");
            preencher(fluxo[0], "recursosSala", "Projetor");
            clicar(fluxo[0], "revisarSala");
            clicar(fluxo[0], "salvarSala");
        });
        aguardar(fluxo[0]);
        assertEquals(1, cenario.gravacoes);
        var salaSalva = cenario.salas.values().stream().filter(s -> "SALA-101".equals(s.identificacao())).findFirst().orElseThrow();
        assertEquals(SituacaoSala.DISPONIVEL, salaSalva.situacao());

        // Alternar situação na ficha
        SwingUtilities.invokeAndWait(() -> {
            clicar(fluxo[0], "alternarSituacaoSala");
        });
        aguardar(fluxo[0]);
        assertEquals(2, cenario.gravacoes);
        assertEquals(SituacaoSala.INDISPONIVEL, cenario.salas.get(salaSalva.id()).situacao());

        // Consulta de salas
        SwingUtilities.invokeAndWait(() -> {
            clicar(fluxo[0], "voltarConsultaSalas");
            preencher(fluxo[0], "buscaSala", "101");
            clicar(fluxo[0], "pesquisarSalas");
        });
        aguardar(fluxo[0]);
        SwingUtilities.invokeAndWait(() -> {
            @SuppressWarnings("unchecked")
            var lista = (JList<Sala>) encontrar(fluxo[0], "resultadosSalas");
            assertEquals(1, lista.getModel().getSize());

            try {
                fluxo[0].setSize(860, 720);
                distribuir(fluxo[0]);
                var img = new java.awt.image.BufferedImage(860, 720, java.awt.image.BufferedImage.TYPE_INT_RGB);
                var g = img.createGraphics();
                fluxo[0].printAll(g);
                g.dispose();
                new java.io.File("target").mkdirs();
                javax.imageio.ImageIO.write(img, "png", new java.io.File("target/aca03-ficha-sala.png"));
            } catch (java.io.IOException erro) {
                throw new RuntimeException(erro);
            }
        });
    }

    @Test
    void cadastroTurmaRevisaoECancelamento() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var fluxo = new FluxoTurmas(cenario.gestao, dialogos);
            clicar(fluxo, "novaTurma");
            preencher(fluxo, "periodoTurma", String.valueOf(periodo.id()));
            preencher(fluxo, "disciplinaTurma", String.valueOf(disciplina.id()));
            preencher(fluxo, "professorTurma", "100");
            preencher(fluxo, "codigoTurma", "T-ENG");
            preencher(fluxo, "capacidadeTurma", "35");
            preencher(fluxo, "diaTurma", "SEGUNDA");
            preencher(fluxo, "inicioTurma", "08:00");
            preencher(fluxo, "fimTurma", "10:00");
            clicar(fluxo, "revisarTurma");
            clicar(fluxo, "cancelarRevisaoTurma");
        });
        assertEquals(0, cenario.gravacoes);
    }

    @Test
    void fluxoCompletoTurmaTransicoesEAlocacaoDeSala() throws Exception {
        var sala = cenario.gestao.salvarSala(new Sala(0, "S-AUDITORIO", "Prédio Central", 50, "", SituacaoSala.DISPONIVEL, 0), UUID.randomUUID());
        var fluxo = new FluxoTurmas[1];

        // Cadastrar turma
        SwingUtilities.invokeAndWait(() -> {
            fluxo[0] = new FluxoTurmas(cenario.gestao, dialogos);
            clicar(fluxo[0], "novaTurma");
            preencher(fluxo[0], "periodoTurma", String.valueOf(periodo.id()));
            preencher(fluxo[0], "disciplinaTurma", String.valueOf(disciplina.id()));
            preencher(fluxo[0], "professorTurma", "100");
            preencher(fluxo[0], "codigoTurma", "TURMA-01");
            preencher(fluxo[0], "capacidadeTurma", "40");
            preencher(fluxo[0], "diaTurma", "QUARTA");
            preencher(fluxo[0], "inicioTurma", "14:00");
            preencher(fluxo[0], "fimTurma", "16:00");
            clicar(fluxo[0], "revisarTurma");
            clicar(fluxo[0], "salvarTurma");
        });
        aguardar(fluxo[0]);
        var turma = cenario.turmas.values().stream().filter(t -> "TURMA-01".equals(t.codigo())).findFirst().orElseThrow();
        assertEquals(SituacaoTurma.PLANEJADA, turma.situacao());

        // Alocar sala
        SwingUtilities.invokeAndWait(() -> {
            preencher(fluxo[0], "idSalaAlocacao", String.valueOf(sala.id()));
            clicar(fluxo[0], "alocarSalaTurma");
        });
        aguardar(fluxo[0]);
        assertEquals(sala.id(), cenario.turmas.get(turma.id()).horarios().getFirst().salaId());

        // Desalocar sala
        SwingUtilities.invokeAndWait(() -> {
            clicar(fluxo[0], "desalocarSalaTurma");
        });
        aguardar(fluxo[0]);
        assertNull(cenario.turmas.get(turma.id()).horarios().getFirst().salaId());

        // Transição: ABRIR turma
        SwingUtilities.invokeAndWait(() -> {
            clicar(fluxo[0], "abrirTurma");
        });
        aguardar(fluxo[0]);
        assertEquals(SituacaoTurma.ABERTA, cenario.turmas.get(turma.id()).situacao());

        // Transição: INICIAR AULAS (EM_ANDAMENTO)
        SwingUtilities.invokeAndWait(() -> {
            clicar(fluxo[0], "iniciarAulasTurma");
        });
        aguardar(fluxo[0]);
        assertEquals(SituacaoTurma.EM_ANDAMENTO, cenario.turmas.get(turma.id()).situacao());

        // Transição: ENCERRAR turma
        SwingUtilities.invokeAndWait(() -> {
            clicar(fluxo[0], "encerrarTurma");
        });
        aguardar(fluxo[0]);
        assertEquals(SituacaoTurma.ENCERRADA, cenario.turmas.get(turma.id()).situacao());

        // Verifica que nenhuma gravação ocorreu na EDT
        assertFalse(cenario.acessouNaEdt);

        // Captura visual para evidências
        SwingUtilities.invokeAndWait(() -> {
            try {
                fluxo[0].setSize(860, 720);
                distribuir(fluxo[0]);
                var img = new java.awt.image.BufferedImage(860, 720, java.awt.image.BufferedImage.TYPE_INT_RGB);
                var g = img.createGraphics();
                fluxo[0].printAll(g);
                g.dispose();
                new java.io.File("target").mkdirs();
                javax.imageio.ImageIO.write(img, "png", new java.io.File("target/aca03-ficha-turma.png"));
            } catch (java.io.IOException erro) {
                throw new RuntimeException(erro);
            }
        });
    }

    private static void distribuir(Container raiz) {
        raiz.doLayout();
        for (var filho : raiz.getComponents()) if (filho instanceof Container grupo) distribuir(grupo);
    }
}
