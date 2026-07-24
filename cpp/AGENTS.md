# RFSimLib-Tests - AGENTS.md

> **CRITICAL RULE:** Never execute `git commit` or `git push` without an explicit user command. This applies to ALL THREE repositories: RFSim (root), RFSimLib, RFSimLib-Tests. Even if the build passes, all tests are green, and changes are trivial — the user decides when to commit. No exceptions.

> Quick reference for AI agents. Full docs in [README.md](README.md).

> Python visualization rules: see [scripts/python/AGENTS.md](scripts/python/AGENTS.md).

---

## Quick Start

```bash
# Linux (symlink)
git clone https://gitlab.inst.falt.ru/uav_platform/imitation_models/electromagnetics/RFSimLib ../RFSimLib
git clone https://gitlab.inst.falt.ru/uav_platform/imitation_models/electromagnetics/RFSimLib-Tests . && bash scripts/bash/symlink/symlink.sh

# Windows (symlink)
git clone https://gitlab.inst.falt.ru/uav_platform/imitation_models/electromagnetics/RFSimLib ../RFSimLib
git clone https://gitlab.inst.falt.ru/uav_platform/imitation_models/electromagnetics/RFSimLib-Tests .
scripts\\bash\\symlink\\symlink.bat
```

## Required Cycle After Any `.cpp/.h/.hpp` Change

```bash
# 1. Formatting
clang-format -i $(git diff --name-only --diff-filter=AM HEAD | grep -E '\.(cpp|h|hpp)$')

# 1a. NEVER apply clang-format to CMake files (`CMakeLists.txt`, `*.cmake`)
#     - it destroys CMake syntax.

# 2. Build (0 errors, 0 warnings)
mkdir -p build && cd build && cmake .. -DCMAKE_BUILD_TYPE=Release && make -j$(nproc)

# 3. Scenario tests (no NaN, no crashes)
cd .. && bash scripts/bash/run/run_all.sh

# 4. Regression tests (separate CMake project)
cd ../tests && mkdir -p build && cd build && cmake .. -DCMAKE_BUILD_TYPE=Release && make -j$(nproc) && ./RFSimRegressionTestsExe
```

Regression tests are a standalone CMake project in `tests`. NEVER include them from the root `CMakeLists.txt`, `RFSimLib/CMakeLists.txt`, or any other primary CMake project.

### Building with GCC via MSYS2 (Windows)

In an MSYS2 terminal (MINGW64), `PATH` is already set:

```bash
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build -j
```

If cmake is invoked outside an MSYS2 terminal, export `PATH` manually:

```bash
export PATH="/<msys2_root>/mingw64/bin:/<msys2_root>/usr/bin:$PATH"
```

where `<msys2_root>` is the MSYS2 installation root.

## Code-style

### Naming

- **Classes, structs, enums** -> `PascalCase` (e.g. `P2PSignalHandler`, `AntennaDynState`, `MaskPoint`)
- **Methods, functions, variables, fields, parameters** -> `snake_case` (e.g. `do_step()`, `make_object_rf_sig()`, `_gain_max_dbi`)
- **Private fields and methods** -> `_` prefix + `snake_case` (e.g. `_gain_max_dbi`, `_fading_margin_db`)
- **Constants** (`const`/`constexpr` variables, including local constants) -> `SCREAMING_SNAKE_CASE` (e.g. `DEFAULT_MAIN_LOBE_HALF_ANGLE_DEG`, `GROUP_ID_1`)
- **Namespaces** -> lowercase (e.g. `rfsim`, `math`)
- **JSON keys** -> `snake_case` (e.g. `"center_freq_hz"`, `"gain_max_dbi"`)

Examples:

```cpp
namespace rfsim {

class P2PSignalHandler {
public:
  void do_step(double dt_s);

private:
  double _fading_margin_db = 0.0;
};

}  // namespace rfsim
```

```cpp
constexpr double DEFAULT_MAIN_LOBE_HALF_ANGLE_DEG = 30.0;
```

```json
{
  "center_freq_hz": 868000000.0,
  "gain_max_dbi": 3.0
}
```

### File Structure

- **Each namespace - its own directory** (e.g. `rfsim/`, `math/`)
- **Each type - its own file** (Java-style). Within a namespace: shared across classes -> `shared/`, used by a single class -> `components/`
- **All header files (except `lib/`) - `.hpp`**
- **All headers use `#pragma once`**
- **Files containing exactly one class/struct/enum - named PascalCase matching the type** (e.g. `Antenna.hpp`, `PropagationResult.hpp`). Files with multiple functions (e.g. `functions.hpp`) - snake_case.
- **Standalone Markdown documentation pages** -> `SCREAMING_SNAKE_CASE` names (e.g. `JSON.md`, `PROPAGATION.md`, `LOGS.md`). Keep conventional repository files such as `README.md` and existing normative document names unchanged unless explicitly requested.
- **RFSimLib Doxygen Markdown pages** -> only files in `RFSimLib/docs/info` are included in Doxygen. Keep runner-only documentation, including `scenario.json`, in `RFSimLib-Tests`.

