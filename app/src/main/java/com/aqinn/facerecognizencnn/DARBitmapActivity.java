package com.aqinn.facerecognizencnn;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aqinn.facerecognizencnn.utils.CameraUtils;
import com.aqinn.facerecognizencnn.utils.CommonUtils;
import com.aqinn.facerecognizencnn.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class DARBitmapActivity extends Activity {

    private static final String TAG = "DARBitmapActivity";

    private static final int SELECT_IMAGE1 = 1, SELECT_IMAGE2 = 2;
    private ImageView imageView1, imageView2;
    private Bitmap yourSelectedImage1 = null, yourSelectedImage2 = null;
    private Bitmap faceImage1 = null, faceImage2 = null;
    TextView faceInfo1, faceInfo2, cmpResult;       //显示face 检测的结果和compare的结果
    private byte[] imageDate1, imageDate2;
    private int mtcnn_landmarks1[] = new int[10];  //存放mtcnn人脸关键点
    private int mtcnn_landmarks2[] = new int[10];

    // 初始参数设置，可以按需修改
    private int minFaceSize = 40;
    private int testTimeCount = 1;
    private int threadsNumber = 2;
    private double threshold = 0.5;            // 人脸余弦距离的阈值

    private FaceRecognize mFaceRecognize = new FaceRecognize();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    public void verifyStoragePermissions() {
        askForPermission();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_darbitmap);
        verifyStoragePermissions();

        initModel();

        //左边的图片
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        faceInfo1 = (TextView) findViewById(R.id.faceInfo1);
        Button buttonImage1 = (Button) findViewById(R.id.select1);
        buttonImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE1);
            }
        });
        //第一张图片人脸检测
        Button buttonDetect1 = (Button) findViewById(R.id.detect1);
        buttonDetect1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (yourSelectedImage1 == null)
                    return;

                //人脸检测
                faceImage1 = null;
                int width = yourSelectedImage1.getWidth();
                int height = yourSelectedImage1.getHeight();
                imageDate1 = getPixelsRGBA(yourSelectedImage1);

                long timeDetectFace = System.currentTimeMillis();   //检测起始时间
                float tempArr[] = mFaceRecognize.detectFromBitmap(yourSelectedImage1); //只检测最大人脸，速度有较大提升
                FaceInfo faceInfo = CommonUtils.floatArr2FaceInfo(tempArr);
                timeDetectFace = System.currentTimeMillis() - timeDetectFace; //人脸检测时间

                mtcnn_landmarks1 = CommonUtils.getUsefulLandmarksFromFaceInfo(faceInfo);

                if (faceInfo != null) {       //检测到人脸
                    faceInfo1.setText("pic1 detect time:" + timeDetectFace);
                    imageView1.setImageBitmap(CameraUtils.drawFaceRegion(yourSelectedImage1, faceInfo));
                    faceImage1 = Bitmap.createBitmap(yourSelectedImage1, (int) faceInfo.x1, (int) faceInfo.y1, (int) (faceInfo.x2 - faceInfo.x1), (int) (faceInfo.y2 - faceInfo.y1));
//                    imageView1.setImageBitmap(faceImage1);
                } else {     //没有人脸
                    faceInfo1.setText("no face");
                }
            }
        });

        //右边的图片
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        faceInfo2 = (TextView) findViewById(R.id.faceInfo2);
        Button buttonImage2 = (Button) findViewById(R.id.select2);
        buttonImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE2);
            }
        });

        Button buttonDetect2 = (Button) findViewById(R.id.detect2);
        buttonDetect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (yourSelectedImage2 == null)
                    return;

                //人脸检测
                faceImage2 = null;
                int width = yourSelectedImage2.getWidth();
                int height = yourSelectedImage2.getHeight();
                imageDate2 = getPixelsRGBA(yourSelectedImage2);

                long timeDetectFace = System.currentTimeMillis();   //检测起始时间
                float tempArr[] = mFaceRecognize.detectFromBitmap(yourSelectedImage2); //只检测最大人脸，速度有较大提升
                FaceInfo faceInfo = CommonUtils.floatArr2FaceInfo(tempArr);
                timeDetectFace = System.currentTimeMillis() - timeDetectFace; //人脸检测时间

                mtcnn_landmarks2 = CommonUtils.getUsefulLandmarksFromFaceInfo(faceInfo);

                if (faceInfo != null) {       //检测到人脸
                    faceInfo2.setText("pic2 detect time:" + timeDetectFace);
                    imageView2.setImageBitmap(CameraUtils.drawFaceRegion(yourSelectedImage2, faceInfo));
                    faceImage2 = Bitmap.createBitmap(yourSelectedImage2, (int) faceInfo.x1, (int) faceInfo.y1, (int) (faceInfo.x2 - faceInfo.x1), (int) (faceInfo.y2 - faceInfo.y1));
//                    imageView2.setImageBitmap(faceImage2);
                } else {     //没有人脸
                    faceInfo2.setText("no face");
                }
            }
        });

        //人脸识别(compare)
        cmpResult = (TextView) findViewById(R.id.textView1);
        Button cmpImage = (Button) findViewById(R.id.facecmp);
        cmpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (faceImage1 == null || faceImage2 == null) { //检测的人脸图片为空
                    cmpResult.setText("没有检测到人脸");
                    return;
                }

                long timeRecognizeFace = System.currentTimeMillis();

                //人脸识别
                float features1[] = mFaceRecognize.recognize(imageDate1, yourSelectedImage1.getWidth(), yourSelectedImage1.getHeight(), mtcnn_landmarks1);
                float features2[] = mFaceRecognize.recognize(imageDate2, yourSelectedImage2.getWidth(), yourSelectedImage2.getHeight(), mtcnn_landmarks2);
                double similar = mFaceRecognize.compare(features1, features2);
                Log.d(TAG, "features1: " + CommonUtils.showArr(features1));
                Log.d(TAG, "features2: " + CommonUtils.showArr(features2));
                timeRecognizeFace = System.currentTimeMillis() - timeRecognizeFace;
                if (similar >= threshold) {      // 这里阈值设置
                    cmpResult.setText("余弦距离: " + similar + " 同一人脸\n" + "识别+对比时间: " + timeRecognizeFace);
                } else {
                    cmpResult.setText("余弦距离: " + similar + " 不同人脸\n" + "识别+对比时间: " + timeRecognizeFace);
                }
            }
        });
    }

    private void initModel() {
        mFaceRecognize = new FaceRecognize();
        mFaceRecognize.initRetainFace(getAssets());

        //拷贝模型到sd卡
        String sdPath = getCacheDir().getAbsolutePath() + "/facem/";
        Utils.copyFileFromAsset(this, "mobilefacenet.bin", sdPath + File.separator + "mobilefacenet.bin");
        Utils.copyFileFromAsset(this, "mobilefacenet.param", sdPath + File.separator + "mobilefacenet.param");
        //模型初始化
        mFaceRecognize.initMobileFacenet(sdPath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            try {
                if (requestCode == SELECT_IMAGE1) {
                    Bitmap bitmap = decodeUri(selectedImage);

                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                    // resize to 227x227
                    //yourSelectedImage1 = Bitmap.createScaledBitmap(rgba, 227, 227, false);
                    yourSelectedImage1 = rgba;

                    imageView1.setImageBitmap(yourSelectedImage1);
                } else if (requestCode == SELECT_IMAGE2) {
                    Bitmap bitmap = decodeUri(selectedImage);
                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    yourSelectedImage2 = rgba;
                    imageView2.setImageBitmap(yourSelectedImage2);
                }
            } catch (FileNotFoundException e) {
                Log.e("MainActivity", "FileNotFoundException");
                return;
            }
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 400;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }

    //提取像素点
    private byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the

        return temp;
    }

    private void copyBigDataToSD(String strOutFileName) throws IOException {
        Log.i(TAG, "start copy file " + strOutFileName);
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        File file = new File(sdDir.toString() + "/facem/");
        if (!file.exists()) {
            file.mkdir();
        }

        String tmpFile = sdDir.toString() + "/facem/" + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            Log.i(TAG, "file exists " + strOutFileName);
            return;
        }
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream(sdDir.toString() + "/facem/" + strOutFileName);
        myInput = this.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        Log.i(TAG, "end copy file " + strOutFileName);
    }

    public void askForPermission() {
        //检测权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "didnt get permission,ask for it!");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1024);
        }
    }
}
