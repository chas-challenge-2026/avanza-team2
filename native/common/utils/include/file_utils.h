#ifndef __FILE_UTILS_H__
#define __FILE_UTILS_H__

#include <stdbool.h> 

/* Returns true or false whether directory exists */
bool directory_exists(const char* _path);

/* Returns true or false whether file exists */
bool file_exists(const char* _path);

/* Tries to create directory if it doesn't already exist
 * Not recursively, needs parent */
int create_directory_if_not_exists(const char* _path);

/* Tries to write string to given file */
int write_string_to_file(const char* _str, const char* _filename);

/* Writes file to heap location */
char* read_file_to_string(const char* _filename);


#endif // __FILE_UTILS_H__ 
