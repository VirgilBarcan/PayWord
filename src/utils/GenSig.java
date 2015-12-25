package utils;

import sun.misc.BASE64Encoder;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by virgil on 25.12.2015.
 */
public class GenSig {

   public static void main(String[] args) throws Exception {
       KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
       kpg.initialize(1024);
       KeyPair keyPair = kpg.genKeyPair();
       PrivateKey privateKey = keyPair.getPrivate();
       PublicKey publicKey = keyPair.getPublic();

       System.out.println("privateKey length=" + ((RSAPrivateKey) privateKey).getModulus().bitLength());
       System.out.println("publicKey length=" + ((RSAPublicKey) publicKey).getModulus().bitLength());

       byte[] data = "test".getBytes("UTF8");

       Signature sig = Signature.getInstance("SHA1withRSA");
       sig.initSign(keyPair.getPrivate());
       sig.update(data);
       byte[] signatureBytes = sig.sign();
       System.out.println("Singature:" + new BASE64Encoder().encode(signatureBytes));

       sig.initVerify(keyPair.getPublic());
       sig.update(data);

       System.out.println(sig.verify(signatureBytes));
  }

}
