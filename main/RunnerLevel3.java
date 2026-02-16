package main;

import java.util.*;

/**
 * Runner for BankSystem Level 3.
 * Tests:
 * 1. Payment scheduling and status tracking.
 * 2. Automatic execution when the timestamp advances.
 * 3. Payment cancellation logic.
 * 4. Insufficient funds handling at execution time.
 */
public class RunnerLevel3 {

    private static long startTime;

    public static void main(String[] args) {
        BankSystemLevel3 bank = new BankSystemLevel3();

        System.out.println("--- Level 3 Integration Testing ---");
        startTimer();

        // 1. Setup Accounts
        bank.createAccount(1, "alice");
        bank.createAccount(2, "bob");
        bank.depositMoney(10, "alice", 1000);
        bank.depositMoney(11, "bob", 1000);

        // 2. Schedule a valid payment
        // Alice pays 400 at t=50 (scheduled at t=20 with 30 delay)
        String pay1 = bank.schedulePayment(20, "alice", 400, 30);
        System.out.println("[TEST] Scheduled Pay1: " + pay1);

        // Verify status before execution time
        assertStatus(bank, 40, "alice", pay1, "SCHEDULED");

        // 3. Test Cancellation
        String pay2 = bank.schedulePayment(41, "bob", 100, 50); // Due t=91
        System.out.println("[TEST] Scheduled Pay2 for Bob: " + pay2);
        boolean cancelled = bank.cancelPayment(45, "bob", pay2);
        System.out.println("[TEST] Cancel Pay2 result: " + cancelled);
        assertStatus(bank, 46, "bob", pay2, "CANCELLED");

        // 4. Trigger Execution
        // Calling any method at t >= 50 should trigger Pay1
        System.out.println("[TEST] Advancing clock to t=60...");
        String pay1Status = bank.getPaymentStatus(60, "alice", pay1);
        System.out.println("[TEST] Pay1 Status at t=60: " + pay1Status); // Expected: EXECUTED

        // Check Alice's balance (should be 1000 - 400 = 600)
        // Note: Using deposit 0 to check balance without a dedicated getBalance method
        Integer aliceBalance = bank.depositMoney(61, "alice", 0);
        System.out.println("[TEST] Alice Balance after Pay1: " + aliceBalance);

        // 5. Test Insufficient Funds (Failure)
        // Alice now has 600. Schedule 800 at t=100.
        String pay3 = bank.schedulePayment(70, "alice", 800, 30);
        System.out.println("[TEST] Scheduled Pay3 (800) for Alice: " + pay3);

        // Advance clock to t=110
        String pay3Status = bank.getPaymentStatus(110, "alice", pay3);
        System.out.println("[TEST] Pay3 Status at t=110: " + pay3Status); // Expected: FAILED

        // 6. Ranking Integrity with Scheduled Payments
        // Alice spent 400 (Pay1), Bob spent 0 (Pay2 was cancelled)
        List<String> topK = bank.getTopKExpenditures(120, 2);
        System.out.println("[TEST] Top Expenditures: " + topK); // Expected: ["alice(400)"]

        stopTimer();
        System.out.println("\n=== SIMULATION COMPLETE ===");
    }

    private static void assertStatus(BankSystemLevel3 bank, int ts, String acc, String pId, String expected) {
        String actual = bank.getPaymentStatus(ts, acc, pId);
        if (actual.equals(expected)) {
            System.out.println("✅ PASS: Status is " + actual);
        } else {
            System.out.println("❌ FAIL: Status is " + actual + " (Expected " + expected + ")");
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