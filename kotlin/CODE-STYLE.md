# CODE-STYLE.md

All code-writing rules for Kotlin projects.

---

## Indentation & Layout

- **2-space indentation** everywhere (Kotlin, Gradle/KTS, JSON, TOML, YAML, Markdown, all config files).
- **Line length**: 100 characters.
- **Hanging indentation** for long signatures and calls:

  ```kotlin
  fun myFunc(
    arg1: String,
    arg2: Int,
  ): ReturnType {
      // ...
  }
  ```

- Formatting is governed by `.editorconfig`. No separate formatter CLI is required — IntelliJ IDEA with the Kotlin plugin reads `.editorconfig` natively and applies formatting on save/reformat.

## Language Usage

- **NEVER use `!!`** (non-null assertion). Use dedicated wrapper functions instead:
  - **For argument validation (`IllegalArgumentException`)** — use `requireNotNullByName { "argName" }` or `requireFieldNotNullByName { "fieldName" }` when validating input parameters, request DTO fields, or any external data passed to a function.
  - **For state validation (`IllegalStateException`)** — use `checkNotNullByName { "stateName" }` or `checkFieldNotNullByName { "fieldName" }` when validating internal invariants, class fields, or service state that should never be null under normal operation.
  - **For repository lookups** — use `findByIdOrThrow()` from your shared utilities.

- **Never** use raw `requireNotNull()` or `checkNotNull()` directly. Always use the named wrappers to ensure consistent error messages and proper exception types. Plain `check { }` and `require { }` (with a boolean condition, not a null check) remain fine to use where convenient.
  - **Distinction between validation types:**

  | Validation type     | Exception                  | Wrapper function                                                             | When to use                                                                                                                             |
  | ------------------- | -------------------------- | ---------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
  | Argument validation | `IllegalArgumentException` | `requireNotNullByName { "name" }`<br>`requireFieldNotNullByName { "field" }` | The **caller** provided invalid data. The function cannot proceed because the input is incomplete or malformed.                         |
  | State validation    | `IllegalStateException`    | `checkNotNullByName { "name" }`<br>`checkFieldNotNullByName { "field" }`     | The **system** is in an invalid state. Something that should have been initialized is missing. This indicates a bug or unexpected flow. |

  ```kotlin
  // Argument validation — caller error
  fun assignItem(request: AssignRequest) {
    val itemId = request.itemId.requireFieldNotNullByName { "itemId" }
    val userId = request.userId.requireFieldNotNullByName { "userId" }
  }

  // --------------------------------------------------

  // State validation — internal inconsistency
  fun executeOperation() {
    val processor = this.processor.checkFieldNotNullByName { "processor" }
    val context = this.context.checkFieldNotNullByName { "context" }
  }
  ```

- **NEVER put two classes in one file.** Even if Kotlin allows it, every `class`, `data class`, `interface`, `object`, `enum class`, or `annotation class` must live in its own file.
  - Exception: `companion object` and `fun main()` next to the application class are acceptable (framework entry point convention).
- One-line `if` and `for` statements are allowed and encouraged when they improve readability.

  ```kotlin
  if (status == OrderStatus.PENDING)
    processOrder(order)

  else if (status == OrderStatus.CANCELLED)
    skipOrder(order)

  else
    throw ConflictException.unified("Order is not processable")
  ```

- Use `for` for iteration over ranges and collections. `while` is permitted when iteration depends on mutable state or complex conditions.

  ```kotlin
  for (item in items) {
    if (item.status != ItemStatus.ACTIVE) continue

    processItem(item, context.lastOrNull(), allHandlers, processedIds)
    itemRepository.save(item)
  }
  ```

- **Type conversion and casting** — Use the appropriate method for each scenario:
  - **For parsing strings to numeric types** — Use `toDouble()`, `toInt()`, `toLong()`, or their `OrNull()` variants with explicit null handling.

    ```kotlin
    val radius = input.radius.toDoubleOrNull()
      ?: throw IllegalArgumentException("Invalid radius format")

    val count = input.count.toIntOrNull() ?: 0
    ```

  - **For safe type casting** — Use `as?` with null handling or smart cast with `is` check.

    ```kotlin
    val point = obj as? Point ?: return

    if (obj is Point)
      processPoint(obj)  // smart cast applied automatically
    ```

  - **For casting with certainty** — Unsafe cast `as` is permitted only when the type is guaranteed (e.g., after validation or in tests).

    ```kotlin
    val point = obj as Point  // only when type is absolutely certain
    ```

