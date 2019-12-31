package com.xiangweixin.openglstudy.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private ICamera mCameraApi;
    private int mCameraIdDefault = 0;
    private CameraDrawer mCameraDrawer;
    private int width;
    private int height;

    public CameraView(Context context) {
        super(context);
        initEGL();
        initCameraApi(context);
    }

    private void initEGL() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);//只有调用requestRender才会重绘
    }

    private void initCameraApi(Context context) {
        mCameraApi = new CameraApi14();
        mCameraDrawer = new CameraDrawer(context.getResources());
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraDrawer.onSurfaceCreated(gl, config);
        mCameraApi.open(mCameraIdDefault);
        mCameraDrawer.setCameraId(mCameraIdDefault);

        ISize previewSize = mCameraApi.getPreviewSize();
        int previewSizeWidth = previewSize.getWidth();
        int previewSizeHeight = previewSize.getHeight();

        mCameraDrawer.setPreviewSize(previewSizeWidth, previewSizeHeight);

        mCameraApi.setPreviewTexture(mCameraDrawer.getSurfaceTexture());
        //默认使用的GLThread每次刷新的时候，都强制要求刷新这个GLSurfaceView
        mCameraDrawer.getSurfaceTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                requestRender();
            }
        });

        mCameraApi.preview();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraDrawer.onSurfaceChanged(gl, width, height);
        GLES20.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraDrawer.onDrawFrame(gl);
    }

}
