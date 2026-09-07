#ifndef __CURL_H__
#define __CURL_H__

#include <curl/curl.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct
{
  char* addr;  // Pointer to address for chunk
  size_t size; // Size of chunk
} Curl_Data;

int curl_init(Curl_Data* _Data);

int curl_get_response(Curl_Data* _Data, const char* _url);

void curl_dispose(Curl_Data* _Data);

#endif
