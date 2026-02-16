package main;

import java.util.Objects;

/**
 * CODE SIGNAL SIMULATOR (Levels 1)
 * Use this to verify logic correctness and execution speed.
 */
public class RunnerLevel1 {
    private static long startTime;

    public static void main(String[] args) {
        BankSystemLevel1 bank = new BankSystemLevel1();

        System.out.println("=== STARTING SIMULATION ===");
        startTimer();

        // --- LEVEL 1 TESTS ---
        System.out.println("\n[Level 1: Basic Operations]");
        test("Create Alice", bank.createAccount(1, "alice"), true);
        test("Create Bob", bank.createAccount(2, "bob"), true);
        test("Duplicate ID", bank.createAccount(3, "alice"), false);
        test("Deposit Alice", bank.depositMoney(10, "alice", 1000), 1000);
        test("Transfer Alice -> Bob", bank.transferMoney(20, "alice", "bob", 400), 600);
        test("Transfer Insufficient", bank.transferMoney(30, "alice", "bob", 5000), null);
        test("Transfer to Self", bank.transferMoney(40, "alice", "alice", 100), null);

        stopTimer();
        System.out.println("\n=== SIMULATION COMPLETE ===");
    }

    // --- HELPER METHODS ---

    private static void test(String description, Object actual, Object expected) {
        boolean passed = Objects.equals(actual, expected);
        String status = passed ? "✅ PASS" : "❌ FAIL";
        System.out.printf("%-10s | %-30s | Actual: %-15s | Expected: %s%n",
                status, description, actual, expected);
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
