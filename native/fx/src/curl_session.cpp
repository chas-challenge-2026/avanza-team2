#include "curl_session.hpp"

CurlSession::CurlSession()
{
  ready_ = (curl_init(&data_) == 0);
}

CurlSession::~CurlSession()
{
  if (ready_)
    curl_dispose(&data_);
}

std::optional<std::string> CurlSession::get(const std::string& _url)
{
  if (!ready_)
    return std::nullopt;

  if (curl_get_response(&data_, _url.c_str()) != 0 || data_.addr == nullptr)
    return std::nullopt;

  return std::string(data_.addr, data_.size);
}
