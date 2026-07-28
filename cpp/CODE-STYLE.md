# CODE-STYLE.md

All code-writing rules for C++ projects.

---

## Indentation & Layout

- **2-space indentation** everywhere (C++, CMake, JSON, TOML, Markdown, all config files).
- **Line length**: 100 characters.
- **Hanging indentation** for long signatures and calls:

  ```cpp
  void my_func(
    const std::string& arg1,
    int arg2) {
    // ...
  }
  ```

- Formatting is governed by `.clang-format`. Run `clang-format -i` on changed files before committing. **Never apply `clang-format` to CMake files** (`CMakeLists.txt`, `*.cmake`) — it destroys CMake syntax.

## Language Usage

- One-line `if` and `for` statements are allowed and encouraged when they improve readability.

  ```cpp
  if (model == "alpha")
    use_alpha();

  else if (model == "beta")
    use_beta();

  else
    throw std::invalid_argument("[Config] Unknown model");
  ```

- Prefer `for` for iteration over ranges and collections. Use `while` only when iteration depends on mutable state or complex conditions:

  ```cpp
  // Prefer:
  for (const auto& item : items) { ... }

  // Acceptable:
  while (sim_time < sim_duration) {
    sim_time += dt;
  }
  ```

- C-style casts are **forbidden**; use C++ casts (`static_cast`, `dynamic_cast`, `const_cast`, `reinterpret_cast`).

  ```cpp
  const size_t idx = static_cast<size_t>(source_idx);
  ```

### auto

- Use `auto` for:
  - Iterator declarations: `auto it = vec.begin()`
  - Complex types: `auto result = std::make_unique<MyType>(...)`
  - Lambda captures and returns
- Avoid `auto` for fundamental types where the type is not obvious from context:
  - Prefer `int count = 5;` over `auto count = 5;`
  - Prefer `double value = compute();` when the return type is not clear from the function name

### const and constexpr

- Use `const` for variables that are never modified after initialization.
- Use `constexpr` for compile-time constants.
- Member functions that do not modify the object state must be marked `const`:

  ```cpp
  double get_value() const { return _value; }
  ```

### nullptr

- Always use `nullptr` for null pointers. Never use `NULL` or `0`:

  ```cpp
  int* ptr = nullptr;
  ```

### override and final

- Use `override` for every virtual function that overrides a base class method:

  ```cpp
  void do_step(double dt) override;
  ```

- Use `final` for classes that should not be derived from:

  ```cpp
  class FinalClass final : public Base { ... };
  ```

### Smart Pointers

- Prefer `std::unique_ptr` for exclusive ownership.
- Use `std::shared_ptr` only when shared ownership is truly needed.
- Use `std::weak_ptr` to break cycles in shared ownership.

  ```cpp
  std::unique_ptr<Processor> processor = std::make_unique<Processor>();
  ```

### Header Guards

- **All headers use `#pragma once`** — never use `#ifndef`/`#define`/`#endif` guards.

  ```cpp
  #pragma once

  namespace myproject {

  class Processor {};

  }  // namespace myproject
  ```

### Standalone Blocks

- Standalone semantic blocks without `if`, `for`, function, class/struct, namespace, or initializer context are forbidden. Do not use bare `{ ... }` only to group related statements; separate such code with section comments instead.

  ```cpp
  // Инициализация фильтра

  Filter flt;
  flt.init(path);
  ```

## Naming Conventions

- **Classes, structs, enums** → `PascalCase` (e.g. `SignalHandler`, `DynState`, `ConfigPoint`)
- **Methods, functions, variables, fields, parameters** → `snake_case` (e.g. `do_step()`, `make_signal()`, `_gain_max_dbi`)
- **Private fields and methods** → `_` prefix + `snake_case` (e.g. `_gain_max_dbi`, `_fading_margin_db`)
- **Constants** (`const`/`constexpr` variables, including local constants) → `SCREAMING_SNAKE_CASE` (e.g. `DEFAULT_HALF_ANGLE_DEG`, `GROUP_ID_1`)
- **Namespaces** → lowercase (e.g. `core`, `math`, `io`)
- **JSON keys** → `snake_case` (e.g. `"center_freq_hz"`, `"gain_max_dbi"`)

