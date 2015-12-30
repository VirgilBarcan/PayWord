package broker;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by virgil on 31.12.2015.
 */
public class BrokerServer implements Runnable {

    private static final int PORT = 1994;

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
            int messageID = dataInputStream.readInt();

            System.out.println("BrokerServer.run: messageID=" + messageID);

            //TODO: Do here all things involving the Broker depending on what the client asked
            try {
                Thread.sleep(10000);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            //TODO: Send data response to client
            String timeStamp = new java.util.Date().toString();
            String returnCode = "ServerSocket responded at "+ timeStamp + (char) 13;
            BufferedOutputStream os = new BufferedOutputStream(connection.getOutputStream());
            OutputStreamWriter osw = new OutputStreamWriter(os, "US-ASCII");
            osw.write(returnCode);
            osw.flush();
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
}
