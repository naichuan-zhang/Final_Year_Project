#include <jni.h>
#include <string>
#include <iostream>
#include <cstdio>
#include <cstdlib>
#include <vector>

#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/stitching.hpp"

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_imageprocessor_SplashActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

char filepath1[100] = "/storage/emulated/0/Download/PacktBook/Chapter6/panorama_stitched.jpg";
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_imageprocessor_ui_stitcher_StitcherFragment_stitch(JNIEnv *env, jobject thiz,
                                                                    jobjectArray images, jint size,
                                                                    jlong addr_src_res) {
    // TODO: implement stitch()
}