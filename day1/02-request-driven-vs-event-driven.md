# **Request-Driven** vs **Event-Driven** architectures

## **1. Core Concept**

| Aspect                    | **Request-Driven Architecture**                                                            | **Event-Driven Architecture**                                                                       |
| ------------------------- | ------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------- |
| **Trigger Mechanism**     | Operations start with an explicit _request_ from a client (like HTTP call or API request). | Operations start with an _event_ — a change or occurrence in the system (like “Payment Completed”). |
| **Flow Type**             | Synchronous or blocking (client waits for a response).                                     | Asynchronous or non-blocking (system reacts to events as they occur).                               |
| **Communication Pattern** | Request → Response                                                                         | Event → Reaction (Publish → Subscribe)                                                              |

## **2. Typical Workflow Example (Fintech Context)**

### 🧾 **Request-Driven Flow (Payment Processing)**

1. A user submits a payment through the mobile app.
2. The app calls the payment API (`/processPayment`).
3. The API directly calls:

   - Bank’s core service for fund transfer
   - Fraud detection service
   - Notification service

4. Each service call happens in sequence or parallel within one transaction.
5. The client waits for a consolidated response.

**Drawback:** Tight coupling, slower response, less resilient to partial failures.

### ⚡ **Event-Driven Flow (Payment Processing)**

1. A user initiates a payment → `PaymentInitiated` event is published to the event bus.
2. Consumers react independently:

   - **Fraud Detection Service** checks for anomalies.
   - **Core Banking System** processes debit.
   - **Notification Service** sends alerts.

3. Each service works asynchronously, reacting to events.
4. The client can query transaction status later if needed.

**Advantage:** Scalable, decoupled, and real-time; system continues even if one service is delayed.

## **3. Fintech Use Case Comparison**

| Use Case                           | **Request-Driven**                                       | **Event-Driven**                                       |
| ---------------------------------- | -------------------------------------------------------- | ------------------------------------------------------ |
| **Account Balance Check**          | Suitable — requires immediate response.                  | Not suitable (user needs instant result).              |
| **Transaction Processing**         | Works but less scalable due to synchronous dependencies. | Ideal — different services can process concurrently.   |
| **Fraud Detection**                | Requires constant data polling or inline check.          | Can react instantly to `TransactionCompleted` events.  |
| **Notifications / Alerts**         | Needs explicit call from another service.                | Automatically triggered by events like `LoanApproved`. |
| **Regulatory Logging / Analytics** | Requires batch updates or hooks.                         | Automatically logs when relevant events occur.         |

## **4. Architecture Characteristics**

| Characteristic           | **Request-Driven**                                   | **Event-Driven**                                       |
| ------------------------ | ---------------------------------------------------- | ------------------------------------------------------ |
| **Coupling**             | Tight — services depend directly on each other.      | Loose — services interact via event bus.               |
| **Scalability**          | Harder to scale due to synchronous calls.            | Easy to scale — each consumer scales independently.    |
| **Failure Isolation**    | Low — one failure can block chain of calls.          | High — failure of one consumer doesn’t stop others.    |
| **Data Flow Visibility** | Easier to trace step-by-step.                        | Needs observability tools (logs, traces) for tracking. |
| **Latency**              | Low for small systems; increases with chained calls. | Slight overhead but better overall throughput.         |

## **5. Summary**

| When to Use                                                                       | **Request-Driven** | **Event-Driven**      |
| --------------------------------------------------------------------------------- | ------------------ | --------------------- |
| You need immediate responses (e.g., balance inquiry).                             | ✅                 | ❌                    |
| You need to process high-volume async events (e.g., fraud detection, audit logs). | ❌                 | ✅                    |
| You want real-time updates and decoupled services.                                | ❌                 | ✅                    |
| Your business logic depends on explicit user actions.                             | ✅                 | ✅ (sometimes hybrid) |

## **💡 Practical Note**

Modern fintech systems often use a **hybrid approach**:

- **Request-Driven** for _customer-facing APIs_ (e.g., transfer funds).
- **Event-Driven** internally for _backend processing_ (e.g., fraud check, notification, ledger update).
