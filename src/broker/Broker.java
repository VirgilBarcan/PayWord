package broker;

/**
 * Created by virgil on 11.12.2015.
 */
public class Broker {

    private static Broker instance;

    public static Broker getInstance() {
        if (instance == null) {
            instance = new Broker();
        }

        return instance;
    }

    private Broker() {

    }

}
