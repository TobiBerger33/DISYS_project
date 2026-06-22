# HANDOFF — Current Percentage Service (+ messaging/structure cleanup)

> Branch: `feature/current-percentage-service` · Last updated: 2026-06-22
> Audience: the next agent continuing this feature. Read top-to-bottom. All decisions are locked; every remaining step has exact code below — apply it, then build & test.
> Style note for the user: the user does the typing and wants concept-before-code, small chunks, German explanations (English for technical terms). Don't take over writing Java unless explicitly told. See the project memory.

---

## 1. Status

**DONE & verified:**
- ✅ **Pom structure unified (Weg A).** All 7 modules now parent off the root `disys-project`, which parents off `spring-boot-starter-parent` **3.4.1**. One Spring Boot version. `jackson-databind` + `jackson-datatype-jsr310` are centralized in the root `<dependencies>` (no per-module versions). `percentage-service` is activated in the root `<modules>`. Verified: `./mvnw clean compile` → **BUILD SUCCESS** (Java 21).
- ✅ **`shared/src/main/java/com/disys/shared/UsageUpdate.java`** — DTO created (hour, communityProduced, communityUsed, gridUsed; no-arg + all-args ctor + getters/setters).

**REMAINING (this is the work list):**
- ⬜ A. Messaging fixes (producer + usage-service) — §5A
- ⬜ B. usage-service publishes `UsageUpdate` — §5B
- ⬜ C. percentage-service implementation (the 30% deliverable) — §5C
- ⬜ D. Build & test — §6

All pom changes are uncommitted working-tree edits (nothing committed yet). `GRADING.md` + `HANDOFF.md` are untracked.

---

## 2. What this service is

End of the message chain. Receives an update from the Usage Service, computes two percentages, writes them to the `percentage` table. **Publishes nothing further.**

```
[Producer]─PRODUCER─┐                                          (Event-Carried State Transfer)
                    ├─(energy.queue)─>[Usage Service]─writes─>usage_data
[User]────USER──────┘                       │
                                            │ publishes UsageUpdate {hour,produced,used,grid}
                                            ▼
                                      (energy.updates)
                                            ▼
                              [CURRENT PERCENTAGE SERVICE]   <-- build this
                                computes %s from the MESSAGE (never reads usage_data)
                                upserts percentage[hour]
                                            ▼
                                   percentage ─> [REST API] ─> [GUI]
```

---

## 3. Locked decisions

| Topic | Decision |
|---|---|
| Build structure | All modules → root `disys-project` → `spring-boot-starter-parent 3.4.1`. jackson+jsr310 central in root. (DONE) |
| Input pattern | **Event-Carried State Transfer**: usage-service sends full values in a `UsageUpdate`; percentage-service computes from the message and **never reads `usage_data`**. |
| `percentage` table | **Upsert per hour** (`findByHour` → update, else insert). REST API reads newest via `findTopByOrderByHourDesc()`. |
| Messaging topology | **Default exchange + queue name as routing key.** No custom exchange/binding. Queues: `energy.queue` (producer/user→usage), `energy.updates` (usage→percentage). |
| JSON converter | Every messaging module's `Jackson2JsonMessageConverter` is built on an `ObjectMapper` with `JavaTimeModule` registered + `WRITE_DATES_AS_TIMESTAMPS` disabled (LocalDateTime → ISO string). |
| Credentials | RabbitMQ **and** Postgres: `disysuser` / `disyspw` everywhere (matches `docker-compose.yml`). |
| percentage-service package | `com.disys.percentage_service` (replaces the copy-pasted `com.disys.community_producer` skeletons — delete those). |
| Rounding | percentages rounded to 2 decimals. |

---

## 4. Infra & DB facts

- `docker-compose.yml`: Postgres (`disysuser`/`disyspw`, db `postgres`, :5432) + RabbitMQ (`disysuser`/`disyspw`, :5672, mgmt :15672).
- Schema owned by **Flyway** (rest-api plugin + `rest-api/flyway.conf`). `ddl-auto=none` in every service. Tables:
  - `usage_data(id uuid, hour timestamp, community_produced float, community_used float, grid_used float)`
  - `percentage(id uuid, hour timestamp, community_depleted float, grid_portion float)`
