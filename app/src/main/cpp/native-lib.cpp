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
const float MATCH_CONF = 0.5F;

extern "C" {


JNIEXPORT jint JNICALL
Java_com_example_imageprocessor_ui_stitcher_StitcherFragment_stitchImages(JNIEnv * env, jobject,
                                                jobjectArray images, jint size, jlong resultMatAddr) {


    jint result = 0;
    vector<Mat> imageVector = vector<Mat>();
    Mat& srcRes = *(Mat*)resultMatAddr, image;
    Mat outputPano = Mat();

    jclass clazz = (env)->FindClass("org/opencv/core/Mat");
    jmethodID getNativeObjAddr = (env)->GetMethodID(clazz, "getNativeObjAddr", "()J");

    for(int i=0; i < size; i++){
        jobject obj = (env->GetObjectArrayElement(images, i));
        jlong result = (env)->CallLongMethod(obj, getNativeObjAddr, NULL);
        image = *(Mat*)result;
        // reduce resolution for fast computation
        float scale = RESOLUTION / image.rows;
        resize(image, image, Size(image.rows * scale, image.cols * scale));
        imageVector.push_back(image);
        LOG("Add a new image ...");
        env->DeleteLocalRef(obj);
    }
    env->DeleteLocalRef(images);

    // try_use_gpu -> increase the speed of the entire process of image stitching
    bool try_use_gpu = true;
    Stitcher stitcher = Stitcher::createDefault(try_use_gpu);
    stitcher.setRegistrationResol(-1);      // 0.6
    stitcher.setSeamEstimationResol(-1);    // 0.1
    stitcher.setCompositingResol(-1);       // 1
    stitcher.setPanoConfidenceThresh(-1);   // 1

    Stitcher::Status status = stitcher.stitch(imageVector, outputPano);

    outputPano.copyTo(srcRes);

    LOG("After stitching ...");

    if (status == Stitcher::OK) {
        result = 0;
    } else if (status == Stitcher::ERR_NEED_MORE_IMGS) {
        result = 1;
    } else if (status == Stitcher::ERR_HOMOGRAPHY_EST_FAIL) {
        result = 2;
    } else if (status == Stitcher::ERR_CAMERA_PARAMS_ADJUST_FAIL) {
        result = 3;
    }

    return result;


}


}