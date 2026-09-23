#pragma once

#include "history.hpp"
#include "table.hpp"

#include <memory>

/*
Caches the ECB fetches behind a TTL so repeated fx_rate/fx_convert calls
don't hit the network on every call. Safe to call from multiple threads.
*/

namespace fx_cache {

/**
 * @brief Cached daily rate table, refetched once older than the TTL.
 * @return The table, or nullptr when the last fetch attempt failed.
 */
std::shared_ptr<const FxTable> latest();

/**
 * @brief Cached full rate history, refetched once older than the TTL.
 * @return The history, or nullptr when the last fetch attempt failed.
 */
std::shared_ptr<const FxHistory> history();

} // namespace fx_cache
