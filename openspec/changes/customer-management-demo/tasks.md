# Tasks

## 1. Event recording

- [x] 1.1 Change `app.kafka.topics` to `customer-change-event` and verify with a listener test and the application startup test
- [x] 1.2 Add `payload` to `AnalyticsEvent`, `AnalyticsEventRequest` and `AnalyticsEventResponse`; save the received string in `EventRecordingService`; verify with unit tests for each `customer-event-recording` scenario
- [x] 1.3 Verify a real duplicate `eventId` leaves the first record unchanged in a Testcontainers test
- [x] 1.4 Rewrite `docs/kafka-events.md` to match the customer repo's text exactly, and update `docs/architecture.md` and `README.md`; verify with a diff against that file

## 2. Security

- [x] 2.1 Update `SecurityConfig` per design.md and `SecurityConfigTests`; verify 401 without a token and open Swagger only under `local`
- [x] 2.2 Add role rules to both controllers and role tokens in controller tests; verify each `analytics-api-access` scenario
- [x] 2.3 Add the springdoc bearer scheme and update the README's run and auth sections; verify the Authorize button appears under `local`

## 3. Integration

- [x] 3.1 Run `./gradlew spotlessApply build` and verify it passes
- [x] 3.2 With `kafka-demo-customer` running against `local-docker`, verify every event type is saved with its payload and the summary counts them
- [x] 3.3 Leave all changes uncommitted for developer review and verify with `git status` that nothing was committed