Examples:

```text
RFSimLib/include/RFSimLib/rfsim/Antenna.hpp
RFSimLib/include/RFSimLib/rfsim/components/MaskPoint.hpp
RFSimLib/include/RFSimLib/rfsim/shared/RFSignal.hpp
RFSimLib/docs/info/JSON.md
RFSimLib-Tests/README.md
```

```cpp
#pragma once

namespace rfsim {

class Antenna {};

}  // namespace rfsim
```

### Comments and Doxygen

- **Always in Russian.**
- **Console output - in English** (ASCII only).
- **Section separators** in `.cpp` -> `// Text` (with `// ------` framing where needed).
- **Every function, class, struct and enum in `.hpp` - Doxygen comments** (`/** @brief ... */` or `/** @brief ... @param ... @return ... */`)
- **Doxygen comments are always multiline** - single-line `/** @brief ... */` is not allowed.
- **For functions:** the comment answers "what does it do?".
- **For classes/structs:** the comment answers "what is it?".
- **@details** - added to complex functions and classes to describe the algorithm, formulas, or implementation specifics.

Examples:

```cpp
// -------------------------------------------------------------------------
// Инициализация среды распространения
// -------------------------------------------------------------------------

/**
 * @brief Результат расчёта распространения радиосигнала.
 */
struct PropagationResult {
  double distance_km = 0.0;  // расстояние между объектами, км
};

/**
 * @brief Возвращает последнюю рассчитанную мощность приёма.
 * @param src_group Идентификатор группы источника.
 * @param target_group Идентификатор группы приёмника.
 * @return Мощность приёма, дБм.
 */
double get_received_power_dbm(int32_t src_group, int32_t target_group) const;
```

```cpp
std::cout << "Loaded " << jammers.size() << " jammer(s)" << std::endl;
```

> `components/` - for types used by only one class within the namespace (e.g. `MaskPoint` is only used by `Jammer`). `shared/` - for types shared by multiple classes within the namespace (e.g. `RFSignal` is used by `Antenna`, `Jammer`, and `P2PSignalHandler`).

### Blank Lines

- One-line `if` and `for` statements are allowed and encouraged when they improve readability.

```cpp
if (model == "p525")
  use_p525();

else if (model == "p528")
  use_p528();

else
  throw std::invalid_argument("[Config] Unknown model");
```

- A blank line is required after one-line `if` and `for` statements too, except when the next line closes the block.

```cpp
if (path.empty()) return;

parser.init(path);
```

- Use `for` for all loops. `while` loops are forbidden; use `for (; condition;)` when there is no init/update expression.

```cpp
for (; sim_time < sim_duration;)
  sim_time += dt;
```

- C-style casts are forbidden; use C++ casts such as `static_cast`.

```cpp
const size_t idx = static_cast<size_t>(jammer_idx);
```

- Standalone semantic blocks without `if`, `for`, function, class/struct, namespace, or initializer context are forbidden. Do not use bare `{ ... }` only to group related statements; separate such code with section comments instead.

```cpp
// Инициализация фильтра

rfsim::NotchFilter flt;
flt.init(path);
```

- Before `if`/`for` and after their closing `}` - a blank line (separates the block from surrounding code).

```cpp
const auto paths = parser.jammer_paths();

for (const auto& path : paths) {
  load_jammer(path);
  register_jammer(path);
}

p2p.do_first_step();
```

```cpp
const auto paths = parser.jammer_paths();

for (const auto& path : paths)
  load_jammer(path);

p2p.do_first_step();
```

- Between adjacent C++ conditional branches (`if`/`else if`/`else`) - a blank line before each `else if` or `else` branch.

```cpp
if (model == "p525")
  use_p525();

else if (model == "p528")
  use_p528();

else
  throw std::invalid_argument("[Config] Unknown model");
```

```cpp
if (model == "p525") {
  use_p525();
  log_model(model);
}

else if (model == "p528") {
  use_p528();
  log_model(model);
}

else {
  report_unknown_model(model);
  throw std::invalid_argument("[Config] Unknown model: model=" + model);
}
```

- Before `catch` - a blank line after the preceding `try` block closing brace.

```cpp
try {
  cfg = rfsim::json_utils::load_file(path, SOURCE);
}

catch (const std::exception& e) {
  throw std::runtime_error(e.what());
}
```

- Before `return` - a blank line, except when `return` is in a one-line `if`/`for`, or when it is the only statement in a block (`function`, `try`, `catch`, etc.).

```cpp
const double loss_db = compute_loss(distance_km, freq_mhz);

return loss_db + fading_margin_db;
```

- After variable declarations - a blank line **if** the variable is not used immediately. Between `Type var;` and `var.init(...)` on the next line - **no** blank line because they are one logical group. If the initialized variable is then used by the following command, put the blank line after the init command, for example:

