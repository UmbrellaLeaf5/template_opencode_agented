# AGENTS.md

## Project & Profile

_Brief description of the project and its purpose._

### Code style

You MUST strictly follow the project's coding standards, naming conventions, and language-specific rules.

Before generating, refactoring, or modifying any code, you are REQUIRED to read and apply the guidelines defined in the external style guide:

- **File Path:** [`./CODE-STYLE.md`](./CODE-STYLE.md)

_Instruction for Agent:_ If you haven't read `./CODE-STYLE.md` in the current session, use your file-reading tool to fetch its content before writing any code. Do not hallucinate styles.

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
time-d "<your command>"
```

Long-running daemons must use `nohup ... >/dev/null 2>&1 &` so the wrapper returns immediately.

`time-d` is part of the `timeout-dead` package. Install once:

```bash
uv tool install timeout-dead   # or: pip install timeout-dead
time-d --version                       # verify installation
```

### Required Cycle After Any `.cpp/.h/.hpp` Change

```bash
# 1. Formatting all changed files
time-d "clang-format -i \$(git diff --name-only --diff-filter=AM HEAD | grep -E '\.(cpp|h|hpp)\$')"

# 2. Build (0 errors, 0 warnings)
time-d "mkdir -p build && cd build && cmake .. -DCMAKE_BUILD_TYPE=Release && make -j\$(nproc)"

# 3. Run tests
time-d "./build/tests"
```

### Mandatory Testing

**Every change must be verified by running the test suite.** No exceptions.

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

## Software Architecture & Design Patterns

### Documentation

- **Standalone Markdown documentation pages** → `SCREAMING_SNAKE_CASE` names (e.g., `JSON.md`, `ARCHITECTURE.md`, `CODE-STYLE.md`). Keep conventional repository files such as `README.md` unchanged unless explicitly requested.

### Project Structure

- Headers are kept in an `include/` directory tree. Source files mirror the header structure under `src/`.
- See [`./CODE-STYLE.md`](./CODE-STYLE.md) for file naming and organisation conventions.

```text
include/myproject/core/Processor.hpp
include/myproject/core/components/Config.hpp
include/myproject/core/shared/State.hpp
src/core/Processor.cpp
```

### Build System

- The project uses **CMake** as its build system.
- Never modify CMake files with `clang-format` — it destroys CMake syntax.
- Regression/integration tests should be a standalone CMake project in a separate directory (e.g., `tests/`). Never include them from the root `CMakeLists.txt` or any primary CMake project.
- Use `DCMAKE_BUILD_TYPE=Release` for production builds.

### Exceptions

- Use standard C++ exceptions: `std::invalid_argument` for invalid input/configuration, `std::runtime_error` for file I/O and environment failures, `std::logic_error` for invalid internal state or wrong API call order.
- Wrap third-party JSON access through utility helpers so raw library exceptions do not escape without source and field context.
- Application/test entry points catch `std::exception`, print `Error: <what()>`, and return non-zero.

### Default Values & Configuration

- **Physical constants** (speed of light, kT0, math constants) are allowed as-is.
- **Everything else** (coordinates, frequencies, powers, gains, margins) must be set explicitly in configuration. Missing required fields must throw — do not silently default to zero.
- **Required fields** use `json_utils::required(...)` — throws if the field is missing.
- **Conversion functions** return the **result type** and throw on invalid values. No `bool` + out-parameters.

## Testing Strategy

### Unit Tests

- Use a testing framework such as **Boost.Test** or **GoogleTest**.
- Tests live in a separate directory (e.g., `tests/`).
- Test files are named `test_<module>.cpp`.
- Each test case should verify a single behavior.

```cpp
// GoogleTest
TEST(ComputeTest, ReturnsCorrectResult) {
  EXPECT_EQ(compute(2, 3), 5);
}

// -----------------------------------------------

// Boost.Test
BOOST_AUTO_TEST_CASE(compute_returns_correct_result) {
  BOOST_CHECK_EQUAL(compute(2, 3), 5);
}
```

### Integration Tests

- Integration tests are a separate CMake project in `tests/integration/`.
- Never include integration tests from the root `CMakeLists.txt` or any primary CMake project.

### Regression Tests

- For projects requiring long-term stability, maintain a separate regression test suite.
- Run regression tests before every release or on CI.

## Environment & Configuration

- Configuration is loaded from JSON files at startup. The configuration format and required fields should be documented in a `JSON.md` or equivalent file.
- `.env.example` is a **committed template** — never use it directly in scripts or at runtime. It exists solely as documentation for developers.
- `.env` is the **actual runtime file** (git-ignored). Developers copy `.env.example` to `.env` and fill in their local values.
- All shell scripts read `.env` first, falling back to `.env.example` with a warning only if `.env` is absent.
