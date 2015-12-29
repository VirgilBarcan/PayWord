package broker;

import user.UserInfo;
import utils.Constants;
import utils.Crypto;
import vendor.Vendor;
import vendor.VendorInfo;

import java.nio.ByteBuffer;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by virgil on 11.12.2015.
 */
public class Broker {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[] identity;
    private List<UserInfo> registeredUsers;
    private List<VendorInfo> registeredVendors;

    private Bank bank;
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
        this.registeredUsers = new ArrayList<>();
        this.registeredVendors = new ArrayList<>();

        this.bank = Bank.getInstance();
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

        this.registeredUsers = new ArrayList<>();
        this.registeredVendors = new ArrayList<>();

        this.bank = Bank.getInstance();
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

        //get length of userIdentity
        byte[] lengthOfIdentityBytes = new byte[4];
        lengthOfIdentityBytes = Arrays.copyOfRange(personalInfo, indexStart, indexEnd);
        int lengthOfIdentity = ByteBuffer.wrap(lengthOfIdentityBytes).getInt();

        System.out.println("Broker.registerNewUser: lengthOfIdentity=" + lengthOfIdentity);

        indexStart = indexEnd;
        indexEnd += lengthOfIdentity;

        //get userIdentity
        byte[] userIdentity = new byte[lengthOfIdentity];
        userIdentity = Arrays.copyOfRange(personalInfo, indexStart, indexEnd);
        String print = "";
        for (int i = 0; i < userIdentity.length; ++i) {
            print += userIdentity[i];
        }
        System.out.println("Broker.registerNewUser: userIdentity=" + print);

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
        print = "";
        for (int i = 0; i < publicKeyBytes.length; ++i) {
            print += publicKeyBytes[i];
        }
        System.out.println("Broker.registerNewUser: publicKeyBytes=" + print);

        indexStart = indexEnd;
        indexEnd += 8;

        //get account number
        byte[] accountNumberBytes = new byte[8];
        accountNumberBytes = Arrays.copyOfRange(personalInfo, indexStart, indexEnd);
        long userAccountNumber = ByteBuffer.wrap(accountNumberBytes).getLong();

        System.out.println("Broker.registerNewUser: userAccountNumber=" + userAccountNumber);

        indexStart = indexEnd;
        indexEnd += 8;

        //get credit limit
        byte[] creditLimitBytes = new byte[8];
        creditLimitBytes = Arrays.copyOfRange(personalInfo, indexStart, indexEnd);
        long userCreditLimit = ByteBuffer.wrap(creditLimitBytes).getLong();

        System.out.println("Broker.registerNewUser: userCreditLimit=" + userCreditLimit);

        //Store all info received from the user in some kind of structure
        UserInfo userInfo = new UserInfo();
        userInfo.setIdentity(userIdentity);
        userInfo.setPublicKey(userPublicKey);
        userInfo.setAccountNumber(userAccountNumber);
        userInfo.setCreditLimit(userCreditLimit);

