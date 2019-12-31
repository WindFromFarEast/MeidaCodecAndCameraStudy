package com.xiangweixin.camerastydy;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.xiangweixin.openglstudy.R;

import java.io.IOException;
import java.util.List;

/**
 * 前后置Camera Retry尝试
 */
public class CameraRetryActivity extends AppCompatActivity {

    private static final String TAG = "CameraRetry";

    private SurfaceView surfaceView;
    private Camera mCamera;
    private int availableCameraCount = 0;

    private int mCurrentCamId = -1;
    private int mBackCameraId = -1;
    private int mFrontCameraId = -1;
    private int mBackCamOri;
    private int mFrontCamOri;

    private Camera.Parameters mCamParams;
    private SurfaceHolder mHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_retry);
        surfaceView = findViewById(R.id.surfaceview);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCamera();
                openCameraAndPreview(mBackCameraId);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void initCamera() {
        availableCameraCount = Camera.getNumberOfCameras();
        Log.i(TAG, "Available Camera Count" + availableCameraCount);
        for (int i = 0; i < availableCameraCount; i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mBackCameraId = i;
                mBackCamOri = cameraInfo.orientation;
                Log.i(TAG, "back camera id: " + mBackCameraId + ", orientation: " + mBackCamOri);
            }
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraId = i;
                mFrontCamOri = cameraInfo.orientation;
                Log.i(TAG, "front camera id: " + mFrontCameraId + ", orientation: " + mFrontCamOri);
            }
        }
    }

    private void openCameraAndPreview(int id) {
        mCamera = Camera.open(id);
        mCurrentCamId = id;

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCurrentCamId, cameraInfo);
        int cameraRotationOffset = cameraInfo.orientation;

        mCamParams = mCamera.getParameters();
        mCamParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        int rotation = ((WindowManager) (getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }
        int displayRotation;
        //根据前置和后置，设置预览方向
        if (mCurrentCamId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            displayRotation = (cameraRotationOffset - degrees + 360) % 360;
        } else {
            displayRotation = (cameraRotationOffset + degrees) % 360;
            displayRotation = (360 - displayRotation) % 360; // compensate
        }
        mCamera.setDisplayOrientation(displayRotation);

        List<Camera.Size> previewSize = mCamParams.getSupportedPreviewSizes();
        mCamParams.setPreviewSize(previewSize.get(0).width, previewSize.get(0).height);
        Log.d(TAG, "preview width: " + previewSize.get(0).width + ", preview height: " + previewSize.get(0).height);

        mCamera.setParameters(mCamParams);
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    private void closeCamera() {
        mCamera.stopPreview();
    }

    private void findBestPreviewSize(int[] size) {
        if (size.length != 2) {
            return;
        }
        List<Camera.Size> previewSizes = mCamParams.getSupportedPreviewSizes();

    }

}
