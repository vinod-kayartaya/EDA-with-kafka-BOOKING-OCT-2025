# EDA Anti-Patterns

## 1. **Event Soup**

A large number of poorly defined events with overlapping meaning. Nobody knows what is truly important.

**Symptoms**
• Events like `UserEvent`, `DataUpdated`, `RecordCreated`
• No versioning
• Multiple producers publishing conflicting structures

**Example**
Bad names:

- `OrderUpdated` (Updated how? Shipped? Cancelled?)
- `PaymentChanged` (Changed what?)

**Better approach**
Name events as business facts:

- `OrderShippedEvent`
- `OrderCancelledEvent`
- `PaymentAuthorizedEvent`
- `PaymentFailedEvent`

**Guideline**
One event should answer a clear question: _What happened?_

Create an **event catalog** documenting payload, owner, and version.

## 2. **Chatty Events**

Events emitted for low-value technical changes.

**Typical cause**
Publishing events directly from row-level triggers or entity listeners.

**Bad example**
Whenever a product’s stock quantity changes by one unit:

```
InventoryCountAdjustedEvent
```

fired repeatedly, even though the consumer only cares about restock completion.

**Better**
Aggregate multiple updates and publish meaningful events:

```
WarehouseRestockedEvent { productId, quantityAdded, timestamp }
```

Focus on _business significance_, not technical noise.

## 3. **Leaky Domain Events**

Events expose internal DB structure, table naming, or technical jargon.

**Example**

```
Customer_tbl_updated_event
```

Payload:

```
{ cust_id, cust_fname, cust_lname, status_flag }
```

Hard to evolve and confusing for business.

**Better**

```
CustomerProfileUpdatedEvent
{
  customerId,
  fullName,
  status
}
```

Do not leak internal schemas. Represent domain language.

## 4. **Eventual Consistency Panic**

Teams fear asynchronous processing delays so they add blocking calls.

**Symptoms**
• After event publish, UI polls repeatedly expecting immediate consistent state
• Synchronous bridging between services to “confirm” event effects

**Example**
Order Service publishes `OrderPlacedEvent` then calls Inventory Service API to confirm stock. That tight coupling defeats EDA.

**Better**
Design UX to reflect the state lifecycle and delay:

```
OrderPlaced → InventoryReserved → PaymentConfirmed → OrderShipped
```

Use readable status transitions and show interim state on UI.

## 5. **Event Consumer Spaghetti**

Multiple services react to the same event differently and repeat business logic.

**Example**
`OrderPlacedEvent` triggers:

- Billing service: calculate tax
- Notification service: send email
- Reporting service: write to data warehouse

One of them starts doing fraud checks too. Another starts calculating loyalty points. Now logic is spread across many consumers.

**Fix**
Define bounded responsibility. Keep core rules where they belong:
• Fraud detection belongs in Risk domain
• Reward points in Loyalty domain

Avoid accidental duplication.

## 6. **Events Used as Commands**

Producer tries to instruct another service through events.

**Example**
A service publishes:

```
SendWelcomeEmailEvent
```

This is not a fact. It is a command.

**Better**
Use a command/request for action:

```
SendEmailCommand
```

Use events only for facts that already happened:

```
UserRegisteredEvent
```

Email service reacts because user registration is a fact that requires communication.

Rules:
Commands = intent
Events = outcome

## 7. **No Error Strategy**

Failures ignored. Messages stuck in retry loops or silently dropped.

**Symptoms**
• No DLQ
• No idempotency
• Out of order processing breaks business logic

**Example**
PaymentService consumer retries failed event forever because the downstream database is down.

**Fix**
• DLQ topic per event type (`OrderPlaced.DLT`)
• Retry with backoff
• Idempotency key (eventId)
• Monitoring on DLQ depth
• Poison message handling

Plan failure from day one.

## 8. **Replay Everything All the Time**

Assuming infinite historical replay solves everything.

**Example**
A customer 10 years ago had an event format that no longer exists. Replaying it breaks modern consumers.

**Better**
• Versioned events with evolution policy
• State snapshots
• Time-boxed retention
• Replay only for specific domain needs (not default behavior)

Replay is a **tool**, not the architecture.

## 9. **No Ownership or Responsibility**

No one team owns schema changes or compatibility.

**Example**
Analytics team adds a new field to an event without telling others and a consumer crashes.

**Fix**
Event ownership = responsibility for:
• Backward compatibility rules
• Schema registry management
• Change communication
• Documentation

Treat events as a public API that evolves with care.

## 10. **Tool-Driven Architecture**

Choosing Kafka or a streaming system and forcing events everywhere.

**Example**
Simple CRUD microservice with no async requirement still publishes dozens of events.

You end up with complex ops, higher cost, slower development.

**Better**
Use EDA where:
• Multiple independent consumers react to the same fact
• Scale and autonomy matter
• Workflows are naturally async

Choose architecture based on **business flow**, not trend.

## Quick Reference Table

| Anti-Pattern               | Safe Practice                              |
| -------------------------- | ------------------------------------------ |
| Event Soup                 | Clear semantics, versioning, event catalog |
| Chatty Events              | Business meaningful events                 |
| Leaky Domain Events        | Domain language, hide internals            |
| Eventual Consistency Panic | Design UI for async lifecycle              |
| Consumer Spaghetti         | Respect bounded context ownership          |
| Events as Commands         | Commands for intent, events for outcomes   |
| No Error Strategy          | DLQ, idempotency, monitoring               |
| Replay Everything          | Versioning, snapshots, retention           |
| No Ownership               | Domain owners steward schemas              |
| Tool-Driven Architecture   | EDA only where async fits                  |
