#include "rates_handler.h"
#include "curl_helper.h"
#include "config.h"
#include "time_utils.h"
#include "file_utils.h"

#include "cJSON.h"

#include <string.h>
#include <stdio.h>

/************************ Internal defs ************************/

#define RBAPI_SWEA_LATEST_URL    "https://api.riksbank.se/swea/v1/Observations/Latest/"
#define RBAPI_SWESTR_LATEST_URL  "https://api.riksbank.se/swestr/v1/Latest/"
// TODO: implement
// #define RBAPI_SWEA_DATE_URL    "https://api.riksbank.se/swea/v1/Observations/"
// #define RBAPI_DATE_FROMTO "/%04c-%02c-%02c"
#define RBAPI_URL_BUF_LEN 256 // max api url len

#define RATE_CACHE_FILENAME_TEMPLATE "%s%s_%04d%02d%02d.json" // /data/SWESTR_20260101.json
#define RATE_CACHE_PATH_BUF_LEN 256 // max cache path len

/*
TODO: Handle request rate limiting 
Example response from riksbank.se when exceeded:
{ "statusCode": 429, "message": "Rate limit is exceeded. Try again in 59 seconds." }
*/

/* Populates Rate struct from SWEA/SWESTR API response json
 * Uses _R->type to decide what fields to find so make sure it's set */
int rates_handler_parse_rbapi_response(Rate* _R, const char* _json)
{
  cJSON* Json_Root = cJSON_Parse(_json);
  if (!Json_Root) 
  {
    perror("cJSON_Parse");
    return 1;
  }

  // Get rate/value json objects
  cJSON* Json_Rate;
  if (_R->type == Swestr)
    Json_Rate = cJSON_GetObjectItemCaseSensitive(Json_Root, "rate");
  else
    Json_Rate = cJSON_GetObjectItemCaseSensitive(Json_Root, "value");

  if (!Json_Rate || !cJSON_IsNumber(Json_Rate))
  {
    fprintf(stderr, "Rate from json unexpected format\n JSON: %s\n", _json);
    cJSON_Delete(Json_Root);
    return 2;
  } 

  // Get date json objects
  cJSON* Json_Date = cJSON_GetObjectItemCaseSensitive(Json_Root, "date");
  if (!Json_Date || !cJSON_IsString(Json_Date))
  {
    fprintf(stderr, "Date from json unexpected format\n JSON: %s\n", _json);
    cJSON_Delete(Json_Root);
    return 3;
  } 

  // Set struct values
  double value = Json_Rate->valuedouble;
  char* date = Json_Date->valuestring;
  time_t date_epoch = time_parse_iso_date_day_str_to_epoch(date);

  _R->date = date_epoch;
  _R->value = value;

  cJSON_Delete(Json_Root);
  return 0;
}

/* Heap allocates and returns full response json */
char* rates_handler_fetch_from_rbapi(RateType _Type)
{
  char* response = NULL;
  const char* series = rates_handler_get_rbapi_seriesid(_Type);

  // Build url to riksbanken API
  char full_url[RBAPI_URL_BUF_LEN];
  size_t full_url_len;

  if (_Type == Swestr) // SWESTR endpoint
  {
    full_url_len = snprintf(full_url, RBAPI_URL_BUF_LEN, 
    "%s%s", RBAPI_SWESTR_LATEST_URL, series);
  }
  else // SWEA endpoint
  {
    full_url_len = snprintf(full_url, RBAPI_URL_BUF_LEN, 
    "%s%s", RBAPI_SWEA_LATEST_URL, series);
  }

  if (full_url_len >= RBAPI_URL_BUF_LEN)
  {
    fprintf(stderr, "URL TOO BIG");
    return response;
  }

  // printf("RBAPI url: %s\n", full_url); // NOTE: dbg

  // Prepare and make call to API using curl helper
  Curl_Data Cd;
  int res = curl_init(&Cd);
  if (res != 0)
  {
    perror("curl_init");
    return response;
  }
  res = curl_get_response(&Cd, full_url);
  if (res != 0)
    perror("curl_get_response");
  
  // Allocate and copy response
  if (Cd.size > 0)
  {
    response = malloc(Cd.size + 1);
    if (!response) 
    {
      perror("malloc");
      curl_dispose(&Cd);
      return response;
    }
    memcpy(response, Cd.addr, Cd.size);
    response[Cd.size] = '\0';

    printf("riksbanken response: %s\n", response); // NOTE: dbg
  }

  curl_dispose(&Cd);

  return response;
}

/************************ Interface defs ************************/

int rates_handler_get_latest(Rate* _R, RateType _Type)
{
  _R->type = _Type;
  const char* series = rates_handler_get_rbapi_seriesid(_Type);
  if (series == NULL)
  {
    fprintf(stderr, "Invalid seriesid from enum: %d\n", _Type);
    return 1;
  }
  
  /* Get todays date values */
  time_t now = time(NULL);
  // NOTE: gmtime is not thread safe
  // Should look into gmtime_r or similar, but it isn't standard
  // struct tm tm_buf;
  // struct tm* tm = gmtime_r(&now, &tm_buf);
  struct tm* tm = gmtime(&now);
  int year  = tm->tm_year + 1900;
  int month = tm->tm_mon + 1;
  int day   = tm->tm_mday;

  // Define cache filepath
  char filepath[RATE_CACHE_PATH_BUF_LEN];
  size_t filepath_len = snprintf(filepath, 
    RATE_CACHE_PATH_BUF_LEN, RATE_CACHE_FILENAME_TEMPLATE,
    DATA_CACHE_RATE_DIR, series, year, month, day);    

  if (filepath_len >= RATE_CACHE_PATH_BUF_LEN)
  {
    fprintf(stderr, "cache name too long");
    return 2;
  }

  // printf("cache filepath: %s\n", filepath); //NOTE: dbg

  // Check if cache exists
  char* rb_response_json = NULL;
  if (file_exists(filepath)) // Get json from cache
  {
    // Read cache file
    rb_response_json = read_file_to_string(filepath);
    if (!rb_response_json)
    {
      perror("read_file_to_string");
      return 3;
    }

    // Parse cache json
    if (rates_handler_parse_rbapi_response(_R, rb_response_json) != 0)
    {
      free(rb_response_json);
      perror("rates_handler_parse_rbapi_response");
      return 4;
    }
    free(rb_response_json);
  } 
  else // Get json from API
  {
    rb_response_json = rates_handler_fetch_from_rbapi(_Type);
    if (!rb_response_json)
    {
      perror("rates_handler_fetch_from_rbapi");
      return 5;
    }

    // Parse cache json
    if (rates_handler_parse_rbapi_response(_R, rb_response_json) != 0)
    {
      perror("rates_handler_parse_rbapi_response");
      free(rb_response_json);
      return 6;
    }

    // Save response to cache file
    if (write_string_to_file(rb_response_json, filepath) != 0)
      perror("write_string_to_file");

    free(rb_response_json);
  }
  
  return 0;
}

const char* rates_handler_get_rbapi_seriesid(RateType _Type)
{
  switch (_Type) {
    case Swestr:
      return "SWESTR";
    case OneMonth:
      return "SETB1MBENCHC";
    case ThreeMonth:
      return "SETB3MBENCH";
    case SixMonth:
      return "SETB6MBENCH";
    case TwoYear:
      return "SEGVB2YC";
    case FiveYear:
      return "SEGVB5YC";
    case TenYear:
      return "SEGVB10YC";
    case None:
      return NULL;
    default:
      return NULL;
  }
}

