# AGENTS.md

## Project & Profile

Terraaero Backend — drone interception simulation service for mobile fire
groups (MOG). Multi-layer architecture with domain package separation, built
on Spring Boot Kotlin multi-module Gradle layout (`root` + `core`).

Package: `space.gitlab.deevlab.terraaero.backend`

### Code style

You MUST strictly follow the project's coding standards, naming conventions, and language-specific rules.

Before generating, refactoring, or modifying any code, you are REQUIRED to read and apply the guidelines defined in the external style guide:

- **File Path:** [`./CODE-STYLE.md`](./CODE-STYLE.md)

_Instruction for Agent:_ If you haven't read `./CODE-STYLE.md` in the current session, use your file-reading tool to fetch its content before writing any code. Do not hallucinate styles.

### Modules

- **Mandatory**: keep the multi-module structure (`root` + at least one application module).
- Application code lives in `core/` (or additional domain modules).
- The root `build.gradle.kts` declares plugin versions with `apply false` and configures shared settings for all subprojects (`allprojects`, `subprojects`).
- Version pins for third-party libraries go in `dependencies.gradle.kts` under `extra["versions"]`.

## Operational Rules & Critical Restrictions

**UNDER NO CIRCUMSTANCES may you commit, push, amend, rebase, or modify the git history without an EXPLICIT instruction to do so.** This is the most important rule in this document. Violating it may result in lost work and broken branches.

This specifically includes:

- `git commit` / `git commit --amend` / `git commit -m "..."`
- `git push` / `git push --force` / `git push --force-with-lease`
- `git add` (stage for commit — prefer working-tree-only edits)
- `git rebase` / `git reset` / `git checkout` (to modify branches)
- Any other command that creates or alters commits

If the user asks "what should the commit message be?" — **suggest a message but do NOT commit**. Wait for an explicit directive such as:

- "commit"
- "commit and push"
- "закоммить"
- "сделай коммит"

**If the user says "обнови AGENTS.md" or similar — this is NOT a commit instruction. Do NOT add or commit files unless told to.**

## Workflow & Verification Commands

### Time limit (HARD REQUIREMENT)

**Every bash command MUST complete within 60 seconds.** All commands must be invoked through the timeout wrapper:

```bash
scripts/python/.venv/Scripts/python scripts/python/timeouted.py "<your command>"
```

Long-running daemons (server start) must use `nohup ... >/dev/null 2>&1 &` so the wrapper returns immediately.

### Verify after changes

Run **all** checks in this order — treat errors as blockers:

```bash
scripts/python/.venv/Scripts/python scripts/python/timeouted.py "./gradlew build"
```

### Mandatory testing

**Every change must be verified by running the test suite.** No exceptions.

```bash
scripts/python/.venv/Scripts/python scripts/python/timeouted.py "cd core/src/test/python && uv run pytest"
```

All JVM tests in `./gradlew build` and all pytest integration tests must pass. If any test fails, fix the issue before considering the change complete. The pytest suite uses `uv` and `pytest`; dependencies are declared in `core/src/test/python/pyproject.toml`.

**Note:** The previous run's server may still be alive on port 8080. Always kill it (see below) before starting a fresh one.

### Start the application

```bash
scripts/python/.venv/Scripts/python scripts/python/timeouted.py "cd .docker/db && docker compose up -d"
```

```bash
scripts/python/.venv/Scripts/python scripts/python/timeouted.py "nohup java -jar core/build/libs/terraaero-back-0.0.1.jar --spring.profiles.active=dev >/dev/null 2>&1 &"
```

```bash
scripts/python/.venv/Scripts/python scripts/python/timeouted.py "for i in $(seq 1 30); do curl -s -o /dev/null http://localhost:8080/api/situation 2>/dev/null && echo ready && break; sleep 3; done"
```

**Automated start script (recommended):**

