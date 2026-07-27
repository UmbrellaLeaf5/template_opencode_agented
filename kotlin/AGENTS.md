# AGENTS.md

## Project & Profile

_Brief description of the project and its purpose._

Package: `com.example.project`

Built on Kotlin with a multi-module Gradle layout (`root` + application module(s)).

### Code style

You MUST strictly follow the project's coding standards, naming conventions, and language-specific rules.

Before generating, refactoring, or modifying any code, you are REQUIRED to read and apply the guidelines defined in the external style guide:

- **File Path:** [`./CODE-STYLE.md`](./CODE-STYLE.md)

_Instruction for Agent:_ If you haven't read `./CODE-STYLE.md` in the current session, use your file-reading tool to fetch its content before writing any code. Do not hallucinate styles.

### Modules

- **Mandatory**: keep the multi-module structure (`root` + at least one application module).
- Application code lives in a dedicated module (e.g., `core/`).
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

**NEVER commit while Git is in detached `HEAD` state.** Before any commit, verify that the repository is on the intended working branch (for example with `git branch --show-current` or `git status`). If the current branch is empty, ambiguous, or not clearly the user's active working branch, stop and ask the user which branch to use before committing.

If the user asks "what should the commit message be?" — **suggest a message but do NOT commit**. Wait for an explicit directive such as:

- "commit"
- "commit and push"
- "закоммить"
- "сделай коммит"

**If the user says "обнови AGENTS.md" or similar — this is NOT a commit instruction. Do NOT add or commit files unless told to.**

## Workflow & Verification Commands

### Time limit (HARD REQUIREMENT)

**Every non-interactive bash command MUST complete within 60 seconds.** All non-interactive commands must be invoked through the timeout wrapper with captured output:

```bash
time-d -c "<your command>"
```

Use `time-d -c` by default for every non-interactive command. Use plain `time-d` without `-c` only for genuinely interactive commands that require a TTY, such as `vim`, `python -i`, REPLs, or terminal UI tools.

For commands that are expected to legitimately take longer than 60 seconds (full builds, full test suites, dependency syncs, large format/lint runs), use an explicit timeout:

```bash
time-d -c --sec <seconds> "<your command>"
```

Choose the smallest reasonable timeout for the command. Do not use a longer timeout to hide a hung process.

Long-running daemons (server start) must use `nohup ... >/dev/null 2>&1 &` so the wrapper returns immediately.

`time-d` is part of the `timeout-dead` package. Install once:

```bash
uv tool install timeout-dead   # or: pip install timeout-dead
time-d --version                       # verify installation
```

### Verify after changes

Run **all** checks in this order — treat errors as blockers:

```bash
time-d -c --sec 300 "./gradlew build"
```

### Mandatory testing

**Every change must be verified by running the test suite.** No exceptions.

```bash
time-d -c --sec 300 "cd app/src/test/python && uv run pytest"
```

All JVM tests in `./gradlew build` and all pytest integration tests must pass. If any test fails, fix the issue before considering the change complete.

**Note:** The previous run's server may still be alive on the default port. Always kill it (see below) before starting a fresh one.

### Start the application

```bash
time-d -c --sec 120 "cd .docker/db && docker compose up -d"
```

```bash
time-d -c "nohup java -jar app/build/libs/app-0.0.1.jar --spring.profiles.active=dev >/dev/null 2>&1 &"
```

```bash
time-d -c --sec 120 "for i in $(seq 1 30); do curl -s -o /dev/null http://localhost:8080/api/health 2>/dev/null && echo ready && break; sleep 3; done"
```

### Stop the application

**Always kill the server when done — do not leave it running indefinitely.**

```bash
time-d -c "for pid in \$(netstat -ano 2>/dev/null | grep ':8080.*LISTENING' | awk '{print \$NF}'); do taskkill -F -PID \$pid 2>/dev/null; done && echo port free"
```

Note: `pkill -f bootRun` does **not** reliably work on Windows. Use the `taskkill` command above.

