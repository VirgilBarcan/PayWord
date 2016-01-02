package user;

import backend.Account;
import backend.Commit;
import broker.Bank;
import broker.BrokerServer;
import utils.Constants;
import vendor.VendorInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by virgil on 31.12.2015.
 */
public class UserClient {

    //region Declarations
    private User user;
    private String brokerHostname;
    private int brokerPort;
    private Socket brokerConnection;
    private DataInputStream brokerDataInputStream;
    private DataOutputStream brokerDataOutputStream;

    private byte[] vendorIdentity;
    private Socket vendorConnection;
    private DataInputStream vendorDataInputStream;
    private DataOutputStream vendorDataOutputStream;
    //endregion

    //region Broker
    public boolean connectToBroker(String brokerHostname, int brokerPort) {
        System.out.println("UserClient.connectToBroker");
        try {
            this.brokerHostname = brokerHostname;
            this.brokerPort = brokerPort;

            InetAddress brokerAddress = InetAddress.getByName(this.brokerHostname);
            this.brokerConnection = new Socket(brokerAddress, this.brokerPort);

            this.brokerDataInputStream = new DataInputStream(brokerConnection.getInputStream());
            this.brokerDataOutputStream = new DataOutputStream(brokerConnection.getOutputStream());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean endCommunicationWithBroker() {
        System.out.println("UserClient.endCommunicationWithBroker");
        try {
            this.brokerDataOutputStream.writeInt(Constants.CommunicationProtocol.END_COMMUNICATION);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean registerToBroker(long creditLimit) {
        System.out.println("UserClient.registerToBroker");
        try {
            int response;

            //send register command + all info required and wait for confirmation
            do {
                //send REGISTER_TO_BROKER command
                this.brokerDataOutputStream.writeInt(Constants.CommunicationProtocol.REGISTER_TO_BROKER);

                byte[] personalInfo = this.user.getPersonalInfo(creditLimit);
                //send length of personal info message
                int personalInfoLength = personalInfo.length;
                this.brokerDataOutputStream.writeInt(personalInfoLength);

                //send personal info
                this.brokerDataOutputStream.write(personalInfo);

                //wait for confirmation
                response = this.brokerDataInputStream.readInt();
            }while(response == Constants.CommunicationProtocol.NOK);

            //get the user certificate length
            int userCertificateLength = this.brokerDataInputStream.readInt();

            //get the user certificate
            byte[] userCertificate = new byte[userCertificateLength];
            this.brokerDataInputStream.read(userCertificate);
            this.user.setUserCertificate(userCertificate);

            System.out.println("UserClient.registerToBroker: register OK");

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    //endregion

    //region Vendor
    public boolean connectToVendor(String vendorHostname, int vendorPort) {
        System.out.println("UserClient.connectToVendor");
        try {
            InetAddress vendorAddress = InetAddress.getByName(vendorHostname);
            this.vendorConnection = new Socket(vendorAddress, vendorPort);

            this.vendorDataInputStream = new DataInputStream(vendorConnection.getInputStream());
            this.vendorDataOutputStream = new DataOutputStream(vendorConnection.getOutputStream());
            System.out.println("UserClient.connectToVendor: connection started");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean endCommunicationWithVendor() {
        System.out.println("UserClient.endCommunicationWithVendor");
        try {
            this.vendorDataOutputStream.writeInt(Constants.CommunicationProtocol.END_COMMUNICATION);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean getVendorIdentity() {
        System.out.println("UserClient.getVendorIdentity");
        try {
            //send GET_IDENTITY command
            this.vendorDataOutputStream.writeInt(Constants.CommunicationProtocol.GET_IDENTITY);

            //get the identity length
            int vendorIdentityLength = this.vendorDataInputStream.readInt();
            this.vendorIdentity = new byte[vendorIdentityLength]; //should be 128 bits (1024 bits for the identity)

            //get the identity
            this.vendorDataInputStream.read(this.vendorIdentity);
            System.out.println("UserClient.getVendorIdentity: vendorIdentity=" + Arrays.toString(this.vendorIdentity));

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean makePaymentToVendor() {
        System.out.println("UserClient.makePaymentToVendor");
        try {
            int response;

            //send make payment command + all info required and wait for confirmation
            do {
                //send MAKE_PAYMENT command
                this.vendorDataOutputStream.writeInt(Constants.CommunicationProtocol.MAKE_PAYMENT);

                int paymentNo;

                VendorInfo vendorInfo = new VendorInfo();
                vendorInfo.setIdentity(vendorIdentity);

                if (user.isFirstPayment(vendorInfo)) {
                    paymentNo = 0;

                    //generate the new hash chain for this vendor
                    user.generateNewHashChain(vendorInfo);

                    //compute the commit(V)
                    Commit commit = user.computeCommitment(vendorInfo);

                    //TODO: send the commit to the vendor
                    //user.sendCommit(vendor, commit);
                }
                else {
                    paymentNo = user.getVendorNoOfPayments(vendorInfo);
                }

                //TODO: send the payment to the vendor
                //user.makePayment(vendor, paymentNo);

                //wait for confirmation
                response = this.vendorDataInputStream.readInt();
            }while(response == Constants.CommunicationProtocol.NOK);

            System.out.println("UserClient.makePaymentToVendor: payment DONE");

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    //endregion

    public void setUser(User user) {
        this.user = user;
    }


    public static void main(String[] args) {
        Bank bank = Bank.getInstance();

        //TODO: Get all this info from args or ask user via Console
        String userIdentity = "user@gmail.com";
        long accountNo = 1;
        long accountBalance = 1000;
        long creditLimit = 1000;

        if (args.length != 0) {
            userIdentity = args[0];
            accountNo = Long.parseLong(args[1]);
            accountBalance = Long.parseLong(args[2]);
            creditLimit = Long.parseLong(args[3]);
        }

        Account userAccount = new Account(accountNo, accountBalance);
        bank.addUserAccount(userAccount);
        User user = new User(userIdentity);
        user.setAccount(userAccount);

        UserClient userClient = new UserClient();
        userClient.connectToBroker(Constants.LOCALHOST, BrokerServer.PORT);
        userClient.setUser(user);

        //register to the Broker
        userClient.registerToBroker(creditLimit);

        //end communication with the Broker
        userClient.endCommunicationWithBroker();


        //communicate with a Vendor; the Vendor will be given by its port
        //TODO: Get the Vendor by asking the user
        int vendorPort = 2001;
        userClient.connectToVendor(Constants.LOCALHOST, vendorPort);
        userClient.getVendorIdentity();
        userClient.makePaymentToVendor();
        userClient.endCommunicationWithVendor();
    }

}
