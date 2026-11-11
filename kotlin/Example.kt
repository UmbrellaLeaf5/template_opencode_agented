// Formatting fixture containing Kotlin examples from CODE-STYLE.md and AGENTS.md.
// Independent snippets are collected in one file; this is not a buildable module.

// CODE-STYLE.md: hanging indentation
fun myFunc(
  arg1: String,
  arg2: Int,
): ReturnType {
  // ...
}

// CODE-STYLE.md: argument validation
fun assignItem(request: AssignRequest) {
  val itemId = request.itemId.requireFieldNotNullByName { "itemId" }
  val userId = request.userId.requireFieldNotNullByName { "userId" }
}

// --------------------------------------------------

// CODE-STYLE.md: state validation
fun executeOperation() {
  val processor = this.processor.checkFieldNotNullByName { "processor" }
  val context = this.context.checkFieldNotNullByName { "context" }
}

// CODE-STYLE.md: one-line conditional branches
fun processStatus(status: OrderStatus) {
  // @formatter:off
  if (status == OrderStatus.PENDING)
    processOrder(order)

  else if (status == OrderStatus.CANCELLED)
    skipOrder(order)

  else
    throw ConflictException.unified("Order is not processable")
  // @formatter:on
}

// CODE-STYLE.md: loop body and nested control flow
fun processItems() {
  for (item in items) {
    if (item.status != ItemStatus.ACTIVE) continue

    processItem(item, context.lastOrNull(), allHandlers, processedIds)
    itemRepository.save(item)
  }
}

// CODE-STYLE.md: parsing and nullable conversion
fun parseValues(input: Input) {
  val radius =
    input.radius.toDoubleOrNull()
      ?: throw IllegalArgumentException("Invalid radius format")

  val count = input.count.toIntOrNull() ?: 0
}

// CODE-STYLE.md: safe cast and smart cast
fun processObject(obj: Any?) {
  val point = obj as? Point ?: return

  if (obj is Point)
    processPoint(obj) // smart cast applied automatically
}

// CODE-STYLE.md: certain cast
fun castKnownType(obj: Any): Point {
  val point = obj as Point // only when type is absolutely certain
  return point
}

// CODE-STYLE.md: scoping function chain
fun getUser(userIdString: String): UserResponse =
  userRepository
    .findByIdOrThrow(userIdString.toUUIDOrThrow(), Constants.Entity.USER)
    .also { logger.debug("Fetching User: {}", userIdString) }
    .let { userMapper.toResponse(it) }

// CODE-STYLE.md: discouraged scoping block and alternatives
fun initializeProcessor(config: Config) {
  run {
    val processor = DataProcessor()
    processor.init(config)
    processor.start()
  }

  val processor = DataProcessor()
  processor.init(config)
  processor.start()

  val configuredProcessor =
    DataProcessor().apply {
      init(config)
      start()
    }
}

// CODE-STYLE.md: blank lines between logical groups
fun updateEntities() {
  selectedItem.predictedStatus = ItemStatus.BUSY
  selectedItem.predictedUpdatedAt = clock.instant()
  itemRepository.saveAndFlush(selectedItem)

  order.assignedItemId = selectedItemId
  order.status = OrderStatus.PENDING
  order.isLocked = false
}

fun updatePosition() {
  order.startLat = motion.startLat
  order.startLon = motion.startLon
  order.startHeight = motion.startHeight
  order.directionLat = motion.directionLat
  order.directionLon = motion.directionLon
}

// CODE-STYLE.md: peer-level definitions and MARKs
fun computeFull(request: CalculationRequest): CalculationPlan? {
  TODO()
}

// --------------------------------------------------

private fun computeNewPlan(
  request: CalculationRequest,
  maxRadius: Double,
): CalculationPlan? {
  TODO()
}

// MARK: Time-to-interaction calculation
// --------------------------------------------------

private fun calculateInteractionTime(
  item: ItemState,
  target: TargetState,
  maxRadius: Double,
): Double? {
  TODO()
}

// CODE-STYLE.md: marker spacing
// MARK: Private Helpers
// --------------------------------------------------

private fun buildResponse(input: Any): Response = TODO()