- **Avoid:**
  - Using `toDouble()` for type casting (use `as?` or `as` for casting)
  - Using `as` without confidence in the type

- **Lambda blocks and scoping functions** — Use `run`, `let`, `apply`, `also`, and `with` when they improve readability and reduce boilerplate. Avoid using them solely for grouping statements without returning a value.
  - **Scoping functions are encouraged for:**
    - **Chaining operations** on an object when the implementation fits in 5 lines or fewer.
    - **Applying transformations** where intermediate variables would add noise.
    - **Debugging or logging** with `also` without breaking the flow.
    - **Executing code on nullable objects** with `let` and safe call operator.

    ```kotlin
    // Good — clean transformation chain
    fun getUser(userIdString: String): UserResponse =
      userRepository.findByIdOrThrow(userIdString.toUUIDOrThrow(), Constants.Entity.USER)
        .also { logger.debug("Fetching User: {}", userIdString) }
        .let { userMapper.toResponse(it) }
    ```

  - **Scoping functions are discouraged for:**
    - **Grouping unrelated statements** without returning a value.
    - **Creating unnecessary nesting** when simple sequential code is clearer.
    - **Exceeding 5 lines** — if a scoping function block grows longer, extract it to a separate function.

    ```kotlin
    // Bad — run used only for grouping, no return value
    run {
      val processor = DataProcessor()
      processor.init(config)
      processor.start()
    }

    // Better — direct sequential code or apply for initialization
    val processor = DataProcessor()
    processor.init(config)
    processor.start()

    // Or with apply when chain is clear
    val processor = DataProcessor().apply {
      init(config)
      start()
    }
    ```

  - **Guideline:** Use scoping functions thoughtfully. They are tools for writing concise, expressive code, not for hiding complexity or grouping arbitrary statements.

## Naming Conventions

- **Variables, parameters, fields** → `camelCase` (e.g. `itemCount`, `userId`, `isActive`)
- **Functions, methods** → `camelCase` (e.g. `getUser()`, `calculateTotal()`)
- **Classes, data classes, interfaces, objects, enums** → `PascalCase` (e.g. `UserService`, `OrderResponse`, `NotFoundException`)
- **Constants** (`const val`) → `SCREAMING_SNAKE_CASE` (e.g. `MAX_RETRIES`, `DEFAULT_TIMEOUT_MS`)
- **JSON keys** → `snake_case` (e.g. `"user_id"`, `"created_at"`). All JSON keys must be **explicitly declared** via `@JsonProperty("snake_case")` — never rely on default naming.
- **Source files** (`.kt`) → `PascalCase`, matching the primary class or the purpose. Even files containing only top-level functions use `PascalCase` (e.g. `StringExtensions.kt`, `ValidationUtils.kt`).

### DTO Naming

- **`*Request` and `*Response` — only for REST API DTOs.** The `*Request` and `*Response` suffixes are allowed exclusively for classes that directly participate in HTTP request/response serialization in controllers. Internal DTOs must never use these suffixes.
- **Internal parameters** use the `*Input`, `*State`, or `*Plan` suffixes instead of `*Request`.
- **Correct naming examples:**
  - `OrderResponse` — API response (allowed)
  - `UpdateOrderRequest` — API request (allowed)
  - `CalculationPlan` — internal plan (allowed)
  - `CalculationInput` — internal DTO (allowed)

- Prefer specific, descriptive names. Avoid ambiguous abbreviations:
  - `resolvedApiKey` rather than `key` when multiple keys exist.
  - `userPreferences` not `prefs`.

## Blank Lines

- Use blank lines to separate logical sections of code. Avoid excessive blank lines that create visual noise.

