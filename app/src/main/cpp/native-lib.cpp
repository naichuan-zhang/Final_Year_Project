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

#ifdef LOG
#define LOG_ERR(x) (std::cerr << x << std::endl)
#define LOG_INFO(x) (std::cout << x << std::endl)
#else
#define LOG_ERR(x)
#define LOG_INFO(x)
#endif


using namespace cv;
using namespace std;

const float RESOLUTION = 1000.0F;
const double resol_regi = 0.6;
const double resol_seam = 0.1;

extern "C" {


JNIEXPORT jint JNICALL
Java_com_example_imageprocessor_ui_stitcher_StitcherFragment_stitchImages(JNIEnv * env, jobject,
                                                jobjectArray images, jint size, jlong resultMatAddr) {


    jint result;
    vector<Mat> imageVector = vector<Mat>();
    Mat& srcRes = *(Mat*)resultMatAddr, image;
    Mat outputPano = Mat();

    jclass clazz = (env)->FindClass("org/opencv/core/Mat");
    jmethodID getNativeObjAddr = (env)->GetMethodID(clazz, "getNativeObjAddr", "()J");

    for(int i=0; i < size; i++){
        jobject obj = (env->GetObjectArrayElement(images, i));
        jlong result = (env)->CallLongMethod(obj, getNativeObjAddr, NULL);
        image = *(Mat*)result;
        // reduce resolution of images for fast computation
        float scale = RESOLUTION / image.rows;
        resize(image, image, Size(image.rows * scale, image.cols * scale));
        // save images into vector
        imageVector.push_back(image);
        LOG_INFO("Add a new image ...");
        env->DeleteLocalRef(obj);
    }
    env->DeleteLocalRef(images);

    // try_use_gpu -> increase the speed of the entire process of image stitching
    bool try_use_gpu = true;
    Stitcher::Mode mode = Stitcher::PANORAMA;
    // create a stitcher instance
    Ptr<Stitcher> stitcher = Stitcher::create(mode, try_use_gpu);

    //
    // 图像配准分辨率 - ratio of resized image
    stitcher->setRegistrationResol(resol_regi);  // 0.6
    // 接缝分辨率
    stitcher->setSeamEstimationResol(resol_seam);   // 0.1
    // 合成分辨率
    stitcher->setCompositingResol(Stitcher::ORIG_RESOL);
    // 匹配置信度
    stitcher->setPanoConfidenceThresh(1.0);
    // 进行波形校正
    stitcher->setWaveCorrection(true);
    // 波形校正类型 - 水平校正
    stitcher->setWaveCorrectKind(detail::WAVE_CORRECT_HORIZ);
    // 查找特征点(特征点查找器) - ORB算法
    stitcher->setFeaturesFinder(makePtr<detail::OrbFeaturesFinder>());
    // 特征匹配方法 - 2NN
    stitcher->setFeaturesMatcher(makePtr<detail::BestOf2NearestMatcher>(try_use_gpu));
    // 光束平差方法 - 射线发散方法
    stitcher->setBundleAdjuster(makePtr<detail::BundleAdjusterRay>());
    // 图像投影变换 - 球面投影方法
    stitcher->setWarper(makePtr<SphericalWarper>());
    // 分块增益补偿方法
    stitcher->setExposureCompensator(makePtr<detail::BlocksGainCompensator>());
    // 接缝线算法
    stitcher->setSeamFinder(makePtr<detail::VoronoiSeamFinder>());
    // 多频段融合方法
    stitcher->setBlender(makePtr<detail::MultiBandBlender>());

    // stitch all images in vector
    Stitcher::Status status = stitcher->stitch(imageVector, outputPano);

    outputPano.copyTo(srcRes);

    LOG_INFO("After stitching ...");

    // handle stitching status
    if (status == Stitcher::OK) {

        result = 0;
    } else if (status == Stitcher::ERR_NEED_MORE_IMGS) {

        result = 1;
        LOG_ERR("ERROR: Need more images");
    } else if (status == Stitcher::ERR_HOMOGRAPHY_EST_FAIL) {

        result = 2;
        LOG_ERR("ERROR: Homography estimation failed");
    } else if (status == Stitcher::ERR_CAMERA_PARAMS_ADJUST_FAIL) {

        result = 3;
        LOG_ERR("ERROR: Camera parameter adjustment failed");
    } else {

        result = 4;
        LOG_ERR("ERROR: Unknown error");
    }

    return result;


}


}