// CODE-STYLE.md: class member spacing
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
    TODO()
  }

  fun placeholder() = Unit
}

// CODE-STYLE.md: one-line if and following blank line
fun handleItem(item: Item?) {
  if (item == null) return@let

  reuseContext(request, context)
}

// CODE-STYLE.md: blank lines around loops
fun collectCandidates(
  items: List<Item>,
  excludedIds: Set<UUID>,
): Item? {
  val activeItems = filterActive(items, excludedIds)

  for (item in activeItems) {
    val time = computeTime(item, target)
    candidates.add(item to time)
  }

  val best = candidates.minByOrNull { it.second }

  return best?.first
}

fun collectCandidatesShort(
  items: List<Item>,
  excludedIds: Set<UUID>,
): Item? {
  val activeItems = filterActive(items, excludedIds)

  for (item in activeItems) {
    candidates.add(item to computeTime(item, target))
  }

  val best = candidates.minByOrNull { it.second }

  return best?.first
}

// CODE-STYLE.md: branches
fun buildState(status: OrderStatus): State {
  if (status == OrderStatus.COMPLETED) {
    return buildTerminalState(input)
  }
  else if (status == OrderStatus.IMPOSSIBLE) {
    return buildTerminalState(input)
  }
  else {
    return buildActiveState(input)
  }
}

fun buildStateWithBlocks(status: OrderStatus): State {
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
}

// CODE-STYLE.md: try/catch spacing
fun loadProcessor() {
  try {
    processor.loadState()
  } catch (e: Exception) {
    log.warn("Failed to load state, starting fresh")
  }
}

// CODE-STYLE.md: blank line before return
fun withinRadius(
  positionA: Position,
  positionB: Position,
  maxRadius: Double,
): Boolean {
  val distance = computeDistance(positionA, positionB)

  return distance <= maxRadius
}

// CODE-STYLE.md: initialization, declarations, and wrapped line
fun processData() {
  val processor = DataProcessor()
  processor.init(config)

  handler.receive(processor, groupId)

  val radius = input.radius.toDoubleOrThrow()
  val status = input.status?.let { OrderStatus.valueOf(it) }

  throw ConflictException.unified(
    "Order " + orderId + " has no persisted state",
  )

  computeNextState(input)
}

// CODE-STYLE.md: expression-body functions
fun multiply(
  a: Int,
  b: Int,
): Int = a * b

fun showMessage(text: String) = println(text)

// Invalid style example: value-returning expression body without a return type.
fun multiplyWithoutReturnType(
  a: Int,
  b: Int,
) = a * b

// CODE-STYLE.md: trivial method
// --------------------------------------------------

fun getUser(id: UUID): UserResponse =
  userRepository.findByIdOrThrow(id).let { mapper.toResponse(it) }

// CODE-STYLE.md: non-trivial method
// MARK: Calculate and persist
// --------------------------------------------------

@Transactional
fun calculateAndPersist(
  entity: OrderEntity,
  item: ItemEntity?,
  time: Instant,
): CalculationState {
  TODO()
}

// CODE-STYLE.md: calculation helper group
// MARK: Calculation helpers
// --------------------------------------------------

private fun calculateTime(input: Any) = TODO()

// --------------------------------------------------

private fun calculatePoint(input: Any) = TODO()

// --------------------------------------------------

private fun validateCalculation(input: Any) = TODO()

// CODE-STYLE.md: private helper group and important helper
// MARK: Private Helpers
// --------------------------------------------------

private fun buildResponse(input: Any) = TODO()

// --------------------------------------------------

private fun validateInput(input: Any) = TODO()

// MARK: Complex calculation algorithm
// --------------------------------------------------

private fun calculateMatrix(input: Any) = TODO()

// CODE-STYLE.md: KDoc, marker, and method
// MARK: Calculate and persist
// --------------------------------------------------

/**
 * Вычисляет и сохраняет состояние для заданной сущности
 * на указанный момент времени.
 *
 * @param entity сущность для расчёта
 * @param item связанный объект или `null`, если не назначен
 * @param time момент времени, на который вычисляется состояние
 * @return результирующее [CalculationState]
 */
