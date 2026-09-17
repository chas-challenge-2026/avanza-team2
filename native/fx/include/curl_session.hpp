#pragma once

#include "curl_helper.h"

#include <optional>
#include <string>

/*
RAII wrapper around common/modules/curl_helper.

curl_init() reserves the response buffer in the constructor and curl_dispose()
frees it in the destructor, so callers never touch Curl_Data directly.
*/

class CurlSession
{
public:
  CurlSession();
  ~CurlSession();

  CurlSession(const CurlSession&)            = delete;
  CurlSession& operator=(const CurlSession&) = delete;

  /**
   * @brief Blocking GET.
   * @param _url Request URL.
   * @return The response body, or nullopt on any failure.
   */
  std::optional<std::string> get(const std::string& _url);

private:
  Curl_Data data_{};
  bool      ready_{false};
};
