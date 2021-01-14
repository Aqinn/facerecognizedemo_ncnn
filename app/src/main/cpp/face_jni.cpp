#include <android/bitmap.h>
#include <android/log.h>
#include <android/asset_manager_jni.h>
#include <jni.h>
#include <string>
#include <vector>
#include <cstring>

// ncnn
#include "net.h"
#include "retinaface.hpp"
#include "recognize.h"

using namespace Face;

#define TAG "NCNN_FACE"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define NCNN_MOBILEFACENET(sig) Java_com_aqinn_facerecognizencnn_FaceRecognize_##sig
#define NCNN_RETINAFACE(sig) Java_com_aqinn_facerecognizencnn_FaceRecognize_##sig
static ncnn::Net retinaface;
static Recognize *mRecognize;

//sdk是否初始化成功
bool detection_sdk_init_ok = false;

extern "C" {

// **************************************** 人脸检测 ***********************************************

JNIEXPORT jint JNICALL
NCNN_RETINAFACE(initRetainFace)(JNIEnv *env, jobject thiz, jobject assetManager) {
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    //init param
    int ret = retinaface.load_param(mgr, "mnet.25-opt.param");
    if (ret != 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "RetinaFace", "load_param failed");
        return JNI_FALSE;
    }
    //init bin
    ret = retinaface.load_model(mgr, "mnet.25-opt.bin");
    if (ret != 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "RetinaFace", "load_model failed");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

JNIEXPORT jfloatArray JNICALL
NCNN_RETINAFACE(detectFromBitmap)(JNIEnv *env, jobject thiz, jobject bitmap) {

    ncnn::Extractor ex = retinaface.create_extractor();
    ncnn::Mat in = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_BGR);
    LOGD("detectFromBitmap c h w : %d %d %d", in.c, in.h, in.w);
    std::vector<FaceObject> objs = detect_retinaface(retinaface, in);
    int count = static_cast<int>(objs.size()), ix = 0;
    if (count <= 0)
        return nullptr;
    //result to 1D-array
    count = static_cast<int>(count * 14);
    float *face_info = new float[count];
    for (auto obj : objs) {
        face_info[ix++] = obj.rect.x;
        face_info[ix++] = obj.rect.y;
        face_info[ix++] = obj.rect.x + obj.rect.width;
        face_info[ix++] = obj.rect.y + obj.rect.height;
        for (int j = 0; j < 5; j++) {
            face_info[ix++] = obj.landmark[j].x;
            face_info[ix++] = obj.landmark[j].y;
        }
    }
    jfloatArray tFaceInfo = env->NewFloatArray(count);
    env->SetFloatArrayRegion(tFaceInfo, 0, count, face_info);
    delete[] face_info;
    return tFaceInfo;
}

JNIEXPORT jobjectArray JNICALL
NCNN_RETINAFACE(detectMultiFaceFromBitmap)(JNIEnv *env, jobject thiz, jobject bitmap) {

    ncnn::Extractor ex = retinaface.create_extractor();
    ncnn::Mat in = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_BGR);
    LOGD("detectMultiFaceFromBitmap c h w : %d %d %d", in.c, in.h, in.w);
    std::vector<FaceObject> objs = detect_retinaface(retinaface, in);
    int count = static_cast<int>(objs.size()), ix = 0;
    if (count <= 0)
        return nullptr;
    //result to 1D-array
    count = static_cast<int>(count * 14);
    jclass cls1dArr = env->FindClass("[F");
    jobjectArray face_infos = env->NewObjectArray(objs.size(), cls1dArr, NULL);
    float *face_info = new float[count];
    int i = 0;
    for (auto obj : objs) {
        ix = 0;
        face_info[ix++] = obj.rect.x;
        face_info[ix++] = obj.rect.y;
        face_info[ix++] = obj.rect.x + obj.rect.width;
        face_info[ix++] = obj.rect.y + obj.rect.height;
        for (int j = 0; j < 5; j++) {
            face_info[ix++] = obj.landmark[j].x;
            face_info[ix++] = obj.landmark[j].y;
        }
        jfloatArray tFaceInfo = env->NewFloatArray(count);
        env->SetFloatArrayRegion(tFaceInfo, 0, 14, face_info);
        env->SetObjectArrayElement(face_infos, i++, tFaceInfo);
        env->DeleteLocalRef(tFaceInfo);
    }
    delete[] face_info;
    return face_infos;
}