- **Important:** Blank line rules may overlap. When multiple rules require a blank line at the same position, use exactly **one blank line**. Never use two or more consecutive blank lines.

  Blank line **needed** — when switching between different variables / logical groups:

  ```kotlin
  selectedItem.predictedStatus = ItemStatus.BUSY
  selectedItem.predictedUpdatedAt = clock.instant()
  itemRepository.saveAndFlush(selectedItem)

  order.assignedItemId = selectedItemId
  order.status = OrderStatus.PENDING
  order.isLocked = false
  ```

  Blank line **not needed** — sequential calls on the same object or related group:

  ```kotlin
  order.startLat = motion.startLat
  order.startLon = motion.startLon
  order.startHeight = motion.startHeight
  order.directionLat = motion.directionLat
  order.directionLon = motion.directionLon
  ```

- Between peer-level definitions (top-level functions, member methods), use one of the following:
  - A separator line (`// --------------------------------------------------`) alone — for trivial methods or private helpers
  - A MARK comment with a separator line — for important methods
  - A MARK comment for a group, with separator lines between methods within the group

  No naked blank lines between peer-level definitions — always use a separator or MARK.

  ```kotlin
  fun computeFull(request: CalculationRequest): CalculationPlan? {
    ...
  }

  // --------------------------------------------------

  private fun computeNewPlan(
    request: CalculationRequest,
    maxRadius: Double,
  ): CalculationPlan? {
    ...
  }

  // MARK: Time-to-interaction calculation
  // --------------------------------------------------

  private fun calculateInteractionTime(
    item: ItemState,
    target: TargetState,
    maxRadius: Double,
  ): Double? {
    ...
  }
  ```

  After a closing `// --------------------------------------------------` or `// MARK:` section header — a blank line before the next statement.

  ```kotlin
  // MARK: Private Helpers
  // --------------------------------------------------

  private fun buildResponse(input): Response
  ```

- **1 blank line** after class header between constructor parameters and class members.

  ```kotlin
  @Service
  class CalculationService(
    // repositories:
    private val itemRepository: ItemRepository,
    private val resultRepository: ResultRepository,

    // services:
    private val stateService: StateService,
    private val persistenceService: PersistenceService,
  ) {

    // MARK: Calculate and persist
    // --------------------------------------------------

    @Transactional
    fun calculateAndPersist(
      entity: OrderEntity,
      item: ItemEntity?,
      time: Instant,
    ): CalculationState {
      ...
    }

    ...

  }
  ```

- A blank line is required after one-line `if` and `for` statements too, except when the next line closes the block.

  ```kotlin
  if (item == null) return@let

  reuseContext(request, context)
  ```

- Before `if`/`for` and after their closing `}` — a blank line (separates the block from surrounding code).

  ```kotlin
  val activeItems = filterActive(items, excludedIds)

  for (item in activeItems) {
    val time = computeTime(item, target)
    candidates.add(item to time)
  }

  val best = candidates.minByOrNull { it.second }
  ```

  ```kotlin
  val activeItems = filterActive(items, excludedIds)

  for (item in activeItems)
    candidates.add(item to computeTime(item, target))

  val best = candidates.minByOrNull { it.second }
  ```

- Between adjacent conditional branches (`if`/`else if`/`else`) — a blank line before each `else if` or `else` branch.

  ```kotlin
  if (status == OrderStatus.COMPLETED)
    return buildTerminalState(input)

  else if (status == OrderStatus.IMPOSSIBLE)
    return buildTerminalState(input)

  else
    return buildActiveState(input)
  ```

  ```kotlin
  if (status == OrderStatus.COMPLETED) {
    log.info("Order {} completed", orderId)
    return buildTerminalState(input)
  }

  else if (status == OrderStatus.IMPOSSIBLE) {
    log.warn("Order {} impossible", orderId)
    return buildTerminalState(input)
  }

  else {
    log.debug("Order {} active", orderId)
    return buildActiveState(input)
  }
  ```

- Before `catch` — a blank line after the preceding `try` block closing brace.

  ```kotlin
  try {
    processor.loadState()
  }

  catch (e: Exception) {
    log.warn("Failed to load state, starting fresh")
  }
  ```

