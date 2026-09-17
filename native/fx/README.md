# fx — FX pipeline

Currency conversion for foreign holdings, backing the base-currency switch
(EPIC #3) and market data for foreign holdings (#55). Tracked in #57.

## What it does

- Fetches the ECB euro foreign exchange reference rates over HTTP, either
  the latest daily rate or the full history back to 1999.
- Parses the `eurofxref` XML into an `FxTable` of `currency -> per EUR`
  (one per trading day for historical lookups).
- Resolves any pair as a cross rate through EUR:

      rate(from -> to) = (to per EUR) / (from per EUR)

- A historical lookup resolves to the ECB trading day at or before the
  requested date, since ECB doesn't publish on weekends or bank holidays.
- Both fetches sit behind a TTL cache, so repeated calls don't hit ECB
  on every lookup.

## Layout

    fx/
    ├── Makefile
    ├── include/
    │   ├── fx_rates.h        # extern "C" entry points for the JNA bridge
    │   ├── fx_table.hpp      # FxTable: currency -> per EUR, cross rates
    │   ├── fx_history.hpp    # FxHistory: date -> FxTable, at_or_before lookup
    │   ├── ecb_source.hpp    # ecb::parse_xml/parse_hist_xml, fetch_latest/fetch_history
    │   ├── rate_cache.hpp    # fx_cache::latest/history, TTL-cached wrappers
    │   └── curl_session.hpp  # RAII wrapper over common/modules/curl_helper
    ├── src/
    │   ├── fx_rates.cpp      # C shim over the C++ implementation
    │   ├── fx_table.cpp
    │   ├── fx_history.cpp
    │   ├── ecb_source.cpp
    │   ├── rate_cache.cpp
    │   └── curl_session.cpp
    └── test/
        └── test_fx.cpp       # offline self-test, `--live` also hits the network

Local sources are C++17 (`std::string_view` parsing, `std::optional`, RAII,
`std::from_chars`). The shared `common/` helpers stay C11 and are linked in.
Only `fx_rates.h` is `extern "C"` so the JNA bridge binds without name mangling;
everything else is a normal C++ header. `src/` holds only the library, it has
no entry point of its own, `test/` is what supplies `main()`.

## Build & run

    cd native
    make fx              # build libfx.so, the module's deliverable
    make fx/run          # build + run the offline self-test (used by CI)
    make fx/run-live     # same, but also hits real ECB endpoints

`libfx.so` exports `fx_rate`/`fx_convert` from `fx_rates.h` for the JNA
bridge to load.

## Not done yet (see #57)

- Wire `libfx.so` into the backend, a Java interface + service calling
  `fx_rate`/`fx_convert` through JNA, replacing the hardcoded conversion
  rate.
