package vendor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by virgil on 31.12.2015.
 */
public class VendorServerClient {

    private Vendor vendor;

    public static void main(String[] args) {
        String host = "localhost";
        int port = 1994;

        try {
            InetAddress address = InetAddress.getByName(host);
            Socket connection = new Socket(address, port);

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeInt(1); //Proof Of Concept
            //TODO: Wait command from User and respond

            //TODO: Send command to Broker server and wait for response


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
