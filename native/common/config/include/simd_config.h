#ifndef __SIMD_CONFIG_H__
#define __SIMD_CONFIG_H__

#include <stdint.h>

// Detect SIMD level
// This can be overriden with compile flags:
// -mavx2, -mavx = AVX2
// -mavx512f     = AVX-512
// -msse, -msse2 = SSE/SSE2

// #define __AVX512F__
// #define __AVX512BW__
// #define __AVX__
// #define __AVX2__

// ============================================================================
// AVX-512 (512-bit)
// ============================================================================
#if defined(__AVX512F__)
  #include <immintrin.h>
  #define HAS_SIMD        1

  // Types
  #define SIMD_DOUBLE_T   __m512d
  #define SIMD_FLOAT_T    __m512
  #define SIMD_INT_T      __m512i

  // Lengths
  #define SIMD_D_LEN      8
  #define SIMD_F_LEN      16
  #define SIMD_I32_LEN    16
  #define SIMD_I64_LEN    8
  
  // Double Functions (Always available with F)
  #define SIMD_D_LOADU    _mm512_loadu_pd
  #define SIMD_D_LOAD     _mm512_load_pd
  #define SIMD_D_SET1     _mm512_set1_pd
  #define SIMD_D_SUB      _mm512_sub_pd
  #define SIMD_D_MUL      _mm512_mul_pd
  #define SIMD_D_ADD      _mm512_add_pd
  #define SIMD_D_STOREU   _mm512_storeu_pd
  #define SIMD_D_STORE    _mm512_store_pd
  #define SIMD_D_ZERO     _mm512_setzero_pd

  // Float Functions (Always available with F)
  #define SIMD_F_LOADU    _mm512_loadu_ps
  #define SIMD_F_LOAD     _mm512_load_ps
  #define SIMD_F_SET1     _mm512_set1_ps
  #define SIMD_F_SUB      _mm512_sub_ps
  #define SIMD_F_MUL      _mm512_mul_ps
  #define SIMD_F_ADD      _mm512_add_ps
  #define SIMD_F_STOREU   _mm512_storeu_ps
  #define SIMD_F_STORE    _mm512_store_ps
  #define SIMD_F_ZERO     _mm512_setzero_ps

  // Integer Functions (32/64-bit always available with F)
  #define SIMD_I32_LOADU  _mm512_loadu_epi32
  #define SIMD_I32_LOAD   _mm512_load_epi32
  #define SIMD_I32_SET1   _mm512_set1_epi32
  #define SIMD_I32_SUB    _mm512_sub_epi32
  #define SIMD_I32_MUL    _mm512_mullo_epi32 // Low 32 bits of multiply
  #define SIMD_I32_ADD    _mm512_add_epi32
  #define SIMD_I32_STOREU _mm512_storeu_epi32
  #define SIMD_I32_STORE  _mm512_store_epi32
  #define SIMD_I32_ZERO   _mm512_setzero_si512

  #define SIMD_I64_LOADU  _mm512_loadu_epi64
  #define SIMD_I64_LOAD   _mm512_load_epi64
  #define SIMD_I64_SET1   _mm512_set1_epi64
  #define SIMD_I64_SUB    _mm512_sub_epi64
  #define SIMD_I64_MUL    _mm512_mullo_epi64
  #define SIMD_I64_ADD    _mm512_add_epi64
  #define SIMD_I64_STOREU _mm512_storeu_epi64
  #define SIMD_I64_STORE  _mm512_store_epi64
  #define SIMD_I64_ZERO   _mm512_setzero_si512

  // Byte/Word Functions (Only if BW is enabled)
  #if defined(__AVX512BW__)
    #define SIMD_LEVEL      "AVX-512BW"
    #define SIMD_I8_LEN     64
    #define SIMD_I16_LEN    32
    
    #define SIMD_I8_LOADU   _mm512_loadu_epi8
    #define SIMD_I8_LOAD    _mm512_load_epi8
    #define SIMD_I8_SET1    _mm512_set1_epi8
    #define SIMD_I8_SUB     _mm512_sub_epi8
    #define SIMD_I8_ADD     _mm512_add_epi8
    #define SIMD_I8_STOREU  _mm512_storeu_epi8
    #define SIMD_I8_STORE   _mm512_store_epi8
    #define SIMD_I8_ZERO    _mm512_setzero_si512

    #define SIMD_I16_LOADU  _mm512_loadu_epi16
    #define SIMD_I16_LOAD   _mm512_load_epi16
    #define SIMD_I16_SET1   _mm512_set1_epi16
    #define SIMD_I16_SUB    _mm512_sub_epi16
    #define SIMD_I16_ADD    _mm512_add_epi16
    #define SIMD_I16_STOREU _mm512_storeu_epi16
    #define SIMD_I16_STORE  _mm512_store_epi16
    #define SIMD_I16_ZERO   _mm512_setzero_si512
  #else
    #define SIMD_LEVEL      "AVX-512"
  #endif