```bash
scripts/python/.venv/Scripts/python scripts/python/timeouted.py "for pid in \$(netstat -ano 2>/dev/null | grep ':8080.*LISTENING' | awk '{print \$NF}'); do taskkill -F -PID \$pid 2>/dev/null; done"
scripts/python/.venv/Scripts/python scripts/python/timeouted.py "cd .docker/db && docker compose down -v && docker compose up -d && sleep 3"
scripts/python/.venv/Scripts/python scripts/python/timeouted.py "nohup java -jar core/build/libs/terraaero-back-0.0.1.jar --spring.profiles.active=dev >/dev/null 2>&1 &"
scripts/python/.venv/Scripts/python scripts/python/timeouted.py "for i in $(seq 1 30); do curl -s -o /dev/null http://localhost:8080/api/situation 2>/dev/null && echo ready && break; sleep 3; done"
```

### Stop the application

**Always kill the server when done — do not leave it running indefinitely.**

```bash
scripts/python/.venv/Scripts/python scripts/python/timeouted.py "for pid in \$(netstat -ano 2>/dev/null | grep ':8080.*LISTENING' | awk '{print \$NF}'); do taskkill -F -PID \$pid 2>/dev/null; done && echo port free"
```

Note: `pkill -f bootRun` does **not** reliably work on Windows. Use the `taskkill` command above.

## Software Architecture & Design Patterns

### Package layout

- **Never create or use a `model` package.** Domain state types must live in explicit purpose folders instead.
- `enum/` contains only enum classes. Kotlin packages for this folder use an escaped segment, for example:

  ```kotlin
  package space.gitlab...mog.`enum`
  import space.gitlab...mog.`enum`.MogActivityStatus
  ```

- `data/` contains only `data class` DTO/state/input classes.
- `data/api/` contains every `*Request` and `*Response` class, including nested response DTOs used by aggregate API responses.
- `data/api/internal/` contains DTOs used as field types inside `data/api/` classes (such as `*Input` and `*State` classes). These are API-visible but are not top-level Request/Response types.
- `data/internal/` contains non-API data classes such as `*Plan`, `*Input`, and `*State` classes that are **not** referenced from `data/api/` classes.
- `storage/` contains only persistence classes: `*Entity` and `*Repository`.
- `service/` contains only services that directly implement controller endpoint operations. Supporting services live in purpose subpackages such as `service/internal/`.
- Domain-owned files stay inside their domain folder. In particular, every `Mog*` file belongs under `mog/`, every `Target*` file belongs under `target/`, and these types must not be parked in `situation/` or other aggregate packages just because they are used by an aggregate response.

### DTO package structure

- Directly controller-visible DTOs live in the endpoint domain's `data/api/`.
- Request/response DTOs used only as nested DTOs still live in the owning domain's `data/api/`.
- Internal non-API DTOs live in the owning domain's `data/internal/`.
- DTOs used as field types inside `data/api/` classes (such as `*Input` and `*State`) live in `data/api/internal/` under the owning domain.
- Aggregate responses must import nested domain DTOs from their owning domains: `Mog*Response` from `mog/data/api/`, `Target*Response` from `target/data/api/`, `Point*Response` from `point/data/api/`, and `Trajectory*Response` from `trajectory/data/api/`.

### Domain folders

- Each domain type lives in its own domain folder:
  - `Mog*` in `mog/`
  - `Target*` in `target/`
  - `Point*` in `point/`
  - `Trajectory*` in `trajectory/`
- Aggregate domains (`situation/`) must not contain foreign files — only their own services and DTOs.

### DTO Design

- **Field types** — all fields in **request** DTOs must be `String` (or `String?`), never `Double`, `UUID`, or `Instant`. All fields in **response** DTOs must use native types (`Double`, `UUID`, `Instant`).
- **JSON naming** — all JSON keys must be **explicitly declared** via `@field:JsonProperty("snake_case")` on every field, never rely on Jackson's default camelCase naming. Entity references never use the `_id` suffix: `assignedMogId` maps to `@JsonProperty("assigned_mog")`, `trajectoryId` maps to `@JsonProperty("trajectory")`, `mogIds` maps to `@JsonProperty("mogs")`.
- **Boolean serialization** — fields with `is` prefix use `@get:JsonIgnore @field:JsonProperty` to prevent duplicate serialization (the `isKilled` / `killed` problem).
- **Partial updates** — all fields default to `null`; absence from JSON means "don't update". Class-level `@AtLeastOneField` annotation rejects empty `{}` bodies. Lists use `= null` default (never `= emptyList()`), so the field can be entirely omitted.

