# CODE-STYLE

All code-writing rules for terraaero backend.

---

## Indentation & layout

- **2-space indentation** everywhere (Kotlin, YAML, TPL).
- **Line length**: 100 characters (see `.editorconfig`).
- **Hanging indentation** for long signatures and calls:

  ```kotlin
  fun myFunc(
    arg1: String,
    arg2: Int,
  ): ReturnType {
      // ...
  }
  ```

- Formatting is governed by `.editorconfig`. No separate formatter CLI is required — IntelliJ IDEA with the Kotlin plugin reads `.editorconfig` natively.

## Language usage

- **NEVER use `!!`** (non-null assertion). Use dedicated wrapper functions from `shared/util/ToNotNullOrThrow.kt` instead:
  - **For argument validation (`IllegalArgumentException`)** — use `requireNotNullByName { "argName" }` or `requireFieldNotNullByName { "fieldName" }` when validating input parameters, request DTO fields, or any external data passed to a function.
  - **For state validation (`IllegalStateException`)** — use `checkNotNullByName { "stateName" }` or `checkFieldNotNullByName { "fieldName" }` when validating internal invariants, class fields, or service state that should never be null under normal operation.
  - **For repository lookups** — use `findByIdOrThrow()` from `shared/util/FindByIdOrThrow.kt`.

- **Never** use raw `requireNotNull()` or `checkNotNull()` directly. Always use the named wrappers to ensure consistent error messages and proper exception types. Plain `check { }` and `require { }` (with a boolean condition, not a null check) remain fine to use where convenient.
  - **Distinction between validation types:**

  | Validation type     | Exception                  | Wrapper function                                                             | When to use                                                                                                                             |
  | ------------------- | -------------------------- | ---------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
  | Argument validation | `IllegalArgumentException` | `requireNotNullByName { "name" }`<br>`requireFieldNotNullByName { "field" }` | The **caller** provided invalid data. The function cannot proceed because the input is incomplete or malformed.                         |
  | State validation    | `IllegalStateException`    | `checkNotNullByName { "name" }`<br>`checkFieldNotNullByName { "field" }`     | The **system** is in an invalid state. Something that should have been initialized is missing. This indicates a bug or unexpected flow. |

  ```kotlin
  // Argument validation — caller error
  fun assignMog(request: AssignRequest) {
    val mogId = request.mogId.requireFieldNotNullByName { "mogId" }
    val targetId = request.targetId.requireFieldNotNullByName { "targetId" }
  }

  // State validation — internal inconsistency
  fun executeInterception() {
    val predictor = this.predictor.checkFieldNotNullByName { "predictor" }
    val trajectory = this.trajectory.checkFieldNotNullByName { "trajectory" }
  }
  ```

- **NEVER put two classes in one file.** Even if Kotlin allows it, every `class`, `data class`, `interface`, `object`, `enum class`, or `annotation class` must live in its own file.
  - Exception: `companion object` and `fun main()` next to the application class are acceptable (Spring Boot entry point convention).
- One-line `if` and `for` statements are allowed and encouraged when they improve readability.

  ```kotlin
  if (status == MogActivityStatus.RESTING)
    assignMog(mog, trajectory)

  else if (status == MogActivityStatus.BUSY)
    skipMog(mog)

  else
    throw ConflictException.unified("MOG is not available")
  ```

- Use `for` for iteration over ranges and collections. `while` is permitted when iteration depends on mutable state or complex conditions.

  ```kotlin
  for (trajectory in trajectories) {
    if (trajectory.status != TrajectoryStatus.PENDING) continue

    assignMogToLastKnownPoint(trajectory, points.lastOrNull(), allMogs, assignedMogIds)
    trajectoryRepository.save(trajectory)
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

    if (obj is Point) {
      processPoint(obj)  // smart cast applied automatically
    }
    ```

  - **For casting with certainty** — Unsafe cast `as` is permitted only when the type is guaranteed (e.g., after validation or in tests).

    ```kotlin
    val point = obj as Point  // only when type is absolutely certain
    ```

