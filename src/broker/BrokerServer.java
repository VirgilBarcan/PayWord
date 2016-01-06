package broker;

import utils.Constants;
import vendor.VendorInfo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by virgil on 31.12.2015.
 */
public class BrokerServer implements Runnable {

    public static final int PORT = 1994;

    private Socket connection;
    private int connectionID;

    private Broker broker = Broker.getInstance();

    public BrokerServer(Socket connection, int connectionID) {
        this.connection = connection;
        this.connectionID = connectionID;
    }


    public static void main(String[] args) {
        int count = 0;

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true) {
                Socket connection = serverSocket.accept();
                Runnable runnable = new BrokerServer(connection, ++count);
                Thread thread = new Thread(runnable);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

            int commandID;

            while ((commandID = dataInputStream.readInt()) != Constants.CommunicationProtocol.END_COMMUNICATION) {
                System.out.println("BrokerServer.run: commandID=" + commandID);

                //TODO: Do here all things involving the Broker depending on what the client asked
                processCommand(commandID, dataInputStream, dataOutputStream);
            }

            System.out.println("BrokerServer.run: Communication with the User ended!");

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
        System.out.println("BrokerServer.processCommand: commandID=" + commandID);
        switch (commandID) {
            case Constants.CommunicationProtocol.REGISTER_TO_BROKER:
                registerToBroker(dataInputStream, dataOutputStream);
                break;

            case Constants.CommunicationProtocol.REDEEM:
                redeem(dataInputStream, dataOutputStream);
                break;

            default:
                break;
        }
    }

    private boolean registerToBroker(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        try {
            //wait for user personal info length
            int lengthOfUserPersonalInfo = dataInputStream.readInt();

            //wait for user personal info
            byte[] userPersonalInfo = new byte[lengthOfUserPersonalInfo];
            dataInputStream.read(userPersonalInfo);

            //send data to broker instance
            boolean resultOfRegister = broker.registerNewUser(userPersonalInfo);

            if (resultOfRegister) {
                System.out.println("BrokerServer.registerToBroker: send OK");
                dataOutputStream.writeInt(Constants.CommunicationProtocol.OK);

                byte[] userCertificate = broker.getUserCertificate(broker.getUserIdentityFromPersonalInfo(userPersonalInfo));
                System.out.println("BrokerServer.registerToBroker: userCertificate=" + Arrays.toString(userCertificate));
                //send the user certificate length
                dataOutputStream.writeInt(userCertificate.length);

                //send the user certificate
                dataOutputStream.write(userCertificate);
            } else {
                System.out.println("BrokerServer.registerToBroker: send NOK");
                dataOutputStream.writeInt(Constants.CommunicationProtocol.NOK);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean redeem(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        try {
            //wait for redeem message length
            int lengthOfRedeemMessage = dataInputStream.readInt();

            //wait for redeem message bytes
            byte[] redeemMessageBytes = new byte[lengthOfRedeemMessage];
            dataInputStream.read(redeemMessageBytes);

            //TODO: Handle the redeem
            System.out.println("BrokerServer.redeem: redeemMessage=" + Arrays.toString(redeemMessageBytes));
            VendorInfo vendorInfo = new VendorInfo();
            vendorInfo.setAccountNumber(3);
            boolean resultOfRedeem = broker.redeem(vendorInfo, redeemMessageBytes);

            if (resultOfRedeem) {
                dataOutputStream.writeInt(Constants.CommunicationProtocol.OK);
            } else {
                dataOutputStream.writeInt(Constants.CommunicationProtocol.NOK);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }
}
