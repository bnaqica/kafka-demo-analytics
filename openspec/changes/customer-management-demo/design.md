# Design

## Context

See proposal.md for motivation. `analytics_event` already uses `eventId` as its `_id`, which gives idempotency, and `EventRecordingService` reads only the shared fields, so new event types need no code. The event contract is defined in `kafka-demo-customer`'s change.

## Goals / Non-Goals

**Non-Goals:**
- Analysing or transforming events; the service only saves them.
- Backfilling payloads on records already stored.

## Decisions

### Reuse `analytics_event`

Add a `payload` string holding the raw JSON to the existing collection, so there is one CRUD API and the `_id` remains the idempotency check. Alternative considered: a separate incoming-event collection like the email service, rejected because analytics has no send step and no second state to track.

The payload is stored as the received string, not parsed, so it is exactly what arrived. It is optional on the CRUD API, since records created by hand have no incoming event.

### Security

Same approach as the customer change: one secured chain, a `local`-only Swagger chain, `realm_access.roles` mapped to authorities, and `@PreAuthorize` on controller methods.

## Risks / Trade-offs

- [The payload holds names, email addresses and phones] → The event contract already excludes date of birth; responses are limited to the two realm roles.
- [Old records have no payload and use old type names] → Local demo data only; they stay and are counted under their old types.

## Migration Plan

Deploy after `kafka-demo-customer` publishes `customer-change-event`. No MongoDB migration is needed because the new field is optional. Rollback: restore the previous code; the extra field is ignored.
