#include <gmp.h>
#include <stdlib.h>
#include <math.h>
#include <sys/types.h>
#include <time.h>
#include <iostream>
#include <cstdlib>
#include <iostream>
#include <fstream>
#include <time.h>
#include <omp.h>
#include <gmpxx.h>
#include <android/log.h>

/* s is the number of points on the polynomial */
//#define s 1000000


/* t is the degree of polynomial and threshould for member revocation */
//#define t 300000


#define MAX_R 10000000
#define MIN_R 1
#define Q 25417061
#define EQ 50834123
#define G 49
#define RNUM 100000
#define TAG "CPP_POLY_LIB"
//#define Q 11
//#define EQ 23
//#define G 4

