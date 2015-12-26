package user;

import utils.Constants;
import utils.Crypto;
import vendor.Vendor;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by virgil on 25.12.2015.
 */
public class User {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[] identity;

    private Map<Vendor, Date> paymentsDone;
    private Map<Vendor, List<String>> hashChains;

    public User() {
        KeyPair keyPair = Crypto.getRSAKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        initIdentity();

        this.paymentsDone = new HashMap<>();
        this.hashChains = new HashMap<>();
    }

    public User(String identity) {
        KeyPair keyPair = Crypto.getRSAKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        initIdentity();

        //Copy the bytes from the given String
        byte[] stringIdentityBytes = identity.getBytes();
        for (int i = 0; i < stringIdentityBytes.length; ++i) {
            this.identity[i] = stringIdentityBytes[i];
        }

        this.paymentsDone = new HashMap<>();
        this.hashChains = new HashMap<>();
    }

    private void initIdentity() {
        this.identity = new byte[Constants.IDENTITY_NO_OF_BITS / 8];
        for (int i = 0; i < Constants.IDENTITY_NO_OF_BITS / 8; ++i) {
            this.identity[i] = 0;
        }
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    private PrivateKey getPrivateKey() {
        return this.privateKey;
    }

}