JNIEXPORT jobjectArray JNICALL
NCNN_RETINAFACE(detectTest)(JNIEnv *env, jobject thiz, jobject bitmap,
                            jint w, jint h, jint ratio) {
    ncnn::Extractor ex = retinaface.create_extractor();
    ncnn::Mat in = ncnn::Mat::from_android_bitmap_resize(env, bitmap, ncnn::Mat::PIXEL_BGR,
                                                         w / ratio,
                                                         h / ratio);
    LOGD("detectMultiFaceFromBitmap c h w : %d %d %d", in.c, in.h, in.w);
    std::vector<FaceObject> objs = detect_retinaface(retinaface, in);
    int count = static_cast<int>(objs.size()), ix = 0;
    if (count <= 0)
        return nullptr;
    //result to 1D-array
    count = static_cast<int>(count * 14);
    jclass cls1dArr = env->FindClass("[F");
    jobjectArray face_infos = env->NewObjectArray(objs.size(), cls1dArr, NULL);
    float *face_info = new float[count];
    int i = 0;
    for (auto obj : objs) {
        ix = 0;
        face_info[ix++] = obj.rect.x * ratio;
        face_info[ix++] = obj.rect.y * ratio;
        face_info[ix++] = obj.rect.x * ratio + obj.rect.width * ratio;
        face_info[ix++] = obj.rect.y * ratio + obj.rect.height * ratio;
        for (int j = 0; j < 5; j++) {
            face_info[ix++] = obj.landmark[j].x * ratio;
            face_info[ix++] = obj.landmark[j].y * ratio;
        }
        jfloatArray tFaceInfo = env->NewFloatArray(count);
        env->SetFloatArrayRegion(tFaceInfo, 0, 14, face_info);
        env->SetObjectArrayElement(face_infos, i++, tFaceInfo);
        env->DeleteLocalRef(tFaceInfo);
    }
    delete[] face_info;
    return face_infos;
}

JNIEXPORT jfloatArray JNICALL
NCNN_RETINAFACE(detectFromStream)(JNIEnv *env, jobject thiz,
                                  jbyteArray yuv420sp, jint width,
                                  jint height, jint view_width,
                                  jint view_height, jint rotate) {
    unsigned char *yuvData = new unsigned char[height * width * 3 / 2];
    jbyte *yuvDataRef = env->GetByteArrayElements(yuv420sp, 0);
    ncnn::kanna_rotate_yuv420sp((const unsigned char *) yuvDataRef, (int) width,
                                (int) height, (unsigned char *) yuvData, (int) height,
                                (int) width, (int) rotate);
    env->ReleaseByteArrayElements(yuv420sp, yuvDataRef, 0);
    unsigned char *rgbData = new unsigned char[height * width * 4];
    ncnn::yuv420sp2rgb((const unsigned char *) yuvData, height, width,
                       (unsigned char *) rgbData);
//    ncnn::Mat in = ncnn::Mat::from_pixels(rgbData, ncnn::Mat::PIXEL_RGB2BGR, height, width);
    ncnn::Mat in = ncnn::Mat::from_pixels_resize(rgbData, ncnn::Mat::PIXEL_RGB2BGR, height, width,
                                                 view_width, view_height);
    ncnn::Extractor ex = retinaface.create_extractor();
    std::vector<FaceObject> objs = detect_retinaface(retinaface, in);
    int count = static_cast<int>(objs.size()), ix = 0;
    if (count <= 0)
        return nullptr;
    //result to 1D-array
    count = static_cast<int>(count * 14);

    float *face_info = new float[count];

    LOGD("11");

    for (auto obj : objs) {

        LOGD("12");

        face_info[ix++] = obj.rect.x;
        face_info[ix++] = obj.rect.y;
        face_info[ix++] = obj.rect.x + obj.rect.width;
        face_info[ix++] = obj.rect.y + obj.rect.height;
        for (int j = 0; j < 5; j++) {
            face_info[ix++] = obj.landmark[j].x;
            face_info[ix++] = obj.landmark[j].y;
        }

        LOGD("13");
    }
    jfloatArray tFaceInfo = env->NewFloatArray(count);
    env->SetFloatArrayRegion(tFaceInfo, 0, count, face_info);

    LOGD("14");

    delete[] face_info;
    return tFaceInfo;
}

