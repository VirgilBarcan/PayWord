package user;

import backend.Account;
import broker.Bank;
import broker.BrokerServer;
import utils.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by virgil on 31.12.2015.
 */
public class UserClient {

    private User user;
    private String brokerHostname;
    private int brokerPort;
    private Socket brokerConnection;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public UserClient(String brokerHostname, int brokerPort) {
        this.brokerHostname = brokerHostname;
        this.brokerPort = brokerPort;

        connectToBroker();
    }

    private void connectToBroker() {
        System.out.println("UserClient.connectToBroker");
        try {
            InetAddress brokerAddress = InetAddress.getByName(this.brokerHostname);
            this.brokerConnection = new Socket(brokerAddress, this.brokerPort);

            this.dataInputStream = new DataInputStream(brokerConnection.getInputStream());
            this.dataOutputStream = new DataOutputStream(brokerConnection.getOutputStream());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean endCommunicationWithBroker() {
        System.out.println("UserClient.endCommunicationWithBroker");
        try {
            this.dataOutputStream.writeInt(Constants.CommunicationProtocol.END_COMMUNICATION);
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
                this.dataOutputStream.writeInt(Constants.CommunicationProtocol.REGISTER_TO_BROKER);

                byte[] personalInfo = this.user.getPersonalInfo(creditLimit);
                //send length of personal info message
                int lengthOfPersonalInfo = personalInfo.length;
                this.dataOutputStream.writeInt(lengthOfPersonalInfo);

                //send personal info
                this.dataOutputStream.write(personalInfo);

                //wait for confirmation
                response = this.dataInputStream.readInt();
            }while(response == Constants.CommunicationProtocol.NOK);

            System.out.println("UserClient.registerToBroker: register OK");

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void setUser(User user) {
        this.user = user;
    }



    public static void main(String[] args) {
        Bank bank = Bank.getInstance();

        //TODO: Get all this info from args or ask user via Console
        String userIdentity = "user1@gmail.com";
        long accountNo = 1;
        long accountBalance = 1000;
        long creditLimit = 1000;

        if (args.length != 0) {
            userIdentity = args[0];
            accountNo = Integer.parseInt(args[1]);
            accountBalance = Integer.parseInt(args[2]);
            creditLimit = Integer.parseInt(args[3]);
        }

        Account user1Account = new Account(accountNo, accountBalance);
        bank.addUserAccount(user1Account);
        User user1 = new User(userIdentity);
        user1.setAccount(user1Account);

        UserClient userClient = new UserClient(Constants.LOCALHOST, BrokerServer.PORT);
        userClient.setUser(user1);

        //register to the Broker
        userClient.registerToBroker(creditLimit);

        //end communication with the Broker
        userClient.endCommunicationWithBroker();
    }

}