@Transactional
fun calculateAndPersist(
  entity: OrderEntity,
  item: ItemEntity?,
  time: Instant,
): CalculationState {
  TODO()
}

// CODE-STYLE.md: MARK formatting pattern
// MARK: POST /api/order/calculate
// --------------------------------------------------

@Transactional
fun calculate(time: Instant): OrderResponse = TODO()

// MARK: Private Helpers
// --------------------------------------------------

private fun buildResponse(input: Any): Response = TODO()

// MARK: Reuse existing context
// --------------------------------------------------

private fun reuseContext(request: CalculationRequest): CalculationPlan? = TODO()

// CODE-STYLE.md: constructor parameter groups
class UserService(
  // repositories:
  private val userRepository: UserRepository,
  // mappers:
  private val userMapper: UserMapper,
  // services:
  private val notificationService: NotificationService,
)

// CODE-STYLE.md: data class field comments
data class FilterConfig(
  val id: UUID = TODO(),
  val dfHz: Double = 0.0, // offset from center, Hz
  val aDb: Double = 0.0, // relative attenuation, dB
)

// CODE-STYLE.md: nested constants
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

// AGENTS.md: controller and string parameters
@GetMapping("/item/{item_id}")
override fun getItem(
  @RequestParam("user_id") userIdString: String,
  @PathVariable("item_id") itemIdString: String,
): ItemResponse = itemService.getItem(userIdString, itemIdString)

// AGENTS.md: early conversion to strongly typed values
fun getItem(
  userIdString: String,
  itemIdString: String,
): ItemResponse {
  val userId = userIdString.toUUIDOrThrow()
  val itemId = itemIdString.toUUIDOrThrow()

  val item = itemRepository.findByIdOrThrow(itemId, "Item")

  return itemMapper.toResponse(item)
}

// AGENTS.md: controller-facing orchestration service
@Service
class SituationService(
  private val situationRepository: SituationRepository,
  private val situationMapper: SituationMapper,
  private val situationPatchHandler: SituationPatchHandler,
) {

  @Transactional
  fun patchSituation(request: SituationPatchRequest): SituationResponse {
    val situation = situationRepository.findActiveOrThrow()
    val patched = situationPatchHandler.patch(situation, request)
    val saved = situationRepository.save(patched)

    return situationMapper.toResponse(saved)
  }
}

// AGENTS.md: constructor-injected mapper group
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

// AGENTS.md: simple mapper
@Component
class UserMapper {
  // MARK: Convert to response
  // --------------------------------------------------

  fun toResponse(user: User) =
    UserResponse(
      id = user.id.checkFieldNotNullByName { "id" },
      fullname = user.fullname,
      email = user.email,
      createdAt = user.createdAt,
      updatedAt = user.updatedAt,
    )

  // MARK: Convert to entity
  // --------------------------------------------------

  fun toEntity(request: UserCreateRequest) =
    User(
      fullname = request.validateAndGetFullname(),
      email = request.validateAndGetEmail(),
    )
}

// AGENTS.md: mapper with multiple response variants
@Component
class ProductMapper {
  // MARK: Convert to response
  // --------------------------------------------------

  fun toResponse(product: Product) =
    ProductResponse(
      id = product.id.checkFieldNotNullByName { "id" },
      name = product.name,
      type = product.type,
      status = product.status,
    )

  // MARK: Convert to summary response
  // --------------------------------------------------

  fun toSummaryResponse(product: Product) =
    ProductSummaryResponse(
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

// AGENTS.md: mapper accepting external data
@Component
class OrderMapper {
  // MARK: Convert to response
  // --------------------------------------------------

  fun toResponse(
    order: Order,
    productIds: List<UUID>, // data from a join table, not on the entity
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
    user: User, // FK entity, resolved by the service
    costs: OrderCosts, // computed value
  ) = Order(
    user = user,
    costs = costs,
    finalCost = costs.totalCost(),
  )
}

// AGENTS.md: mapper use in service
fun getUser(userIdString: String): UserResponse =
  userRepository
    .findByIdOrThrow(userIdString.toUUIDOrThrow(), Constants.Entity.USER)
    .let { userMapper.toResponse(it) }

// --------------------------------------------------

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
