# Proposal

## Why

The demo's analytics service should save every customer change event as it arrives and ignore redeliveries. Today it keeps only the shared fields, listens to topics being retired, and any authenticated caller can change its data.

## What Changes

- **BREAKING** Consume only `customer-change-event`, replacing `customer-events` and `email-events`.
- Save the full incoming event payload with each record, still keyed by `eventId`.
- **BREAKING** Writes require `customer-admin`, reads require either role, and the `local` profile requires tokens.

## Capabilities

### New Capabilities

- `customer-event-recording`: saving customer change events exactly once.
- `analytics-api-access`: token and role requirements for the analytics API.

### Modified Capabilities

None. `openspec/specs/` is empty.

## Impact

- **Code:** analytics document and DTOs, `EventRecordingService`, topic config, `SecurityConfig`, tests and docs in this repo.
- **Event contract:** depends on `customer-management-demo` in `kafka-demo-customer`. `kafka-demo-email` stops publishing `email-events`. `docs/kafka-events.md` changes identically in all three service repos.
- **`kafka-demo-ui`:** the analytics panel must send a token and will show the new event types.
