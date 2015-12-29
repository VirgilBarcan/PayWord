package vendor;

import broker.Broker;
import user.Account;
import user.User;
import user.UserInfo;
import utils.Commit;
import utils.Constants;
import utils.Crypto;
import utils.Payment;

import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;

/**
 * Created by virgil on 25.12.2015.
 */
public class Vendor {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[] identity;
    private Account account;

    private Map<UserInfo, Commit> userCommitments;
    private Map<UserInfo, List<Payment>> userPayments;

    private List<UserInfo> allUsers;

    public Vendor() {
        KeyPair keyPair = Crypto.getRSAKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        initIdentity();
        this.account = new Account();

        this.userCommitments = new HashMap<>();
        this.userPayments = new HashMap<>();

        this.allUsers = new ArrayList<>();
    }

    public Vendor(String identity) {
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

        this.userCommitments = new HashMap<>();
        this.userPayments = new HashMap<>();

        this.allUsers = new ArrayList<>();
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

    private UserInfo getUserWithIdentity(byte[] userIdentity) {
        for (UserInfo userInfo : allUsers)
            if (Arrays.equals(userInfo.getIdentity(), userIdentity))
                return userInfo;
        return null;
    }

    /**
     * Add a new commit from a user
     * @param commit
     * @return
     */
    public boolean addNewCommit(User user, Commit commit) {
        //check U's signature on commit
        //get the unsigned part
        int size = 892; //the no of bytes of the message without the signed hash
        byte[] message = Arrays.copyOfRange(commit.getBytes(), 0, size);

        //get the signed hash
        byte[] signedHash = Arrays.copyOfRange(commit.getBytes(), size, commit.getBytes().length);

        Signature signature = null;
        boolean result = false;
        try {
            signature = Signature.getInstance("SHA1WithRSA");
            signature.initVerify(user.getPublicKey());
            signature.update(message);
            result = signature.verify(signedHash);
            System.out.println("Vendor.addNewCommit: verify User signature on commit result: " + result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        if (result) {
            //check B's signature on C(U)
            //extract C(U) from the commit
            byte[] userCertificate = Arrays.copyOfRange(commit.getBytes(), this.identity.length, this.identity.length + 732);

            //get the unsigned part
            byte[] unsignedPart = Arrays.copyOfRange(userCertificate, 0, 604);

            //get the signed hash
            signedHash = Arrays.copyOfRange(userCertificate, 604, userCertificate.length);

            //check the Broker signature on the user certificate
            signature = null;
            try {
                Broker broker = Broker.getInstance();
                signature = Signature.getInstance("SHA1WithRSA");
                signature.initVerify(broker.getPublicKey());
                signature.update(unsignedPart);
                result = signature.verify(signedHash);
                System.out.println("Vendor.addNewCommit: verify Broker signature on User certificate result: " + result);
                if (result) {
                    //TODO: extract userInfo from the commit, if possible
                    UserInfo userInfo = new UserInfo();
                    userInfo.setIdentity(user.getIdentity());

                    userCommitments.put(userInfo, commit);
                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }

        }

        return result;
    }

    public boolean addNewPayment(User user, Payment payment) {
        //TODO: extract userInfo from the commit, if possible
        UserInfo userInfo = new UserInfo();
        userInfo.setIdentity(user.getIdentity());

        if (userPayments.get(userInfo) != null) {
            List<Payment> listOfPayments = userPayments.get(userInfo);
            listOfPayments.add(payment);
            userPayments.remove(userInfo);
            userPayments.put(userInfo, listOfPayments);
        }
        else {
            List<Payment> listOfPayments = new ArrayList<>();
            listOfPayments.add(payment);
            userPayments.put(userInfo, listOfPayments);
        }

        System.out.println("Vendor.addNewPayment: paymentNo=" + payment.getPaywordNo());

        return true;
    }

    /**
     * This is the third step in the scheme
     * The Vendor has to send to the Broker a message containing: commit(U), c(l), l, where l is the last index of a payment
     * @return true if the action completed with success, false otherwise
     */
    public boolean redeem() {

        //redeem all payments done by all users
        for (UserInfo userInfo : userCommitments.keySet()) {

            List<Payment> paymentList = userPayments.get(userInfo);
            int size = userCommitments.get(userInfo).getBytes().length + paymentList.get(paymentList.size() - 1).getBytes().length + Constants.INT_NO_OF_BYTES;
            byte[] message = new byte[size];

            int index = 0;

            //copy the commit
            byte[] commitBytes = userCommitments.get(userInfo).getBytes();
            for (int i = 0; i < commitBytes.length; ++i, ++index)
                message[index] = commitBytes[i];

            //copy the last payword received
            byte[] lastPaywordBytes = paymentList.get(paymentList.size() - 1).getBytes();
            for (int i = 0; i < lastPaywordBytes.length; ++i, ++index)
                message[index] = lastPaywordBytes[i];

            //copy the index of the last payword received
            byte[] lastPaywordIndexBytes = ByteBuffer.allocate(4).putInt(paymentList.size()).array();
            for (int i = 0; i < lastPaywordIndexBytes.length; ++i, ++index)
                message[index] = lastPaywordIndexBytes[i];

            //send message to the Broker to redeem the payments
            Broker broker = Broker.getInstance();
            broker.redeem(this, message);
        }

        return true;
    }
}
