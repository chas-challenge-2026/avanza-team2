# Backtest Engine

Module for backtesting historic price actions.

## Shared lib interface functions

Run a backtest on mutliple time series of the same length. Returns pointer to memory adddress where result is saved.
```C
BacktestResult* run_backtest(const double* prices, int instruments, int days, const char* strategy);
```

Free pointer to BacktestResult. Essential for removing allocated memory from previous backtest. Without it, there will be memory leaks.
```C
void backtest_free_result(BacktestResult* result);
```

## Build instructions

Module "risk" must be built beforehand since backtest depends on ../risk/build/librisk.a

Compile the main backtest module:
```bash
make
```

Compile and execute test
```bash
make test-run
```

Run tests using tools:
```bash
make test-gdb
make test-valgrind # Note: Pick an AVX_LEVEL lower than 512
```

Remove all generated files:
```bash
make clean
```
