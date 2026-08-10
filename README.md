# Kingdom Quest Events Microservice

## Overview

The Kingdom Quest Events Service is a standalone Spring Boot REST microservice responsible for managing temporary kingdom events used by the main Kingdom Quest application.

The service provides a REST API for creating, editing, retrieving, and deleting events. It also automatically manages event activation and expiration through scheduled jobs.

The service uses Spring Cache to cache frequently accessed event data and automatically evicts relevant caches whenever event state changes.

**Main Application Repository:**
https://github.com/vi-10/kingdom-quest-app

---

## Technology Stack

* Java 17
* Spring Boot 4.1.0
* Spring Web
* Spring Data JPA
* Spring Cache
* Spring Validation
* MySQL
* Maven
* Lombok

---

## Responsibilities

The Events microservice is responsible for:

* Event creation
* Event editing
* Event deletion
* Retrieving the active event
* Retrieving all events
* Event validation
* Event overlap prevention
* Automatic event activation
* Automatic event deactivation
* Caching event data
* Providing a REST API for the main application

The main Kingdom Quest application consumes this service through REST.

---

## Event Entity

An event contains:

* UUID id
* title
* description
* affectedQuestType
* bonusXp
* bonusGold
* start
* end
* active

The affectedQuestType determines which type of quest receives the event bonus.

---

## REST API

The service exposes endpoints for event management.

### Get Active Event

```http
GET /api/v1/event
```

Returns the currently active event.

If there is no active event, the endpoint returns `200 OK` with an empty response body.

---

### Create Event

```http
POST /api/v1/event
```

Creates a new event.

Validation is performed on incoming requests.

Events cannot:

* Have a blank title
* Have a blank description
* Have a null quest type
* Have negative rewards
* Have an invalid start/end range
* Overlap with another event
* Have a duplicate title

---

### Edit Event

```http
PUT /api/v1/event
```

Updates an existing event.

The same validation and overlap rules used during creation are also applied when editing.

---

### Get All Events

```http
GET /api/v1/event/all
```

Returns all events.

---

### Delete Event

```http
DELETE /api/v1/event/{eventId}
```

Deletes an event by its UUID.

---

## Scheduling

The microservice uses Spring's scheduling mechanism to automatically manage event state.

Scheduling is enabled using Spring's scheduling support.

### Event Status Update

The job checks all events and determines whether each event should currently be active based on its start and end times.

This means events are automatically activated when their start time is reached and deactivated when their end time is reached.

---

### Expired Event Deactivation

This job runs every 30 seconds after the previous execution has completed.

It finds active events whose end time has passed and deactivates them.

---

## Caching

The service uses Spring's caching abstraction.

Caching is enabled through Spring's caching support.

### Active Event Cache

The active event is cached using:

This prevents repeated database queries when retrieving the currently active event.

---

### Events Cache

The complete list of events is cached using:

This reduces repeated database access when retrieving all events.

---

### Cache Eviction

The following operations evict the relevant caches:

* Create event
* Edit event
* Delete event
* Update event status
* Deactivate expired events

---

## Validation

The REST API uses Jakarta Bean Validation.

Additional service-level validation ensures that:

* Start time occurs before end time
* Event periods cannot overlap
* Event titles are unique

---

## Error Handling

The service provides centralized API exception handling.

Unexpected exceptions are handled as internal server errors and return:

---

## Testing

The Events microservice contains multiple levels of automated tests:

* Unit tests
* Integration tests
* REST/API controller tests
  
---


## Running the Service

### Database

Configure the MySQL connection in application.properties.

### Start the service

The service must be running for the main Kingdom Quest application to retrieve and manage events.

---

## Related Repository

The main Kingdom Quest application that consumes this microservice is available here:

https://github.com/vi-10/kingdom-quest-app
