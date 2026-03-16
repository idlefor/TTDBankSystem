# High-Performance Bank System (Java)

This document outlines the architectural design and implementation strategy for a high-performance banking system simulation. The design is optimized to handle large-scale transaction volumes and real-time ranking queries within the constraints of competitive programming environments like CodeSignal.

---

## 📋 Table of Contents

- [Core Principles](#core-principles)
- [Data Structure Strategy](#data-structure-strategy)
- [Implementation Steps](#implementation-steps)
- [Critical Edge Cases](#critical-edge-cases)
- [Java Performance Pro-Tips](#java-performance-pro-tips)

---

## Core Principles

### 🎯 Fixed-Point Precision
All monetary values are treated as **micros** (1/1,000,000th of a unit) and stored using `long`. This avoids the precision errors of `double` and the overflow risks of `int`.

### ⏱️ Event-Driven Chronology
The system treats the `timestamp` parameter as a simulation clock. Every operation begins with a "catch-up" phase to process events scheduled in the virtual past.

### 📊 Index-First Architecture
We maintain live, sorted indices for queries rather than sorting data on-demand. This shifts the computational cost from **Queries** (expensive/frequent) to **Updates** (controlled/logarithmic).

---

## Data Structure Strategy

| Requirement | Data Structure | Performance (Big O) | Reasoning |
|------------|----------------|---------------------|-----------|
| **Account Lookup** | `HashMap<String, Account>` | O(1) | Instant access to account objects by ID |
| **Top K Ranking** | `TreeSet<Account>` | O(log N) update / O(K) query | Keeps accounts sorted by expenditure at all times |
| **Scheduled Tasks** | `PriorityQueue<Payment>` | O(log P) | Min-Heap that always keeps the next pending payment at the top |
| **Historical Query** | `TreeMap<Long, Long>` | O(log T) | Allows O(log N) search for the "most recent balance before time X" |

---

## Implementation Steps

### Step 1: Account & Transaction Logic (Level 1)

- Create a `private static class Account`
- Implement `createAccount`, `depositMoney`, and `transferMoney`
- **Crucial**: Ensure `sourceId != targetId` in transfers to prevent logic anomalies

### Step 2: Live Indexing for Top K (Level 2)

- Implement `withdrawMoney`
- Maintain a `totalOutgoing` field in `Account`

#### The Update Pattern

To update a sorted index in Java, you must:

```java
ranking.remove(account);
// Update the balance/expenditure fields
ranking.add(account);
```

> ⚠️ This prevents the `TreeSet` from becoming "corrupted" when an object's internal state changes.

### Step 3: The Execution Engine (Level 3)

- Implement `schedulePayment(timestamp, accountId, amount, delay)`
- Create a `processScheduled(currentTimestamp)` method

#### Trigger Logic
Invoke `processScheduled` at the very beginning of **every public method**.

#### Lazy Cancellation
To cancel a payment, simply update its status to `CANCELLED` in O(1). The execution engine will ignore it when it eventually reaches the top of the heap.

### Step 4: Account Merging & History (Level 4)

#### Account Merging
1. Mark the source account as `isMerged = true`
2. Record `mergeTimestamp`
3. Move balance to target

#### Historical Snapshots
- Every time a balance changes, record `(timestamp, currentBalance)` in a `TreeMap` inside the `Account`
- Use `history.floorEntry(queryTime)` to retrieve the past state

---

## Critical Edge Cases

| Case | Handling |
|------|----------|
| **Self-Transfer** | Transfers from Account A to Account A should be rejected |
| **Insufficient Funds** | Scheduled payments that fail due to balance should be marked `FAILED`, not retried |
| **Merge Validity** | An account cannot be merged if it is already a "source" of a previous merge |
| **Post-Merge History** | Queries for a merged account's balance at a time after the merge must return `null` or `0` as per the specific prompt instructions |

---

## Java Performance Pro-Tips

### 🚀 Use Static Inner Classes
Reduces memory overhead by removing the implicit pointer to the outer class.

```java
private static class Account {
    // Implementation
}
```

### 🎁 Prefer `Long` (Object) for Returns
Allows the use of `null` to signify a missing account or invalid operation.

```java
public Long getBalance(String accountId) {
    // Can return null if account doesn't exist
}
```

### 📦 Initial Capacity
If the number of accounts is known, initialize the `HashMap` with an appropriate capacity to avoid rehashing.

```java
Map<String, Account> accounts = new HashMap<>(expectedSize);
```

---
