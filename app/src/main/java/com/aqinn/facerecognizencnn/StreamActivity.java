package com.aqinn.facerecognizencnn;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aqinn.facerecognizencnn.utils.CommonUtils;
import com.aqinn.facerecognizencnn.utils.FileUtils;
import com.aqinn.facerecognizencnn.view.CameraSetting;
import com.aqinn.facerecognizencnn.view.DrawView;
import com.aqinn.facerecognizencnn.view.Holdable;
import com.aqinn.facerecognizencnn.view.MySurfaceHolder;

import java.io.IOException;

/**
 * @author Aqinn
 * @date 2021/1/6 12:23 PM
 */
public class StreamActivity extends AppCompatActivity implements Holdable {

    private static final String TAG = "StreamActivity";

    private SurfaceView mPreview;
    private DrawView mDrawView;
    private int mCameraWidth;
    private int mCameraHeight;
    private static final int NET_H_INPUT = 128;
    private static final int NET_W_INPUT = 128;
    Camera mOpenedCamera;
    int mOpenedCameraId = 0;
    MySurfaceHolder mDemoSurfaceHolder = null;
    private boolean mIsDetectingFace = false;

    int mCameraFacing = -1;
    int mRotate = -1;
    SurfaceHolder mSurfaceHolder;

    private FaceRecognize mFaceRecognize = new FaceRecognize();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_face_collect);
        askForPermission();
        initAllView();
        init();
    }

    private void initAllView() {
        mPreview = findViewById(R.id.live_detection_preview);
        mDrawView = findViewById(R.id.drawView);
    }

    private void init() {
        //start SurfaceHolder
        mDemoSurfaceHolder = new MySurfaceHolder(this);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        if (null != mDemoSurfaceHolder) {
            SurfaceHolder holder = mPreview.getHolder();
            holder.setKeepScreenOn(true);
            mDemoSurfaceHolder.setSurfaceHolder(holder);
        }
    }

    @Override
    public void openCamera() {
        openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    private void openCamera(int cameraFacing) {
        mIsDetectingFace = true;
        mCameraFacing = cameraFacing;
        try {
            int numberOfCameras = Camera.getNumberOfCameras();
            if (numberOfCameras < 1) {
                Log.e(TAG, "no camera device found");
            } else if (1 == numberOfCameras) {
                mOpenedCamera = Camera.open(0);
                mOpenedCameraId = 0;
            } else {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == cameraFacing) {
                        mOpenedCamera = Camera.open(i);
                        mOpenedCameraId = i;
                        break;
                    }
                }
            }
            if (mOpenedCamera == null) {
                Log.e(TAG, "can't find camera");
            } else {

                int r = CameraSetting.initCamera(getApplicationContext(), mOpenedCamera, mOpenedCameraId);
                if (r == 0) {
                    //设置摄像头朝向
                    CameraSetting.setCameraFacing(cameraFacing);

                    Camera.Parameters parameters = mOpenedCamera.getParameters();
                    mRotate = CameraSetting.getRotate(getApplicationContext(), mOpenedCameraId, mCameraFacing);
                    mCameraWidth = parameters.getPreviewSize().width;
                    mCameraHeight = parameters.getPreviewSize().height;
                    int device = 0;  // 代表用 CPU
                    int ret = mFaceRecognize.initRetainFace(getAssets());
                    if (ret == 1) {
                        mIsDetectingFace = true;
                    } else {
                        mIsDetectingFace = false;
                        Log.e(TAG, "Face detector init failed " + ret);
                    }
                } else {
                    Log.e(TAG, "Failed to init camera");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "open camera failed:" + e.getLocalizedMessage());
        }
    }

    @Override
    public void startPreview(SurfaceHolder surfaceHolder) {
        try {
            if (null != mOpenedCamera) {
                Log.i(TAG, "start preview, is previewing");
                mOpenedCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        if (mIsDetectingFace) {
                            Camera.Parameters mCameraParameters = camera.getParameters();
                            float[] res = mFaceRecognize.detectFromStream(data, mCameraWidth, mCameraHeight, mDrawView.getWidth(), mDrawView.getHeight(), mRotate);
                            FaceInfo[] faceInfoList = new FaceInfo[1];
                            FaceInfo faceInfo = CommonUtils.floatArr2FaceInfo(res);
                            if (faceInfo != null)
                                faceInfoList[0] = faceInfo;
                            Log.i(TAG, "detect from stream ret " + faceInfo);
                            mDrawView.addFaceRect(faceInfoList);
                        } else {
                            Log.i(TAG, "No face");
                        }
                    }
                });
                mOpenedCamera.setPreviewDisplay(surfaceHolder);
                mOpenedCamera.startPreview();
                mSurfaceHolder = surfaceHolder;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeCamera() {
        Log.i(TAG, "closeCamera");
        mIsDetectingFace = false;
        if (mOpenedCamera != null) {
            try {
                mOpenedCamera.stopPreview();
                mOpenedCamera.setPreviewCallback(null);
                Log.i(TAG, "stop preview, not previewing");
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "Error setting camera preview: " + e.toString());
            }
            try {
                mOpenedCamera.release();
                mOpenedCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "Error setting camera preview: " + e.toString());
            } finally {
                mOpenedCamera = null;
            }
        }
//        mFaceDetector.deinit();
    }

    public void askForPermission() {
        //检测权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "didnt get permission,ask for it!");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1024);
        }
    }
}