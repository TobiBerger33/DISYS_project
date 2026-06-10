# Energy Communities — Project Specification

## Overview

An energy community is an association of at least two participants for the joint production and utilization of energy. There are **community producers/users** and **grid producers/users**. This project is a distributed system, consisting of multiple components, that shows the current energy distribution and usage.

### Idea

At the center of the system is a **message queue** that receives energy production and usage messages. Based on these updates, a service calculates the current community and grid usages. If a community user wants energy, the **community energy pool** is used first; otherwise the **grid** delivers the energy.

After the usage is calculated, another service calculates the **percentage for the current hour** based on the usage.

The current distribution of energy can be monitored on a **Graphical User Interface (GUI)**. Historical data can also be queried.

---

## Components

Six components must be developed. Every component is its own application that can be started independently from the others.

### 1. Community Energy Producer

Sends the following message to the queue:

| Field | Value |
|---|---|
| `type` | `PRODUCER` |
| `association` | `COMMUNITY` |
| `kwh` | kWh produced in a minute (e.g. `0.003`) |
| `datetime` | datetime of the energy production (e.g. `2025-01-10T14:33:00`) |

- Sends a message every couple of seconds with a semi-random (but plausible) amount of kWh.
- Must incorporate a **Weather API** so that more energy is produced when the sun is shining.
- Suggested APIs: [openweathermap.org](https://openweathermap.org/), [open-meteo.com](https://open-meteo.com/).

### 2. Community Energy User

Sends the following message to the queue:

| Field | Value |
|---|---|
| `type` | `USER` |
| `association` | `COMMUNITY` |
| `kwh` | kWh used in a minute (e.g. `0.001`) |
| `datetime` | datetime of the energy usage (e.g. `2025-01-10T14:34:00`) |

- Sends a message every couple of seconds with a semi-random (but plausible) amount of kWh.
- Must incorporate the **time of day** so that more energy is needed in peak hours (morning and evening).

### 3. Usage Service

Every time a new `PRODUCER` or `USER` message comes in, the database is updated. Data from individual minutes is accumulated into the corresponding hours.

**Example — DB before message:**

| hour | community_produced | community_used | grid_used |
|---|---|---|---|
| 2025-01-10T14:00:00 | 18.05 | 18.02 | 1.056 |
| 2025-01-10T13:00:00 | 15.015 | 14.033 | 2.049 |

**Incoming message:**

```
type: USER
association: COMMUNITY
kwh: 0.05
datetime: 2025-01-10T14:34:00
```

**DB after message:**

| hour | community_produced | community_used | grid_used |
|---|---|---|---|
| 2025-01-10T14:00:00 | 18.05 | 18.05 | 1.076 |
| 2025-01-10T13:00:00 | 15.015 | 14.033 | 2.049 |

The community user required more energy than was available in the community production pool, so grid usage also increased. Of the `0.05`, `0.03` is taken from the community pool and `0.02` from the grid.

> **Invariant:** `community_used` can never be greater than `community_produced`.

### 4. Current Percentage Service

Recalculates percentages whenever usage changes.

| hour | community_depleted | grid_portion |
|---|---|---|
| 2025-01-10T14:00:00 | 100.00 | 5.63 |

- `community_depleted` — how much of the community pool has been consumed (in %).
- `grid_portion` — share of grid energy in total energy (`community_used + grid_used`), in %.
- The table holds only the **current hour**.

### 5. GUI

- Built with **JavaFX**.
- Displays current percentage data and historical data based on a time filter.
- **The GUI is not directly connected to the database.** It uses the REST API to fetch data.

### 6. REST API

- Built with **Spring Boot**.
- Connected to the database, but **read-only**.

Endpoints:

| Method | Path | Description |
|---|---|---|
| `GET` | `/energy/current` | Returns the percentage data of the current hour. |
| `GET` | `/energy/historical?start=...&end=...` | Returns usage data for the given time period. |

---

## Example Timeline

1. **Community Energy Producer** sends production data to the queue based on the current weather.
2. **Community Energy User** sends minute usage data to the queue based on time of day.
3. **Usage Service** picks up the minute data and updates the hour data in the database.
4. **Usage Service** sends a message to the queue that new data is available.
5. **Current Percentage Service** picks up the new data and saves the calculated percentage data to the database.
6. **GUI** wants to refresh the current percentage and sends a `GET` request to the REST API.
7. **REST API** handles the request, reads the data from the database and returns it to the GUI.
8. **GUI** displays the data to the user.

---

## Component Diagram

```
 ┌──────────────────────┐     ┌──────────────────────┐
 │ Community Producer   │     │ Community User       │
 │ (Weather API)        │     │ (time-of-day curve)  │
 └──────────┬───────────┘     └──────────┬───────────┘
            │ PRODUCER msg               │ USER msg
            ▼                            ▼
       ┌─────────────────────────────────────┐
       │           Message Queue             │
       └──────────────┬──────────────────────┘
                      │
        ┌─────────────┴─────────────┐
        ▼                           ▼
 ┌───────────────┐         ┌─────────────────────────┐
 │ Usage Service │────────►│ Current Percentage Svc  │
 └──────┬────────┘  "new   └──────────┬──────────────┘
        │           data"             │
        ▼                             ▼
       ┌───────────────────────────────┐
       │           Database            │
       └──────────────┬────────────────┘
                      │ read-only
                      ▼
              ┌───────────────┐
              │   REST API    │  (Spring Boot)
              └──────┬────────┘
                     │ HTTP
                     ▼
              ┌───────────────┐
              │      GUI      │  (JavaFX)
              └───────────────┘
```

---

## Tech Stack (planned)

- **Language:** Java
- **Build:** Maven (multi-module)
- **GUI:** JavaFX
- **REST API:** Spring Boot
- **Message Queue:** (to be decided — e.g. RabbitMQ / ActiveMQ)
- **Database:** (to be decided — e.g. PostgreSQL / H2)
- **Weather API:** open-meteo.com or openweathermap.org

## Module Layout

```
DISYS_project/
├── community-producer/    # Component 1
├── community-user/        # Component 2
├── usage-service/         # Component 3
├── percentage-service/    # Component 4
├── gui/                   # Component 5 (JavaFX)
└── rest-api/              # Component 6 (Spring Boot)
```
