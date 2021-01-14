package com.aqinn.facerecognizencnn.view;

import android.util.Log;
import android.view.SurfaceHolder;

public class MySurfaceHolder implements SurfaceHolder.Callback {
    private static final String TAG = "MySurfaceHolder";

    private Holdable mHoldable;

    private SurfaceHolder mHolder;



    public MySurfaceHolder(Holdable holdable) {
        mHoldable = holdable;
    }

        /**
         * @param surfaceHolder SurfaceView的holder
         */
    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = surfaceHolder;
        mHolder.addCallback(this);
    }


    /********************************
     * SurfaceHolder.Callback function start
     *********************************/

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        Log.i(TAG, "surfaceCreated");
        mHoldable.openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        mHoldable.startPreview(surfaceHolder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        holder.removeCallback(this);

        mHoldable.closeCamera();
    }

    /********************************
     * SurfaceHolder.Callback function end
     *********************************/

}