### Controllers

- Controllers are **thin** — each method delegates to exactly one service call.
- Controllers never access repositories directly.
- Use `@Suppress("unused")` on controller classes (Spring instantiates via reflection).
- Use `@Tag(name = "...")` for Swagger documentation.

### Controller-Service Communication

- **Controller layer**: all controller method parameters and `@RequestParam`/`@PathVariable` arguments must be `String` (or `String?`). DTO request classes already enforce `String` fields — this is about raw controller parameters.

- **`*String` suffix**: every controller parameter that carries a raw value destined for conversion **must** end with the `String` suffix (exception: parameters that are `@RequestBody` Request DTO classes — the suffix is already inside the DTO fields).

  ```kotlin
  @GetMapping("/file/{file_id}")
  override fun getFile(
    @RequestParam("user_id") userIdString: String,
    @PathVariable("file_id") fileIdString: String,
  ): FileResponse = fileService.getFile(userIdString, fileIdString)
  ```

- **Service layer**: service method parameters that receive raw `String` values from the controller **must also use the `*String` suffix**, matching the controller naming exactly.

- **Strict typing**: immediately at the start of every service method that receives `String` parameters, **cast every String to its proper type** (UUID, Double, Instant, etc.) using the conversion functions from `shared/util/StringExtensions.kt`. After the initial conversion block, **no raw String arguments should remain in scope** — work exclusively with strongly-typed values.

  ```kotlin
  fun getFile(
    userIdString: String,
    fileIdString: String,
  ): FileResponse {
    val userId = userIdString.toUUIDOrThrow()
    val fileId = fileIdString.toUUIDOrThrow()

    val file = fileRepository.findByIdOrThrow(fileId, "File")

    return fileMapper.toResponse(file)
  }
  ```

- **DTOs are the exception**: when a service receives a Request DTO object, the DTO fields are already `String` inside the class — no `*String` suffix on the parameter itself. The service method unwraps and converts the DTO's String fields inside its body.

### Services

- **`*Service` naming** — Only classes that directly implement controller endpoint operations (i.e., are injected into controllers and called from controller methods) may have the `Service` suffix. All other supporting classes (internal helpers, calculators, validators, etc.) must use descriptive names without the `Service` suffix, such as `*Calculator`, `*Validator`, `*Helper`, `*Manager`, or domain-specific names like `SituationWriteLock`.

- **Service responsibilities** — A `*Service` class orchestrates the endpoint flow: validating input, calling repositories, invoking mappers, and coordinating other components. It does not contain complex algorithms or low‑level logic — those belong to separate internal classes (e.g., in `service/internal/`).

### Mappers

- **Mappers are mandatory.** Every conversion between layers (Entity to/from Response DTO, Request DTO to Entity, Entity to internal DTO, raw values to typed DTO) must go through a dedicated mapper class. No inline mapping anywhere in services or controllers.

- **Never use MapStruct.** Mappers are plain Kotlin `@Component` classes with hand-written functions. No annotation-based code generation, no mapping interfaces, no reflection.

- **Never delegate mapping logic to separate services.** A mapper is a mapper — not a service. It has no dependencies on other mappers, no side effects, no database access. It purely transforms data.

- **Mappers sit at the domain root**, at the same directory level as `service/` and `storage/`:

  ```
  mog/
    data/
    enum/
    service/
      internal/
        MogInputParsingService.kt
    storage/
      MogEntity.kt
      MogRepository.kt
    MogMapper.kt          ← at domain root, alongside service/ and storage/
  ```

  Note: unlike services (which go in `service/`), mappers are placed directly at the domain root. They are not services — they are pure data transformers with no database access, no side effects, and no other service dependencies.