- Before `return` in a multi-line (block body) function — a blank line, except when `return` is in a one-line `if`/`for`, or when it is the only statement in a block (`function`, `try`, `catch`, etc.).

  ```kotlin
  val distance = computeDistance(positionA, positionB)

  return distance <= maxRadius
  ```

- Inside functions, a blank line is required both **before and after** variable declarations, unless they belong to the same logical group.

  Between `val x = Type()` and `x.init(...)` on the next line — **no** blank line because they are one logical group:

  ```kotlin
  val processor = DataProcessor()
  processor.init(config)

  handler.receive(processor, groupId)
  ```

  Two consecutive `val`/`var` declarations in a row are also valid without a blank line between them:

  ```kotlin
  val radius = input.radius.toDoubleOrThrow()
  val status = input.status?.let { OrderStatus.valueOf(it) }
  ```

- Within a class (constructor parameters or body fields), blank lines between consecutive fields are **not** needed. A blank line is required only when introducing a new logical group preceded by a comment or annotation.

- After a long wrapped line — a blank line before the next statement.

  ```kotlin
  throw ConflictException.unified(
    "Order " + orderId + " has no persisted state"
  )

  computeNextState(input)
  ```

- **Separator and MARK comments** — see `Comments` section for formatting rules.

## Single-Expression Functions

- If a function body consists of a single expression, use expression body (`=`) instead of block body (`{}`).
- When the function returns a value, the return type **must** be specified explicitly:

  ```kotlin
  fun multiply(a: Int, b: Int): Int = a * b
  ```

- When the function returns `Unit`, omit the return type:

  ```kotlin
  fun showMessage(text: String) = println(text)
  ```

- **Invalid** — omitting the return type for a value-returning function:

  ```kotlin
  fun multiply(a: Int, b: Int) = a * b  // WRONG — no explicit return type
  ```

- Single-expression functions must not use `return`. The expression body (`=`) replaces the need for `return`.

## Comments

- **Separator line: exactly 50 dashes** (`// --------------------------------------------------`). Never use shorter or longer separators.

- **Public methods** — Every non-trivial public method must be preceded by a `// MARK:` comment. The comment describes the method's purpose or, for endpoint methods, the HTTP path.
  - **Trivial methods** (single expression, one-liner) — a separator line alone is sufficient:

    ```kotlin
    // --------------------------------------------------

    fun getUser(id: UUID): UserResponse =
      userRepository.findByIdOrThrow(id).let { mapper.toResponse(it) }
    ```

  - **Non-trivial methods** — require a full MARK comment:

    ```kotlin
    // MARK: Calculate and persist
    // --------------------------------------------------

    @Transactional
    fun calculateAndPersist(
      entity: OrderEntity,
      item: ItemEntity?,
      time: Instant,
    ): CalculationState {
      ...
    }
    ```

  - **Groups of related functions** — a single MARK comment may mark the start of a group instead of annotating each method individually. Use judgement based on context. Between methods within the group, use a separator line:

    ```kotlin
    // MARK: Calculation helpers
    // --------------------------------------------------

    private fun calculateTime(...) { ... }

    // --------------------------------------------------

    private fun calculatePoint(...) { ... }

    // --------------------------------------------------

    private fun validateCalculation(...) { ... }
    ```

- All private helpers must be grouped under a `// MARK: Private Helpers` section marker. Within this section:
  - Most private methods use a separator line (`// --------------------------------------------------`) between them.
  - Important private methods (complex algorithm, critical business logic) may get their own `// MARK:` comment.

  ```kotlin
  // MARK: Private Helpers
  // --------------------------------------------------

  private fun buildResponse(...) { ... }

  // --------------------------------------------------

  private fun validateInput(...) { ... }

  // MARK: Complex calculation algorithm
  // --------------------------------------------------

  private fun calculateMatrix(...) { ... }
  ```

