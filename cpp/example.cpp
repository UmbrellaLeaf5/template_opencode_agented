// Formatting fixture: C++ examples copied from CODE-STYLE.md and AGENTS.md.
// The snippets are intentionally disabled because they are independent examples,
// not one translation unit or a buildable program.
#if 0

// CODE-STYLE.md: hanging indentation
void my_func(
  const std::string& arg1,
  int arg2) {
  // ...
}

// CODE-STYLE.md: conditional branches
if (model == "alpha")
  use_alpha();

else if (model == "beta")
  use_beta();

else
  throw std::invalid_argument("[Config] Unknown model");

// CODE-STYLE.md: range for and stateful while
for (const auto& item : items) { ... }

while (sim_time < sim_duration) {
  sim_time += dt;
}

// CODE-STYLE.md: C++ cast
const size_t idx = static_cast<size_t>(source_idx);

// CODE-STYLE.md: auto examples
auto it = vec.begin();
auto result = std::make_unique<MyType>(...);
int count = 5;
auto count = 5;
double value = compute();

// CODE-STYLE.md: const member function
double get_value() const { return _value; }

// CODE-STYLE.md: nullptr
int* ptr = nullptr;

// CODE-STYLE.md: override and final
void do_step(double dt) override;
class FinalClass final : public Base { ... };

// CODE-STYLE.md: smart pointers
std::unique_ptr<Processor> processor = std::make_unique<Processor>();

// CODE-STYLE.md: header guard convention
#pragma once

namespace myproject {

class Processor {};

}  // namespace myproject

// CODE-STYLE.md: no standalone semantic blocks
// Инициализация фильтра

Filter flt;
flt.init(path);

// CODE-STYLE.md: naming conventions
namespace core {

class SignalHandler {
 public:
  void do_step(double dt_s);

 private:
  double _fading_margin_db = 0.0;
};

}  // namespace core

constexpr double DEFAULT_HALF_ANGLE_DEG = 30.0;

// CODE-STYLE.md: blank line between logical groups
selected_item.status = ItemStatus::BUSY;
selected_item.updated_at = clock.now();
_repository.save(selected_item);

order.assigned_item_id = selected_item_id;
order.status = OrderStatus::PENDING;
order.is_locked = false;

// CODE-STYLE.md: related field assignments
order.start_lat = motion.start_lat;
order.start_lon = motion.start_lon;
order.start_height = motion.start_height;
order.direction_lat = motion.direction_lat;
order.direction_lon = motion.direction_lon;

// CODE-STYLE.md: peer-level function separators and MARK comments
Plan compute_full(const Request& request) {
  ...
}

// --------------------------------------------------

Plan compute_new_plan(
  const Request& request,
  double max_radius) {
  ...
}

// MARK: Time-to-interaction calculation
// --------------------------------------------------

double calculate_time(
  const State& item,
  const State& target,
  double max_radius) {
  ...
}

// CODE-STYLE.md: class member groups and method layout
class CalculationService {
  // repositories:
  ItemRepository& _item_repository;
  ResultRepository& _result_repository;

  // services:
  StateService& _state_service;
  PersistenceService& _persistence_service;

  // MARK: Calculate and persist
  // --------------------------------------------------

  void calculate_and_persist(
    const Entity& entity,
    const Item* item,
    double time_s) {
    ...
  }

};

// CODE-STYLE.md: one-line if and following blank line
if (path.empty()) return;

parser.init(path);

// CODE-STYLE.md: blocks separated from surrounding statements
const auto paths = parser.get_paths();

for (const auto& path : paths) {
  load_source(path);
  register_source(path);
}

handler.do_first_step();

const auto paths = parser.get_paths();

for (const auto& path : paths) load_source(path);

handler.do_first_step();

// CODE-STYLE.md: conditional branches with blocks
if (model == "alpha") {
  use_alpha();
  log_model(model);
}

else if (model == "beta") {
  use_beta();
  log_model(model);
}

else {
  report_unknown(model);
  throw std::invalid_argument("[Config] Unknown model: model=" + model);
}

// CODE-STYLE.md: try/catch layout
try {
  cfg = json_utils::load_file(path, SOURCE);
}

catch (const std::exception& e) {
  throw std::runtime_error(e.what());
}

// CODE-STYLE.md: blank line before return
const double loss_db = compute_loss(distance_km, freq_mhz);

return loss_db + fading_margin_db;

// CODE-STYLE.md: declaration and initialization as one group
Filter flt;
flt.init(path);

handler.receive(flt, group_id);

const double radius = input.radius;
const auto* status = input.status;

