#include <jni.h>
#include <string>
#include <iostream>
#include <cstdio>
#include <cstdlib>
#include <vector>

#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/features2d.hpp"
#include "opencv2/core.hpp"
#include "opencv2/stitching.hpp"

using namespace cv;
using namespace std;

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_imageprocessor_ui_stitcher_StitcherFragment_processPanorama(JNIEnv * env, jobject clazz,
                                            jlongArray imageAddressArray, jlong outputAddress) {

    // Get the length of the long array
    jsize a_len = env->GetArrayLength(imageAddressArray);
    // Convert the jlongArray to an array of jlong
    jlong *imgAddressArr = env->GetLongArrayElements(imageAddressArray, 0);
    // Create a vector to store all the image
    vector< Mat > imgVec;
    for(int k=0;k<a_len;k++)
    {
        // Get the image
        Mat & curimage=*(Mat*)imgAddressArr[k];
        Mat newimage;
        // Convert to a 3 channel Mat to use with Stitcher module
        cvtColor(curimage, newimage, CV_BGRA2RGB);
        // Reduce the resolution for fast computation
        float scale = 1000.0f / curimage.rows;
        resize(newimage, newimage, Size(scale * curimage.rows, scale * curimage.cols));
        imgVec.push_back(newimage);
    }
    Mat & result  = *(Mat*) outputAddress;
    Stitcher stitcher = Stitcher::createDefault();
    stitcher.stitch(imgVec, result);
    // Release the jlong array
    env->ReleaseLongArrayElements(imageAddressArray, imgAddressArr ,0);

}

}