- **Mappers are constructor-injected** into services, grouped under a `// mappers:` comment:

  ```kotlin
  @Service
  class SituationService(
    // repositories:
    private val mogRepository: MogRepository,
    ...

    // mappers:
    private val mogMapper: MogMapper,
    private val targetMapper: TargetMapper,

    // services:
    private val situationWriteLock: SituationWriteLock,
    ...
  )
  ```

- **Controller never touches mappers** — only services are injected into controllers.

#### Forbidden anti-patterns

The following conversion patterns were used in the project **before** the mapper rule was introduced. All of them are **permanently forbidden** — their presence in code is considered a style violation:

- Inline construction of Response DTOs directly inside a service — `MogResponse(id = ..., speed = ..., ...)`, `TargetResponse(id = ..., ...)`, `TrajectoryResponse(...)`, and any other response class inside the body of `buildResponse()` or similar methods.
- Inline Entity construction via constructor inside `forEach` — `PointEntity(trajectoryId = ..., lat = ..., ...)`, `TrajectoryEntity(name = ..., status = ...)`, `TargetEntity(trajectoryId = ...)` inside the body of `replace()` or `patch()`.
- Manual field-by-field copy from internal DTO to Entity — `trajectory.startLat = motion.startLat`, `trajectory.directionLat = motion.directionLat`, and similar assignment chains.
- Construction of internal DTOs from raw Entity fields — `MogPlanningState(position = Point3D(mog.realLat, ...), speed = mog.speed, ...)`, `TargetMotionState(...)`, `InterceptionRequest(...)`, `TrajectoryMotion(...)`, `SimulationState(...)` inside `calculate()` or `selectBestMog()`.
- Methods that mix business logic with mapping — `SituationResponseService.buildResponse()` iterates trajectories, computes `haversine(...)` distances, populates `mogDestinations`, and builds all response objects in one method.
- Parsing service masquerading as a mapper — `MogInputParsingService` parses strings (which is fine) but also constructs `MogEntity` via `MogEntity.create(...)` (which belongs in `MogMapper`).
- Entity companion factory — `MogEntity.create(lat, lon, height, ...)` as a factory method inside the Entity class itself. Entity construction belongs in `MogMapper.toEntity(...)`.
- Extension function `toEntity` on an internal DTO — `ParsedMogInput.toEntity(status, timestamp)` inside `MogInputParsingService`. This is hidden mapping that should be an explicit mapper method.

Instead of all of the above — **mappers only**.

#### Correct examples

**Simple mapper — Entity to Response, Request to Entity:**

```kotlin
// user/UserMapper.kt
@Component
class UserMapper {

  // MARK: toResponse
  // -----------------------------------------------

  fun toResponse(user: User) = UserResponse(
    id = user.id.checkFieldNotNullByName { "id" },
    fullname = user.fullname,
    email = user.email,
    createdAt = user.createdAt,
    updatedAt = user.updatedAt,
  )

  // MARK: toEntity
  // -----------------------------------------------

  fun toEntity(request: UserCreateRequest) = User(
    fullname = request.validateAndGetFullname(),
    email = request.validateAndGetEmail(),
  )
}
```

**Mapper with multiple response variants and external dependencies:**

```kotlin
// listing/ListingMapper.kt
@Component
class ListingMapper {

  // MARK: toResponse
  // -----------------------------------------------

  fun toResponse(listing: Listing) = ListingResponse(
    id = listing.id.checkFieldNotNullByName { "id" },
    name = listing.name,
    type = listing.type,
    status = listing.status,
    ...
  )

  // MARK: toStatusResponse
  // -----------------------------------------------

  fun toStatusResponse(listing: Listing) = ListingStatusResponse(
    id = listing.id.checkFieldNotNullByName { "id" },
    slicerStatus = listing.status,
    error = listing.error,
    ...
  )

  // MARK: toEntity
  // -----------------------------------------------

  fun toEntity(
    request: ListingCreateRequest,
    user: User,
    file: File,
  ) = Listing(
    name = request.validateAndGetName(),
    user = user,
    file = file,
    type = request.validateAndGetType().toEnumOrThrow(),
  )
}
```