JNIEXPORT void JNICALL
NCNN_RETINAFACE(deinitRetainFace)(JNIEnv *env, jobject thiz) {
    retinaface.clear();
}

// **************************************** 人脸识别 ***********************************************

JNIEXPORT JNICALL jboolean
NCNN_MOBILEFACENET(initMobileFacenet)(JNIEnv *env, jobject instance,
                                      jstring modelPath_) {
    LOGD("JNI开始人脸识别模型初始化");
    //如果已初始化则直接返回
    if (detection_sdk_init_ok) {
        //  LOGD("人脸检测模型已经导入");
        return true;
    }
    jboolean tRet = false;
    if (NULL == modelPath_) {
        //   LOGD("导入的人脸检测的目录为空");
        return tRet;
    }
    //获取模型的绝对路径的目录（不是/aaa/bbb.bin这样的路径，是/aaa/)
    const char *faceDetectionModelPath = env->GetStringUTFChars(modelPath_, 0);
    if (NULL == faceDetectionModelPath) {
        return tRet;
    }
    std::string tFaceModelDir = faceDetectionModelPath;
    //没判断是否正确导入，懒得改了
    mRecognize = new Recognize(tFaceModelDir);
    mRecognize->SetThreadNum(3);
    detection_sdk_init_ok = true;
    tRet = true;
    return tRet;
}


// 人脸识别
JNIEXPORT jfloatArray JNICALL
NCNN_MOBILEFACENET(recognize)(JNIEnv *env, jobject instance,
                              jbyteArray faceData_, jint w, jint h, jintArray landmarks) {
    LOGD("进入人脸识别模型");
    double similar = 0;

    jbyte *faceData = env->GetByteArrayElements(faceData_, NULL);

    unsigned char *faceImageCharData = (unsigned char *) faceData;

    jint *detect_landmarks = env->GetIntArrayElements(landmarks, NULL);

    int *detectLandmarks = (int *) detect_landmarks;

    ncnn::Mat ncnn_img = ncnn::Mat::from_pixels(faceImageCharData, ncnn::Mat::PIXEL_RGBA2RGB, w,
                                                h);

    //人脸对齐
    ncnn::Mat det = mRecognize->preprocess(ncnn_img, detectLandmarks);

    std::vector<float> feature;
    mRecognize->start(det, feature);

    env->ReleaseByteArrayElements(faceData_, faceData, 0);
    env->ReleaseIntArrayElements(landmarks, detect_landmarks, 0);

    // 转换成 float 数组
    float *feature_float = new float[feature.size()];
    for (int i = 0; i < feature.size(); ++i) {
        feature_float[i] = feature[i];
    }

    jfloatArray result;
    result = env->NewFloatArray(128);

    env->SetFloatArrayRegion(result, 0, 128, feature_float);

    return result;
}

JNIEXPORT jdouble JNICALL
NCNN_MOBILEFACENET(compare)(JNIEnv *env, jobject instance, jfloatArray feature1,
                            jfloatArray feature2) {
    jfloat *featureData1 = (jfloat *) env->GetFloatArrayElements(feature1, 0);
    jsize featureSize1 = env->GetArrayLength(feature1);
    jfloat *featureData2 = (jfloat *) env->GetFloatArrayElements(feature2, 0);
    jsize featureSize2 = env->GetArrayLength(feature2);
    std::vector<float> featureVector1(featureSize1), featureVector2(featureSize1);
    if (featureSize1 != featureSize2) {
        return 0;
    }
    for (int i = 0; i < featureSize1; i++) {
        featureVector1.push_back(featureData1[i]);
        featureVector2.push_back(featureData2[i]);
    }
    double similar = 0;
    similar = calculSimilar(featureVector1, featureVector2, 1);
    return similar;
}


JNIEXPORT void JNICALL
NCNN_MOBILEFACENET(deinitMobileFacenet)(JNIEnv *env, jobject instance) {
    mRecognize = nullptr;
}


}