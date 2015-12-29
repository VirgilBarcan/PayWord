import broker.Bank;
import broker.Broker;
import user.Account;
import user.User;
import vendor.Vendor;

/**
 * Created by virgil on 26.12.2015.
 */
public class Main {

    public static void main(String[] args) {
        Bank bank = Bank.getInstance();
        Broker broker = Broker.getInstance();

        Account user1Account = new Account(1, 10000);
        bank.addUserAccount(user1Account);
        User user1 = new User("user1@gmail.com");
        user1.setAccount(user1Account);

        Account user2Account = new Account(2, 9000);
        bank.addUserAccount(user2Account);
        User user2 = new User("user2@gmail.com");
        user2.setAccount(user2Account);

        Account vendor1Account = new Account(3, 900000);
        bank.addUserAccount(vendor1Account);
        Vendor vendor1 = new Vendor("vendor1@gmail.com");
        vendor1.setAccount(vendor1Account);

        user1.registerToBroker(1000);
        user2.registerToBroker(1234);

        System.out.println("User1 account balance=" + bank.getAccountBalance(user1.getAccount().getAccountNumber()));
        System.out.println("Vendor1 account balance=" + bank.getAccountBalance(vendor1.getAccount().getAccountNumber()));
        for (int i = 0; i < 10; ++i) {
            user1.payVendor(vendor1);
        }
        user2.payVendor(vendor1);
        vendor1.redeem();

        System.out.println("User1 account balance=" + bank.getAccountBalance(user1.getAccount().getAccountNumber()));
        System.out.println("Vendor1 account balance=" + bank.getAccountBalance(vendor1.getAccount().getAccountNumber()));
    }

}