### Setup

```bash
time-d -c --sec 300 "./gradlew build"
```

This downloads dependencies and builds the project. For IDE: IntelliJ IDEA with Kotlin plugin is recommended. Open the project root directory and let Gradle sync.

## Software Architecture & Design Patterns

### Documentation

- **Standalone Markdown documentation pages** → `SCREAMING_SNAKE_CASE` names (e.g., `CONFIG.md`, `ARCHITECTURE.md`, `CODE-STYLE.md`). Keep conventional repository files such as `README.md` unchanged unless explicitly requested.

### Package layout

- **Never create or use a `model` package.** Domain state types must live in explicit purpose folders instead.
- `enum/` contains only enum classes.
- `data/` contains only `data class` DTO/state/input classes.
- `data/api/` contains every `*Request` and `*Response` class, including nested response DTOs used by aggregate API responses (for web applications).
- `data/api/internal/` contains DTOs used as field types inside `data/api/` classes (such as `*Input` and `*State`).
- `data/internal/` contains non-API data classes such as `*Plan`, `*Input`, and `*State` classes that are **not** referenced from `data/api/`.
- `storage/` contains only persistence classes: `*Entity` and `*Repository`.
- `service/` contains only services that directly implement controller endpoint operations. Supporting services live in purpose subpackages such as `service/internal/`.
- Domain-owned files stay inside their domain folder. Types must not be parked in unrelated aggregate packages just because they are used by an aggregate response.

### Domain folders

- Each domain type lives in its own domain folder (e.g., `order/`, `user/`, `product/`).
- Aggregate domains must not contain foreign files — only their own services and DTOs.
- Reference other domains from aggregate services via imports, not by placing foreign types in the aggregate package.

### DTO Design (for web applications)

- **Field types** — all fields in **request** DTOs must be `String` (or `String?`), never raw domain types like `Double`, `UUID`, or `Instant`. All fields in **response** DTOs must use native types (`Double`, `UUID`, `Instant`).
- **JSON naming** — all JSON keys must be **explicitly declared** via `@field:JsonProperty("snake_case")` on every field. Never rely on Jackson's default camelCase naming.
- **Boolean serialization** — fields with `is` prefix use `@get:JsonIgnore @field:JsonProperty` to prevent duplicate serialization.
- **Partial updates** — all fields default to `null`; absence from JSON means "don't update". Lists use `= null` default (never `= emptyList()`), so the field can be entirely omitted.

### Controllers (for web applications)

- Controllers are **thin** — each method delegates to exactly one service call.
- Controllers never access repositories directly.
- Use `@Suppress("unused")` on controller classes (the framework instantiates via reflection).
- Use appropriate API documentation annotations (e.g., `@Tag(name = "...")` for Swagger).

### Controller-Service Communication

- **Controller layer**: all controller method parameters and `@RequestParam`/`@PathVariable` arguments must be `String` (or `String?`).

- **`*String` suffix**: every controller parameter that carries a raw value destined for conversion **must** end with the `String` suffix (exception: parameters that are `@RequestBody` Request DTO classes — the suffix is already inside the DTO fields).

  ```kotlin
  @GetMapping("/item/{item_id}")
  override fun getItem(
    @RequestParam("user_id") userIdString: String,
    @PathVariable("item_id") itemIdString: String,
  ): ItemResponse = itemService.getItem(userIdString, itemIdString)
  ```

- **Service layer**: service method parameters that receive raw `String` values from the controller **must also use the `*String` suffix**, matching the controller naming exactly.

- **Strict typing**: immediately at the start of every service method that receives `String` parameters, **cast every String to its proper type** (UUID, Double, Instant, etc.) using conversion functions. After the initial conversion block, **no raw String arguments should remain in scope** — work exclusively with strongly-typed values.

  ```kotlin
  fun getItem(
    userIdString: String,
    itemIdString: String,
  ): ItemResponse {
    val userId = userIdString.toUUIDOrThrow()
    val itemId = itemIdString.toUUIDOrThrow()

    val item = itemRepository.findByIdOrThrow(itemId, "Item")

    return itemMapper.toResponse(item)
  }
  ```

