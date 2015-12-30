package user;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by virgil on 31.12.2015.
 */
public class UserClient {

    private User user;

    public static void main(String[] args) {
        String host = "localhost";
        int port = 1994;

        try {
            InetAddress address = InetAddress.getByName(host);
            Socket connection = new Socket(address, port);

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeInt(1); //Proof of Concept

            //TODO: Send command to Broker/Vendor server and wait for response


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
