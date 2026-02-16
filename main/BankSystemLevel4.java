package main;

import java.util.*;

/*
 * LEVEL 4: History & Merging
 * - getBalance: TreeMap lookup for historical state.
 * - mergeAccounts: Close source account, transfer balance.
 * - All methods: Add isMerged check. except createAccount, getTopKExpenditures, cancelPayment/getPaymentStatus updateFinancials
 */
public class BankSystemLevel4 {
    private static class Account {
        String id;
        long balance = 0L;
        long totalOutgoing = 0L;
        // NEW level 4
        private final TreeMap<Integer, Long> history = new TreeMap<>();
        boolean isMerged = false;
        long mergedtimestamp = -1;

        public Account(String id, int timestamp) {
            this.id = id;
            //NEW
            this.history.put(timestamp, 0L);
        }
    }

    private static class Payment {
        String id, accId, status = "SCHEDULED";
        long amt, time;

        public Payment(String id, String accId, long amt, long time){
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
    private final Map<String,Payment> allPayments = new HashMap<>();
    private final PriorityQueue<Payment> pq = new PriorityQueue<>(Comparator.comparingLong(p -> p.time));


    //NEW Level 4
    public Integer getBalance(int timestamp, String accountId, int queryTime) {
        processScheduled(timestamp);
        Account acc = accounts.get(accountId);
        // account is only valid until mergedtimestamp . at and after it will return null
        if(acc == null || (acc.isMerged && queryTime >= acc.mergedtimestamp)) return null;

        // use floor entry to search for historical check
        Map.Entry<Integer,Long> entry = acc.history.floorEntry(queryTime);
        // return record that is at querytime
        return ( entry == null ) ? null : entry.getValue().intValue();
    }

    //NEW Level 4
    public boolean mergeAccounts(int timestamp, String accountId1, String accountId2) {
        // acc1 acc2
        Account a1 = accounts.get(accountId1);
        Account a2 = accounts.get(accountId2);
        // do validation before merging if any error return false
        if(a1==null || a2==null || a1==a2 || a1.isMerged || a2.isMerged ) return false;
        // remove a1 from ranking
        ranking.remove(a1);
        // get current a1 balance
        long bal = a1.balance;
        // set merged flag and timestamp and balance to 0
        a1.balance = 0; a1.isMerged = true; a1.mergedtimestamp = timestamp;
        // update acc2
        updateFinancials(a2, bal, false, timestamp);
        return true;
    }

    private void processScheduled(int now) {
        // check current queue
        while(!pq.isEmpty() && pq.peek().time <= now) {
            // extract lastest payment top of stack
            Payment p = pq.poll();
            // if payment status cancel still continue
            if("CANCELLED".equals(p.status)) continue;
            // check acc
            Account acc = accounts.get(p.accId);
            // if payment amt is greater balance EXECUTED Payment
            if(acc != null && !acc.isMerged && acc.balance >= p.amt) {
                updateFinancials(acc, -p.amt, true, (int) p.time); //NEW
                p.status ="EXECUTED";
            } else {
                p.status= "FAILED";
            }
        }
    }

    public boolean cancelPayment(int timestamp, String accountId, String paymentId) {
        processScheduled(timestamp);
        Payment p = allPayments.get(paymentId);
        // if payment dont exist and account doest match and it is not schedule we skip cancel
        if(p == null || !p.accId.equals(accountId) || !"SCHEDULED".equals(p.status)) return false;
        p.status = "CANCELLED";

        return true;
    }

    public String schedulePayment(int timestamp, String accountId, int amount, int delay) {
        processScheduled(timestamp);
        // check if account match or amount is more than 0
        Account acc = accounts.get(accountId);
        if(acc == null || acc.isMerged || amount <= 0) return null;
        // generate pid
        String pId = "pay" + (allPayments.size() + 1);
        // create new payment with pid
        Payment p = new Payment(pId, accountId, (long) amount, (long) timestamp+delay);
        // add this new payment into Priority queue
        pq.add(p);
        // insert into allPayment hashMap
        allPayments.put(pId, p);
        return pId;
    }

    public String getPaymentStatus(int timestamp, String accountId, String paymentId) {
        processScheduled(timestamp);
        Payment p = allPayments.get(paymentId);
        if(p == null || !p.accId.equals(accountId)) return "INVALID_ACCOUNT";
        return p.status;
    }

    public boolean createAccount(int timestamp, String accountId) {
        processScheduled(timestamp);
        if(accounts.containsKey(accountId)) return false;
        Account acc = new Account(accountId, timestamp);
        accounts.put(accountId, acc);
        ranking.add(acc);
        return true;
    }

    public Integer depositMoney(int timestamp, String accountId, int amount) {
        processScheduled(timestamp); // process timestamp
        Account acc = accounts.get(accountId);
        if(acc == null || acc.isMerged || amount < 0) return null;

        updateFinancials(acc, +(long) amount, false, timestamp);
        return (int) acc.balance;
    }

    public Integer withdrawMoney(int timestamp, String accountId, int amount) {
        processScheduled(timestamp); // process timestamp
        Account acc = accounts.get(accountId);
        if(acc == null || acc.isMerged ||acc.balance <= amount || amount < 0) return null;

        updateFinancials(acc, -(long) amount, true, timestamp);
        return (int) acc.balance;
    }

    public Integer transferMoney(int timestamp, String srcId, String destId, int amount) {
        processScheduled(timestamp); // process timestamp
        Account src = accounts.get(srcId);
        Account dest = accounts.get(destId);
        if(src == null || dest == null || src == dest || src.isMerged || dest.isMerged
                || amount <= 0 || src.balance < amount) return null;

        updateFinancials(src, -(long) amount, true, timestamp);
        updateFinancials(dest, +(long) amount, false, timestamp);
        return (int) src.balance;
    }

    private void updateFinancials(Account acc, long change, boolean isExpense, int timestamp) {
        // remove acc from ranking
        ranking.remove(acc);
        // balance change update
        acc.balance += change;
        // if expense and update the change
        if(isExpense && change < 0) acc.totalOutgoing += Math.abs(change);
        // NEW level 4 add account history update
        acc.history.put(timestamp,acc.balance);
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