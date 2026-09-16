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

On Linux (debian/ubuntu): `sudo apt install build-essential valgrind gdb`
On macOS: `xcode-select --install && brew install valgrind gdb`

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

### Installing system environment

Before building some modules, paths and configs must be installed with the correct permissions. By default dirs are installed in /var/lib/avanza2 and /var/log/avanza2 but can be changed in the root Makefile.
```bash
sudo make install
```
Default userid and groupid are 1000 but can be overriden using UID= and GID= flags:
```bash
sudo make install UID=1001 GID=1001
```

### Global Targets

| Command | Description |
|---|---|
| `make install` | Install environment |
| `make` or `make all` | Build **all** modules |
| `make clean` | Clean build artifacts for **all** modules |
| `make <module>` | Build a specific module (e.g., `make helloworld`) |
| `make <module>/clean` | Clean a specific module only |

### Run & Debug Targets (Module specific)

| Command | Description |
|---|---|
| `make <module>/run` | Build and run the module binary |
| `make <module>/test` | Build and run the module binary |
| `make <module>/valgrind` | Run under Valgrind (full leak check) |
| `make <module>/gdb` | Launch the binary inside GDB |
| `make <module>/run-asan` | Build with AddressSanitizer + UBSan and run |
| `make <module>/profile` | Build with `-pg` for gprof profiling |

## Submodule Makefiles

Each module has its own `Makefile` that handles compilation, linking, and local targets. The submodule Makefile is the source of truth for:

- **Compiler selection** — uses `clang` on macOS, `gcc` on Linux
- **Build flags** — C standard, warnings, debug info, include paths
- **Source discovery** — `find` scans `src/` for all `*.c` files
- **Dependency tracking** — `-MMD -MP` generates `.d` files for incremental builds
- **Linking** — LTO enabled; library flags (`LIBS`) declared locally

### Supported Submodule Targets

Depends on the module specific implementation

`clean`   
`run`      
`asan`     
`fuzz-asan`
`run-asan` 
`profile`  
`valgrind` 
`gdb`          
`test`         
`test-run`     
`test-gdb`     
`test-valgrind`

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

When more modules are added, simply append their name to `MODULES` in the root Makefile.