- **KDoc** — Every long or important function must have a multi-line KDoc placed immediately before the function, after the blank-line separator:

  ```kotlin
  // MARK: Calculate and persist
  // --------------------------------------------------

  /**
   * Вычисляет и сохраняет состояние для заданной сущности
   * на указанный момент времени.
   *
   * @param entity сущность для расчёта
   * @param item связанный объект или `null`, если не назначен
   * @param time момент времени, на который вычисляется состояние
   * @return результирующий [CalculationState]
   */
  @Transactional
  fun calculateAndPersist(
    entity: OrderEntity,
    item: ItemEntity?,
    time: Instant,
  ): CalculationState {
    ...
  }
  ```

  The KDoc block is separated from the blank-line separator by one blank line and sits directly above the function with no blank line between KDoc and the function.

- **Language of comments:**
  - **KDoc** — **exclusively in Russian**. All `@param`, `@return`, and description text must be written in Russian.
  - **`// MARK:` comments** — **exclusively in English**.
  - **Regular `//` comments** — either English or Russian is acceptable, but **Russian is preferred**. Historic English comments may remain, but new or modified comments should use Russian.

- After `// MARK:` the text must start with a **capital letter** or be in **ALL CAPS**:
  - `// MARK: POST /api/order/calculate`
  - `// MARK: Private Helpers`
  - `// MARK: Time-to-interaction calculation`
- `// MARK:` text must be a readable English phrase, not a function or method identifier. Do not use camelCase or PascalCase names such as `calculateAndPersist` or `ToResponse` in MARK comments.
- Regular (non-MARK) comments may start with either a capital or lowercase letter.
- Format (remember about blank lines!):

  ```kotlin
  // MARK: POST /api/order/calculate
  // --------------------------------------------------

  @Transactional
  fun calculate(time: Instant): OrderResponse { ... }

  // MARK: Private Helpers
  // --------------------------------------------------

  private fun buildResponse(...) { ... }

  // MARK: Reuse existing context
  // --------------------------------------------------

  private fun reuseContext(request: CalculationRequest): CalculationPlan?
  ```

- Constructor parameters must be grouped with category comments:

  ```kotlin
  class UserService(
    // repositories:
    private val userRepository: UserRepository,

    // mappers:
    private val userMapper: UserMapper,

    // services:
    private val notificationService: NotificationService,
  )
  ```

- Every data class property must have a short side comment explaining meaning and units where not obvious from naming. Self-explanatory fields (e.g. `userId`, `itemId`) don't need a comment.

  ```kotlin
  data class FilterConfig(
    val id: UUID = ...,
    val dfHz: Double = 0.0,  // offset from center, Hz
    val aDb: Double = 0.0,   // relative attenuation, dB
  )
  ```

## Constants

- **No magic values anywhere in code.** This applies to **all** literal types: string literals (`"user"`, `"/config/path"`), numeric constants (`3.14`, `86400`, `4096`), and any other hardcoded value that carries domain meaning. Every shared value must be defined in exactly one central location.

- **The only permissible inline literals** are:
  - Unit conversion factors (`1e6`, `1e-3`, `1000`) — values that only translate between measurement units, never domain logic
  - Precision/sentinel constants (`1e-9`, `-1`) — values that define computational precision or "no value" markers
  - Trivial initialisers (`0`, `1`, `""`) — when semantically obvious and not carrying domain meaning

- All shared strings, magic numbers, regex patterns, and validation messages live in a dedicated `object Constants` in `Constants.kt`.

- Organize into nested objects: `Math`, `Entity`, `ErrorDescription`, `Validation`, `Pattern`.

- Reference constants via `Constants.<Object>.<FIELD>` — never inline magic values.

```kotlin
object Constants {
  object Math {
    const val EARTH_RADIUS_M = 6_371_000.0
    const val DEG_TO_RAD = PI / 180.0
  }

  // --------------------------------------------------

  object Entity {
    const val USER = "User"
    const val ORDER = "Order"
  }

  // --------------------------------------------------

  object Validation {
    const val INVALID_EMAIL = "Invalid email format"
    const val REQUIRED_FIELD = "Required field is missing"
  }

  // --------------------------------------------------

  object Pattern {
    const val UUID_RE = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
  }
}
```
