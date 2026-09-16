# fx — FX pipeline

Currency conversion for foreign holdings, backing the base-currency switch
(EPIC #3) and market data for foreign holdings (#55). Tracked in #57.

## What it does

- Fetches the ECB euro foreign exchange reference rates over HTTP.
- Parses the `eurofxref` XML into an `FxTable` of `currency -> per EUR`.
- Resolves any pair as a cross rate through EUR:

      rate(from -> to) = (to per EUR) / (from per EUR)

## Layout

    fx/
    ├── Makefile
    ├── include/
    │   ├── fx_rates.h        # extern "C" entry points for the JNA bridge
    │   ├── fx_table.hpp      # FxTable: currency -> per EUR, cross rates
    │   ├── ecb_source.hpp    # ecb::parse_xml / ecb::fetch_latest
    │   └── curl_session.hpp  # RAII wrapper over common/modules/curl_helper
    └── src/
        ├── main.cpp          # offline parse self-test, `--live` hits the network
        ├── fx_rates.cpp      # C shim over the C++ implementation
        ├── fx_table.cpp
        ├── ecb_source.cpp
        └── curl_session.cpp

Local sources are C++17 (`std::string_view` parsing, `std::optional`, RAII,
`std::from_chars`). The shared `common/` helpers stay C11 and are linked in.
Only `fx_rates.h` is `extern "C"` so the JNA bridge binds without name mangling;
everything else is a normal C++ header.

## Build & run

    cd native
    make fx                 # build
    make fx/run             # offline parse test (used by CI)
    ./fx/build/fx --live    # fetch today's rates from ECB

## Not done yet (see #57)

- Cache the fetched table and reuse it instead of refetching per call.
- Historical lookups: honour the `_date` argument against
  `eurofxref-hist.xml` / `eurofxref-hist-90d.xml`.
- JNA bridge and wiring into the backend.