- Run migrations once before E2E: `export JAVA_HOME=$(/usr/libexec/java_home -v 21); ./mvnw -f rest-api/pom.xml flyway:migrate` (paths in `flyway.conf` are relative to the rest-api module).
- Ports: rest-api 8080, usage-service 8082, percentage-service **8083** (new).

---

## 5. Remaining work — exact code

### §5A — Messaging fixes (the "logic errors")

**A1. `community-producer/src/main/java/com/disys/community_producer/RabbitMQConfig.java`** — replace whole file. (Fixes queue mismatch + JSR-310.) Switches to default exchange + `energy.queue`, drops the DirectExchange/Binding/custom-RabbitTemplate, adds the JSR-310 converter (Spring Boot then auto-wires a RabbitTemplate using this converter bean):

```java
package com.disys.community_producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "energy.queue";

    @Bean
    public Queue energyQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
```

**A2. `community-producer/.../MessagePublisher.java`** — the publish call now targets the default exchange via the queue name:

```java
    public void publish(EnergyMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE, message);
    }
```

**A3. `usage-service/.../RabbitMQConfig.java`** — replace the `messageConverter()` bean body with the JSR-310 version (so it can DEserialize `EnergyMessage.datetime` and serialize `UsageUpdate.hour`). Add imports for `ObjectMapper`, `SerializationFeature`, `JavaTimeModule`:

```java
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
```

**A4. `usage-service/src/main/resources/application.properties`** — fix RabbitMQ creds:

```
spring.rabbitmq.username=disysuser
spring.rabbitmq.password=disyspw
```

### §5B — usage-service publishes `UsageUpdate`

**`usage-service/.../MessageConsumer.java`** — in `handleEnergyMessage`, replace the hour-string send with a `UsageUpdate` (add `import com.disys.shared.UsageUpdate;`):

```java
            UsageUpdate update = new UsageUpdate(
                    updated.getHour(),
                    updated.getCommunityProduced(),
                    updated.getCommunityUsed(),
                    updated.getGridUsed());
            rabbitTemplate.convertAndSend(RabbitMQConfig.UPDATES_QUEUE, update);
            log.info("Update geschickt für Stunde {}", updated.getHour());
```

### §5C — percentage-service implementation

**First delete the copy-pasted skeletons** (empty classes in the wrong package + stray FXML):
```
percentage-service/src/main/java/com/disys/community_producer/MessageConsumer.java
percentage-service/src/main/java/com/disys/community_producer/PercentageCalculator.java
percentage-service/src/main/java/com/disys/community_producer/PercentageRepository.java
percentage-service/src/main/java/com/disys/community_producer/PercentageServiceApp.java
percentage-service/src/main/resources/com/disys/community_producer/main.fxml   (and the empty dirs)
```

Then create these under `percentage-service/src/main/java/com/disys/percentage_service/`:

**`PercentageServiceApp.java`**
```java
package com.disys.percentage_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PercentageServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(PercentageServiceApp.class, args);
    }
}
```

**`RabbitMQConfig.java`** (declare the queue so the service is independently startable + JSON converter)
```java
package com.disys.percentage_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String UPDATES_QUEUE = "energy.updates";

    @Bean
    public Queue updatesQueue() {
        return new Queue(UPDATES_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
```

**`Percentage.java`** (entity → `percentage` table)
```java
package com.disys.percentage_service;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "percentage")
public class Percentage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime hour;

    @Column(name = "community_depleted", nullable = false)
    private double communityDepleted;

    @Column(name = "grid_portion", nullable = false)
    private double gridPortion;

    public Percentage() {}

    public Percentage(LocalDateTime hour) {
        this.hour = hour;
    }

    public UUID getId() { return id; }

    public LocalDateTime getHour() { return hour; }
    public void setHour(LocalDateTime hour) { this.hour = hour; }

    public double getCommunityDepleted() { return communityDepleted; }
    public void setCommunityDepleted(double communityDepleted) { this.communityDepleted = communityDepleted; }

    public double getGridPortion() { return gridPortion; }
    public void setGridPortion(double gridPortion) { this.gridPortion = gridPortion; }
}
```

**`PercentageRepository.java`**
```java
package com.disys.percentage_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PercentageRepository extends JpaRepository<Percentage, UUID> {
    Optional<Percentage> findByHour(LocalDateTime hour);
}
```

