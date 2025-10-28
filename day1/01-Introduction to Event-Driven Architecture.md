## **1. What is Event-Driven Architecture?**

Event-Driven Architecture is a software design paradigm where **events** trigger the flow of data and operations, instead of relying on traditional request-response flows.

- **Event:** A significant change in state or an occurrence that the system cares about.
- **Producer (Emitter):** Component that generates the event.
- **Consumer (Listener/Subscriber):** Component that reacts to the event.
- **Event Bus / Stream / Queue:** Channel through which events travel from producers to consumers.

Think of it as **“something happens → system reacts automatically”**, rather than “system checks periodically if something happened.”

## **2. Key Components in EDA**

1. **Event Producers:** Systems that detect changes, like a transaction completion or account creation.
2. **Event Consumers:** Systems that respond, like sending notifications or updating analytics dashboards.
3. **Event Channels:** Messaging systems like **Kafka, RabbitMQ, AWS SNS/SQS**, or **Azure Event Hubs**.
4. **Event Store (optional):** Persistent storage for events for auditing or replay.

## **3. Why EDA in Fintech?**

Fintech systems often need:

- Real-time processing (payments, fraud detection)
- High scalability (millions of transactions)
- Decoupled services (payments, notifications, risk analysis)

EDA allows **loose coupling**, **real-time reactions**, and **scalable workflows**.

## **4. Fintech Use Cases**

### **a) Real-time Payments Processing**

- **Event:** Customer initiates a fund transfer.
- **Producers:** Payment gateway emits `TransactionInitiated` event.
- **Consumers:**

  - Core banking system debits account
  - Fraud detection service analyzes transaction
  - Notification service sends SMS/email

- **Benefit:** All services react independently in real time.

### **b) Fraud Detection**

- **Event:** Large transaction over threshold.
- **Producers:** Payment service emits `TransactionCompleted` event.
- **Consumers:**

  - Machine learning service analyzes transaction for fraud
  - Security team gets alerts

- **Benefit:** Fraud detection happens immediately without slowing down the main transaction processing.

### **c) Portfolio Updates / Stock Trading**

- **Event:** Stock price crosses a user-set threshold.
- **Producers:** Market data feed emits `PriceChange` event.
- **Consumers:**

  - Trading system executes buy/sell orders automatically
  - Portfolio tracker updates user's dashboard

- **Benefit:** Users get instant reactions to market changes.

### **d) Regulatory Compliance and Audit Logging**

- **Event:** Transaction is completed.
- **Producers:** Core banking or payment service emits `TransactionCompleted` event.
- **Consumers:**

  - Audit service logs transaction for compliance
  - Analytics service calculates trends or generates reports

- **Benefit:** Compliance is automated and decoupled from transaction flow.

### **e) Notifications & Alerts**

- **Event:** Loan application status changes.
- **Producers:** Loan management system emits `LoanApproved` or `LoanRejected` event.
- **Consumers:**

  - Notification service sends SMS/email
  - CRM system updates customer profile

- **Benefit:** Customers and internal systems are updated automatically.

## **5. Advantages in Fintech**

1. **Scalability:** Each service can scale independently.
2. **Flexibility:** Easy to add new consumers (e.g., analytics, marketing) without changing producers.
3. **Resilience:** Failures in one service don’t block others.
4. **Real-time reactions:** Critical for payments, fraud detection, trading.

## **6. Example Diagram (Conceptual)**

![EDA Example](./eda-example.png)
