package br.edu.scoa.academico.infraestrutura.identidade;
import br.edu.scoa.academico.aplicacao.identidade.*;
import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
public final class SenhasPbkdf2 implements Senhas {
    private static final int ITERACOES=600_000;
    public String gerar(char[] senha){
        if(senha==null||senha.length<8||senha.length>128)throw new FalhaIdentidade(FalhaIdentidade.Codigo.SENHA_INVALIDA);
        byte[] sal=new byte[16];new SecureRandom().nextBytes(sal);
        return "pbkdf2-sha256$"+ITERACOES+"$"+Base64.getEncoder().encodeToString(sal)+"$"+Base64.getEncoder().encodeToString(derivar(senha,sal,ITERACOES));
    }
    public boolean confere(char[] senha,String hash){
        if(senha==null||senha.length>128||hash==null)return false;
        try{
            String[] partes=hash.split("\\$");if(partes.length!=4||!partes[0].equals("pbkdf2-sha256"))return false;
            int iteracoes=Integer.parseInt(partes[1]);if(iteracoes!=ITERACOES)return false;
            byte[] sal=Base64.getDecoder().decode(partes[2]), esperado=Base64.getDecoder().decode(partes[3]);
            if(sal.length!=16||esperado.length!=32)return false;
            return MessageDigest.isEqual(esperado,derivar(senha,sal,iteracoes));
        }catch(IllegalArgumentException e){return false;}
    }
    private byte[] derivar(char[] senha,byte[] sal,int iteracoes){
        var spec=new PBEKeySpec(senha,sal,iteracoes,256);
        try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}
        catch(GeneralSecurityException e){throw new IllegalStateException("PBKDF2 indisponível",e);}
        finally{spec.clearPassword();}
    }
}
