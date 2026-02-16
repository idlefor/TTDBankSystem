package main;

import java.util.*;

/*
 * LEVEL 3: Scheduled Payments
 * - add new static payment class
 * - schedulePayment: Add to PriorityQueue with delay. return pId
 * - processScheduled: The "Clock" logic that runs before every method.
 * - cancelPayment/getPaymentStatus: Manage the queue.
 */
public class BankSystemLevel3 {
    private static class Account {
        String id;
        long balance = 0L;
        long totalOutgoing = 0L;

        public Account(String id) { this.id = id;}
    }
    //NEW LEVEL3 new class
    private static class Payment {
        String id, accId, status = "SCHEDULED";
        long amt, time;

        public Payment(String id, String accId, long amt, long time) {
            this.id = id;
            this.accId = accId;
            this.amt = amt;
            this.time = time;
        }
    }

    // System State
    private final Map<String, Account> accounts = new HashMap<>();
    private final TreeSet<Account> ranking = new TreeSet<>(
            (a,b) -> {
                if(a.totalOutgoing != b.totalOutgoing) return Long.compare(b.totalOutgoing, a.totalOutgoing);
                return a.id.compareTo(b.id);
            });
    //NEW LEVEL 3 System State
    private final HashMap<String, Payment> allPayments = new HashMap<>();
    private final PriorityQueue<Payment> pq = new PriorityQueue<>(Comparator.comparingLong(p -> p.time));

    //NEW
    public boolean cancelPayment(int timestamp, String accountId, String paymentId) {
        processScheduled(timestamp);
        Payment p = allPayments.get(paymentId);
        // payment dont exist or it is not scheduled we return false
        if(p == null || !p.accId.equals(accountId) || !"SCHEDULED".equals(p.status)) return false;
        p.status = "CANCELLED";

        return true;
    }

    //NEW
    public String schedulePayment(int timestamp, String accountId, int amount, int delay) {
        processScheduled(timestamp);
        if(!accounts.containsKey(accountId) || amount <= 0) return null;
        // set up pid
        String pId = "pay" +(allPayments.size() +1);
        // setup payment
        Payment p = new Payment(pId, accountId, (long) amount, (long) timestamp + delay);
        // put payment into AllPayment
        allPayments.put(pId, p);
        // add payment into pq
        pq.add(p);
        return pId;
    }

    //NEW
    public String getPaymentStatus(int timestamp, String accountId, String paymentId) {
        processScheduled(timestamp);
        Payment p = allPayments.get(paymentId);

        // if payment id mismatch return invalid account
        if(p == null || !p.accId.equals(accountId)) return "INVALID_ACCOUNT";
        return p.status;
    }

    //NEW this is main engine
    private void processScheduled(int now) {
        // find out how many payment to now use priority queue peek
        while (!pq.isEmpty() && pq.peek().time <= now) {
            // top most account to get from poll
            Payment p = pq.poll();
            // check if cancelled
            if("CANCELLED".equals(p.status)) continue;
            // check account
            Account acc = accounts.get(p.accId);
            // if acc balance is greater than amount execute it
            if(acc != null || acc.balance >= p.amt) {
                updateFinancials(acc, -(long) p.amt, true);
                p.status = "EXECUTED";
            } else {
                p.status = "FAILED"; // else fail
            }
        }
    }

    public boolean createAccount(int timestamp, String accountId) {
        processScheduled(timestamp); //NEW
        if(accounts.containsKey(accountId)) return false;
        Account acc = new Account(accountId);
        accounts.put(accountId, acc);
        ranking.add(acc);
        return true;
    }

    public Integer depositMoney(int timestamp, String accountId, int amount) {
        processScheduled(timestamp);//NEW
        Account acc = accounts.get(accountId);
        if(acc == null || amount <= 0) return null;

        updateFinancials(acc, +(long) amount, false);
        return (int) acc.balance;
    }

    public Integer withdrawMoney(int timestamp, String accountId, int amount) {
        processScheduled(timestamp);//NEW
        Account acc = accounts.get(accountId);
        if(acc == null || acc.balance <= amount || amount < 0) return null;

        updateFinancials(acc, -(long) amount, true);
        return (int) acc.balance;
    }

    public Integer transferMoney(int timestamp, String srcId, String destId, int amount) {
        processScheduled(timestamp);//NEW
        Account src = accounts.get(srcId);
        Account dest = accounts.get(destId);
        if(src == null || dest == null || src == dest || amount <= 0 || src.balance < amount) return null;

        updateFinancials(src, -(long) amount, true);
        updateFinancials(dest, +(long) amount, false);
        return (int) src.balance;
    }

    private void updateFinancials(Account acc, long change, boolean isExpense) {
        // remove acc from ranking
        ranking.remove(acc);
        // balance change update
        acc.balance += change;
        // if expense and update the change
        if(isExpense && change < 0) acc.totalOutgoing += Math.abs(change);
        //add back acc to ranking after update change
        ranking.add(acc);
    }

    public List<String> getTopKExpenditures(int timestamp, int k) {
        processScheduled(timestamp);
        List<String> res = new ArrayList<>();
        int count = 0;

        for(Account a: ranking) {
            if(count >= k || a.totalOutgoing == 0) break;
            res.add(a.id + "(" + a.totalOutgoing + ")");
            count++;
        }
        return res;
    }
}