// ============================================================================
// AVX / AVX2 (256-bit)
// ============================================================================
#elif defined(__AVX2__) || defined(__AVX__)
  #include <immintrin.h>
  #define HAS_SIMD        1

  // Types
  #define SIMD_DOUBLE_T   __m256d
  #define SIMD_FLOAT_T    __m256
  #define SIMD_INT_T      __m256i

  // Lengths
  #define SIMD_D_LEN      4
  #define SIMD_F_LEN      8
  #define SIMD_I32_LEN    8
  #define SIMD_I64_LEN    4

  // Double Functions
  #define SIMD_D_LOADU    _mm256_loadu_pd
  #define SIMD_D_LOAD     _mm256_load_pd
  #define SIMD_D_SET1     _mm256_set1_pd
  #define SIMD_D_SUB      _mm256_sub_pd
  #define SIMD_D_MUL      _mm256_mul_pd
  #define SIMD_D_ADD      _mm256_add_pd
  #define SIMD_D_STOREU   _mm256_storeu_pd
  #define SIMD_D_STORE    _mm256_store_pd
  #define SIMD_D_ZERO     _mm256_setzero_pd

  // Float Functions
  #define SIMD_F_LOADU    _mm256_loadu_ps
  #define SIMD_F_LOAD     _mm256_load_ps
  #define SIMD_F_SET1     _mm256_set1_ps
  #define SIMD_F_SUB      _mm256_sub_ps
  #define SIMD_F_MUL      _mm256_mul_ps
  #define SIMD_F_ADD      _mm256_add_ps
  #define SIMD_F_STOREU   _mm256_storeu_ps
  #define SIMD_F_STORE    _mm256_store_ps
  #define SIMD_F_ZERO     _mm256_setzero_pd

  // Integer Functions (AVX2 supports full integer set)
  #if defined(__AVX2__)
    #define SIMD_LEVEL      "AVX2"
    #define SIMD_I8_LEN     32
    #define SIMD_I16_LEN    16
    
    #define SIMD_I8_LOADU   _mm256_loadu_si256 // Cast to __m256i
    #define SIMD_I8_LOAD    _mm256_load_si256 // Cast to __m256i
    #define SIMD_I8_SET1    _mm256_set1_epi8
    #define SIMD_I8_SUB     _mm256_sub_epi8
    #define SIMD_I8_ADD     _mm256_add_epi8
    #define SIMD_I8_STOREU  _mm256_storeu_si256
    #define SIMD_I8_STORE   _mm256_store_si256
    #define SIMD_I8_ZERO    _mm256_setzero_si256

    #define SIMD_I16_LOADU  _mm256_loadu_si256
    #define SIMD_I16_LOAD   _mm256_load_si256
    #define SIMD_I16_SET1   _mm256_set1_epi16
    #define SIMD_I16_SUB    _mm256_sub_epi16
    #define SIMD_I16_ADD    _mm256_add_epi16
    #define SIMD_I16_STOREU _mm256_storeu_si256
    #define SIMD_I16_STORE  _mm256_store_si256
    #define SIMD_I16_ZERO   _mm256_setzero_si256

    #define SIMD_I32_LOADU  _mm256_loadu_si256
    #define SIMD_I32_LOAD   _mm256_load_si256
    #define SIMD_I32_SET1   _mm256_set1_epi32
    #define SIMD_I32_SUB    _mm256_sub_epi32
    #define SIMD_I32_MUL    _mm256_mullo_epi32
    #define SIMD_I32_ADD    _mm256_add_epi32
    #define SIMD_I32_STOREU _mm256_storeu_si256
    #define SIMD_I32_STORE  _mm256_store_si256
    #define SIMD_I32_ZERO   _mm256_setzero_si256

    #define SIMD_I64_LOADU  _mm256_loadu_si256
    #define SIMD_I64_LOAD   _mm256_load_si256
    #define SIMD_I64_SET1   _mm256_set1_epi64x
    #define SIMD_I64_SUB    _mm256_sub_epi64
    #define SIMD_I64_MUL    _mm256_mullo_epi64
    #define SIMD_I64_ADD    _mm256_add_epi64
    #define SIMD_I64_STOREU _mm256_storeu_si256
    #define SIMD_I64_STORE  _mm256_store_si256
    #define SIMD_I64_ZERO   _mm256_setzero_si256

  #else // AVX only has limited integer support
    #define SIMD_LEVEL      "AVX"
    #define SIMD_I32_LEN    4 // Only 4 i32s supported in pure AVX for some ops
    #define SIMD_I64_LEN    4
    
    // Pure AVX integer support is limited; 
    // better to require AVX2 for integers.
  #endif

