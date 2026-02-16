package main;

import java.util.*;

/*
 * LEVEL 2: Statistics & Withdrawals- Ranking & Indexing (Goal: 15 Minutes)
 * - withdrawMoney: Decrease balance, track expenditure.
 * - getTopKExpenditures: Return formatted list of top spenders.
 * - Update transferMoney/depositMoney: Transfers now count as expenditure for the sender!
 */
public class BankSystemLevel2 {
    private static class Account {
        String id;
        long balance = 0L;
        long totalOutgoing = 0L;//NEW
        public Account(String id) { this.id = id;}
    }

    private final Map<String, Account> accounts = new HashMap<>();
    //NEW system state
    private final TreeSet<Account> ranking = new TreeSet<>(
            (a,b) -> {
                if(a.totalOutgoing != b.totalOutgoing) return Long.compare(b.totalOutgoing,a.totalOutgoing);
                return a.id.compareTo(b.id);
            });

    //NEW
    public Integer withdrawMoney(int timestamp, String accountId, int amount) {
        Account acc = accounts.get(accountId);
        // if lack of fund or amount less than 0 return null
        if(acc == null || acc.balance < amount || amount <0) return null;

        updateFinancials(acc, -(long) amount, true);
        return (int) acc.balance;
    }
    //NEW
    public Integer transferMoney(int timestamp, String srcId, String destId, int amount) {
        Account src = accounts.get(srcId);
        Account dest = accounts.get(destId);
        if(src == null || dest == null || src == dest || amount <= 0 || src.balance < amount) return null;

        updateFinancials(src, -(long) amount, true);//NEW
        updateFinancials(dest, +(long) amount, false);//NEW
        return (int) src.balance;
    }

    //NEW
    private void updateFinancials(Account acc, long change, boolean isExpense) {
        // remove acc from ranking first
        ranking.remove(acc);
        // update the acc change amount
        acc.balance += change;
        // if expense update the change
        if(isExpense && change < 0) acc.totalOutgoing = Math.abs(change);
        //add back acc to ranking after above change
        ranking.add(acc);
    }

    //NEW
    public List<String> getTopKExpenditures(int timestamp, int k) {
       List<String> res = new ArrayList<>();
       int count = 0;

       for(Account a : ranking) {
           if(count >= k || (a.totalOutgoing == 0)) break;
           res.add(a.id + "(" + a.totalOutgoing +")");
           count++;
       }
       return res;
    }

    public boolean createAccount(int timestamp, String accountId) {
        if(accounts.containsKey(accountId)) return false;
        Account acc = new Account(accountId);
        accounts.put(accountId, acc);
        ranking.add(acc); // NEW
        return true;
    }

    public Integer depositMoney(int timestamp, String accountId, int amount) {
        Account acc = accounts.get(accountId);
        if(acc == null || amount < 0) return null;

        updateFinancials(acc, +(long) amount, false);// NEW
        return (int) acc.balance;
    }
}