"""Formatting fixture containing Python examples from CODE-STYLE.md and AGENTS.md."""

from pathlib import Path
from typing import Protocol, TypeVar
from uuid import UUID, uuid4

import logging
import os
import pytest
import typer
from dataclasses import dataclass
from pydantic import BaseModel


# CODE-STYLE.md: hanging indentation
def my_func(
  arg1: str,
  arg2: int,
) -> ReturnType: ...


# CODE-STYLE.md: type annotations and separator
def get_user(user_id: str) -> User: ...


# --------------------------------------------------


def process(items: list[Item]) -> None: ...


# CODE-STYLE.md: pathlib
def load_config(path: Path) -> Config: ...


# CODE-STYLE.md: dataclass
@dataclass
class Order:
  id: UUID
  user_id: UUID
  total: float
  status: str


# CODE-STYLE.md: f-strings and logging representations
msg = f"User {user_id} not found in group {group_name}"
logger.debug("Raw input: %r", raw_input)
logger.debug(f"Raw input: {raw_input!r}")


# CODE-STYLE.md: context managers
with path.open("r") as f:
  data = json.load(f)

with lock:
  shared_state.update(value)


# CODE-STYLE.md: loops and comprehensions
for item in items:
  if item.is_active:
    process(item)

active = [item for item in items if item.is_active]
pairs = [(a, b) for a in as_list for b in bs_list]
result = [x for sub in nested for x in sub.filter() if x.ok]
total = sum(item.cost for item in order.items)


# CODE-STYLE.md: pathlib operations
config_dir = Path.home() / ".config" / "myapp"
config_dir.mkdir(parents=True, exist_ok=True)


# CODE-STYLE.md: script entry point
#!/usr/bin/env python3
if __name__ == "__main__":
  main()


# CODE-STYLE.md: enum
from enum import Enum


class OrderStatus(Enum):
  PENDING = "pending"
  COMPLETED = "completed"
  CANCELLED = "cancelled"


# CODE-STYLE.md: frozen dataclass
@dataclass(frozen=True)
class Config:
  timeout_s: int
  retry_count: int


# CODE-STYLE.md: protocol
class Processor(Protocol):
  def process(self, data: str) -> str: ...


# CODE-STYLE.md: logging
logger = logging.getLogger(__name__)

logger.info("Processing item: %s", item_id)
logger.debug("Raw input: %r", raw_data)


# CODE-STYLE.md: import groups
import os
from pathlib import Path
from typing import TypeVar

import typer
from pydantic import BaseModel

from myproject.core import UserService
from myproject.config import settings


# CODE-STYLE.md: exports
__all__ = ["UserService", "OrderService", "User", "Order"]


# CODE-STYLE.md: top-level separator and MARK examples
def compute_full(request: CalculationRequest) -> CalculationPlan | None: ...


# --------------------------------------------------


def _compute_new_plan(
  request: CalculationRequest,
  max_radius: float,
) -> CalculationPlan | None: ...


# MARK: Time-to-interaction calculation
# --------------------------------------------------


def _calculate_interaction_time(
  item: ItemState,
  target: TargetState,
  max_radius: float,
) -> float | None: ...


# CODE-STYLE.md: class method separators
class Calculator:
  def calculate_time(self, *args: object) -> float: ...

  # --------------------------------------------------

  def calculate_point(self, *args: object) -> Point: ...

  # --------------------------------------------------

  def validate_calculation(self, *args: object) -> None: ...


# CODE-STYLE.md: helper group
# MARK: Private Helpers
# --------------------------------------------------


def _build_response(*args: object) -> Response: ...


def _build_response_member(*args: object) -> Response: ...


# --------------------------------------------------


def _validate_input(*args: object) -> None: ...


# MARK: Complex calculation algorithm
# --------------------------------------------------


def _calculate_matrix(*args: object) -> Matrix: ...


# CODE-STYLE.md: explicit constructor and private fields
class CalculationService:
  def __init__(
    self,
    # repositories:
    item_repository: ItemRepository,
    result_repository: ResultRepository,
    # services:
    state_service: StateService,
    persistence_service: PersistenceService,
  ) -> None:
    self._item_repository = item_repository
    self._result_repository = result_repository
    self._state_service = state_service
    self._persistence_service = persistence_service

  # MARK: Calculate and persist
  # --------------------------------------------------

  def calculate_and_persist(
    self,
    entity: OrderEntity,
    item: ItemEntity | None,
    time: datetime,
  ) -> CalculationState: ...


# CODE-STYLE.md: blank line around control flow and return
def find_distance(a: Point, b: Point, max_radius: float) -> bool:
  result = compute()

  if result is None:
    return

  for item in items:
    process(item)

  distance = compute_distance(a, b)

  return distance <= max_radius


for item in items:
  if item.is_active:
    process(item)


