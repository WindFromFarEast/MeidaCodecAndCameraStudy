package com.xiangweixin.openglstudy.SurfaceTexture;

import android.content.Context;
import android.graphics.SurfaceTexture;

import java.nio.FloatBuffer;

public class CameraFboRender implements EGLSurfaceView.Render, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "CameraFboRender";

    private static float vertexData[] = {
            -1f, -1f, 0f,
            1f, -1f, 0f,
            -1f, 1f, 0f,
            1f, 1f, 0f,
    };

    private static float textureData[] = {
            0f, 1f, 0f,
            1f, 1f, 0f,
            0f, 0f, 0f,
            1f, 0f, 0f
    };

    private static final int COOORDS_PER_VERTEX = 3;

    private final int vertexCount = vertexData.length / COOORDS_PER_VERTEX;
    private static final int vertexStride = COOORDS_PER_VERTEX * 4; // 4 bytes every float.

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private int program;
    private int avPosition;
    private int afPosition;
    private int uMatrix;
    private int fboId;
    private int fboTextureId;
    private int cameraRenderTextureId;
    private int vboId;

    private float[] matrix = new float[16];

    private int screenX, screenH;
    private Context context;
    private SurfaceTexture surfaceTexture;

    private

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onSurfaceCreated() {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {

    }

    @Override
    public void onDrawFrame() {

    }

}
