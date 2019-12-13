package com.xiangweixin.openglstudy;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private boolean isRenderSet = false;

    private ViewGLRender mRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGLSurfaceView();
    }

    private void initGLSurfaceView() {
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        mRender = new Texture2DShapeRender(this);
        glSurfaceView.setRenderer(mRender);
        isRenderSet = true;
        setContentView(glSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRenderSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRenderSet) {
            glSurfaceView.onResume();
        }
    }



}
