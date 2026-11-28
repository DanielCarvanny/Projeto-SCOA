package br.edu.scoa.academico.apresentacao.identidade;
import br.edu.scoa.fundacao.persistencia.ConfiguracaoBanco;
import br.edu.scoa.academico.aplicacao.identidade.*;
import br.edu.scoa.academico.aplicacao.alunos.*;
import br.edu.scoa.academico.aplicacao.professores.*;
import br.edu.scoa.academico.apresentacao.alunos.*;
import br.edu.scoa.academico.apresentacao.professores.*;
import br.edu.scoa.academico.infraestrutura.identidade.*;
import br.edu.scoa.academico.infraestrutura.persistencia.*;
import br.edu.scoa.administrativo.infraestrutura.persistencia.MigracoesAdministrativo;
import br.edu.scoa.almoxarifado.infraestrutura.persistencia.MigracoesAlmoxarifado;
import java.time.Clock;
import java.util.Arrays;
import javax.swing.*;

public final class AplicacaoAcademica {
    private AplicacaoAcademica(){}
    public static void main(String[] argumentos){
        try{
            var banco=ConfiguracaoBanco.doAmbiente(System.getenv(),"SCOA_DB_");
            if(argumentos.length==1&&argumentos[0].equals("--migrar")){
                MigracoesIdentidade.executar(banco);
                var f=MigracoesAcademico.configurar(banco);
                // Identidade foi instalada primeiro: baseline explícita só para o histórico acadêmico novo.
                if(f.info().applied().length==0)MigracoesAplicacao.inicializarHistoricoAcademico(banco);
                f.migrate();
                MigracoesAlmoxarifado.configurar(banco).migrate();
                MigracoesAdministrativo.configurar(banco).migrate();
                System.out.println("Migrações concluídas.");return;
            }
            var senhas=new SenhasPbkdf2();var repo=new RepositorioIdentidadeMysql(banco,senhas);
            if(argumentos.length==1&&argumentos[0].equals("--inicializar")){
                var console=System.console();if(console==null)throw new IllegalStateException("Execute em um terminal interativo para informar a senha sem exibi-la.");
                String cpf=ValidacaoPessoa.cpf(console.readLine("CPF do primeiro coordenador: "));
                String nome=console.readLine("Nome: "),email=console.readLine("E-mail: "),login=ServicoIdentidade.login(console.readLine("Login: "));
                ValidacaoPessoa.dados(nome,email,"");char[] senha=console.readPassword("Senha inicial (mínimo 8 caracteres): ");
                try{repo.inicializar(cpf,nome.trim(),email.trim(),login,senhas.gerar(senha));System.out.println("Conta inicial criada. Abra a aplicação e entre com suas credenciais.");}finally{if(senha!=null)Arrays.fill(senha,'\0');}return;
            }
            if(argumentos.length!=0)throw new IllegalArgumentException("Use --migrar, --inicializar ou nenhum argumento para abrir.");
            var identidade=new ServicoIdentidade(repo,senhas,Clock.systemUTC());
            var alunos=new RepositorioAlunosMysql(banco);var professores=new RepositorioProfessoresMysql(banco);
            SwingUtilities.invokeLater(()->{
                var dialogos=new DialogosJOptionPane();
                var painel=new PainelIdentidade(identidade,dialogos,(sessao,aluno)->{
                    var acesso=new AcessoAcademicoAutenticado(identidade,sessao,alunos,professores);long autor=sessao.usuarioId();
                    if(!aluno)return new FluxoProfessores(new GestaoProfessores(autor,acesso,acesso,acesso,professores),dialogos,"SCOA · MySQL · operador autenticado");
                    var cadastrar=new CadastrarAluno(acesso,alunos);var consultar=new ConsultarAluno(acesso,alunos);
                    return new FluxoAlunos(new CadastroAlunoPorCpf(autor,acesso,acesso,cadastrar),new ControladorAlunos(autor,cadastrar,consultar,new AtualizarAluno(acesso,alunos)),dialogos,"SCOA · MySQL · operador autenticado",new GestaoFichaAluno(autor,consultar,acesso,acesso));
                },(sessao,tipo)->new br.edu.scoa.academico.apresentacao.curriculo.FluxoCatalogo(new br.edu.scoa.academico.aplicacao.curriculo.GestaoCatalogo(identidade,sessao,new RepositorioCatalogoMysql(banco),Clock.systemUTC()),tipo,dialogos),sessao->new br.edu.scoa.academico.apresentacao.curriculo.FluxoMatrizes(new br.edu.scoa.academico.aplicacao.curriculo.GestaoMatrizes(identidade,sessao,new RepositorioMatrizesMysql(banco),new RepositorioCatalogoMysql(banco),Clock.systemUTC()),dialogos),sessao->new br.edu.scoa.academico.apresentacao.curriculo.FluxoPeriodos(new br.edu.scoa.academico.aplicacao.curriculo.GestaoPeriodos(identidade,sessao,new RepositorioPeriodosMysql(banco),Clock.systemUTC()),dialogos),sessao->new br.edu.scoa.academico.apresentacao.turmas.FluxoTurmas(new br.edu.scoa.academico.aplicacao.turmas.GestaoTurmas(identidade,sessao,new RepositorioTurmasMysql(banco),new RepositorioPeriodosMysql(banco),new RepositorioCatalogoMysql(banco),professores,Clock.systemUTC()),dialogos));
                var janela=new JFrame("SCOA — Acadêmico persistente");janela.setContentPane(painel);janela.setSize(860,780);janela.setMinimumSize(new java.awt.Dimension(800,720));janela.setLocationRelativeTo(null);janela.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                janela.addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosing(java.awt.event.WindowEvent e){painel.encerrar();}});janela.setVisible(true);
            });
        }catch(Exception e){System.err.println(e instanceof FalhaIdentidade f?MensagensIdentidade.erro(f):"Não foi possível iniciar. Confira ambiente, banco e migrações. Nenhuma credencial será exibida.");System.exit(1);}
    }
}