# CODE-STYLE.md: docstring spacing
def my_func(arg: str) -> int:
  """Process the argument."""

  value = int(arg)

  return value


# CODE-STYLE.md: related initialization and declarations
processor = DataProcessor()
processor.init(config)

handler.receive(processor)

radius = input.radius
status = OrderStatus.of(input.status) if input.status else None


# CODE-STYLE.md: field-by-field initialization
entity = OrderEntity()

entity.start_lat = input.lat
entity.start_lon = input.lon
entity.speed = input.speed


# CODE-STYLE.md: conditional branch layout
if status == OrderStatus.COMPLETED:
  log.info("Order %s completed", order_id)
  return build_terminal_state(input)

elif status == OrderStatus.IMPOSSIBLE:
  log.warn("Order %s impossible", order_id)
  return build_terminal_state(input)

else:
  log.debug("Order %s active", order_id)
  return build_active_state(input)


try:
  processor.load_state()

except Exception:
  logger.warn("Failed to load state, starting fresh")


raise ConflictError("Order " + order_id + " has no persisted state")

compute_next_state(input)


# CODE-STYLE.md: trivial and non-trivial public functions
# --------------------------------------------------


def get_user(user_id: str) -> User:
  return _repository.find_by_id(user_id)


# MARK: Calculate and persist
# --------------------------------------------------


def calculate_and_persist(
  entity: OrderEntity,
  item: ItemEntity | None,
  time: datetime,
) -> CalculationState:
  """
  Вычисляет и сохраняет состояние для заданной сущности.

  Args:
    entity (OrderEntity): сущность для расчёта
    item (ItemEntity | None): связанный объект или None, если не назначен
    time (datetime): момент времени, на который вычисляется состояние

  Returns:
    CalculationState: результирующее состояние
  """
  ...


# MARK: POST /api/order/calculate
# --------------------------------------------------


def calculate(order_id: str) -> OrderResponse: ...


# MARK: Calculation helpers
# --------------------------------------------------


def calculate_time(*args: object) -> float: ...


# --------------------------------------------------


def calculate_point(*args: object) -> Point: ...


# --------------------------------------------------


def validate_calculation(*args: object) -> None: ...


# CODE-STYLE.md: private helper methods and section layout
# MARK: Private Helpers
# --------------------------------------------------


def _build_response(*args: object) -> Response: ...


# --------------------------------------------------


def _validate_input(*args: object) -> None: ...


# MARK: Complex calculation algorithm
# --------------------------------------------------


def _calculate_matrix(*args: object) -> Matrix: ...


# CODE-STYLE.md: data class field comments
@dataclass
class FilterConfig:
  id: UUID
  df_hz: float = 0.0  # offset from center, Hz
  a_db: float = 0.0  # relative attenuation, dB


# CODE-STYLE.md: grouped constructor parameters
class UserService:
  def __init__(
    self,
    # repositories:
    user_repository: UserRepository,
    # mappers:
    user_mapper: UserMapper,
    # services:
    notification_service: NotificationService,
  ) -> None: ...


# CODE-STYLE.md: constants
class Math:
  EARTH_RADIUS_M: float = 6_371_000.0
  DEG_TO_RAD: float = pi / 180.0


# --------------------------------------------------


class Entity:
  USER = "User"
  ORDER = "Order"
  PRODUCT = "Product"


# --------------------------------------------------


class Validation:
  INVALID_EMAIL = "Invalid email format"
  REQUIRED_FIELD = "Required field is missing"


# --------------------------------------------------


class Pattern:
  UUID_RE = r"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"


earth_r = Math.EARTH_RADIUS_M
entity_user = Entity.USER


# CODE-STYLE.md: pytest fixture, parametrized and skipped tests
@pytest.fixture
def sample_user() -> User:
  return User(id=uuid4(), fullname="Test User", email="test@example.com")


# --------------------------------------------------


@pytest.mark.parametrize(
  "name,valid",
  [
    ("Alice", True),
    ("", False),
  ],
)
def test_validate_name(name: str, valid: bool) -> None:
  assert User.is_valid_name(name) == valid


# --------------------------------------------------


@pytest.mark.skipif(not os.getenv("CI"), reason="Requires external service")
def test_external_api() -> None: ...


# AGENTS.md: CLI entry point
app = typer.Typer()


# --------------------------------------------------


@app.command()
def process(file: str):
  """Process a file."""
  result = service.process(file)
  print(result)


# AGENTS.md: custom exception hierarchy
class BaseError(Exception):
  """Base exception for application errors."""


# --------------------------------------------------


class NotFoundError(BaseError):
  """Resource not found."""


# --------------------------------------------------


class ConflictError(BaseError):
  """Resource conflict."""


# AGENTS.md: configuration models
class DatabaseConfig(BaseModel):
  url: str
  pool_size: int = 10


# --------------------------------------------------


class AppConfig(BaseModel):
  db: DatabaseConfig
  log_level: str = "INFO"
