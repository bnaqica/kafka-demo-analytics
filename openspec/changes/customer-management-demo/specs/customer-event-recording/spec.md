# Spec Delta

## Purpose

Saves every customer change event in MongoDB exactly once, including the payload as it was received.

## ADDED Requirements

### Requirement: Save customer change events
The service SHALL consume `customer-change-event` and save each event with its `eventId`, `eventType`, `sourceTopic`, `customerId`, `occurredAt`, `receivedAt` and full payload as received, for any event type.

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
A record that is malformed or missing a required shared field SHALL go to `customer-change-event-dlq` without retries.

#### Scenario: Missing eventId
- **WHEN** a record has no `eventId`
- **THEN** it is published to the dead-letter topic and nothing is saved

### Requirement: Summary counts by type
`GET /api/analytics/summary` SHALL return the total events and the count for each event type.

#### Scenario: Mixed types
- **WHEN** one `customer-addition` and two `customer-change` events are saved
- **THEN** the summary reports 3 in total with those counts by type
