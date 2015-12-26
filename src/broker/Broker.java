package broker;

import user.User;
import utils.Constants;
import utils.Crypto;
import vendor.Vendor;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by virgil on 11.12.2015.
 */
public class Broker {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[] identity;
    private List<User> registerdUsers;
    private List<Vendor> registeredVendors;

    private static Broker instance;

    public static Broker getInstance() {
        if (instance == null) {
            instance = new Broker();
        }

        return instance;
    }

    private Broker() {
        KeyPair keyPair = Crypto.getRSAKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        initIdentity();
        this.registerdUsers = new ArrayList<>();
        this.registeredVendors = new ArrayList<>();
    }

    public Broker(String identity) {
        KeyPair keyPair = Crypto.getRSAKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        initIdentity();

        //Copy the bytes from the given String
        byte[] stringIdentityBytes = identity.getBytes();
        for (int i = 0; i < stringIdentityBytes.length; ++i) {
            this.identity[i] = stringIdentityBytes[i];
        }

        this.registerdUsers = new ArrayList<>();
        this.registeredVendors = new ArrayList<>();
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