- **Avoid:**
  - Using `toDouble()` for type casting (use `as?` or `as` for casting)
  - Using `as` without confidence in the type

    ```kotlin
    // Wrong — using toDouble for type casting
    val doubleValue = intValue.toDouble()

    // Wrong — unsafe cast without guarantee
    val point = obj as Point
    ```

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
      val predictor = TrajectoryPredictor()
      predictor.init(config)
      predictor.start()
    }

    // Better — direct sequential code or apply for initialization
    val predictor = TrajectoryPredictor()
    predictor.init(config)
    predictor.start()

    // Or with apply when chain is clear
    val predictor = TrajectoryPredictor().apply {
      init(config)
      start()
    }
    ```

  - **Guideline:** Use scoping functions thoughtfully. They are tools for writing concise, expressive code, not for hiding complexity or grouping arbitrary statements.

## DTO Naming

- **`*Request` и `*Response` — только для REST API DTO.** Суффиксы `*Request` и `*Response` допустимы исключительно для классов, непосредственно участвующих в сериализации/десериализации HTTP-запросов и ответов контроллеров. Внутренние DTO и DTO из `data/internal/` никогда не должны использовать эти суффиксы.
- **Внутренние параметры расчётов** используют суффиксы `*Input`, `*State` или `*Plan` вместо `*Request`.
- **Примеры правильного именования:**
  - `SituationResponse` — API ответ (допустимо)
  - `UpdateSituationRequest` — API запрос (допустимо)
  - `InterceptionPlan` — внутренний план перехвата (допустимо)
  - `InterceptionPlanRequest` — внутренний DTO, назван без `*Request` (допустимо)
- **DTO в `data/api/`** — всегда API-видимые Request/Response.
- **DTO в `data/api/internal/`** — API-видимые как типы полей (но не top-level запросы/ответы); используют `*Input`, `*State` и т.д.
- **DTO в `data/internal/`** — внутренние, не API-видимые. Используют `*Input`, `*State`, `*Plan` и т.д.

- **`*Service` suffix** — только для классов, напрямую реализующих операции конечных точек контроллера (injected in controllers). Все остальные вспомогательные классы должны использовать описательные имена без суффикса `Service`: `*Calculator`, `*Validator`, `*Helper`, `*Manager`, `*Selector`, `*Builder`, `*Handler`, `*Persister`, `*Recorder`, `*Resetter` и т.д.

  ```kotlin
  // Controller delegates (единственные с суффиксом Service):
  class SituationService       // injected into SituationController
  class FileService            // injected into FileController

  // Supporting classes (без суффикса Service):
  class InterceptionCalculator    // calculates interception times
  class SituationResponseBuilder  // builds response DTOs
  class SituationPatchHandler     // applies patch operations
  class BestMogSelector           // selects best MOG
  class TrajectoryPredictor       // predicts trajectory
  class SituationWriteLock        // advisory DB lock
  class MogMapper                 // maps between layers
  ```

## Blank lines

- Use blank lines to separate logical sections of code. Avoid excessive blank lines that create visual noise.

- **Important:** Blank line rules may overlap. When multiple rules require a blank line at the same position, use exactly **one blank line**. Never use two or more consecutive blank lines.

  Blank line **needed** — when switching between different variables / logical groups:

  ```kotlin
  bestMog.predictedStatus = MogActivityStatus.BUSY
  bestMog.predictedUpdatedAt = clock.instant()
  mogRepository.saveAndFlush(bestMog)

  trajectory.assignedMogId = bestMogId
  trajectory.status = TrajectoryStatus.PENDING
  trajectory.isLocked = false
  ```

  Blank line **not needed** — sequential calls on the same object or related group:

  ```kotlin
  trajectory.startLat = motion.startLat
  trajectory.startLon = motion.startLon
  trajectory.startHeight = motion.startHeight
  trajectory.directionLat = motion.directionLat
  trajectory.directionLon = motion.directionLon
  ```

- Between peer-level definitions (top-level functions, member methods), use one of the following:
  - A separator line (`// -----------------------------------------------`) alone — for trivial methods or private helpers
  - A MARK comment with a separator line — for important methods
  - A MARK comment for a group, with separator lines between methods within the group

  No naked blank lines between peer-level definitions — always use a separator or MARK.

  ```kotlin
  fun computeFull(request: InterceptionRequest): InterceptionPlan? {
    ...
  }

  // -----------------------------------------------

  private fun computeNewPlan(
    request: InterceptionRequest,
    mogRadius: Double,
  ): InterceptionPlan? {
    ...
  }

  // MARK: Time-to-intercept calculation
  // -----------------------------------------------

  private fun timeToIntercept(
    mog: MogPlanningState,
    target: TargetMotionState,
    mogRadius: Double,
  ): Double? {
    ...
  }
  ```

  After a closing `// -----------------------------------------------` or `// MARK:` section header — a blank line before the next statement.

  ```kotlin
  // MARK: Private Helpers
  // -----------------------------------------------

  private fun buildSituationResponse(input): SituationResponse
  ```