// ============================================================================
// SSE / SSE2 (128-bit)
// ============================================================================
#elif defined(__SSE__) || defined(__SSE2__)
  #include <xmmintrin.h>
  #define HAS_SIMD        1

  // Types
  #define SIMD_DOUBLE_T   __m128d
  #define SIMD_FLOAT_T    __m128
  #define SIMD_INT_T      __m128i

  // Lengths
  #define SIMD_D_LEN      2
  #define SIMD_F_LEN      4
  #define SIMD_I32_LEN    4
  #define SIMD_I64_LEN    2

  // Double Functions (SSE2 only)
  #if defined(__SSE2__)
    #define SIMD_LEVEL      "SSE2"

    #define SIMD_D_LOADU    _mm_loadu_pd
    #define SIMD_D_LOAD     _mm_load_pd
    #define SIMD_D_SET1     _mm_set1_pd
    #define SIMD_D_SUB      _mm_sub_pd
    #define SIMD_D_MUL      _mm_mul_pd
    #define SIMD_D_ADD      _mm_add_pd
    #define SIMD_D_STOREU   _mm_storeu_pd
    #define SIMD_D_STORE    _mm_store_pd
    #define SIMD_D_ZERO     _mm_setzero_pd
  #else
    #define SIMD_LEVEL      "SSE"
  #endif

  // Float Functions
  #define SIMD_F_LOADU    _mm_loadu_ps
  #define SIMD_F_LOAD     _mm_load_ps
  #define SIMD_F_SET1     _mm_set1_ps
  #define SIMD_F_SUB      _mm_sub_ps
  #define SIMD_F_MUL      _mm_mul_ps
  #define SIMD_F_ADD      _mm_add_ps
  #define SIMD_F_STOREU   _mm_storeu_ps
  #define SIMD_F_STORE    _mm_store_ps
  #define SIMD_F_ZERO     _mm_setzero_ps

  // Integer Functions (SSE2)
  #if defined(__SSE2__)
    #define SIMD_I8_LEN     16
    #define SIMD_I16_LEN    8
    
    #define SIMD_I8_LOADU   _mm_loadu_si128
    #define SIMD_I8_LOAD    _mm_load_si128
    #define SIMD_I8_SET1    _mm_set1_epi8
    #define SIMD_I8_SUB     _mm_sub_epi8
    #define SIMD_I8_ADD     _mm_add_epi8
    #define SIMD_I8_STOREU  _mm_storeu_si128
    #define SIMD_I8_STORE   _mm_store_si128
    #define SIMD_I8_ZERO    _mm_setzero_si128

    #define SIMD_I16_LOADU  _mm_loadu_si128
    #define SIMD_I16_LOAD   _mm_load_si128
    #define SIMD_I16_SET1   _mm_set1_epi16
    #define SIMD_I16_SUB    _mm_sub_epi16
    #define SIMD_I16_ADD    _mm_add_epi16
    #define SIMD_I16_STOREU _mm_storeu_si128
    #define SIMD_I16_STORE  _mm_store_si128
    #define SIMD_I16_ZERO   _mm_setzero_si128

    #define SIMD_I32_LOADU  _mm_loadu_si128
    #define SIMD_I32_LOAD   _mm_load_si128
    #define SIMD_I32_SET1   _mm_set1_epi32
    #define SIMD_I32_SUB    _mm_sub_epi32
    #define SIMD_I32_MUL    _mm_mullo_epi32
    #define SIMD_I32_ADD    _mm_add_epi32
    #define SIMD_I32_STOREU _mm_storeu_si128
    #define SIMD_I32_STORE  _mm_store_si128
    #define SIMD_I32_ZERO   _mm_setzero_si128

    #define SIMD_I64_LOADU  _mm_loadu_si128
    #define SIMD_I64_LOAD   _mm_load_si128
    #define SIMD_I64_SET1   _mm_set1_epi64x
    #define SIMD_I64_SUB    _mm_sub_epi64
    #define SIMD_I64_MUL    _mm_mullo_epi64
    #define SIMD_I64_ADD    _mm_add_epi64
    #define SIMD_I64_STOREU _mm_storeu_si128
    #define SIMD_I64_STORE  _mm_store_si128
    #define SIMD_I64_ZERO   _mm_setzero_si128
  #endif


// ============================================================================
// Scalar (No SIMD)
// ============================================================================
#else
  #define HAS_SIMD        0
  #define SIMD_LEVEL      "Scalar"
#endif

#if HAS_SIMD
  #include <immintrin.h>
#endif

#endif // __SIMD_CONFIG_H__
