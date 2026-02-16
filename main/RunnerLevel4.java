package main;

import java.util.List;
import java.util.Objects;

/**
 * COMPREHENSIVE ASSERT RUNNER (Levels 1-4)
 * This runner validates every operation using a unified assertion method.
 */
public class RunnerLevel4 {
    private static long startTime;

    public static void main(String[] args) {
        BankSystemLevel4 bank = new BankSystemLevel4();

        System.out.println("=== STARTING UNIFIED ASSERTION SUITE ===");
        startTimer();

        // --- LEVEL 1 & 2: ACCOUNT & RANKING ---
        assertTest("Create Alice", bank.createAccount(1, "alice"), true);
        assertTest("Create Bob", bank.createAccount(2, "bob"), true);
        assertTest("Deposit Alice", bank.depositMoney(10, "alice", 1000), 1000);
        assertTest("Withdraw Alice", bank.withdrawMoney(20, "alice", 200), 800);

        // --- TRANSFER TESTS ---
        assertTest("Transfer Alice -> Bob", bank.transferMoney(30, "alice", "bob", 300), 500); // Alice: 800-300=500
        assertTest("Check Bob Balance after transfer", bank.depositMoney(31, "bob", 0), 300);
        assertTest("Transfer Insufficient Funds", bank.transferMoney(40, "alice", "bob", 5000), null);
        assertTest("Transfer to Self", bank.transferMoney(45, "alice", "alice", 100), null);

        // Total Expenditures: Alice spent 200 (withdraw) + 300 (transfer) = 500
        assertTest("Top K Ranking (Expenditure check)", bank.getTopKExpenditures(46, 1), List.of("alice(500)"));

        // --- LEVEL 3: SCHEDULING & AUTOMATION ---
        String pay1 = bank.schedulePayment(50, "alice", 400, 50); // Due t=100
        assertTest("Initial status Pay1", bank.getPaymentStatus(60, "alice", pay1), "SCHEDULED");

        // --- CANCELLATION TEST ---
        String payToCancel = bank.schedulePayment(70, "bob", 100, 50); // Due t=120
        assertTest("Cancel Bob's payment at t=80", bank.cancelPayment(80, "bob", payToCancel), true);
        assertTest("Verify Cancelled Status", bank.getPaymentStatus(85, "bob", payToCancel), "CANCELLED");
        assertTest("Cancel already cancelled payment", bank.cancelPayment(90, "bob", payToCancel), false);

        // Trigger Pay1 (Alice 500 -> 100)
        assertTest("Alice Bal check at t=105", bank.depositMoney(105, "alice", 0), 100);
        assertTest("Verify Pay1 Executed", bank.getPaymentStatus(105, "alice", pay1), "EXECUTED");

        // Verify Bob's cancelled payment never executes (Balance remains 300)
        assertTest("Bob balance at t=125 (Cancelled payment)", bank.depositMoney(125, "bob", 0), 300);

        // --- LEVEL 4: HISTORY & MERGING ---
        System.out.println("\n--- Level 4: Time Machine & Merging ---");

        bank.createAccount(130, "charlie");
        bank.depositMoney(140, "charlie", 500);
        bank.withdrawMoney(150, "charlie", 100);

        assertTest("Charlie Hist t=145", bank.getBalance(160, "charlie", 145), 500);
        assertTest("Charlie Hist t=155", bank.getBalance(160, "charlie", 155), 400);

        // Merge Bob (300) -> Alice (100) = 400
        assertTest("Merge Bob into Alice", bank.mergeAccounts(170, "bob", "alice"), true);
        assertTest("Alice Bal Post-Merge", bank.depositMoney(171, "alice", 0), 400);

        // Transfer involving merged account
        assertTest("Transfer from Merged Account", bank.transferMoney(180, "bob", "charlie", 50), null);
        assertTest("Transfer to Merged Account", bank.transferMoney(185, "charlie", "bob", 50), null);

        stopTimer();
        System.out.println("\n=== ALL TESTS PASSED ===");
    }

    /**
     * Unified assertion method for all return types.
     */
    private static void assertTest(String description, Object actual, Object expected) {
        if (Objects.equals(actual, expected)) {
            System.out.println("✅ PASS: [" + description + "] -> " + actual);
        } else {
            System.err.println("❌ FAIL: [" + description + "]");
            System.err.println("   Expected: " + expected);
            System.err.println("   Actual:   " + actual);
        }
    }

    private static void startTimer() {
        startTime = System.currentTimeMillis();
    }

    private static void stopTimer() {
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("\nTotal Execution Time: " + duration + " ms");
        if (duration > 200) {
            System.out.println("⚠️ Warning: Execution is slow. Aim for < 500ms for large datasets.");
        }
    }
}