# CODE-STYLE.md

Additional code-writing rules for Python `.ipynb` notebooks.

These rules **extend** the base Python style guide and do not replace it:

- **Base style guide:** [`../CODE-STYLE.md`](../CODE-STYLE.md)

All rules from `../CODE-STYLE.md` apply to notebook code cells as well. This file only defines notebook-specific rules that are not already covered by the base Python guide.

## Table of Contents

- [Indentation & Layout](#indentation--layout)
- [Language Usage](#language-usage)
- [Imports](#imports)
- [Naming Conventions](#naming-conventions)
- [Blank Lines](#blank-lines)
- [Comments](#comments)
- [Constants](#constants)
- [Plotting Rules](#plotting-rules)
- [Testing](#testing)

---

## Indentation & Layout

- Notebook code cells follow the same formatting rules as regular Python files. Format notebooks with Ruff before considering the work complete:

  ```bash
  time-d -c "ruff format ."
  time-d -c "ruff check ."
  ```

- Execute notebooks with `nbconvert` to verify that outputs are reproducible from a clean kernel:

  ```bash
  time-d -c --sec 300 "jupyter nbconvert --to notebook --execute notebooks/example.ipynb --inplace"
  ```

- Keep cells short and purpose-specific. A cell should load data, configure a plot, render a plot, or explain a result - not mix all of these responsibilities.

- Section-level code cells must be at most 15 lines. If a section grows beyond that, split it into smaller cells or move reusable logic into a utility module.

- Do not add empty cells or trailing whitespace-only cells. If a cell exists, it must have a clear purpose.

## Language Usage

### No Function Definitions In Notebooks

- Do not define functions or classes inside `.ipynb` files. No `def`, no `class`, no nested helpers in notebook cells.

  Bad:

  ```python
  def load_results(path: Path) -> pd.DataFrame:
    return pd.read_csv(path)
  ```

  Good:

  ```python
  from plot_utils import load_results
  ```

- Move all reusable logic to a `.py` utility module (`plot_utils.py`, `notebook_utils.py`, `data_utils.py`, or a more specific module name) and import it into the notebook.

- If a utility import is reported by Ruff as unused, treat that as a self-check: either remove the import, or move suitable logic from the notebook into that utility module and use the imported helper.

### Notebook Code Cells

- Notebook cells are allowed to orchestrate work: choose paths, call imported helpers, create plots, and display results.

  ```python
  data = load_results(LOGS / "received_power.csv")

  fig, ax = plt.subplots(figsize=DEFAULT_FIG_SIZE)

  plot_received_power(ax, data, label="Model")
  style_power_axis(ax)
  place_legend_outside_plot(ax)

  plt.tight_layout()
  ```

- Notebook cells must not contain reusable parsing, filtering, interpolation, validation, or plotting-style logic. Put that logic in imported `.py` files.

## Imports

- The first code cell must import notebook utilities. If there are project-specific helpers, they must be imported there.

  ```python
  import plot_utils
  ```

- All remaining imports and notebook magic commands belong in a separate imports section after the utility-import cell.

  ```python
  # Импорты
  %matplotlib inline

  from pathlib import Path

  import matplotlib.pyplot as plt
  import pandas as pd
  ```

- Do not define fallback helpers inline when an import fails. Fix the import path or module structure instead.

## Naming Conventions

- Keep notebook-level constants in `SCREAMING_SNAKE_CASE`.

  ```python
  LOGS = Path("../../build/logs/test_2_dynamic")
  CSV_FILES = sorted(LOGS.glob("*.csv"))
  ```

- Use descriptive names for plotted quantities. Names must include units when they represent physical values.

  ```python
  distance_km = data["distance_km"]
  received_power_dbm = data["received_power_dbm"]
  ```

- Keep the variable name `LOGS` for the active log directory when a notebook works with generated logs.

## Blank Lines

- Use blank lines to separate notebook cell phases: load/validate data, create figure, draw curves, style axes, and finalize layout.

  ```python
  data = load_results(LOGS / "received_power.csv")
  validate_received_power(data)

  fig, ax = plt.subplots(figsize=DEFAULT_FIG_SIZE)

  plot_received_power(ax, data, label="Model")
  style_power_axis(ax)
  place_legend_outside_plot(ax)

  plt.tight_layout()
  ```

- Add a blank line before and after `plt.subplots(...)`.

- Add a blank line before `plt.tight_layout()`.

- Do not add extra blank lines solely at the end of a cell.

- Standard separator comments from Python files (`# --------------------------------------------------`) are optional in notebooks. Prefer ending the current cell and continuing in the next cell instead of adding separator-only comments.

## Comments

### Markdown Cells

- Markdown cells in notebooks must be written in Russian.

- Markdown cells should explain the verification goal, input logs, plotted quantities, and interpretation of the result.

  ```markdown
  ## Проверка мощности приёма

  График сравнивает мощность приёма модели с опорной кривой из статьи.
  ```

- Do not use Markdown cells as a substitute for code comments that explain reusable logic. Reusable logic belongs in `.py` utility files with normal Python documentation.

### Code Comments

- Notebook code comments follow the base Python comment-language rules from [`../CODE-STYLE.md`](../CODE-STYLE.md).

- Prefer moving comment-heavy code into a utility module. If a notebook cell needs many comments, it probably contains logic that belongs in `.py`.

## Constants

- Reused constants must live in a `.py` utility module, not in notebook cells.

  ```python
  from plot_utils import DEFAULT_FIG_SIZE, RECEIVED_POWER_DBM_COLUMN
  ```

- Notebook-local constants are allowed only when they are specific to that notebook and not reused elsewhere.

  ```python
  REFERENCE_DISTANCE_KM = 10.0
  ```

- Article/reference constants that are reused across notebooks must be placed in a utility module and imported.

- Absolute paths are forbidden in notebook code cells as well. Use paths relative to the notebook location, project root, environment variables, CLI arguments, or imported utility functions. Absolute paths are allowed only in terminal commands shown for manual execution.

  ```python
  # Good
  LOGS = Path("../../build/logs/test_2_dynamic")

  # Bad
  LOGS = Path("C:\\Users\\user\\project\\build\\logs\\test_2_dynamic")
  ```

## Plotting Rules

- Every plot without exception must have a legend.

  ```python
  ax.plot(distance_km, received_power_dbm, label="Model")
  ax.legend()
  ```

- The legend must not overlap plotted lines or markers inside the canvas. Move it outside the plot area or choose a safe location.

  ```python
  ax.legend(loc="upper left", bbox_to_anchor=(1.02, 1.0), borderaxespad=0.0)
  ```

- Every plot must have axis labels. Axis labels must include units when units exist.

  ```python
  ax.set_xlabel("Distance, km")
  ax.set_ylabel("Received power, dBm")
  ```

- Always configure grid ticks so plots are readable and visually clean. Use major and minor ticks when default ticks are not enough.

  ```python
  ax.minorticks_on()
  ax.grid(True, which="major", linestyle="-", alpha=0.7)
  ax.grid(True, which="minor", linestyle=":", alpha=0.35)
  ```

- Every plot must have a grid suitable for reading values.

- Use titles that identify the scenario/data source and plotted quantity.

  ```python
  ax.set_title("test_2_dynamic: received power")
  ```

- Highlight thresholds and reference values explicitly with dashed lines and labels.

  ```python
  ax.axhline(threshold_dbm, linestyle="--", label="Threshold")
  ```

- Prefer shared plotting helpers from utility modules for repeated styling.

  ```python
  style_power_axis(ax)
  place_legend_outside_plot(ax)
  ```

## Testing

- Run Ruff on notebooks and utility modules after every notebook change:

  ```bash
  time-d -c "ruff format ."
  time-d -c "ruff check ."
  ```

- Execute changed notebooks with `nbconvert` from a clean kernel:

  ```bash
  time-d -c --sec 300 "jupyter nbconvert --to notebook --execute notebooks/example.ipynb --inplace"
  ```

- Do not clear notebook outputs before committing. If the project keeps notebook outputs under version control, verify that updated outputs are intentional and reproducible. Clear only stale crash outputs or accidental noisy outputs.

- If a notebook depends on generated logs or large external artifacts, validate paths at the top of the notebook and fail with a clear error.

  ```python
  if not LOGS.exists():
    raise FileNotFoundError(f"Missing log directory: log_dir={LOGS}")
  ```
