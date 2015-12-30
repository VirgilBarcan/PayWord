package broker;

import backend.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by virgil on 29.12.2015.
 */
public class Bank {

    private List<Account> accounts;

    private static Bank instance;

    private Bank() {
        accounts = new ArrayList<>();
    }

    public static Bank getInstance() {
        if (instance == null) {
            instance = new Bank();
        }

        return instance;
    }

    public void addUserAccount(Account account) {
        this.accounts.add(account);
    }

    public void takeMoneyFromAccount(long accountNo, double sumToTake) {
        for (Account account : accounts) {
            if (account.getAccountNumber() == accountNo) {
                double oldBalance = account.getAccountBalance();
                account.setAccountBalance(oldBalance - sumToTake);
            }
        }
    }

    public void addMoneyToAccount(long accountNo, double sumToAdd) {
        for (Account account : accounts) {
            if (account.getAccountNumber() == accountNo) {
                double oldBalance = account.getAccountBalance();
                account.setAccountBalance(oldBalance + sumToAdd);
            }
        }
    }

    public double getAccountBalance(long accountNo) {
        for (Account account : accounts) {
            if (account.getAccountNumber() == accountNo) {
                return account.getAccountBalance();
            }
        }

        return Double.NaN;
    }
}
