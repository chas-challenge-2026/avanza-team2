# Stock price provider

## Selected provider

Twelve Data is selected as the shared stock price provider.

The API key must be supplied through the `TWELVE_DATA_API_KEY`
environment variable and must never be committed to Git.

## Verified symbols

The following symbols have been identified through Twelve Data:

| Application ticker | Twelve Data symbol | Exchange | Currency |
|---|---|---|---|
| AAPL | AAPL | NASDAQ | USD |
| ERIC-B | ERIC.B | OMX | SEK |

The remaining Nasdaq Stockholm symbols must be verified before implementation.

## Usage limits

The free plan uses API credits and has minute-based and daily limits.
Successful price responses should therefore be cached.

## Fallback behavior

- Use the latest price returned by Twelve Data.
- Cache successful responses.
- Use the latest cached price if the provider is temporarily unavailable.
- Handle HTTP 429 as a rate-limit error.
- If no current or cached price exists, mark the price as unavailable.
- Never substitute a fabricated price such as `100.0`.

## Provider errors

The backend must handle:

- Unknown symbols.
- Missing price data.
- Provider timeouts.
- Rate-limit responses.
- Other unsuccessful provider responses.