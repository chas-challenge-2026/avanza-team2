#include "curl_helper.h"

/*============= Internal functions =============*/

size_t curl_callback(void* _contents, size_t _size, size_t _nmemb, Curl_Data* _Data);

/*==============================================*/

/*
 * Resources:
 * https://curl.se/libcurl/c/CURLOPT_WRITEFUNCTION.html
 * https://stackoverflow.com/questions/13905774/in-c-how-do-you-use-libcurl-to-read-a-http-response-into-a-string?rq=3
 * https://curl.se/libcurl/c/getinmemory.html
 *
 */

int curl_init(Curl_Data* _Data)
{
  memset(_Data, 0, sizeof(Curl_Data));

  _Data->addr = malloc(1); // We simply allocate an address
  if (_Data->addr == NULL)
    return -1;

  return 0;
}

size_t curl_callback(void* _contents, size_t _size, size_t _nmemb, Curl_Data* _Data)
{
  size_t realsize = _size * _nmemb;
  Curl_Data* mem = (Curl_Data*)_Data;

  char* ptr = realloc(mem->addr, mem->size + realsize + 1); /* We reallocate memory for our chunk
                                                               and make a pointer to the new addr */
  if (!ptr) {
    perror("Not enough memory - realloc returned NULL\n");
    return 0;
  }

  mem->addr = ptr; /* We redefine our addr to the pointer since realloc went well */
  memcpy(&(mem->addr[mem->size]), _contents,
         realsize);      /* We copy realsize*bytes from contents to our chunk */
  mem->size += realsize; /* we add realsize to our chunksize */
  mem->addr[mem->size] = 0;

  return realsize; /* We return the size of the chunk... */
}

int curl_get_response(Curl_Data* _Data, const char* _url)
{
  CURL* curl;
  CURLcode res;
  char error[CURL_ERROR_SIZE];

  _Data->size = 0; /* We will reallocate memory to it in write_memory(), for now 0 data */

  curl = curl_easy_init();
  if (!curl) {
    fprintf(stderr, "curl_easy_init failed\n");
    return -1;
  }

  curl_easy_setopt(curl, CURLOPT_NOSIGNAL, 1L);
  curl_easy_setopt(curl, CURLOPT_URL, _url);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, curl_callback);
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void*)_Data);
  curl_easy_setopt(curl, CURLOPT_USERAGENT,
                   "TempleOSExplorer/1.0 (TempleBot/16.0; HolyCScript) "
                   "DivineEngine/20231220");
  curl_easy_setopt(curl, CURLOPT_ERRORBUFFER, error);

  res = curl_easy_perform(curl);
  if (res != CURLE_OK) {
    perror("CURL");
#ifdef DEBUG
    printf("curl_easy_perform() failed: %s\n", curl_easy_strerror(res));
#endif

    return -1;
  } else {
#ifdef DEBUG
    printf("Response addr:\n%s\n", _Data->addr);
    printf("Response size:\n%lu\n", (unsigned long)_Data->size);
#endif
  }

  curl_easy_cleanup(curl);
  /* free(_data->addr); */

  return 0;
}

/* Caller needs to free the data from memory when they're done using it 🐦 */
void curl_dispose(Curl_Data* _Data)
{
  if (_Data->addr != NULL) {
    printf("Disposing curl\n");
    free(_Data->addr);
    _Data->addr = NULL;
  }
}
