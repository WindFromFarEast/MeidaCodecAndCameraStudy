package com.xiangweixin.openglstudy.camera;

import android.graphics.SurfaceTexture;

public interface ICamera {
    boolean open(int cameraId);

    void setAspectRatio(AspectRatio aspectRatio);

    boolean preview();

    boolean close();

    void setPreviewTexture(SurfaceTexture surfaceTexture);

    ISize getPreviewSize();

    ISize getPictureSize();

    void takePhoto(TakePhotoCallback callback);

    void setOnPreviewFrameCallback(PreviewFrameCallback callback);

    interface TakePhotoCallback {
        void onTakePhoto(byte[] bytes, int width, int height);
    }

    interface PreviewFrameCallback {
        void onPreviewFrame(byte[] bytes, int width, int height);
    }
}
