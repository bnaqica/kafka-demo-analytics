# Architecture

## The Kafka Demo system

```mermaid
flowchart LR
    UI[kafka-demo-ui<br/>React :5173] -->|POST/GET /api/customers| C[kafka-demo-customer<br/>:8081]
    UI -->|GET /api/emails| E[kafka-demo-email<br/>:8082]
    UI -->|GET /api/analytics| A[kafka-demo-analytics<br/>:8083]
    C --- PG[(Postgres<br/>kafka_demo_customer_db)]
    C -->|CUSTOMER_CREATED| T1{{customer-events}}
    T1 --> E
    T1 --> A
    E -->|EMAIL_SENT| T2{{email-events}}
    T2 --> A
    E --- M1[(MongoDB<br/>kafka_demo_email_db)]
    A --- M2[(MongoDB<br/>kafka_demo_analytics_db)]
```


| Repo | Role | Port | Data store |
|---|---|---|---|
| kafka-demo-customer | Owns customers, publishes `customer-events` | 8081 | Postgres |
| kafka-demo-email | Sends a welcome email per new customer, publishes `email-events` | 8082 | MongoDB |
| kafka-demo-analytics | Counts every event on `customer-events` and `email-events` | 8083 | MongoDB |
| kafka-demo-ui | React UI over all three | 5173 | — |
| local-docker | Kafka, Postgres, MongoDB for local dev | — | — |

## This service: kafka-demo-analytics

Listens to every event on `customer-events` and `email-events`, stores each one in MongoDB, and serves counts by event type.

- **Produces:** — (only DLQ records)
- **Consumes:** `customer-events`, `email-events` (group `kafka-demo-analytics`)
- **Stores:** MongoDB

## Design decisions

- **Envelope only.** `AnalyticsEventListener` reads `eventId`, `eventType`, `occurredAt` and
  `customerId`, so any new event type on a subscribed topic is counted with no code change.
  Add a topic by appending it to `app.kafka.topics`.
- **Idempotent on `eventId`**, which is the document `_id`; a redelivery hits a duplicate key and is skipped.
- **Counts are computed on read** with a `$group` aggregation. Fine for a demo; pre-aggregate if volume grows.
- **Retries then DLQ** to `<topic>-dlq`; malformed events skip the retries.

## Known gaps

- No time-bucketed metrics (per day/hour).
- No Testcontainers integration tests yet.