```cpp
rfsim::NotchFilter flt;
flt.init(path);

p2p.receive(flt, GROUP_IDS.antenna_1);
```

- When initializing a struct/object field by field (`sig.id = ...; sig.freq = ...;`) - a blank line between declaration and the first assignment (separates declaration from multi-field population).

```cpp
rfsim::RFSignal sig;

sig.id = id;
sig.center_freq_hz = center_freq_hz;
sig.tx_power_dbm = tx_power_dbm;
```

- Every struct field must have a short side comment explaining meaning and units, for example `double df_hz = 0.0;  // отстройка от несущей, Гц`.

```cpp
struct MaskPoint {
  double df_hz = 0.0;  // отстройка от несущей, Гц
  double a_db = 0.0;   // относительное ослабление, дБ
};
```

- After a long wrapped line - a blank line before the next statement.

```cpp
throw std::invalid_argument("[P2PSignalHandler::init] Invalid JSON field: field=" +
                            field_name + ", value=" + value);

p2p.do_first_step();
```

- After a closing `// ---` section header - a blank line before the next statement.

```cpp
// -------------------------------------------------------------------------
// Инициализация среды распространения
// -------------------------------------------------------------------------

rfsim::P2PSignalHandler p2p;
```

### Default Values

- **Physical constants** (speed of light, kT0, PZ-90.11 constants, ITU-R formulas) are allowed as-is.
- **Everything else** (coordinates, frequencies, powers, gains, NF, fading margin) must be set explicitly in JSON. Use explicit `0` when the physical value is zero; missing JSON fields must not silently become zero.
- **Required fields** (`center_freq_hz`; standalone antenna `position/lat_deg`, `position/lon_deg`, `position/h_m`) use `rfsim::json_utils::required(...)` and throw if missing.
- Attached antenna `position` is optional when its global state is updated through `Antenna::receive(VehicleDynState)`; `position_rel` is then applied on registration and every simulation step before publishing `AntennaDynState`.
- Standalone antenna uses required `position` as its global position. Its `orientation_rel` is treated as global orientation, but `position_rel` is not treated as global position and does not replace `position`.
- **Conversion functions** return the **result type** and throw on invalid values. No `bool` + out-parameters.

Examples:

```cpp
const double CENTER_FREQ_HZ = rfsim::json_utils::required<double>(cfg, "/center_freq_hz", SOURCE);
```

```json
{
  "tx_power_dbm": 0.0,
  "gain_max_dbi": 0.0,
  "fading_margin_db": 3.0
}
```

```cpp
double hz_to_mhz(double freq_hz) {
  if (freq_hz <= 0.0)
    throw std::invalid_argument("[math::hz_to_mhz] Invalid frequency: freq_hz=" +
                                std::to_string(freq_hz));

  return freq_hz / 1.0e6;
}
```

### Exceptions

- Use standard C++ exceptions only: `std::invalid_argument` for invalid JSON/user input/enums, `std::runtime_error` for file I/O and environment failures, `std::logic_error` for invalid internal state or wrong API call order.
- Exception text format: `[Source] Message: key=value, key=value`. `Source` is `Class::method` or `namespace::function`.
- Always include the failing context: `path=...` for files, `field=...` for JSON fields, `sig_type=...`/`mask_type=...` for enums, `src_group=...` and `target_group=...` for P2P pairs.
- Wrap `nlohmann::json` access through `rfsim::json_utils` helpers so raw JSON exceptions do not escape without source and field context.
- Do not use `std::cerr + return` for critical library initialization or logging failures; throw an exception and let the application boundary handle it.
- Application/test entry points catch `std::exception`, print `Error: <what()>`, and return non-zero.

Examples:

```cpp
if (mask_type == JammerMaskType::UNKNOWN)
  throw std::invalid_argument("[Jammer::init] Invalid mask_type: mask_type=" + mask_type_str);
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

## Repository Rules

- **Changes to RFSimLib and RFSimLib-Tests are committed to each repo separately.**
- **ABSOLUTELY FORBIDDEN to run `git commit` or `git push` without an explicit user command.** Even if the build passes and tests run — the user decides when to commit.
- Also forbidden: `git commit --amend`, `git rebase`, and any other history-altering operations without an explicit command.

Examples:

```text
User: commit the current changes
Agent: commit RFSimLib, RFSimLib-Tests, and root RFSim separately.
```

```text
User: make the changes and run tests
Agent: do not commit or push.
```

## Scenario Structure

```
<scenario_name>/
  scenario.json               <- required: sim_duration_s, dt_s, group_id
  vehicle_1/vehicle.json
  antenna_1/antenna.json
  antenna_2/antenna.json
  jammer_1/jammer.json
  jammer_2/jammer.json        <- optional
  P2P/p2p_signal_handler.json <- current scenarios use uppercase P2P
  README.md                   <- required for committed scenarios
```
