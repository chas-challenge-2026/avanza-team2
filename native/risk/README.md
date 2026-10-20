# Risk & Volatility Calculations

Archive and shared library for functions used in calculating portfolio risk.

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

Run test using debugging tools:
```bash
make test-gdb
make test-valgrind # Note: Pick an AVX_LEVEL lower than 512
```

To achieve greater speeds in calculations AVX_LEVEL can be set. This is done automatically if no compiler flag is set by checking system cpu flags (using lscpu on linux and sysctl on MacOS).
Otherwise the avx level can be manually overriden using the AVX_LEVEL compiler flag.
Built-in options are (ranked from best to worst performance-wise):
- avx512bw 
- avx512vl 
- avx512 
- avx2 
- avx
- sse2
- none (default)

Example for avx2 (AVX-256):
```bash
make AVX_LEVEL=avx2
```

Remove all generated files:
```bash
make clean
```

## Known limitations & Bugs

# Platforms 
Minor considerations have been taken in build steps for MacOS, but it has not been tested if it actually builds and runs on MacOS yet. Only linux is guaranteed to work at the moment.

# AVX Level auto-detection
The shell commands for finding the best supported AVX instruction set on the CPU is only tested on linux running on x86_64.
Only x86_64 CPU architecture is guaranteed to work, ARM and/or RISC-V might compile with manual override but AVX will probably not be auto-detected. 
lscpu also needs to be installed for auto-detection to work.

# Test-build 
The test build does not run real "tests" but rather simple cherry-picked benchmarking on the various functions. It will fail if any doesn't return 0 (or 429 in the case of rates_handler) but relatively hardcoded values are being used.
Actual tests with proper cases should be implemented at some point

