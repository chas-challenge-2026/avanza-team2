#ifndef __CONFIG_H__
#define __CONFIG_H__

#include "user_config.h"


/* FALLBACK DIRS (should generate user_config.h instead) */
#ifndef LOG_DIR
#define LOG_DIR "data/logs/"
#endif

#ifndef DATA_DIR
#define DATA_DIR "data/"
#endif

#ifndef DATA_CACHE_DIR
#define DATA_CACHE_DIR "data/cache/"
#endif

#ifndef DATA_CACHE_RATE_DIR
#define DATA_CACHE_RATE_DIR "data/cache/rate/"
#endif


#endif // __CONFIG_H__ 
