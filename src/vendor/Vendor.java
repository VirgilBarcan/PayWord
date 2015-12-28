package vendor;

import broker.Broker;
import user.UserInfo;
import utils.Commit;
import utils.Constants;
import utils.Crypto;
import utils.Payment;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by virgil on 25.12.2015.
 */
public class Vendor {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[] identity;

    private Map<UserInfo, Commit> userCommitments;
    private Map<UserInfo, List<Payment>> userPayments;

    public Vendor() {
        KeyPair keyPair = Crypto.getRSAKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        initIdentity();

        this.userCommitments = new HashMap<>();
        this.userPayments = new HashMap<>();
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

        this.userCommitments = new HashMap<>();
        this.userPayments = new HashMap<>();
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

    /**
     * Add a new commit from a user
     * @param commit
     * @return
     */
    public boolean addNewCommit(Commit commit) {
        //TODO: extract userInfo from the commit, if possible
        UserInfo userInfo = new UserInfo();
        userInfo.setIdentity(new byte[1024]);

        userCommitments.put(userInfo, commit);

        //TODO: check U's signature on commit


        //TODO: check B's signature on C(U)

        return true;
    }

    public boolean addNewPayment(Payment payment) {
        //TODO: extract userInfo from the commit, if possible
        UserInfo userInfo = new UserInfo();
        userInfo.setIdentity(new byte[1024]);

        List<Payment> listOfPayments = new ArrayList<>();
        listOfPayments.add(payment);
        userPayments.put(userInfo, listOfPayments);

        System.out.println("Vendor.addNewPayment: paymentNo=" + payment.getPaywordNo());

        return true;
    }

    /**
     * This is the third step in the scheme
     * The Vendor has to send to the Broker a message containing: commit(U), c(l), l, where l is the last index of a payment
     * @return true if the action completed with success, false otherwise
     */
    public boolean redeem() {
        //TODO: fix this
        UserInfo userInfo = new UserInfo();
        userInfo.setIdentity(new byte[1024]);

        List<Payment> paymentList = userPayments.get(userInfo);
        int size = userCommitments.get(userInfo).getBytes().length + paymentList.get(paymentList.size() - 1).getBytes().length + Constants.INT_NO_OF_BYTES;
        byte[] message = new byte[24];

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
        broker.redeem(message);

        return true;
    }
}
