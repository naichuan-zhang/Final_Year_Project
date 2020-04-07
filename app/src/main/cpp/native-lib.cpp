#include <jni.h>
#include <string>
#include <iostream>
#include <cstdio>
#include <cstdlib>
#include <vector>
#include <android/log.h>

#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/features2d.hpp"
#include "opencv2/core.hpp"
#include "opencv2/stitching.hpp"

#define TAG "native-lib"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

using namespace cv;
using namespace std;

const float RESOLUTION = 1000.0F;

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_imageprocessor_ui_stitcher_StitcherFragment_stitchImages(JNIEnv * env, jobject clazz,
                                            jlongArray imageAddressArray, jlong outputAddress) {

    jsize length = env->GetArrayLength(imageAddressArray);
    jlong * imageAddresses = env->GetLongArrayElements(imageAddressArray, nullptr);
    vector<Mat> imageVector;

    for (int i = 0; i < length; i++) {
        Mat & curImage = *(Mat*) imageAddresses[i];
        Mat newImage;
        cvtColor(curImage, newImage, CV_BGRA2RGB);
        // reduce resolution for fast computation
        float scale = RESOLUTION / curImage.rows;
        resize(newImage, newImage,
                Size(scale * curImage.rows, scale * curImage.cols));
        imageVector.push_back(newImage);
        LOG("Add a new image...");
    }

    Mat & result = *(Mat*) outputAddress;
    Stitcher stitcher = Stitcher::createDefault();
    stitcher.stitch(imageVector, result);
    LOG("After stitching...");
    env->ReleaseLongArrayElements(imageAddressArray, imageAddresses, 0);


}

}