**`PercentageCalculator.java`** (the brain — see §5D for formulas)
```java
package com.disys.percentage_service;

import com.disys.shared.UsageUpdate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PercentageCalculator {

    private final PercentageRepository percentageRepository;

    public PercentageCalculator(PercentageRepository percentageRepository) {
        this.percentageRepository = percentageRepository;
    }

    @Transactional
    public Percentage process(UsageUpdate update) {
        double produced = update.getCommunityProduced();
        double used = update.getCommunityUsed();
        double grid = update.getGridUsed();

        // wie viel vom produzierten Pool verbraucht wurde
        double depleted = produced > 0 ? (used / produced) * 100.0 : 0.0;
        if (depleted > 100.0) depleted = 100.0;

        // Anteil des Netzstroms an der Gesamtnutzung
        double total = used + grid;
        double portion = total > 0 ? (grid / total) * 100.0 : 0.0;

        Percentage row = percentageRepository.findByHour(update.getHour())
                .orElse(new Percentage(update.getHour()));
        row.setCommunityDepleted(round2(depleted));
        row.setGridPortion(round2(portion));
        return percentageRepository.save(row);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
```

**`MessageConsumer.java`**
```java
package com.disys.percentage_service;

import com.disys.shared.UsageUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

    private final PercentageCalculator calculator;

    public MessageConsumer(PercentageCalculator calculator) {
        this.calculator = calculator;
    }

    @RabbitListener(queues = RabbitMQConfig.UPDATES_QUEUE)
    public void handleUpdate(UsageUpdate update) {
        log.info("Update empfangen für Stunde {}", update.getHour());
        Percentage result = calculator.process(update);
        log.info("Percentage gespeichert {}: depleted={}%, gridPortion={}%",
                 result.getHour(), result.getCommunityDepleted(), result.getGridPortion());
    }
}
```

**`percentage-service/src/main/resources/application.properties`** — overwrite (currently a copy of rest-api's):
```
server.port=8083
spring.application.name=percentage-service

spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=disysuser
spring.datasource.password=disyspw
spring.jpa.hibernate.ddl-auto=none

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=disysuser
spring.rabbitmq.password=disyspw
```

---

## 5D. Calculation (verified vs spec example)

Spec example `produced=18.05, used=18.05, grid=1.076` → `100.00` and `5.63`.

| column | formula | edge case |
|---|---|---|
| `community_depleted` | `used / produced * 100` (cap 100) | `produced==0` → 0 |
| `grid_portion` | `grid / (used + grid) * 100` | `used+grid==0` → 0 |

Check: `18.05/18.05*100 = 100.00` ✓ · `1.076/19.126*100 = 5.63` ✓

---

## 6. Build & test

Prereq: `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`; `docker compose up -d`.

1. `./mvnw clean compile` — must stay BUILD SUCCESS.
2. `./mvnw -f rest-api/pom.xml flyway:migrate` — create the tables.
3. Run percentage-service alone: `./mvnw -f percentage-service/pom.xml spring-boot:run` (run via `-f`, NOT `-pl … -am` — that binds the aggregator pom and fails).
4. **Isolated test:** in RabbitMQ mgmt UI (`http://localhost:15672`, `disysuser`/`disyspw`) publish to `energy.updates` a JSON like
   `{"hour":"2025-01-10T14:00:00","communityProduced":18.05,"communityUsed":18.05,"gridUsed":1.076}` →
   expect a `percentage` row `100.0 / 5.63`. (Set the message's `content_type` to `application/json`.)
5. **E2E:** start rest-api, usage-service, percentage-service, then producer (+ user). Watch the `percentage` table update.

---

## 7. Out of scope / flagged

- **community-user** is half-baked (uses `@Value("${energy.queue.name}")` likely undefined; sends a hand-built JSON string instead of an `EnergyMessage` object; mixes `@Component`+`@Configuration`). It is NOT in the root `<modules>`. Leave for its owner, but it must adopt the same topology (default exchange, `energy.queue`, send an `EnergyMessage` object) to work E2E.
- gui has two parallel package trees (`com.disys.gui` + `com.energycommunity.javafxgui`) — cosmetic debt, not touched.
