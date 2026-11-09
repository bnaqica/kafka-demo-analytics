# Spec Delta

## Purpose

Defines who can call the analytics API: both roles can read, and only customer-admin can change records.

## ADDED Requirements

### Requirement: Token required in every profile
Every endpoint except the health check SHALL require a valid `kafka-demo` realm bearer token in every profile. In the `local` profile only, the Swagger pages SHALL be open.

#### Scenario: No token
- **WHEN** a request without a token calls an analytics endpoint
- **THEN** the API responds 401

#### Scenario: Swagger in local
- **WHEN** the Swagger page is requested without a token in the `local` profile
- **THEN** the page is returned

### Requirement: Reads allowed for both roles
GET endpoints SHALL be allowed for `customer-admin` and `customer-viewer`.

#### Scenario: Viewer reads the summary
- **WHEN** a `customer-viewer` calls `GET /api/analytics/summary`
- **THEN** the API responds 200

### Requirement: Writes limited to customer-admin
POST, PUT and DELETE SHALL require `customer-admin`.

#### Scenario: Viewer tries to delete
- **WHEN** a `customer-viewer` deletes an analytics event
- **THEN** the API responds 403 and the record still exists

#### Scenario: Admin creates a record
- **WHEN** a `customer-admin` posts a valid analytics event
- **THEN** the API responds 201
