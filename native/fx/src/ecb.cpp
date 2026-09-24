#include "ecb.hpp"

#include <curl/curl.h>

#include <charconv>
#include <string>

namespace {

/*
Reads the value of `name="..."` (or `name='...'`) from within a single element.
Searching is bounded to `_element`, so a missing attribute never picks up a
value from a neighbouring element.
*/
std::optional<std::string_view> attr_value(std::string_view _element, std::string_view _name)
{
  std::size_t key = _element.find(_name);
  if (key == std::string_view::npos)
    return std::nullopt;

  std::size_t open = key + _name.size();
  if (open >= _element.size() || (_element[open] != '"' && _element[open] != '\''))
    return std::nullopt;

  char        quote = _element[open];
  std::size_t start = open + 1;
  std::size_t stop  = _element.find(quote, start);
  if (stop == std::string_view::npos)
    return std::nullopt;

  return _element.substr(start, stop - start);
}

std::optional<double> to_double(std::string_view _text)
{
  double value        = 0.0;
  auto [ptr, ec]      = std::from_chars(_text.data(), _text.data() + _text.size(), value);
  if (ec != std::errc{} || ptr != _text.data() + _text.size())
    return std::nullopt;

  return value;
}

// curl_global_init isn't thread-safe, and curl_easy_init's implicit
// auto-init isn't either; a function-local static runs it exactly once
// even if fetch_latest()/fetch_history() are ever called concurrently.
void ensure_curl_global_init()
{
  static const bool once = (curl_global_init(CURL_GLOBAL_DEFAULT) == CURLE_OK);
  (void)once;
}

std::size_t write_callback(char* _data, std::size_t _size, std::size_t _count, void* _out)
{
  static_cast<std::string*>(_out)->append(_data, _size * _count);
  return _size * _count;
}

std::optional<std::string> http_get(const std::string& _url)
{
  ensure_curl_global_init();

  CURL* curl = curl_easy_init();
  if (!curl)
    return std::nullopt;

  std::string body;
  curl_easy_setopt(curl, CURLOPT_URL, _url.c_str());
  curl_easy_setopt(curl, CURLOPT_NOSIGNAL, 1L);
  // Without these curl waits indefinitely, and the first lookup of the day
  // runs this fetch on the caller's thread, so a hanging ECB would block it.
  // The total limit is sized for the full history file, which is several MB.
  curl_easy_setopt(curl, CURLOPT_CONNECTTIMEOUT, 5L);
  curl_easy_setopt(curl, CURLOPT_TIMEOUT, 30L);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, &body);

  CURLcode result = curl_easy_perform(curl);
  curl_easy_cleanup(curl);

  return result == CURLE_OK ? std::optional(body) : std::nullopt;
}

} // namespace

namespace ecb {

FxTable parse_xml(std::string_view _xml)
{
  FxTable table;

  // Walk one <Cube ...> element at a time and pull both attributes from that
  // same element, so an entry without a rate is skipped rather than pairing
  // the currency with the next entry's rate.
  std::size_t pos = 0;
  while ((pos = _xml.find("<Cube ", pos)) != std::string_view::npos) {
    std::size_t tag_end = _xml.find('>', pos);
    if (tag_end == std::string_view::npos)
      break;

    std::string_view element = _xml.substr(pos, tag_end - pos);
    pos                      = tag_end + 1;

    auto currency = attr_value(element, "currency=");
    auto rate     = attr_value(element, "rate=");
    if (!currency || !rate)
      continue;

    if (auto value = to_double(*rate))
      table.set(*currency, *value);
  }

  return table;
}

std::optional<FxTable> fetch_latest()
{
  auto body = http_get(std::string(daily_url));
  if (!body)
    return std::nullopt;

  FxTable table = parse_xml(*body);
  if (table.size() <= 1) // nothing beyond the seeded EUR entry
    return std::nullopt;

  return table;
}

FxHistory parse_hist_xml(std::string_view _xml)
{
  FxHistory history;

  // Walk one <Cube time="..."> block at a time and hand its inner
  // <Cube currency=".." rate=".."/> entries to parse_xml, so each trading
  // day's rates stay grouped under that day's date.
  std::size_t pos = 0;
  while ((pos = _xml.find("<Cube time=", pos)) != std::string_view::npos) {
    std::size_t open_end = _xml.find('>', pos);
    if (open_end == std::string_view::npos)
      break;

    auto date = attr_value(_xml.substr(pos, open_end - pos), "time=");

    std::size_t block_end = _xml.find("</Cube>", open_end);
    if (block_end == std::string_view::npos)
      break;

    std::string_view block = _xml.substr(open_end + 1, block_end - (open_end + 1));
    pos                    = block_end + std::string_view("</Cube>").size();

    if (date)
      history.set(std::string(*date), parse_xml(block));
  }

  return history;
}

std::optional<FxHistory> fetch_history()
{
  auto body = http_get(std::string(hist_url));
  if (!body)
    return std::nullopt;

  FxHistory history = parse_hist_xml(*body);
  if (history.empty())
    return std::nullopt;

  return history;
}

} // namespace ecb