- **DTOs are the exception**: when a service receives a Request DTO object, the DTO fields are already `String` inside the class — no `*String` suffix on the parameter itself. The service method unwraps and converts the DTO's String fields inside its body.

### Services

- **`*Service` naming** — Only classes that directly implement controller endpoint operations (i.e., are injected into controllers and called from controller methods) may have the `Service` suffix. All other supporting classes (internal helpers, calculators, validators, etc.) must use descriptive names without the `Service` suffix, such as `*Calculator`, `*Validator`, `*Helper`, `*Manager`, or domain-specific names.

- **Service responsibilities** — A `*Service` class orchestrates the endpoint flow: validating input, calling repositories, invoking mappers, and coordinating other components. It does not contain complex algorithms or low‑level logic — those belong to separate internal classes.

### Mappers

- **Mappers are mandatory.** Every conversion between layers (Entity to/from Response DTO, Request DTO to Entity, Entity to internal DTO, raw values to typed DTO) must go through a dedicated mapper class. No inline mapping anywhere in services or controllers.

- **Never use code generation tools for mapping** (e.g., MapStruct). Mappers are plain Kotlin classes with hand-written functions.

- **Never delegate mapping logic to separate services.** A mapper is a mapper — not a service. It has no dependencies on other mappers, no side effects, no database access. It purely transforms data.

- **Mappers sit at the domain root**, at the same directory level as `service/` and `storage/`:

  ```
  order/
    data/
    enum/
    service/
      internal/
        OrderCalculator.kt
    storage/
      OrderEntity.kt
      OrderRepository.kt
    OrderMapper.kt          ← at domain root, alongside service/ and storage/
  ```

  Note: unlike services (which go in `service/`), mappers are placed directly at the domain root. They are not services — they are pure data transformers with no database access, no side effects, and no other service dependencies.

- **Mappers are constructor-injected** into services, grouped under a `// mappers:` comment:

  ```kotlin
  @Service
  class OrderService(
    // repositories:
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,

    // mappers:
    private val orderMapper: OrderMapper,
    private val userMapper: UserMapper,

    // services:
    private val orderCalculator: OrderCalculator,
  )
  ```

- **Controller never touches mappers** — only services are injected into controllers.

#### Forbidden anti-patterns

The following conversion patterns are **permanently forbidden** — their presence in code is considered a style violation:

- Inline construction of Response DTOs directly inside a service.
- Inline Entity construction via constructor inside `forEach` or similar loops.
- Manual field-by-field copy from internal DTO to Entity.
- Construction of internal DTOs from raw Entity fields inside business logic methods.
- Methods that mix business logic with mapping.
- Parsing/utility classes that also construct entities.
- Entity companion factory methods for construction from DTOs.
- Extension functions on DTOs that produce entities.

Instead of all of the above — **mappers only**.

#### Correct examples

**Simple mapper — Entity to Response, Request to Entity:**

```kotlin
// user/UserMapper.kt
@Component
class UserMapper {

  // MARK: Convert to response
  // --------------------------------------------------

  fun toResponse(user: User) = UserResponse(
    id = user.id.checkFieldNotNullByName { "id" },
    fullname = user.fullname,
    email = user.email,
    createdAt = user.createdAt,
    updatedAt = user.updatedAt,
  )

  // MARK: Convert to entity
  // --------------------------------------------------

  fun toEntity(request: UserCreateRequest) = User(
    fullname = request.validateAndGetFullname(),
    email = request.validateAndGetEmail(),
  )
}
```

**Mapper with multiple response variants and external dependencies:**

