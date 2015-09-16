#include <iostream>
#include <sys/time.h>
#include <math.h>
#include <cstring>
#include <cstdio>
#include <string>
#include <jni.h>
#include "extract.h"

using namespace std;

extern "C" {
JNIEXPORT jstring JNICALL Java_org_ccnx_videoplayer_CCNVideoPlayer_extractPolyKey( 
	JNIEnv* env, jobject thiz , jstring filename , jint polynomial, jint x , jint y );
}

JNIEXPORT jstring JNICALL
Java_org_ccnx_videoplayer_CCNVideoPlayer_extractPolyKey( JNIEnv* env,
                                                  jobject thiz , jstring filename, jint polynomial, jint x , jint y)
{
    jboolean isCopy;

    // C enterpretation
  	string unable_to_open_file ( "Unable to open file." );
  	string buffer;
    
    const char * c_filename = env->GetStringUTFChars(filename, &isCopy);
    string cpp_filename ( c_filename );
    
	// Free memory used to hold string
    env->ReleaseStringUTFChars ( filename, c_filename );


    __android_log_write(ANDROID_LOG_INFO, TAG, cpp_filename.c_str());

    // extract ( degree_of_polynomial , std::string sharegen , int x_share , int y_share ) 
    buffer = extract ( polynomial , cpp_filename , x , y ); // kept static for development
        
    // Extracted key
	  return env->NewStringUTF(buffer.c_str());
}