        if (registeredUsers.contains(userInfo))
            return false;
        else {
            registeredUsers.add(userInfo);
            return true;
        }
    }

    /**
     * Get the certificate for the user given by the userIdentity
     * @param userIdentity the identity of the user
     * @return the certificate
     */
    public byte[] getUserCertificate(byte[] userIdentity) {
        UserInfo userInfo = getUserWithIdentity(userIdentity);

        int size = this.identity.length +
                userInfo.getIdentity().length;
        size += this.publicKey.getEncoded().length + userInfo.getPublicKey().getEncoded().length;
        size += 8; //the date is stored as 64 bits long value, therefore 8 bytes
        size += 8; //the accountNumber is stored as 64 bits long value, therefore 8 bytes
        size += 8; //the creditLimit is stored as 64 bits long value, therefore 8 bytes
        byte[] message = new byte[size];

        int index = 0;
        //copy the identity of the Broker
        for (int i = 0; i < this.identity.length; ++i, ++index) {
            message[index] = this.identity[i];
        }
        String print = "";
        for (int i = 0; i < this.identity.length; ++i) {
            print += this.identity[i];
        }
        System.out.println("Broker.registerNewUser: identity=" + print);

        //copy the identity of the User
        for (int i = 0; i < userIdentity.length; ++i, ++index) {
            message[index] = userIdentity[i];
        }
        print = "";
        for (int i = 0; i < userIdentity.length; ++i) {
            print += userIdentity[i];
        }
        System.out.println("Broker.registerNewUser: userIdentity=" + print);

        //copy the publicKey of the Broker
        byte[] publicKeyEncoded = publicKey.getEncoded();
        for (int i = 0; i < publicKeyEncoded.length; ++i, ++index) {
            message[index] = publicKeyEncoded[i];
        }

        //copy the publicKey of the User
        byte[] userPublicKeyEncoded = userInfo.getPublicKey().getEncoded();
        for (int i = 0; i < userPublicKeyEncoded.length; ++i, ++index) {
            message[index] = userPublicKeyEncoded[i];
        }

        //copy the expire date of the message
        LocalDateTime currentDateLocalDateTime = LocalDateTime.now();
        LocalDateTime expireDateLocalDate = currentDateLocalDateTime.plusMonths(1);
        long expireDateLong = expireDateLocalDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        byte[] expireDateBytes =  ByteBuffer.allocate(8).putLong(expireDateLong).array();
        for (int i = 0; i < expireDateBytes.length; ++i, ++index) {
            message[index] = expireDateBytes[i];
        }

        //copy the accountNumber
        byte[] accountNumberBytes = ByteBuffer.allocate(8).putLong(userInfo.getAccountNumber()).array();
        for (int i = 0; i < accountNumberBytes.length; ++i, ++index) {
            message[index] = accountNumberBytes[i];
        }

        //copy the creditLimit
        byte[] creditLimitBytes = ByteBuffer.allocate(8).putLong(userInfo.getCreditLimit()).array();
        for (int i = 0; i < creditLimitBytes.length; ++i, ++index) {
            message[index] = creditLimitBytes[i];
        }

        System.out.println("Broker.getUserCertificate: message length=" + message.length);
        print = "";
        for (int i = 0; i < size; ++i) {
            print += message[i];
        }
        System.out.println("Broker.getUserCertificate: message=" + print);

        //hash and sign
        size += 20; //the length of the hash of, SHA-1 gives 160 bits of output
        byte[] certificate = new byte[size];
        index = 0;
        for (int i = 0; i < message.length; ++i, ++index) {
            certificate[index] = message[i];
        }

        //copy the hash of the message that is build so far
        byte[] hash = Crypto.hashMessage(message);

        //TODO: Sign the hash

        for (int i = 0; i < hash.length; ++i, ++index) {
            certificate[index] = hash[i];
        }

        System.out.println("Broker.getUserCertificate: certificate length=" + certificate.length);
        print = "";
        for (int i = 0; i < size; ++i) {
            print += certificate[i];
        }
        System.out.println("Broker.getUserCertificate: certificate=" + print);

        return certificate;
    }

    private UserInfo getUserWithIdentity(byte[] userIdentity) {
        for (UserInfo userInfo : registeredUsers)
            if (Arrays.equals(userInfo.getIdentity(), userIdentity))
                return userInfo;
        return null;
    }

    public boolean redeem(Vendor vendor, byte[] message) {
        //TODO: check commit(U)

        //TODO: check last payment (apply hash function l times)

        //TODO: check if payment is authentic and not already redeemed


        //get User identity from the certificate inside the commit


        //TODO: make payment to Vendor and take money from User
        int lastPaymentIndex = ByteBuffer.wrap(message, message.length - 4, 4).getInt();
        System.out.println("Broker.redeem: lastPaymentIndex=" + lastPaymentIndex);

        //Proof of Concept
        bank.takeMoneyFromAccount(1, lastPaymentIndex);
        bank.addMoneyToAccount(vendor.getAccount().getAccountNumber(), lastPaymentIndex);

        return true;
    }
}