- **1 blank line** after class header between constructor parameters and class members.

  ```kotlin
  @Service
  class SimulationCalculator(
    // repositories:
    private val mogRepository: MogRepository,
    private val targetRepository: TargetRepository,
    private val pointRepository: PointRepository,

    // services:
    private val simulationStateCalculationService: SimulationStateCalculationService,
    private val simulationPersistenceService: SimulationPersistenceService,
  ) {

    // MARK: calculateAndPersist
    // -----------------------------------------------

    @Transactional
    fun calculateAndPersist(
      traj: TrajectoryEntity,
      mog: MogEntity?,
      time: Instant,
    ): SimulationState {
      ...
    }

    ...

  }
  ```

  **Note:** The blank line after `{` is the required 1 blank line. The MARK comment follows immediately after it. When blank line rules overlap, use only one blank line (see general principle above).

- A blank line is required after one-line `if` and `for` statements too, except when the next line closes the block.

  ```kotlin
  if (mog == null) return@let

  reuseDestination(request, destination)
  ```

- Before `if`/`for` and after their closing `}` — a blank line (separates the block from surrounding code).

  ```kotlin
  val freeMogs = filterFreeMogs(mogs, excludedIds)

  for (mog in freeMogs) {
    val time = computeInterceptTime(mog, target)
    candidates.add(mog to time)
  }

  val best = candidates.minByOrNull { it.second }
  ```

  ```kotlin
  val freeMogs = filterFreeMogs(mogs, excludedIds)

  for (mog in freeMogs)
    candidates.add(mog to computeInterceptTime(mog, target))

  val best = candidates.minByOrNull { it.second }
  ```

- Between adjacent conditional branches (`if`/`else if`/`else`) — a blank line before each `else if` or `else` branch.

  ```kotlin
  if (status == TrajectoryStatus.COMPLETED)
    return buildTerminalState(input)

  else if (status == TrajectoryStatus.IMPOSSIBLE)
    return buildTerminalState(input)

  else
    return buildActiveState(input)
  ```

  ```kotlin
  if (status == TrajectoryStatus.COMPLETED) {
    log.info("Trajectory {} completed", trajectoryId)
    return buildTerminalState(input)
  }

  else if (status == TrajectoryStatus.IMPOSSIBLE) {
    log.warn("Trajectory {} impossible", trajectoryId)
    return buildTerminalState(input)
  }

  else {
    log.debug("Trajectory {} active", trajectoryId)
    return buildActiveState(input)
  }
  ```

- Before `catch` — a blank line after the preceding `try` block closing brace.

  ```kotlin
  try {
    predictor.loadState()
  }

  catch (e: Exception) {
    log.warn("Failed to load predictor state, starting fresh")
  }
  ```

- Before `return` in a multi-line (block body) function — a blank line, except when `return` is in a one-line `if`/`for`, or when it is the only statement in a block (`function`, `try`, `catch`, etc.).

  ```kotlin
  val distance = haversine(mogPosition, targetPosition)

  return distance <= mog.radius
  ```

- Inside functions, a blank line is required both **before and after** variable declarations, unless they belong to the same logical group.

  Between `val x = Type()` and `x.init(...)` on the next line — **no** blank line because they are one logical group:

  ```kotlin
  val predictor = TrajectoryPredictor()
  predictor.init(config)

  p2p.receive(predictor, GROUP_IDS.antenna_1)
  ```

  Two consecutive `val`/`var` declarations in a row are also valid without a blank line between them:

  ```kotlin
  val radius = input.radius.toDoubleOrThrow()
  val status = input.status?.let { MogActivityStatus.valueOf(it) }
  ```

- Within a class (constructor parameters or body fields), blank lines between consecutive fields are **not** needed. A blank line is required only when introducing a new logical group preceded by a comment or annotation.

- When initializing an object field by field — a blank line between declaration and the first assignment (separates declaration from multi-field population). The criterion is that the variable is subsequently used in several independent lines that form a logical block. This differs from the `init` case where `val x = Type()` and `x.init(...)` are one inseparable group — there, no blank line is needed because the lines are not separable by meaning.

  ```kotlin
  val entity = MogEntity()

  entity.lat = input.lat.toDoubleOrThrow()
  entity.lon = input.lon.toDoubleOrThrow()
  entity.speed = input.speed.toDoubleOrThrow()
  ```

- After a long wrapped line — a blank line before the next statement.

  ```kotlin
  throw ConflictException.unified(
    "Trajectory " + trajectoryId + " has no persisted target position"
  )

  computeNextState(input)
  ```

## Single-expression functions

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

