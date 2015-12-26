package broker;

import user.User;
import utils.Constants;
import utils.Crypto;
import vendor.Vendor;

import java.nio.ByteBuffer;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
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

    public boolean registerNewUser(byte[] personalInfo) {
        int indexStart = 0;
        int indexEnd = 0;

        //get all info from the array

        indexStart = indexEnd;
        indexEnd += 4;

        //get length of identity
        byte[] lengthOfIdentityBytes = new byte[4];
        lengthOfIdentityBytes = Arrays.copyOfRange(personalInfo, indexStart, indexEnd);
        int lengthOfIdentity = ByteBuffer.wrap(lengthOfIdentityBytes).getInt();

        System.out.println("Broker.registerNewUser: lengthOfIdentity=" + lengthOfIdentity);

        indexStart = indexEnd;
        indexEnd += lengthOfIdentity;

        //get identity
        byte[] identity = new byte[lengthOfIdentity];
        identity = Arrays.copyOfRange(personalInfo, indexStart, indexEnd);

        indexStart = indexEnd;
        indexEnd += 4;

        //get length of public key
        byte[] lengthOfPublicKeyBytes = new byte[4];
        lengthOfPublicKeyBytes = Arrays.copyOfRange(personalInfo, indexStart, indexEnd);
        int lengthOfPublicKey = ByteBuffer.wrap(lengthOfPublicKeyBytes).getInt();

        System.out.println("Broker.registerNewUser: lengthOfPublicKey=" + lengthOfPublicKey);

        indexStart = indexEnd;
        indexEnd += lengthOfPublicKey;

        //get public key
        byte[] publicKeyBytes = new byte[lengthOfPublicKey];
        publicKeyBytes = Arrays.copyOfRange(personalInfo, indexStart, indexEnd);

        PublicKey userPublicKey = null;
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            userPublicKey = keyFactory.generatePublic(keySpec);

            System.out.println("Broker.registerNewUser: userPublicKey=" + ((RSAPublicKey) userPublicKey).getModulus().toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        indexStart = indexEnd;
        indexEnd += 8;

        //get account number
        byte[] accountNumberBytes = new byte[8];
        accountNumberBytes = Arrays.copyOfRange(personalInfo, indexStart, indexEnd);
        long accountNumber = ByteBuffer.wrap(accountNumberBytes).getLong();

        System.out.println("Broker.registerNewUser: accountNumber=" + accountNumber);

        indexStart = indexEnd;
        indexEnd += 8;

        //get credit limit
        byte[] creditLimitBytes = new byte[8];
        creditLimitBytes = Arrays.copyOfRange(personalInfo, indexStart, indexEnd);
        long creditLimit = ByteBuffer.wrap(creditLimitBytes).getLong();

        System.out.println("Broker.registerNewUser: creditLimit=" + creditLimit);

        return false;
    }

    public byte[] getUserCertificate() {

        return new byte[0];
    }
}