**Mapper accepting external data (not from the entity):**

```kotlin
// order/OrderMapper.kt
@Component
class OrderMapper {

  // MARK: toResponse
  // -----------------------------------------------

  fun toResponse(
    order: Order,
    listingIds: List<UUID>,   // data from a join table, not on the entity
  ) = OrderResponse(
    id = order.id.checkFieldNotNullByName { "id" },
    userId = order.user.id.checkFieldNotNullByName { "user.id" },
    costs = order.costs,
    finalCost = order.finalCost,
    listingIds = listingIds,
    ...
  )

  // MARK: toEntity
  // -----------------------------------------------

  fun toEntity(
    user: User,               // FK entity, resolved by the service
    costs: OrderCosts,        // computed value
  ) = Order(
    user = user,
    costs = costs,
    finalCost = costs.totalCost(),
  )
}
```

**Mapper usage inside a service:**

```kotlin
// Service reads an entity and hands it to the mapper:
fun getUser(userIdString: String): UserResponse =
  userRepository.findByIdOrThrow(userIdString.toUUIDOrThrow(), Constants.Entity.USER)
    .let { userMapper.toResponse(it) }

// Service resolves FK, mapper assembles the Entity:
fun createOrder(
  request: OrderCreateRequest,
  userIdString: String,
): IdResponse {
  val userId = userIdString.toUUIDOrThrow()
  val user = userRepository.findByIdOrThrow(userId, Constants.Entity.USER)
  val costs = orderCalculator.computeCosts(request)
  val order = orderMapper.toEntity(user, costs)
  val saved = orderRepository.save(order)

  return IdResponse(saved.id.checkFieldNotNullByName { "id" })
}
```

**Key principle:** the service is responsible for resolving dependencies (findById, computeCosts), the mapper is responsible for assembling the object. The mapper **never** accesses the database, never calls other services, and never makes business decisions.

### Exceptions

- Use the `BaseClientException` hierarchy for client-facing errors (`BadRequestException`, `NotFoundException`, `ConflictException`).
- Each exception takes two messages: `devMessage` (shown in dev profile) and `prodMessage` (shown in prod).
- Propagate exceptions to the `GlobalExceptionHandler` — do not catch and convert in controllers.
- Use `BaseClientExceptionFactory.unified()` for concise exception creation when dev and prod messages are the same.

## Persistence & Database Engineering

### Entities and JPA relationships

- **No `@ManyToOne` with lazy proxies.** Use bare UUID FK columns and resolve related entities manually via `repository.findById()`. This avoids `LazyInitializationException` and Hibernate session management issues.
- **No bidirectional relationships** — no `@OneToMany` back-pointers.
- Entity IDs use `@GeneratedValue(strategy = GenerationType.UUID)` with `var id: UUID? = null`. Do NOT set the ID manually when constructing entities for `save()` — let Hibernate generate it via `persist()`. Setting the ID manually causes `save()` to call `merge()` instead of `persist()`, which can cause stale-state conflicts after bulk deletes.

### Liquibase migrations

- **NEVER modify an already-applied migration.** Once a changeset has been executed in any environment (even local dev), its checksum is recorded in the `databasechangelog` table. Modifying the changeset will cause a checksum mismatch and the application will fail to start.
- To change the schema or data, always create a **new** migration file with a new changeset id.
- To remove seed data that was inserted in an old migration, use a new migration with `DELETE` statements — do not edit the original INSERT.
- Migration files live in `core/src/main/resources/db/changelog/changes/` and are registered in `changelog-master.yml`.

## Environment & Configuration

- `.env.example` is a **committed template** — never use it directly in scripts or at runtime. It exists solely as documentation for developers.
- `.env` is the **actual runtime file** (git-ignored). Developers copy `.env.example` to `.env` and fill in their local values.
- All shell scripts in `.docker/db/` read `.env` first, falling back to `.env.example` with a warning only if `.env` is absent.
- `application.yml` loads `.env` via `spring.config.import: optional:file:.env`.
