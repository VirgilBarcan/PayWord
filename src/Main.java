import broker.Broker;
import user.User;
import vendor.Vendor;

/**
 * Created by virgil on 26.12.2015.
 */
public class Main {

    public static void main(String[] args) {
        Broker broker = Broker.getInstance();
        User user1 = new User("user1@gmail.com");
        Vendor vendor1 = new Vendor("vendor1@gmail.com");

        user1.registerToBroker(1000);
    }

}
