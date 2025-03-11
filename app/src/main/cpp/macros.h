#ifndef MY_TEST_APP_MACROS_H
#define MY_TEST_APP_MACROS_H

#include <android/log.h>

#define LOG_TAG "NativeSieve"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)


#endif //MY_TEST_APP_MACROS_H
