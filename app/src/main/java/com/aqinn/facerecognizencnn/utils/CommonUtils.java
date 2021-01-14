package com.aqinn.facerecognizencnn.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.aqinn.facerecognizencnn.FaceInfo;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * @author Aqinn
 * @date 2021/1/6 2:42 PM
 */
public class CommonUtils {

    public static String showArr(float arr[]) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i] + "f,");
        }
        return sb.toString();
    }

    //提取像素点
    public static byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the
        return temp;
    }

    public static int[] getUsefulLandmarksFromFaceInfo(FaceInfo faceInfo){
        int arr[] = new int[10];
        arr[0] = (int)faceInfo.keypoints[0][0];
        arr[1] = (int)faceInfo.keypoints[1][0];
        arr[2] = (int)faceInfo.keypoints[2][0];
        arr[3] = (int)faceInfo.keypoints[3][0];
        arr[4] = (int)faceInfo.keypoints[4][0];
        arr[5] = (int)faceInfo.keypoints[0][1];
        arr[6] = (int)faceInfo.keypoints[1][1];
        arr[7] = (int)faceInfo.keypoints[2][1];
        arr[8] = (int)faceInfo.keypoints[3][1];
        arr[9] = (int)faceInfo.keypoints[4][1];
        return arr;
    }

    public static int[] floatArr2IntArr(float f[]) {
        int arr[] = new int[f.length];
        for (int i = 0; i < f.length; i++) {
            arr[i] = (int) f[i];
        }
        return arr;
    }

    public static FaceInfo floatArr2FaceInfo(float arr[]) {
        FaceInfo faceInfo = new FaceInfo();
        try {
            faceInfo.x1 = arr[0];
            faceInfo.y1 = arr[1];
            faceInfo.x2 = arr[2];
            faceInfo.y2 = arr[3];
            faceInfo.keypoints = new float[5][2];
            faceInfo.keypoints[0][0] = arr[4];
            faceInfo.keypoints[0][1] = arr[5];
            faceInfo.keypoints[1][0] = arr[6];
            faceInfo.keypoints[1][1] = arr[7];
            faceInfo.keypoints[2][0] = arr[8];
            faceInfo.keypoints[2][1] = arr[9];
            faceInfo.keypoints[3][0] = arr[10];
            faceInfo.keypoints[3][1] = arr[11];
            faceInfo.keypoints[4][0] = arr[12];
            faceInfo.keypoints[4][1] = arr[13];
            faceInfo.landmarks = null;
            faceInfo.score = 0;
        } catch (Exception e) {
            faceInfo = null;
        }
        return faceInfo;
    }

    /**把byte字节流转成bitmap
     * @param bytes
     */
    public static Bitmap byteToBitmap(byte[] bytes) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = false;//为true时，返回的bitmap为null
//        opts.outConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        return bitmap;
    }

    /**把bitmap转成byte字节流
     * @param bm
     */
    public static byte[] bitmapToByte(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


}
