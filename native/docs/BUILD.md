# Building the Native Modules

This guide covers how to build, run, test, and debug the native C/C++ modules that live under `native/`. All modules are managed by a top-level Makefile and individual submodule Makefiles.

---

## Prerequisites

| Tool | Minimum Version | Purpose |
|---|---|---|
| `gcc` or `clang` | gcc 11+ / clang 14+ | C/C++ compiler |
| `make` | 4.0+ | Build orchestration |
| `valgrind` | 3.22+ | Memory debugging (optional) |
| `gdb` | 10+ | Interactive debugger (optional) |

On Linux: `sudo apt install build-essential valgrind gdb`
On macOS: `xcode-select --install && brew install valgrind gdb`

---

## Directory Layout

```
native/
├── Makefile              # Root Makefile — orchestrates all modules
├── helloworld/           # Example module (buildable now)
│   ├── Makefile          # Module-level Makefile
│   ├── src/main.c
│   └── include/
├── price_fetcher/        # Planned: live/historical price fetching
├── api/                  # Planned: shared API types and JNA bridge headers
├── utils/                # Shared utilities (curl_helper, etc.)
├── libs/                 # Third-party native libraries (vendor)
└── docs/
    └── BUILD.md          # ← this file
```

---

## Root Makefile (`native/Makefile`)

The root Makefile is the entry point for all build operations. It iterates over the `MODULES` list and delegates to each submodule's Makefile via `make -C`.

### Adding a New Module

1. Create the module directory (e.g., `native/risk/`) with its own `Makefile`, `src/`, and `include/` dirs.
2. Add the module name to the `MODULES` variable in the root `Makefile`:
   ```make
   MODULES := helloworld risk
   ```
3. The root Makefile now automatically exposes all targets for the new module.

### Global Targets

| Command | Description |
|---|---|
| `make` or `make all` | Build **all** modules |
| `make clean` | Clean build artifacts for **all** modules |
| `make <module>` | Build a specific module (e.g., `make helloworld`) |
| `make <module>/clean` | Clean a specific module only |

### Run & Debug Targets

| Command | Description |
|---|---|
| `make <module>/run` | Build and run the module binary |
| `make <module>/valgrind` | Run under Valgrind (full leak check) |
| `make <module>/gdb` | Launch the binary inside GDB |
| `make <module>/run-asan` | Build with AddressSanitizer + UBSan and run |
| `make <module>/profile` | Build with `-pg` for gprof profiling |

### Fuzzing Targets

| Command | Description |
|---|---|
| `make <module>/fuzz` | Build a fuzz target for the module |
| `make <module>/fuzz-asan` | Build fuzz target with AddressSanitizer linked |

### Install Target

| Command | Description |
|---|---|
| `make <module>/install` | Install the built shared library to the system prefix |

---

## Submodule Makefiles

Each module has its own `Makefile` that handles compilation, linking, and local targets. The submodule Makefile is the source of truth for:

- **Compiler selection** — uses `clang` on macOS, `gcc` on Linux
- **Build flags** — C standard, warnings, debug info, include paths
- **Source discovery** — `find` scans `src/` for all `*.c` files
- **Dependency tracking** — `-MMD -MP` generates `.d` files for incremental builds
- **Linking** — LTO enabled; library flags (`LIBS`) declared locally

### Submodule Targets

| Target | Description |
|---|---|
| `all` (default) | Compile sources and link the binary into `build/<bin>` |
| `clean` | Remove the `build/` directory |
| `run` | Execute the built binary |
| `asan` | Rebuild with `-fsanitize=address,undefined` |
| `profile` | Rebuild with `-pg` for gprof |
| `valgrind` | Run the binary under Valgrind |
| `gdb` | Open the binary in GDB |
| `print` | Print discovered sources, objects, and dependency files (debugging) |

---

## Quick Start

```bash
# Build everything
cd native
make

# Build and run the helloworld example
make helloworld
make helloworld/run

# Debug with Valgrind
make helloworld/valgrind

# Clean all
make clean
```

---

## Compiler Flags Reference

| Flag | Purpose |
|---|---|
| `-std=c11` | C11 language standard |
| `-Wall -Wextra -Wfatal-errors` | Strict warnings; stop on first error |
| `-g` | Debug symbols for GDB/Valgrind |
| `-MMD -MP` | Auto-generate dependency files for incremental builds |
| `-flto` | Link-Time Optimization |
| `-fsanitize=address,undefined` | AddressSanitizer + UndefinedBehaviorSanitizer |
| `-pg` | Generate profiling data for gprof |

---

## Shared Includes

The root Makefile exports `-I./include` so all submodules can see root-level headers. Per-module headers live in their own `include/` directories and are added via `-Iinclude` in the submodule Makefile.

For cross-module shared utilities (e.g., `native/utils/`), add the include path to `CFLAGS` in the consuming module's Makefile:
```make
CFLAGS += -I../utils/include
```

---

## IDE Setup

A `compile_flags.txt` file is provided at the project root for editor LSP integration (clangd, VS Code C/C++ extension). It lists all include paths and compiler standards used across the native modules.

---

## Planned Modules

The following modules are planned for v2 and will follow the same build pattern once implemented:

| Module | Language | Purpose |
|---|---|---|
| `backtest/` | C++17 | Investment strategy backtesting engine |
| `risk/` | C (BLAS) | Volatility, Sharpe ratio, max drawdown |
| `fx/` | C++ | Historical and real-time FX rate pipeline |
| `price_fetcher/` | C | Live and historical market price fetching |

When these modules are added, simply append their name to `MODULES` in the root Makefile.