```kotlin
// product/ProductMapper.kt
@Component
class ProductMapper {

  // MARK: Convert to response
  // --------------------------------------------------

  fun toResponse(product: Product) = ProductResponse(
    id = product.id.checkFieldNotNullByName { "id" },
    name = product.name,
    type = product.type,
    status = product.status,
  )

  // MARK: Convert to summary response
  // --------------------------------------------------

  fun toSummaryResponse(product: Product) = ProductSummaryResponse(
    id = product.id.checkFieldNotNullByName { "id" },
    status = product.status,
    error = product.error,
  )

  // MARK: Convert to entity
  // --------------------------------------------------

  fun toEntity(
    request: ProductCreateRequest,
    owner: User,
    file: File,
  ) = Product(
    name = request.validateAndGetName(),
    owner = owner,
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

  // MARK: Convert to response
  // --------------------------------------------------

  fun toResponse(
    order: Order,
    productIds: List<UUID>,   // data from a join table, not on the entity
  ) = OrderResponse(
    id = order.id.checkFieldNotNullByName { "id" },
    userId = order.user.id.checkFieldNotNullByName { "user.id" },
    costs = order.costs,
    finalCost = order.finalCost,
    productIds = productIds,
  )

  // MARK: Convert to entity
  // --------------------------------------------------

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

// --------------------------------------------------

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

- Use a `BaseClientException` hierarchy for client-facing errors (`BadRequestException`, `NotFoundException`, `ConflictException`).
- Each exception takes two messages: `devMessage` (shown in dev profile) and `prodMessage` (shown in prod).
- Propagate exceptions to a global exception handler — do not catch and convert in controllers.
- Use a `unified()` factory method for concise exception creation when dev and prod messages are the same.

## Persistence & Database Engineering

### Entities and ORM relationships

If using an ORM (e.g., JPA/Hibernate):

- **Avoid `@ManyToOne` with lazy proxies.** Use bare FK columns and resolve related entities manually via `repository.findById()`. This avoids `LazyInitializationException` and session management issues.
- **No bidirectional relationships** — no reverse collection mappings.
- Entity IDs use auto-generation (e.g., `@GeneratedValue(strategy = GenerationType.UUID)`) with `var id: UUID? = null`. Do NOT set the ID manually when constructing entities for `save()` — let the provider generate it. Setting the ID manually causes merge semantics instead of persist, which can cause stale-state conflicts.

### Database migrations

- **NEVER modify an already-applied migration.** Once a migration has been executed in any environment (even local dev), its checksum is recorded. Modifying the changeset will cause a checksum mismatch and the application will fail to start.
- To change the schema or data, always create a **new** migration file with a new changeset id.
- To remove seed data that was inserted in an old migration, use a new migration with `DELETE` statements — do not edit the original INSERT.

## Testing Strategy

### Unit Tests

- Use **JUnit 5** (`@Test`, `@DisplayName`) for unit tests.
- Tests live in `src/test/kotlin/` mirroring the main source structure.
- Test file naming: `<Class>Test.kt`.
- Run unit tests via `time-d -c --sec 300 "./gradlew test"`.

### Integration Tests

- Place integration tests in `src/test/kotlin/` with `@Tag("integration")` or under `integration/` package.
- Use **Testcontainers** or **Spring Boot Test** for integration tests.
- Run integration tests: `time-d -c --sec 300 "./gradlew test -DincludeTags=\"integration\""`.

### Coverage

- Aim for high coverage of core business logic. Use **Jacoco** or **Kover** to measure coverage.
- Run coverage report: `time-d -c --sec 300 "./gradlew test jacocoTestReport"` (or `time-d -c --sec 300 "./gradlew koverReport"`).

## Environment & Configuration

- `.env.example` is a **committed template** — never use it directly in scripts or at runtime. It exists solely as documentation for developers.
- `.env` is the **actual runtime file** (git-ignored). Developers copy `.env.example` to `.env` and fill in their local values.
- All shell scripts read `.env` first, falling back to `.env.example` with a warning only if `.env` is absent.
- The application configuration loads `.env` via the appropriate mechanism (e.g., `spring.config.import: optional:file:.env` for Spring Boot).
