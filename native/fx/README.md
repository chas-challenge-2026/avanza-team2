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
- Both fetches sit behind a cache that's invalidated once a day at 15:30
  UTC, shortly after ECB publishes around 16:00 CET, instead of refetching
  on every lookup. A failed refetch keeps serving the last known good table
  and tries ECB again after 5 minutes.
- `fx_rate_date` tells the caller which ECB trading day a lookup uses, so an
  old rate can be told apart from today's.

## Layout

    fx/
    ├── Makefile
    ├── include/
    │   ├── rates.h    # extern "C" entry points for the JNA bridge
    │   ├── rates.hpp  # the cache and its rollover rules, exposed for the self-test
    │   ├── table.hpp  # FxTable: currency -> per EUR, cross rates
    │   ├── history.hpp # FxHistory: date -> FxTable, at_or_before lookup
    │   └── ecb.hpp    # ecb::parse_xml/parse_hist_xml, fetch_latest/fetch_history
    ├── src/
    │   ├── rates.cpp  # C shim over the cache and the C++ implementation
    │   ├── table.cpp
    │   ├── history.cpp
    │   └── ecb.cpp    # parses the XML, fetches over HTTP via libcurl directly
    └── test/
        └── test_fx.cpp # offline self-test, `--live` also hits the network

Local sources are C++17 (`std::string_view` parsing, `std::optional`, RAII,
`std::from_chars`) and call `libcurl` directly, no shared C helper module is
linked in. Only `rates.h` is `extern "C"` so the JNA bridge binds without
name mangling; everything else is a normal C++ header. `src/` holds only
the library, it has no entry point of its own, `test/` is what supplies
`main()`.

## Build & run

    cd native
    make fx              # build libfx.so, the module's deliverable
    make fx/run          # build + run the offline self-test (used by CI)
    make fx/run-live     # same, but also hits real ECB endpoints

`libfx.so` exports `fx_rate`/`fx_convert`/`fx_rate_date` from `rates.h`
for the JNA bridge to load.
