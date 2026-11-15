package br.edu.scoa.academico.identidade;
import br.edu.scoa.academico.infraestrutura.identidade.SenhasPbkdf2;
import br.edu.scoa.academico.aplicacao.identidade.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SegurancaIdentidadeTest {
    @Test void hashTemSalAleatorioEConfereSenhaSemExporTexto() {
        var senhas=new SenhasPbkdf2(); char[] senha="Teste123!".toCharArray();
        String a=senhas.gerar(senha), b=senhas.gerar(senha);
        assertNotEquals(a,b); assertFalse(a.contains("Teste123!"));
        assertTrue(senhas.confere(senha,a)); assertFalse(senhas.confere("Outra123!".toCharArray(),a));
        assertFalse(senhas.confere(senha,"inválido"));
    }
    @Test void senhaCurtaRecusada() {
        assertThrows(FalhaIdentidade.class,()->new SenhasPbkdf2().gerar("curta".toCharArray()));
    }
}
