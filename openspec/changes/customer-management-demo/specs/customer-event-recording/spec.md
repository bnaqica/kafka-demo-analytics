# Spec Delta

## Purpose

Saves every customer change event in MongoDB exactly once, including the payload as it was received.

## ADDED Requirements

### Requirement: Save customer change events
The service SHALL consume every topic listed in `app.kafka.topics` (comma-separated, `customer-change-event` by default) and save each event with its `eventId`, `eventType`, `sourceTopic`, `customerId`, `occurredAt`, `receivedAt` and full payload as received, for any event type.

#### Scenario: Event received
- **WHEN** a `customer-address-change` event arrives
- **THEN** a record is saved holding the unchanged payload

#### Scenario: New event type
- **WHEN** an event with a type this service has never seen arrives
- **THEN** it is saved with no code change

### Requirement: Events recorded once
An event whose `eventId` is already saved SHALL be skipped.

#### Scenario: Redelivery
- **WHEN** the same event is delivered twice
- **THEN** one record exists and the first record is unchanged

### Requirement: Invalid events dead-lettered
A record that is not valid JSON, or whose `eventId`, `eventType`, `customerId` or `occurredAt` is missing or unreadable, SHALL go to `<topic>-dlq`, for example `customer-change-event-dlq`, without retries. `eventId` and `customerId` SHALL be UUIDs and `occurredAt` an ISO-8601 instant. Any other failure, such as the database being unavailable, SHALL be retried three times one second apart and then go to `<topic>-dlq`.

#### Scenario: Missing eventId
- **WHEN** a record has no `eventId`
- **THEN** it is published to the dead-letter topic and nothing is saved

#### Scenario: Unreadable occurredAt
- **WHEN** a record's `occurredAt` is not an ISO-8601 instant
- **THEN** it is published to the dead-letter topic without retries and nothing is saved

#### Scenario: Database unavailable
- **WHEN** saving an event fails on every attempt
- **THEN** the record is retried three times and then published to the dead-letter topic

### Requirement: Summary counts by type
`GET /api/analytics/summary` SHALL return `totalEvents`, the number of saved events, and `eventsByType`, an object mapping each saved event type to its count, sorted by type. Types with no events SHALL be left out.

#### Scenario: Mixed types
- **WHEN** one `customer-addition` and two `customer-change` events are saved
- **THEN** the summary is `{"totalEvents": 3, "eventsByType": {"customer-addition": 1, "customer-change": 2}}`

#### Scenario: No events
- **WHEN** no events are saved
- **THEN** the summary is `{"totalEvents": 0, "eventsByType": {}}`

### Requirement: Analytics event CRUD
The API SHALL support `POST` and `GET` on `/api/analytics/events` and `GET`, `PUT` and `DELETE` on `/api/analytics/events/{eventId}`. The list SHALL return the most recent events by `occurredAt`, newest first, up to `limit`, which defaults to 20 and must be from 1 to 200. A created record without an `eventId` SHALL get a generated one. A record created through the API has no `payload` unless one is given, and an update without a `payload` SHALL keep the existing one.

#### Scenario: Create a record
- **WHEN** a valid analytics event is posted
- **THEN** the API responds 201 with the record

#### Scenario: Duplicate eventId
- **WHEN** a record is posted with an `eventId` that exists
- **THEN** the API responds 409

#### Scenario: Recent events
- **WHEN** a client calls `GET /api/analytics/events?limit=5` with 10 saved events
- **THEN** the 5 with the latest `occurredAt` are returned, newest first

#### Scenario: Limit out of range
- **WHEN** a client requests `limit=201`
- **THEN** the API responds 400

#### Scenario: Update keeps the payload
- **WHEN** a record with a payload is updated without one
- **THEN** its payload is unchanged

#### Scenario: Unknown record
- **WHEN** a client requests an `eventId` that does not exist
- **THEN** the API responds 404
