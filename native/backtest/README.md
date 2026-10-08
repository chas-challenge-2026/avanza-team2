# Backtest Engine

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
