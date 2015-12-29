package user;

import broker.Broker;
import utils.*;
import vendor.Vendor;

import java.nio.ByteBuffer;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by virgil on 25.12.2015.
 */
public class User {

    private Broker broker;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[] identity;
    private Account account;

    private byte[] userCertificate;

    private int hashChainLength;
    private Map<Vendor, List<Payment>> paymentsDone;
    private Map<Vendor, List<List<Payword>>> hashChains;

    public User() {
        broker = Broker.getInstance();
        KeyPair keyPair = Crypto.getRSAKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        initIdentity();
        this.account = new Account();

        this.hashChainLength = 10000;
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

        this.hashChainLength = 10000;
        this.paymentsDone = new HashMap<>();
        this.hashChains = new HashMap<>();
    }

    private void initIdentity() {
        this.identity = new byte[Constants.IDENTITY_NO_OF_BITS / 8];
        for (int i = 0; i < Constants.IDENTITY_NO_OF_BITS / 8; ++i) {
            this.identity[i] = 0;
        }
    }

    public byte[] getIdentity() {
        return this.identity;
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

    public Account getAccount() {
        return this.account;
    }

    /**
     * This is the first step in the scheme, the registration of the User to the Broker
     * The User sends his personal info to the Broker
     * The Broker sends the certificate
     * @param creditLimit the credit limit that the user wants to be imposed
     * @return true if the action completed with success, false otherwise
     */
    public boolean registerToBroker(long creditLimit) {
        byte[] personalInfo = getPersonalInfo(creditLimit);

        //send personal info to the Broker
        boolean sendResult = broker.registerNewUser(personalInfo);

        //wait to get the certificate
        this.userCertificate = broker.getUserCertificate(identity);
        System.out.println("User.registerToBroker: certificate length=" + userCertificate.length);
        String print = "";
        for (int i = 0; i < userCertificate.length; ++i) {
            print += userCertificate[i];
        }
        System.out.println("User.registerToBroker: certificate=" + print);

        return sendResult;
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
        String print = "";
        for (int i = 0; i < identity.length; ++i) {
            print += identity[i];
        }
        System.out.println("User.getPersonalInfo: identity=" + print);

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
        print = "";
        for (int i = 0; i < publicKeyEncoded.length; ++i) {
            print += publicKeyEncoded[i];
        }
        System.out.println("User.getPersonalInfo: userPublicKey=" + print);

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

    /**
     * This is the second step in the scheme
     *
     * @param vendor the Vendor that the User wants to pay
     * @return true if the action completed with success, false otherwise
     */
    public boolean payVendor(Vendor vendor) {
        int paymentNo;
        if (isFirstPayment(vendor)) {
            paymentNo = 0;

            //generate the new hash chain for this vendor
            generateNewHashChain(vendor);

            //compute the commit(V)
            Commit commit = computeCommitment(vendor);

            //send the commit to the vendor
            sendCommit(vendor, commit);
        }
        else {
            paymentNo = paymentsDone.get(vendor).size();
        }

        //send the payment to the vendor
        makePayment(vendor, paymentNo);

        return false;
    }

    /**
     * Check if this Vendor was already payed this day
     * @param vendor the Vendor
     * @return true if this is the first payment to the Vendor, false otherwise
     */
    private boolean isFirstPayment(Vendor vendor) {
        return !paymentsDone.containsKey(vendor);
    }

    /**
     * Generate a new hash chain for the Vendor, in order to make it possible to pay him
     * @param vendor the Vendor
     */
    private void generateNewHashChain(Vendor vendor) {
        System.out.println("Started generating hash chain");
        List<Payword> currentHashChain = new ArrayList<>();

        byte[] cn = Crypto.getSecret(1024);

        Payword last = new Payword(cn); //c(n-1)
        currentHashChain.add(last);
        for (int i = this.hashChainLength - 2; i >= 0; --i) {
            Payword current = new Payword(last);
            currentHashChain.add(current);

            last = current;
        }

        System.out.println("Finished generating hash chain");

        if (hashChains.get(vendor) != null) {
            List<List<Payword>> vendorPreviousHashChains = hashChains.get(vendor);
            vendorPreviousHashChains.add(currentHashChain);
            hashChains.remove(vendor);
            hashChains.put(vendor, vendorPreviousHashChains);
        }
        else {
            List<List<Payword>> vendorPreviousHashChains = new ArrayList<>();
            vendorPreviousHashChains.add(currentHashChain);
            hashChains.put(vendor, vendorPreviousHashChains);
        }

    }

    /**
     * Generate a commit for the Vendor
     * commit(V) = sigU(V, C(U), c0, D, I), where
     *  V is the identity of the Vendor,
     *  C(U) is the certificate of the User, generated by the Broker,
     *  c0 is the root of the hash chain,
     *  D is the current date,
     *  I are additional info: length of the chain, etc.
     * @param vendor the Vendor
     * @return the commit
     */
    private Commit computeCommitment(Vendor vendor) {
        int size = vendor.getIdentity().length + userCertificate.length + 20 + Constants.LONG_NO_OF_BYTES + Constants.INT_NO_OF_BYTES;
        byte[] message = new byte[size];

        System.out.println("User.computeCommitment: size = " + size);

        int index = 0;

        //copy vendor's identity
        byte[] vendorIdentity = vendor.getIdentity();
        for (int i = 0; i < vendorIdentity.length; ++i, ++index) {
            message[index] = vendorIdentity[i];
        }

        //copy user certificate
        for (int i = 0; i < this.userCertificate.length; ++i, ++index) {
            message[index] = this.userCertificate[i];
        }

        //copy the root of the signedHash chain, c0
        List<List<Payword>> allHashChains = hashChains.get(vendor);
        List<Payword> lastHashChainComputed = allHashChains.get(allHashChains.size() - 1);
        byte[] c0 = lastHashChainComputed.get(this.hashChainLength - 1).getBytes();
        for (int i = 0; i < c0.length; ++i, ++index) {
            message[index] = c0[i];
        }

        //copy the current date
        LocalDateTime currentDateLocalDateTime = LocalDateTime.now();
        long currentDateLong = currentDateLocalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        byte[] currentDateBytes = ByteBuffer.allocate(8).putLong(currentDateLong).array();
        for (int i = 0; i < currentDateBytes.length; ++i, ++index) {
            message[index] = currentDateBytes[i];
        }

        //copy the length of the chain
        byte[] lengthOfChainBytes = ByteBuffer.allocate(4).putInt(this.hashChainLength).array();
        for (int i = 0; i < lengthOfChainBytes.length; ++i, ++index) {
            message[index] = lengthOfChainBytes[i];
        }

        //hash and sign
        byte[] signedHash = null;

        //sign the message
        Signature sig = null;
        try {
            sig = Signature.getInstance("SHA1WithRSA");
            sig.initSign(getPrivateKey());
            sig.update(message);
            signedHash = sig.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        size += signedHash.length; //the length of the signed hash
        byte[] commitBytes = new byte[size];
        index = 0;
        for (int i = 0; i < message.length; ++i, ++index) {
            commitBytes[index] = message[i];
        }

        for (int i = 0; i < signedHash.length; ++i, ++index) {
            commitBytes[index] = signedHash[i];
        }

        Commit commit = new Commit(commitBytes);

        return commit;
    }

    /**
     * Send the commit to the Vendor
     * @param vendor the Vendor
     * @param commit the commit
     */
    private void sendCommit(Vendor vendor, Commit commit) {
        vendor.addNewCommit(this, commit);
    }

    /**
     * Make a new payment to the vendor
     * @param vendor the Vendor
     * @param paymentNo the index of the payment
     */
    private void makePayment(Vendor vendor, int paymentNo) {
        byte[] bytes = new byte[24];

        System.out.println("User.makePayment: paymentNo=" + paymentNo);

        int index = 0;

        //copy the paymentNo-th payword
        List<List<Payword>> allHashChains = hashChains.get(vendor);
        List<Payword> lastHashChainComputed = allHashChains.get(allHashChains.size() - 1);
        byte[] ci = lastHashChainComputed.get(this.hashChainLength - paymentNo - 1).getBytes();
        for (int i = 0; i < ci.length; ++i, ++index) {
            bytes[index] = ci[i];
        }

        //copy the bytes of paymentNo
        byte[] paymentNoBytes = ByteBuffer.allocate(4).putInt(paymentNo).array();
        for (int i = 0; i < paymentNoBytes.length; ++i, ++index) {
            bytes[index] = paymentNoBytes[i];
        }

        Payment payment = new Payment(bytes);
        vendor.addNewPayment(this, payment);

        List<Payment> paymentList;
        if (paymentsDone.get(vendor) != null)
            paymentList = paymentsDone.get(vendor);
        else
            paymentList = new ArrayList<>();

        paymentList.add(payment);
        paymentsDone.remove(vendor);
        paymentsDone.put(vendor, paymentList);
    }
}
