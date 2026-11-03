# Risk & Volatility Calculations

Archive and shared C library for functions used in calculating portfolio risk and performance.

## Build instructions

Regular build will place librisk.so and librisk.so in BUILD_DIR (default: ./build). This can be overridden using the BUILD_DIR flag.

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

## Shared/archive library interface functions

### Volatility calculations
```C
// volatility.h

/** Calculates the volatility from an array of series return doubles */
double risk_calc_volatility_double(const double* _returns, int _n);

/** Calculates the volatility from an array of series return floats */
float risk_calc_volatility_float(const float* _returns, int _n);

/** Calculates the volatility from an array of series return int32_ts */
double risk_calc_volatility_int32_t(const int32_t* _returns, int _n);

/** Calculates the volatility from an array of series return doubles
 * SIMD version auto detected using simd_config.h
 * NOTE: These are actually slower than scalar if using less than ~500 inputs */
double risk_calc_volatility_double_simd(const double* _returns, size_t _n);

/** Calculates the volatility from an array of series return floats
 * SIMD version auto detected using simd_config.h 
 * NOTE: These are actually slower than scalar if using less than ~500 inputs */
float risk_calc_volatility_float_simd(const float* _returns, size_t _n);

```

### Sharpe ratio and Max drawdown
Sharpe ratio is a step beyond simple volatility calculations for determining the overall risk a portfolio has. It basically compares the overall returns to a riskfree rate when calulcating the volatility, comparing the performance to simply holding cash in a savings account.

```C
// volatility.h

/** Calculates the sharpe ratio on a set of series returns
 * Takes arg _rfrate for annual risk-free rate (ex. from t-bill/bonds/overnight cash benchmark)
 * and _year_freq the frequency of returns in a year, so 252 for standard market daily returns 
 * (either can be set to zero for rateless calculation) */
double risk_calc_sharpe_ratio_double(const double* _returns, size_t _n, double _rfrate, size_t _year_freq);

/* Calculates max drawdown on cumulative asset values
 * Returns a positive fraction, so 0.25 means a 25% drop 
 * Returns 1.0 if all was lost or 0.0 if invalid input or no value change */
double risk_calc_max_drawdown(const double* _values, size_t _n);

```

### Moving Average calculations
Moving averages are price indicators based on historical asset price action. They are used to indicate what a current fair price should be based on previous trends. So if the price is above the MA, it is considered overpriced. Likewise if the price is below the MA, it is considered undervalued.

```C
// ma.h

/** Calculates Simple Moving Average (SMA) on set of data
 * Returns a heap allocated array with _n - _window amount of doubles
 * Index 0 of resulting array corresponds to index _window-1 from the _data array
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
double* risk_calc_sma_double(const double* _data, size_t _n, size_t _window);

/** Calculates Weighted Moving Average (WMA) on set of data
 * Same as SMA but prioratizes most recent data using a weight
 * most recent data has the highest weight=_window and oldest has weight=1
 * Returns a heap allocated array with _n - _window amount of doubles
 * Index 0 of resulting array corresponds to index _window-1 from the _data array
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
double* risk_calc_wma_double(const double* _data, size_t _n, size_t _window);

/** Calculates Exponentially Weighted Moving Average (EMA/EWMA) on set of data
 * Same as WMA but the weights are exponential, _window only used for alpha
 * Returns a heap allocated array with _n amount of doubles
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
double* risk_calc_ema_double(const double* _data, size_t _n, size_t _window);

/** Calculates Simple Moving Average (SMA) on set of data
 * Returns a heap allocated array with _n - _window amount of floats
 * Index 0 of resulting array corresponds to index _window-1 from the _data array
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
float* risk_calc_sma_float(const float* _data, size_t _n, size_t _window);

/** Calculates Weighted Moving Average (WMA) on set of data
 * Same as SMA but prioratizes most recent data using a weight
 * most recent data has the highest weight=_window and oldest has weight=1
 * Returns a heap allocated array with _n - _window amount of floats
 * Index 0 of resulting array corresponds to index _window-1 from the _data array
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
float* risk_calc_wma_float(const float* _data, size_t _n, size_t _window);

/** Calculates Exponentially Weighted Moving Average (EMA/EWMA) on set of data
 * Same as WMA but the weights are exponential, _window only used for alpha
 * Returns a heap allocated array with _n amount of floats
 * Parameters:
 *    _data: array of input data
 *    _n: amount of data elements
 *    _window: size of the MA window */
float* risk_calc_ema_float(const float* _data, size_t _n, size_t _window);

```

### Risk-free rates fetching
The rates handler fetches risk-free rates from riksbank.se API. There are several different rates that can be fetched, these are:

Swestr      -- Overnight cash benchmark
OneMonth    -- Swedish T-Bill 1 Month
ThreeMonth  -- Swedish T-Bill 3 Month
SixMonth    -- Swedish T-Bill 6 Month
TwoYear     -- Swedish Gov Bond 2 Years
FiveYear    -- Swedish Gov Bond 5 Years
TenYear     -- Swedish Gov Bond 10 Years

When in doubt, choose SWESTR, since it's the best benchmark rate, especially for rolling Sharpe Ratio calcalculation.


```C
// rates_handler.h

/* Procures interest rate in decimal form (i.e 0.036 for 3.6%) based on type chosen 
 * Fetches rate from riskbanken API, caches and reuses response 
 * Riksbank API rate limit: 5 Requests/min and 1000 requests/day
 * Returns: 0 for success, 429 for request rate limit hit, else misc errors
 * NOTE: all rates are in SEK */
int rates_handler_get_latest(Rate* _R, RateType _Type);

/* Procures overnight riskfree interest rate in decimal form (i.e 0.036 for 3.6%) 
 * Fetches rate from riskbanken API, caches and reuses response 
 * Riksbank API rate limit: 5 Requests/min and 1000 requests/day
 * Returns: 0 for success, 429 for request rate limit hit, else misc errors
 * NOTE: all rates are in SEK 
 * Simpler version without timestamp and only uses SWESTR series */
int rates_handler_get_latest_swestr(double* _rate);

/* Returns the equivalent SeriesId string per T-Bill type for riksbank API
 * Returns NULL on None or unknown type */
const char* rates_handler_get_rbapi_seriesid(RateType _Type);

```

## Known limitations & Bugs

### Platforms 
Minor considerations have been taken in build steps for MacOS, but it has not been tested if it actually builds and runs on MacOS yet. Only linux is guaranteed to work at the moment.

### AVX Level auto-detection
The shell commands for finding the best supported AVX instruction set on the CPU is only tested on linux running on x86_64.
Only x86_64 CPU architecture is guaranteed to work, ARM and/or RISC-V might compile with manual override but AVX will probably not be auto-detected. 
lscpu also needs to be installed for auto-detection to work.

### Test-build 
The test build does not run real "tests" but rather simple cherry-picked benchmarking on the various functions. It will fail if any doesn't return 0 (or 429 in the case of rates_handler) but relatively hardcoded values are being used.
Actual tests with proper cases should be implemented at some point