Examples:

```cpp
namespace core {

class SignalHandler {
public:
  void do_step(double dt_s);

private:
  double _fading_margin_db = 0.0;
};

}  // namespace core
```

```cpp
constexpr double DEFAULT_HALF_ANGLE_DEG = 30.0;
```

```json
{
  "center_freq_hz": 868000000.0,
  "gain_max_dbi": 3.0
}
```

- Prefer specific, descriptive names. Avoid ambiguous abbreviations:
  - `resolved_config_path` rather than `path` when multiple paths exist.
  - `_gain_max_dbi` not `_gain`.

## File Structure

- **Each namespace — its own directory** (e.g. `core/`, `math/`).
- **Each type — its own file** (Java-style). Within a namespace: shared across classes → `shared/`, used by a single class → `components/`.
- **All header files (except legacy or third-party) → `.hpp`**.
- **Files containing exactly one class/struct/enum → named PascalCase matching the type** (e.g. `Antenna.hpp`, `Result.hpp`). Files with multiple utility functions → `snake_case` (e.g. `functions.hpp`, `utils.hpp`).

```text
include/myproject/core/Antenna.hpp
include/myproject/core/components/Config.hpp
include/myproject/core/shared/Signal.hpp
```

## Blank Lines

- Use blank lines to separate logical sections of code. Avoid excessive blank lines that create visual noise.

- **Important:** Blank line rules may overlap. When multiple rules require a blank line at the same position, use exactly **one blank line**. Never use two or more consecutive blank lines.

  Blank line **needed** — when switching between different variables / logical groups:

  ```cpp
  selected_item.status = ItemStatus::BUSY;
  selected_item.updated_at = clock.now();
  _repository.save(selected_item);

  order.assigned_item_id = selected_item_id;
  order.status = OrderStatus::PENDING;
  order.is_locked = false;
  ```

  Blank line **not needed** — sequential calls on the same object or related group:

  ```cpp
  order.start_lat = motion.start_lat;
  order.start_lon = motion.start_lon;
  order.start_height = motion.start_height;
  order.direction_lat = motion.direction_lat;
  order.direction_lon = motion.direction_lon;
  ```

- Between peer-level definitions (top-level functions, member methods), use one of the following:
  - A separator line (`// --------------------------------------------------`) alone — for trivial methods or private helpers
  - A MARK comment with a separator line — for important methods
  - A MARK comment for a group, with separator lines between methods within the group

  No naked blank lines between peer-level definitions — always use a separator or MARK.

  ```cpp
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
  ```

- **1 blank line** after class header between members declaration and the first method.

  ```cpp
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
  ```

  **Note:** The blank line after the last member is the required 1 blank line. The MARK comment follows immediately after it. When blank line rules overlap, use only one blank line.

- A blank line is required after one-line `if` and `for` statements too, except when the next line closes the block.

  ```cpp
  if (path.empty()) return;

  parser.init(path);
  ```

- Before `if`/`for` and after their closing `}` — a blank line (separates the block from surrounding code).

  ```cpp
  const auto paths = parser.get_paths();

  for (const auto& path : paths) {
    load_source(path);
    register_source(path);
  }

  handler.do_first_step();
  ```

  ```cpp
  const auto paths = parser.get_paths();

  for (const auto& path : paths)
    load_source(path);

  handler.do_first_step();
  ```

- Between adjacent conditional branches (`if`/`else if`/`else`) — a blank line before each `else if` or `else` branch.

  ```cpp
  if (model == "alpha")
    use_alpha();

  else if (model == "beta")
    use_beta();

  else
    throw std::invalid_argument("[Config] Unknown model");
  ```

  ```cpp
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
  ```

