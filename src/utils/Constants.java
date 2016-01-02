package utils;

/**
 * Created by virgil on 26.12.2015.
 */
public class Constants {

    public static final int IDENTITY_NO_OF_BITS = 1024;
    public static final int KEY_NO_OF_BITS = 1024;
    public static final int LONG_NO_OF_BYTES = 8;
    public static final int INT_NO_OF_BYTES = 4;

    public static final String LOCALHOST = "localhost";

    public static class CommunicationProtocol {
        public static final int END_COMMUNICATION = -1;
        public static final int OK = 1;
        public static final int NOK = 0;
        public static final int REGISTER_TO_BROKER = 11;
        public static final int GET_IDENTITY = 111;
        public static final int MAKE_PAYMENT = 1111;
    }

}
