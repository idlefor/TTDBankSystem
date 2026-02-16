package main;

import java.util.*;

/*
 * Level 1: Account Management (Goal: 10 Minutes)
 * - add new static account class
 * - createAccount: Return false if ID exists.
 * - depositMoney: Return balance or null if ID missing.
 * - transferMoney: Return src balance or null if invalid/insufficient.
 */
public class BankSystemLevel1 {
    private static class Account {
        String id;
        long balance = 0L;

        public Account(String id) {
            this.id = id;
        }
    }

    // System State
    private final Map<String, Account> accounts = new HashMap<>();

    public boolean createAccount(int timestamp, String accountId) {
        Account acc = new Account(accountId);
        if(accounts.containsKey(accountId)) return false;

        accounts.put(accountId, acc);
        return true;
    }

    public Integer depositMoney(int timestamp, String accountId, int amount) {
        Account acc = accounts.get(accountId);
        if(acc == null || amount <= 0) return null;

        acc.balance += (long) amount;
        return (int) acc.balance;
    }

    public Integer transferMoney(int timestamp, String srcId, String destId, int amount) {
        Account src = accounts.get(srcId);
        Account dest = accounts.get(destId);
        if(src == null || dest == null || src==dest || amount <= 0 || src.balance < amount) return null;

        src.balance -= (long) amount;
        dest.balance += (long) amount;
        return (int) src.balance;
    }
}