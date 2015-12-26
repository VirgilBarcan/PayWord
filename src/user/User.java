package user;

import broker.Broker;
import utils.Constants;
import utils.Crypto;
import vendor.Vendor;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by virgil on 25.12.2015.
 */
public class User {

    private Broker broker;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[] identity;
    private Account account;

    private Map<Vendor, Date> paymentsDone;
    private Map<Vendor, List<String>> hashChains;

    public User() {
        broker = Broker.getInstance();
        KeyPair keyPair = Crypto.getRSAKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        initIdentity();
        this.account = new Account();

        this.paymentsDone = new HashMap<>();
        this.hashChains = new HashMap<>();
    }

    public User(String identity) {
        broker = Broker.getInstance();
        KeyPair keyPair = Crypto.getRSAKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        initIdentity();

        //Copy the bytes from the given String
        byte[] stringIdentityBytes = identity.getBytes();
        for (int i = 0; i < stringIdentityBytes.length; ++i) {
            this.identity[i] = stringIdentityBytes[i];
        }
        this.account = new Account();

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

    public void setAccount(Account account) {
        this.account = account;
    }

    private Account getAccount() {
        return this.account;
    }

    public boolean registerToBroker(long creditLimit) {
        byte[] personalInfo = getPersonalInfo(creditLimit);

        //send personal info to the Broker
        boolean sendResult = broker.registerNewUser(personalInfo);

        //wait to get the certificate
        byte[] certificate = broker.getUserCertificate();

        return false;
    }

    /**
     * Get the array of byte that represents the personal info of the user
     * The format of the array is:
     *  - 4 bytes for the length of the identity
     *  - the identity bytes
     *  - 4 bytes for the length of the public key encoded value
     *  - the public key encoded value
     *  - 8 bytes for the account number
     *  - 8 bytes for the credit limit
     * @param creditLimit the credit limit
     * @return the array of bytes that represents the personal info of the user
     */
    private byte[] getPersonalInfo(long creditLimit) {
        int size = Constants.INT_NO_OF_BYTES + identity.length + Constants.INT_NO_OF_BYTES + publicKey.getEncoded().length + 2 * Constants.LONG_NO_OF_BYTES;
        byte[] personalInfo = new byte[size];

        int index = 0;

        System.out.println("User.getPersonalInfo: lengthOfIdentity=" + identity.length);
        //copy identity length
        byte[] lengthOfIdentity = ByteBuffer.allocate(Constants.INT_NO_OF_BYTES).putInt(identity.length).array();
        for (int i = 0; i < Constants.INT_NO_OF_BYTES; ++i, ++index) {
            personalInfo[index] = lengthOfIdentity[i];
        }

        //copy identity
        for (int i = 0; i < identity.length; ++i, ++index) {
            personalInfo[index] = identity[i];
        }

        byte[] publicKeyEncoded = publicKey.getEncoded();


        System.out.println("User.getPersonalInfo: publicKeyLength=" + publicKeyEncoded.length);
        //copy publicKey length
        byte[] lengthOfPublicKey = ByteBuffer.allocate(Constants.INT_NO_OF_BYTES).putInt(publicKeyEncoded.length).array();
        for (int i = 0; i < Constants.INT_NO_OF_BYTES; ++i, ++index) {
            personalInfo[index] = lengthOfPublicKey[i];
        }

        System.out.println("User.getPersonalInfo: userPublicKey=" + ((RSAPublicKey) publicKey).getModulus().toString());
        //copy publicKey
        for (int i = 0; i < publicKeyEncoded.length; ++i, ++index) {
            personalInfo[index] = publicKeyEncoded[i];
        }

        System.out.println("User.getPersonalInfo: accountNumber=" + getAccount().getAccountNumber());
        //copy account number
        byte[] accountNumberBytes = ByteBuffer.allocate(Constants.LONG_NO_OF_BYTES).putLong(getAccount().getAccountNumber()).array();
        for (int i = 0; i < Constants.LONG_NO_OF_BYTES; ++i, ++index) {
            personalInfo[index] = accountNumberBytes[i];
        }

        System.out.println("User.getPersonalInfo: creditLimit=" + creditLimit);
        //copy credit limit
        byte[] creditLimitBytes = ByteBuffer.allocate(Constants.LONG_NO_OF_BYTES).putLong(creditLimit).array();
        for (int i = 0; i < Constants.LONG_NO_OF_BYTES; ++i, ++index) {
            personalInfo[index] = creditLimitBytes[i];
        }

        return personalInfo;
    }
}
