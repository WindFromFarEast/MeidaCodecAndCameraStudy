package com.xiangweixin.openglstudy.camera;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraDrawer implements GLSurfaceView.Renderer {

    private int mCameraId;

    private SurfaceTexture mSurfaceTexture;

    private int mTextureId;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private int mPreviewWidth;
    private int mPreviewHeight;

    //视图矩阵 控制旋转和变化
    private float[] mModelMatrix = new float[16];

    private OesFilter mOesFilter;

    public CameraDrawer(Resources res) {
        mOesFilter = new OesFilter(res);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mTextureId = genOesTextureId();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.mSurfaceWidth = width;
        this.mSurfaceHeight = height;
        calculateMatrix();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }
        mOesFilter.draw();
    }

    private int genOesTextureId() {
        int[] textureObjectId = new int[1];
        GLES20.glGenTextures(1, textureObjectId, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureObjectId[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return textureObjectId[0];
    }

    private void calculateMatrix() {
        Gl2Utils.getShowMatrix(mModelMatrix, mPreviewWidth, mPreviewHeight, mSurfaceWidth, mSurfaceHeight);
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Gl2Utils.flip(mModelMatrix, true, false);
            Gl2Utils.rotate(mModelMatrix, 90);
        } else {
            int rotataAngle = 270;
            Gl2Utils.rotate(mModelMatrix, rotataAngle);
        }
        mOesFilter.setMatrix(mModelMatrix);
    }

    public void setCameraId(int cameraId) {
        this.mCameraId = cameraId;
    }

    public void setPreviewSize(int previewWidth, int previewHeight) {
        this.mPreviewWidth = previewWidth;
        this.mPreviewHeight = previewHeight;
        calculateMatrix();
    }

}
