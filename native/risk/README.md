# Risk & Volatility Calculations

## Build instructions

Ensure all dependencies are initialized:
```bash
git submodule update --init --recursive
```

Compile the main risk module:
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

Override SIMD instruction set (options: avx512, avx2, avx, sse2, none):
(This should be detected automatically otherwise)
```bash
make AVX_LEVEL=avx512
```

Remove all generated files:
```bash
make clean
```