// CODE-STYLE.md: class field groups
// repositories:
ItemRepository& _item_repository;
ResultRepository& _result_repository;

// services:
StateService& _state_service;

// CODE-STYLE.md: field-by-field object population
Signal sig;

sig.id = id;
sig.center_freq_hz = center_freq_hz;
sig.tx_power_dbm = tx_power_dbm;

// CODE-STYLE.md: wrapped line followed by blank line
throw std::invalid_argument(
  "[Handler::init] Invalid config field: field=" + field_name + ", value=" + value);

handler.do_first_step();

// CODE-STYLE.md: trivial public method
// --------------------------------------------------

DataPoint get_point(const Id& id) const { return _repository.find_by_id(id); }

// CODE-STYLE.md: non-trivial public method
// MARK: Process step
// --------------------------------------------------

void process_step(
  const Entity& entity,
  const Item* item,
  double time_s) {
  ...
}

// CODE-STYLE.md: group of related functions
// MARK: Calculation helpers
// --------------------------------------------------

void calculate_time(...) { ... }

// --------------------------------------------------

void calculate_point(...) { ... }

// --------------------------------------------------

void validate_calculation(...) { ... }

// CODE-STYLE.md: private helper group
// MARK: Private Helpers
// --------------------------------------------------

void build_response(...) { ... }

// --------------------------------------------------

void validate_input(...) { ... }

// MARK: Complex calculation algorithm
// --------------------------------------------------

void calculate_matrix(...) { ... }

// CODE-STYLE.md: separator before method
// MARK: Private Helpers
// --------------------------------------------------

void build_response(...) { ... }

// CODE-STYLE.md: struct field comments
struct Config {
  double df_hz = 0.0;  // offset from center, Hz
  double a_db = 0.0;   // relative attenuation, dB
};

// CODE-STYLE.md: Doxygen, MARK, and method
// MARK: Process step
// --------------------------------------------------

/**
 * @brief Вычисляет и сохраняет состояние для заданной сущности.
 * @param entity сущность для расчёта
 * @param item связанный объект или `nullptr`, если не назначен
 * @param time_s момент времени в секундах
 */
void process_step(
  const Entity& entity,
  const Item* item,
  double time_s) {
  ...
}

// CODE-STYLE.md: console output
std::cout << "Loaded " << sources.size() << " source(s)" << std::endl;

// CODE-STYLE.md: .cpp section marker
// MARK: Инициализация среды обработки
// --------------------------------------------------

Handler handler;

// CODE-STYLE.md: constants and nested namespaces
namespace myproject::constants {

namespace math {
  constexpr double EARTH_RADIUS_M = 6'371'000.0;
  constexpr double DEG_TO_RAD = M_PI / 180.0;
}  // namespace math

// --------------------------------------------------

namespace entity {
  constexpr const char* USER = "User";
  constexpr const char* ORDER = "Order";
}  // namespace entity

// --------------------------------------------------

namespace config {
  constexpr const char* CENTER_FREQ_HZ = "/center_freq_hz";
}  // namespace config

// --------------------------------------------------

namespace validation {
  constexpr const char* INVALID_FREQ = "Invalid frequency value";
  constexpr const char* REQUIRED_FIELD = "Required field is missing";
}  // namespace validation

// --------------------------------------------------

namespace pattern {
  // regex patterns if needed
}  // namespace pattern

}  // namespace myproject::constants

// CODE-STYLE.md: exception format
if (type == ConfigType::UNKNOWN)
  throw std::invalid_argument("[Handler::init] Invalid type: type=" + type_str);

try {
  run_model();
}

catch (const std::exception& e) {
  std::cerr << "Error: " << e.what() << std::endl;

  return 1;
}

// AGENTS.md: required configuration field
const double center_freq_hz = json_utils::required<double>(cfg, "/center_freq_hz", SOURCE);

// AGENTS.md: conversion function and exception context
double hz_to_mhz(double freq_hz) {
  if (freq_hz <= 0.0)
    throw std::invalid_argument(
      "[math::hz_to_mhz] Invalid frequency: freq_hz=" + std::to_string(freq_hz));

  return freq_hz / 1.0e6;
}

// AGENTS.md: GoogleTest and Boost.Test
TEST(ComputeTest, ReturnsCorrectResult) {
  EXPECT_EQ(compute(2, 3), 5);
}

// --------------------------------------------------

BOOST_AUTO_TEST_CASE(compute_returns_correct_result) {
  BOOST_CHECK_EQUAL(compute(2, 3), 5);
}

#endif  // 0
