package utils;

import java.security.*;

/**
 * Created by virgil on 26.12.2015.
 */
public class Crypto {

    public static KeyPair getRSAKeyPair() {
        KeyPairGenerator keyPairGenerator = null;
        KeyPair keyPair = null;

        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(Constants.KEY_NO_OF_BITS);

            keyPair = keyPairGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return keyPair;
    }

}
