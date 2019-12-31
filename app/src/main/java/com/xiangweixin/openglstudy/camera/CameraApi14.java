package com.xiangweixin.openglstudy.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraApi14 implements ICamera {

    private int mCameraId;

    private Camera mCamera;

    private Camera.Parameters mCameraParameters;
    private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private final ISize.ISizeMap mPreviewSizes = new ISize.ISizeMap();
    private final ISize.ISizeMap mPictureSizes = new ISize.ISizeMap();

    //目标宽高
    private int mDesiredHeight = 1920;
    private int mDesiredWidth = 1080;
    private boolean mAutoFocus;
    public ISize mPreviewSize;
    public ISize mPicSize;
    //当前相机的宽高比
    private AspectRatio mRatio;

    private final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);

    private TakePhotoCallback photoCallback;

    public CameraApi14() {
        mRatio = AspectRatio.of(mDesiredWidth, mDesiredHeight).inverse();
    }

    @Override
    public boolean open(int cameraId) {
        if (mCamera != null) {
            releaseCamera();
        }
        mCameraId = cameraId;
        mCamera = Camera.open(mCameraId);
        if (mCamera != null) {
            mCameraParameters = mCamera.getParameters();
            mPreviewSizes.clear();
            for (Camera.Size size : mCameraParameters.getSupportedPreviewSizes()) {
                mPreviewSizes.add(new ISize(size.width, size.height));
            }
            mPictureSizes.clear();
            for (Camera.Size size : mCameraParameters.getSupportedPictureSizes()) {
                mPictureSizes.add(new ISize(size.width, size.height));
            }
            adjustParameterByAspectRatio();
            return true;
        }
        return false;
    }

    private void adjustParameterByAspectRatio() {
        SortedSet<ISize> sizes = mPreviewSizes.sizes(mRatio);
        if (sizes == null) return;//不支持
        ISize previewSize;
        mPreviewSize = new ISize(mDesiredWidth, mDesiredHeight);
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            previewSize = new ISize(mDesiredHeight, mDesiredWidth);
        } else {
            previewSize = mPreviewSize;
        }

        mPicSize = mPictureSizes.sizes(mRatio).last();

        mCameraParameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
        mCameraParameters.setPictureSize(mPicSize.getWidth(), mPicSize.getHeight());

        setAutoFocusInternal(mAutoFocus);

        mCameraParameters.setRotation(90);
        mCamera.setParameters(mCameraParameters);
        mCamera.setDisplayOrientation(90);
    }

    private boolean setAutoFocusInternal(boolean autoFocus) {
        mAutoFocus = autoFocus;
        final List<String> modes = mCameraParameters.getSupportedFocusModes();
        if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else {
            mCameraParameters.setFocusMode(modes.get(0));
        }
        return true;
    }

    @Override
    public void setAspectRatio(AspectRatio aspectRatio) {
        this.mRatio = aspectRatio;
    }

    @Override
    public boolean preview() {
        if (mCamera != null) {
            mCamera.startPreview();
            return true;
        }
        return false;
    }

    @Override
    public boolean close() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    @Override
    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ISize getPreviewSize() {
        return mPreviewSize;
    }

    @Override
    public ISize getPictureSize() {
        return mPicSize;
    }

    @Override
    public void takePhoto(TakePhotoCallback callback) {
        this.photoCallback = callback;
        if (getAutoFocus()) {
            mCamera.cancelAutoFocus();
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    takePictureInternal();
                }
            });
        } else {
            takePictureInternal();
        }
    }

    @Override
    public void setOnPreviewFrameCallback(final PreviewFrameCallback callback) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    callback.onPreviewFrame(data, mPreviewSize.getWidth(), mPreviewSize.getHeight());
                }
            });
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    void takePictureInternal() {
        if (!isPictureCaptureInProgress.getAndSet(true)) {
            mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    isPictureCaptureInProgress.set(false);
                    if (photoCallback != null) {
                        photoCallback.onTakePhoto(data, mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    }
                    camera.cancelAutoFocus();
                    camera.startPreview();
                }
            });
        }
    }

    boolean getAutoFocus() {
        String focusMode = mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }
}
