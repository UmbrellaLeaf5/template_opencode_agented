# CONTRIBUTING.md

## Commit Message Guidelines

A well-structured and informative commit message is essential for maintaining a clear and understandable version history of the codebase.
Below is the commit message template we will follow.

---

## Commit Format

```
<type>(<scope>): <subject>
```

**Subject only.** A commit message must be a single line. Never add a body or a footer — no description, issue references, trailers, `Co-authored-by`, or `Signed-off-by` lines. This rule applies to both humans and AI agents.

### Type (required)

The type of change describing the purpose of the commit:

| Type       | Description                                     |
| ---------- | ----------------------------------------------- |
| `feat`     | New feature for the user                        |
| `fix`      | Bug fix                                         |
| `docs`     | Documentation changes                           |
| `style`    | Code formatting (indentation, semicolons, etc.) |
| `refactor` | Code refactoring without behavior change        |
| `test`     | Adding or modifying tests                       |
| `chore`    | Routine tasks, maintenance, tooling changes     |

### Scope (optional)

Specifies the module, component, or functional area affected by the commit:

- `ui`
- `solver`
- `database`
- etc.

### Subject (required)

A short, imperative statement summarizing the change.

---

## Examples

```
fix(solver): 0 angle velocity in Rolling
```

```
feat(api): add pagination support for listings
```

```
docs(readme): update installation instructions
```

```
refactor(database): optimize user queries with join fetch
```
