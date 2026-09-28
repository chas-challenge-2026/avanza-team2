#ifndef __CURL_H__
#define __CURL_H__

#include <curl/curl.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct
{
  char* addr;  // Pointer to address for chunk
  size_t size; // Size of chunk
} Curl_Data;

/**
 * @brief Initializes a Curl_Data buffer for a request.
 * @param _Data Buffer to initialize.
 * @return 0 on success, -1 on failure.
 */
int curl_init(Curl_Data* _Data);

/**
 * @brief Performs a blocking GET and stores the response in _Data.
 * @param _Data Buffer previously initialized with curl_init.
 * @param _url Request URL.
 * @return 0 on success, -1 on failure.
 */
int curl_get_response(Curl_Data* _Data, const char* _url);

/**
 * @brief Frees the response buffer held by _Data.
 * @param _Data Buffer previously initialized with curl_init.
 */
void curl_dispose(Curl_Data* _Data);

#ifdef __cplusplus
} // extern "C"
#endif

#endif
