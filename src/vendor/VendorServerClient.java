package vendor;

import backend.Account;
import broker.Bank;
import broker.Broker;
import utils.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by virgil on 31.12.2015.
 */
public class VendorServerClient {

    private int port;
    private Vendor vendor;

    public VendorServerClient(int port) {
        this.port = port;
    }

    public void initServer() {
        int connectionsCount = 0;

        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                Socket connection = serverSocket.accept();
                Runnable runnable = new ConnectionRunnable(connection, ++connectionsCount);
                Thread thread = new Thread(runnable);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public static void main(String[] args) {
        Bank bank = Bank.getInstance();

        //TODO: Get all this info from args or ask user via Console
        String vendorIdentity = "vendor1@gmail.com";
        long accountNo = 1000;
        long accountBalance = 9999;
        int port = 2001;

        if (args.length != 0) {
            vendorIdentity = args[0];
            accountNo = Long.parseLong(args[1]);
            accountBalance = Long.parseLong(args[2]);
            port = Integer.parseInt(args[3]);
        }

        Account vendorAccount = new Account(accountNo, accountBalance);
        bank.addUserAccount(vendorAccount);
        Vendor vendor = new Vendor(vendorIdentity);
        vendor.setAccount(vendorAccount);

        VendorServerClient vendorServerClient = new VendorServerClient(port);
        vendorServerClient.setVendor(vendor);
        vendorServerClient.initServer();
    }


    private class ConnectionRunnable implements Runnable {

        private Socket connection;
        private int connectionID;

        public ConnectionRunnable(Socket connection, int connectionID) {
            this.connection = connection;
            this.connectionID = connectionID;
        }

        @Override
        public void run() {
            try {
                DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

                int commandID;

                while ((commandID = dataInputStream.readInt()) != Constants.CommunicationProtocol.END_COMMUNICATION) {
                    System.out.println("VendorServerClient.ConnectionRunnable.run: commandID=" + commandID);

                    //TODO: Do here all things that the client asked
                    processCommand(commandID, dataInputStream, dataOutputStream);

                    //TODO: Send data response to client
                    dataOutputStream.writeInt(commandID);
                }

                System.out.println("VendorServerClient.ConnectionRunnable.run: Communication with the User ended!");

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    connection.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        private void processCommand(int commandID, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
            System.out.println("VendorServerClient.ConnectionRunnable.processCommand: commandID=" + commandID);
            try {
                switch (commandID) {
                    case Constants.CommunicationProtocol.GET_IDENTITY:
                        //send the identity length
                        dataOutputStream.writeInt(vendor.getIdentity().length);

                        //send the identity
                        dataOutputStream.write(vendor.getIdentity());

                        break;

                    case Constants.CommunicationProtocol.MAKE_PAYMENT:
                        //TODO: Handle the payment

                        //Proof of concept: will use some result that depends on the result of the action
                        if (true) {
                            dataOutputStream.writeInt(Constants.CommunicationProtocol.OK);
                        }
                        else {
                            dataOutputStream.writeInt(Constants.CommunicationProtocol.NOK);
                        }

                        break;

                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
