# Stock price provider

## Selected provider

Marketstack is the shared stock price provider for current end-of-day prices and
historical end-of-day data. The latest-price endpoint returns a closing price;
it is not an intraday real-time quote.

The API key must be supplied through the `MARKETSTACK_API_KEY`
environment variable and must never be committed to Git.

## Symbol mapping

The backend maps application tickers to Marketstack symbols in `MarketService`.
The exchange and currency values are application metadata.

| Application ticker | Marketstack symbol | Exchange | Currency |
| ------------------ | ------------------ | -------- | -------- |
| AAPL               | AAPL               | XNAS     | USD      |
| ERIC-B             | ERIC-B.ST          | XSTO     | SEK      |
| VOLV-B             | VOLV-B.ST          | XSTO     | SEK      |
| SWED-A             | SWED-A.ST          | XSTO     | SEK      |
| SAND               | SAND.ST            | XSTO     | SEK      |

## Usage limits and history

The Marketstack Free plan has a limit of 100 requests per month and provides up
to one year of historical data. Confirm current limits in the Marketstack
account before relying on them, since plans can change.

Historical imports default to a rolling 360-calendar-day window ending on the
previous UTC day. The backend requests multiple symbols together and paginates
responses with a page size of up to 1,000 rows. Each page is a separate API
request.

Successful current-price responses are cached in memory for five minutes. This
cache is not persistent across restarts.

## Fallback behavior and known gaps

The intended behavior is to use the latest cached price when Marketstack is
temporarily unavailable. Currently, an expired cache entry is removed before
refreshing, so a failed refresh returns no provider price instead of the stale
cached value.

Some portfolio paths still use hardcoded fallback prices, including a default
price of `100.0`. These are placeholders, not Marketstack data, and should be
removed so unavailable prices are represented as unavailable.

## Provider errors

The backend should handle and distinguish:

- Unknown symbols.
- Missing price data.
- Provider timeouts.
- HTTP 429 rate-limit responses.
- Other unsuccessful provider responses.

Currently, the Marketstack client converts unsuccessful HTTP responses and
other `RestClientException`s to an empty price result; it does not expose a
distinct rate-limit error to callers.