- **Public methods** — Every non-trivial public method must be preceded by a `// MARK:` comment. The comment describes the method's purpose or, for endpoint methods, the HTTP path.
  - **Trivial methods** (single expression, one-liner) — a separator line alone is sufficient:

    ```kotlin
    // -----------------------------------------------

    fun getUser(id: UUID): UserResponse =
      userRepository.findByIdOrThrow(id).let { mapper.toResponse(it) }
    ```

  - **Non-trivial methods** — require a full MARK comment:

    ```kotlin
    // MARK: calculateAndPersist
    // -----------------------------------------------

    @Transactional
    fun calculateAndPersist(
      traj: TrajectoryEntity,
      mog: MogEntity?,
      time: Instant,
    ): SimulationState {
      ...
    }
    ```

  - **Groups of related functions** — a single MARK comment may mark the start of a group instead of annotating each method individually. Use judgement based on context. Between methods within the group, use a separator line:

    ```kotlin
    // MARK: Interception calculations
    // -----------------------------------------------

    private fun calculateInterceptTime(...) { ... }

    // -----------------------------------------------

    private fun calculateInterceptPoint(...) { ... }

    // -----------------------------------------------

    private fun validateInterception(...) { ... }
    ```

- All private helpers must be grouped under a `// MARK: Private Helpers` section marker. Within this section:
  - Most private methods use a separator line (`// -----------------------------------------------`) between them.
  - Important private methods (complex algorithm, critical business logic) may get their own `// MARK:` comment.

  ```kotlin
  // MARK: Private Helpers
  // -----------------------------------------------

  private fun buildSituationResponse(...) { ... }

  // -----------------------------------------------

  private fun validateInput(...) { ... }

  // MARK: Complex interception algorithm
  // -----------------------------------------------

  private fun calculateInterceptionMatrix(...) { ... }
  ```

- **KDoc** — Every long or important function must have a multi-line KDoc placed immediately before the function, after the blank-line separator:

  ```kotlin
  // MARK: calculateAndPersist
  // -----------------------------------------------

  /**
   * Вычисляет и сохраняет состояние симуляции для заданной траектории
   * на указанный момент времени. Обновляет предсказанную позицию цели,
   * статус МОГ и флаги disintegration при необходимости.
   *
   * @param traj сущность траектории для расчёта
   * @param mog назначенная сущность МОГ или `null`, если не назначена
   * @param time момент времени, на который вычисляется состояние
   * @return результирующий [SimulationState]
   */
  @Transactional
  fun calculateAndPersist(
    traj: TrajectoryEntity,
    mog: MogEntity?,
    time: Instant,
  ): SimulationState {
    ...
  }
  ```

  The KDoc block is separated from the blank-line separator by one blank line and sits directly above the function with no blank line between KDoc and the function.

- **Language of comments:**
  - **KDoc** — **exclusively in Russian**. All `@param`, `@return`, and description text must be written in Russian.
  - **`// MARK:` comments** — **exclusively in English**.
  - **Regular `//` comments** — either English or Russian is acceptable, but **Russian is preferred**. Historic English comments may remain, but new or modified comments should use Russian.

- After `// MARK:` the text must start with a **capital letter** or be in **ALL CAPS**:
  - `// MARK: POST /api/situation/calculate`
  - `// MARK: Private Helpers`
  - `// MARK: Time-to-intercept calculation`
- Regular (non-MARK) comments may start with either a capital or lowercase letter.
- Format (remember about blank lines!):

  ```kotlin
  // MARK: POST /api/situation/calculate
  // -----------------------------------------------

  @Transactional
  fun calculate(time: Instant): SituationResponse { ... }

  // MARK: Private Helpers
  // -----------------------------------------------

  private fun buildSituationResponse(...) { ... }

  // MARK: Reuse existing MOG destination
  // -----------------------------------------------

  private fun reuseDestination(request: InterceptionRequest): InterceptionPlan?
  ```

- Constructor parameters must be grouped with category comments:

  ```kotlin
  class UserService(
    // repositories:
    private val userRepository: UserRepository,

    // services:
    private val listingService: ListingService,
  )
  ```

- Every data class property must have a short side comment explaining meaning and units where not obvious from naming. Self-explanatory fields (e.g. `targetId`, `maskId`) don't need a comment.

  ```kotlin
  data class MaskPoint(
    val maskId: UUID = ...,
    val dfHz: Double = 0.0,  // offset from carrier, Hz
    val aDb: Double = 0.0,   // relative attenuation, dB
  )
  ```

## Constants

- All shared strings, magic numbers, regex patterns, and validation messages live in `shared/Constants.kt`.
- Organize into nested objects: `Math`, `Entity`, `ErrorDescription`, `Validation`, `Pattern`.
- Reference constants via `Constants.<Object>.<FIELD>` — never inline magic values.