- Before `catch` — a blank line after the preceding `try` block closing brace.

  ```cpp
  try {
    cfg = json_utils::load_file(path, SOURCE);
  }

  catch (const std::exception& e) {
    throw std::runtime_error(e.what());
  }
  ```

- Before `return` — a blank line, except when `return` is in a one-line `if`/`for`, or when it is the only statement in a block (`function`, `try`, `catch`, etc.).

  ```cpp
  const double loss_db = compute_loss(distance_km, freq_mhz);

  return loss_db + fading_margin_db;
  ```

- Inside functions, a blank line is required both **before and after** variable declarations, unless they belong to the same logical group.

  Between `Type var;` and `var.init(...)` on the next line — **no** blank line because they are one logical group:

  ```cpp
  Filter flt;
  flt.init(path);

  handler.receive(flt, group_id);
  ```

  Two consecutive variable declarations in a row are also valid without a blank line between them:

  ```cpp
  const double radius = input.radius;
  const auto* status = input.status;
  ```

- Within a class (member fields), blank lines between consecutive fields are **not** needed. A blank line is required only when introducing a new logical group preceded by a comment.

  ```cpp
  // repositories:
  ItemRepository& _item_repository;
  ResultRepository& _result_repository;

  // services:
  StateService& _state_service;
  ```

- When initializing a struct/object field by field (`sig.id = ...; sig.freq = ...;`) — a blank line between declaration and the first assignment (separates declaration from multi-field population). The criterion is that the variable is subsequently used in several independent lines that form a logical block. This differs from the init case where `Filter flt; flt.init(path)` is one inseparable group — there, no blank line is needed because the lines are not separable by meaning.

  ```cpp
  Signal sig;

  sig.id = id;
  sig.center_freq_hz = center_freq_hz;
  sig.tx_power_dbm = tx_power_dbm;
  ```

- After a long wrapped line — a blank line before the next statement.

  ```cpp
  throw std::invalid_argument("[Handler::init] Invalid config field: field=" +
                              field_name + ", value=" + value);

  handler.do_first_step();
  ```

## Comments

### Section separators & MARK comments

- Use section separators and MARK comments to organise code into logical groups. This is the same system used in Kotlin: every non-trivial function is preceded by a MARK comment, and all functions are separated by section separator lines.

- **Separator line: exactly 50 dashes** (`// --------------------------------------------------`). Never use shorter or longer separators.

- **Public methods** — Every non-trivial public method must be preceded by a `// MARK:` comment. The comment describes the method's purpose.
  - **Trivial methods** (single expression, one-liner) — a separator line alone is sufficient:

    ```cpp
    // --------------------------------------------------

    DataPoint get_point(const Id& id) const {
      return _repository.find_by_id(id);
    }
    ```

  - **Non-trivial methods** — require a full MARK comment:

    ```cpp
    // MARK: Process step
    // --------------------------------------------------

    void process_step(
      const Entity& entity,
      const Item* item,
      double time_s) {
      ...
    }
    ```

  - **Groups of related functions** — a single MARK comment may mark the start of a group instead of annotating each method individually. Use judgement based on context. Between methods within the group, use a separator line:

    ```cpp
    // MARK: Calculation helpers
    // --------------------------------------------------

    void calculate_time(...) { ... }

    // --------------------------------------------------

    void calculate_point(...) { ... }

    // --------------------------------------------------

    void validate_calculation(...) { ... }
    ```

- All private helpers must be grouped under a `// MARK: Private Helpers` section marker. Within this section:
  - Most private methods use a separator line (`// --------------------------------------------------`) between them.
  - Important private methods (complex algorithm, critical business logic) may get their own `// MARK:` comment.

  ```cpp
  // MARK: Private Helpers
  // --------------------------------------------------

  void build_response(...) { ... }

  // --------------------------------------------------

  void validate_input(...) { ... }

  // MARK: Complex calculation algorithm
  // --------------------------------------------------

  void calculate_matrix(...) { ... }
  ```

