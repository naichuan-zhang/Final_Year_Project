#include <jni.h>
#include <string>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>

#include "opencv2/opencv.hpp"

using namespace cv;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_imageprocessor_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
