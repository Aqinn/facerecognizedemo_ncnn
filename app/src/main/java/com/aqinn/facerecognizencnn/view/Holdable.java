package com.aqinn.facerecognizencnn.view;

import android.view.SurfaceHolder;

/**
 * @author Aqinn
 * @date 2021/1/6 12:31 PM
 */
public interface Holdable {

    void openCamera();

    void startPreview(SurfaceHolder surfaceHolder);

    void closeCamera();

}