- After a closing `// --------------------------------------------------` or `// MARK:` section header — a blank line before the next statement.

  ```cpp
  // MARK: Private Helpers
  // --------------------------------------------------

  void build_response(...) { ... }
  ```

- After `// MARK:` the text must start with a **capital letter** or be in **ALL CAPS**:
  - `// MARK: Process step`
  - `// MARK: Private Helpers`
  - `// MARK: Time-to-interaction calculation`
- `// MARK:` text must be a readable English phrase, not a function or method identifier. Do not use camelCase or PascalCase names such as `processStep` or `GetReceivedPowerDbm` in MARK comments.

- **Every struct field** must have a short side comment explaining meaning and units where not obvious from naming. Self-explanatory fields (e.g. `id`, `name`) don't need a comment.

  ```cpp
  struct Config {
    double df_hz = 0.0;  // offset from center, Hz
    double a_db = 0.0;   // relative attenuation, dB
  };
  ```

### Doxygen

- **Every function, class, struct and enum in `.hpp` — Doxygen comments** (`/** @brief ... */` or `/** @brief ... @param ... @return ... */`)
- **Doxygen comments are always multiline** — single-line `/** @brief ... */` is not allowed.
- **For functions:** the comment answers "what does it do?".
- **For classes/structs:** the comment answers "what is it?".
- **@details** — added to complex functions and classes to describe the algorithm, formulas, or implementation specifics.
- The Doxygen block is separated from the MARK/separator by one blank line and sits directly above the function with no blank line between KDoc and the function.

  ```cpp
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
  ```

### Language of comments

- **Doxygen** — **exclusively in Russian**. All `@brief`, `@param`, `@return`, and description text must be written in Russian.
- **`// MARK:` comments** — **exclusively in English**.
- **Regular `//` comments** — either English or Russian is acceptable, but **Russian is preferred**. Historic English comments may remain, but new or modified comments should use Russian.
- **Console output** — in English (ASCII only).

  ```cpp
  std::cout << "Loaded " << sources.size() << " source(s)" << std::endl;
  ```

- Section separators in `.cpp` — use `// MARK:` comments and 50-dash separator lines (same as the general separator rules above):

  ```cpp
  // MARK: Инициализация среды обработки
  // --------------------------------------------------

  Handler handler;
  ```

## Constants

- **No magic values anywhere in code.** This applies to **all** literal types: string literals (`"user"`, `"/config/path"`), numeric constants (`3.14`, `86400`, `4096`), and any other hardcoded value that carries domain meaning. Every shared value must be defined in exactly one central location.

- **The only permissible inline literals** are:
  - Unit conversion factors (`1e6`, `1e-3`, `1000`) — values that only translate between measurement units, never domain logic
  - Precision/sentinel constants (`1e-9`, `-1`) — values that define computational precision or "no value" markers
  - Trivial initialisers (`0`, `1`, `""`) — when semantically obvious and not carrying domain meaning

- All shared constants (magic numbers, default values, configuration keys, string identifiers, regex patterns, validation messages) live in a dedicated namespace in their own header (e.g., `constants.hpp`).

- Organize into nested namespaces: `math`, `entity`, `config`, `validation`, `pattern`.

- Reference constants by their fully qualified name — never inline magic values.

```cpp
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
```

## Exceptions (Style)

- Exception text format: `[Source] Message: key=value, key=value`. `Source` is `Class::method` or `namespace::function`.
- Always include the failing context: `path=...` for files, `field=...` for JSON fields, `type=...` for enums.
- Use standard C++ exceptions: `std::invalid_argument` for invalid input/enums, `std::runtime_error` for file I/O and environment failures, `std::logic_error` for invalid internal state or wrong API call order.

Examples:

```cpp
if (type == ConfigType::UNKNOWN)
  throw std::invalid_argument("[Handler::init] Invalid type: type=" + type_str);
```

```cpp
try {
  run_model();
}

catch (const std::exception& e) {
  std::cerr << "Error: " << e.what() << std::endl;

  return 1;
}
```
