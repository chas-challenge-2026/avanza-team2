#include "rates_handler.h"
#include "curl_helper.h"
#include "config.h"
#include "time_utils.h"
#include "file_utils.h"

#include "cJSON_Utils.h"

#include <string.h>
#include <stdio.h>

/****** Internal defs ******/

#define RBAPI_SWEA_LATEST_URL    "https://api.riksbank.se/swea/v1/Observations/Latest/"
// TODO: implement
// #define RBAPI_SWEA_DATE_URL    "https://api.riksbank.se/swea/v1/Observations/"
// #define RBAPI_SWESTR_LATEST_URL  "https://api.riksbank.se/swestr/v1/Latest/"
// #define RBAPI_DATE_FROMTO "/%04c-%02c-%02c"
#define RBAPI_URL_BUF_LEN 512 // max api url len

#define RATE_CACHE_FILENAME_TEMPLATE "tbill_%s_%04d%02d%02d" // ex. rate_3M_20260101
#define RATE_CACHE_PATH_BUF_LEN 256 // max cache path len

/*
TODO: Get overnight cash benchmark rate from SWESTR endpoint


SETB1MBENCHC 	Swedish Treasury Bill maturity 1 month (03/01/1983 - )	
SETB3MBENCH 	Swedish Treasury Bill maturity 3 months (03/01/1983 - )	
SETB6MBENCH 	Swedish Treasury Bill maturity 6 months (02/01/1984 - )	
SETB12MBENCH 	Swedish Treasury Bill maturity 12 months (02/01/1984 - 21/04/2011)	

Let's turn off 12month for 
*/

/* Returns the equivalent SeriesId string per T-Bill type for riksbank API
 * Returns NULL on None or unknown type */
const char* rates_handler_get_rbapi_swea_seriesid(TBillType _Tbt)
{
  switch (_Tbt) {
    case OneMonth:
      return "SETB1MBENCHC";
    case ThreeMonth:
      return "SETB3MBENCHC";
    case SixMonth:
      return "SETB6MBENCHC";
    // case OneYear:
    //   return "SETB12MBENCHC";
    case None:
      return NULL;
    default:
      return NULL;
  }
}

/* Rate.type is returned as None by default, set it manually */
int rates_handler_parse_rbapi_response(TBillRate* _Tbr, const char* _json)
{
  cJSON* Json_Root = cJSON_Parse(_json);
  if (!Json_Root) 
  {
    perror("cJSON_Parse");
    return 1;
  }

  // Get date and rate json objects
  cJSON* Json_Rate = cJSON_GetObjectItemCaseSensitive(Json_Root, "value");
  if (!Json_Rate || !cJSON_IsNumber(Json_Rate))
  {
    fprintf(stderr, "Rate from json unexpected format");
    cJSON_Delete(Json_Root);
    return 2;
  } 
  cJSON* Json_Date = cJSON_GetObjectItemCaseSensitive(Json_Root, "date");
  if (!Json_Rate || !cJSON_IsNumber(Json_Rate))
  {
    fprintf(stderr, "Date from json unexpected format");
    cJSON_Delete(Json_Root);
    return 3;
  } 

  // Set struct values
  double value = Json_Rate->valuedouble;
  char* date = Json_Date->valuestring;
  time_t date_epoch = time_parse_iso_date_day_str_to_epoch(date);

  _Tbr->date = date_epoch;
  _Tbr->value = value;

  cJSON_Delete(Json_Root);
  return 0;
}

/* Heap allocates
 * TODO: Modularize a bit to handle both swea and swestr, latest and date-bound */
char* rates_handler_tbill_fetch_from_rbapi(TBillType _Tbt)
{
  char* response = NULL;
  const char* series = rates_handler_get_rbapi_swea_seriesid(_Tbt);

  // Build url to riksbanken API
  char full_url[RBAPI_URL_BUF_LEN];
  size_t base_url_len = strlen(RBAPI_SWEA_LATEST_URL);
  size_t rate_type_len = strlen(series);
  if ((base_url_len + rate_type_len + 1) > RBAPI_URL_BUF_LEN) {
    fprintf(stderr, "URL TOO BIG %s%s", RBAPI_SWEA_LATEST_URL, series);
    return response;
  }
  snprintf(full_url, RBAPI_URL_BUF_LEN, "%s%s", RBAPI_SWEA_LATEST_URL, series);

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
      return response;
    }
    memcpy(response, Cd.addr, Cd.size);
    response[Cd.size] = '\0';
    printf("riksbanken response: %s\n", response);
  }

  curl_dispose(&Cd);

  return response;
}

/***************************/

// TODO: Maybe make it easier to error check, returntype int instead or something
TBillRate rates_handler_tbill_get_latest(TBillType _Tbt)
{
  char filepath[RATE_CACHE_PATH_BUF_LEN], filename[RATE_CACHE_PATH_BUF_LEN];

  TBillRate Tbr = {
    .date = -1,
    .type = _Tbt,
    .value = 0.0,
  };

  size_t cache_dir_len = strlen(DATA_CACHE_RATE_DIR);
  const char* series = rates_handler_get_rbapi_swea_seriesid(_Tbt);
  
  /* Get todays date values */
  time_t now = time(NULL);
  struct tm* tm = gmtime(&now);
  int year  = tm->tm_year + 1900;
  int month = tm->tm_mon + 1;
  int day   = tm->tm_mday;

  size_t name_len = snprintf(filename, 
    RATE_CACHE_PATH_BUF_LEN, RATE_CACHE_FILENAME_TEMPLATE,
    series, year, month, day);    

  printf("cache filename: %s\n", filename);

  if (RATE_CACHE_PATH_BUF_LEN < cache_dir_len + name_len)
  {
    fprintf(stderr, "cache name too long");
    return Tbr;
  }

  snprintf(filepath, RATE_CACHE_PATH_BUF_LEN, "%s%s",
    DATA_CACHE_RATE_DIR, filename);

  printf("cache filepath: %s\n", filepath);

  // Check if cache exists and get value from there
  char* rb_response_json = NULL;
  if (file_exists(filepath)) // Get json from cache
  {
    // Read cache file
    rb_response_json = read_file_to_string(filepath);
    if (!rb_response_json)
    {
      perror("read_file_to_string");
      return Tbr;
    }

    // Parse cache json
    if (rates_handler_parse_rbapi_response(&Tbr, rb_response_json) != 0)
    {
      free(rb_response_json);
      perror("rates_handler_parse_rbapi_response");
      return Tbr;
    }
    free(rb_response_json);
  } 
  else // Get json from API
  {
    rb_response_json = rates_handler_tbill_fetch_from_rbapi(_Tbt);
    if (!rb_response_json)
    {
      perror("rates_handler_get_tbill_from_rbapi");
      return Tbr;
    }

    // Parse cache json
    if (rates_handler_parse_rbapi_response(&Tbr, rb_response_json) != 0)
    {
      perror("rates_handler_parse_rbapi_response");
      return Tbr;
    }

    // Save response to cache file
    if (write_string_to_file(rb_response_json, filepath) != 0)
      perror("write_string_to_file");
  }
  
  return Tbr;
}

