package com.xiangweixin.openglstudy.SurfaceTexture;

import android.opengl.EGL14;
import android.view.Surface;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class EGLHelper {

    private static final String TAG = "EGLHelper";
    private EGL10 mEGL;
    private EGLDisplay mEglDisplay;
    private EGLContext mEglContext;
    private EGLSurface mEglSurface;

    private final int mRedSize = 8;
    private final int mGreenSize = 8;
    private final int mBlueSize = 8;
    private final int mAlphaSize = 8;
    private final int mDepthSize = 8;
    private final int mStencilSize = 8;

    public void initEGL(Surface surface, EGLContext eglContext) {
        //获取EGL实例
        mEGL = (EGL10) EGLContext.getEGL();
        //得到默认的显示设备
        mEglDisplay = mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglInitialize failed.");
        }
        //初始化默认显示设备
        int[] version = new int[2];
        if (!mEGL.eglInitialize(mEglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed.");
        }
        //设置显示设备的参数属性
        int[] attrib_list = new int[] {
                EGL10.EGL_RED_SIZE, mRedSize,
                EGL10.EGL_GREEN_SIZE, mGreenSize,
                EGL10.EGL_BLUE_SIZE, mBlueSize,
                EGL10.EGL_ALPHA_SIZE, mAlphaSize,
                EGL10.EGL_DEPTH_SIZE, mDepthSize,
                EGL10.EGL_STENCIL_SIZE, mStencilSize,
                EGL10.EGL_RENDERABLE_TYPE, 4,//egl版本 2.0
                EGL10.EGL_NONE
        };

        int[] num_config = new int[1];
        if (!mEGL.eglChooseConfig(mEglDisplay, attrib_list, null, 1, num_config)) {
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        int numConfigs = num_config[0];
        if (numConfigs <= 0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }
        //从系统中获取对应属性的配置
        EGLConfig[] configs = new EGLConfig[numConfigs];
        if (!mEGL.eglChooseConfig(mEglDisplay, attrib_list, configs, configs.length, num_config)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }
        EGLConfig eglConfig = chooseConfig(mEGL, mEglDisplay, configs);
        if (eglConfig == null) {
            eglConfig = configs[0];
        }
        //创建EGLContext
        int[] contextAttr = new int[]{
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };
        if (eglContext == null) {
            mEglContext = mEGL.eglCreateContext(mEglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, contextAttr);
        } else {
            mEglContext = mEGL.eglCreateContext(mEglDisplay, eglConfig, eglContext, contextAttr);
        }
        //创建渲染的Surface
        mEglSurface = mEGL.eglCreateWindowSurface(mEglDisplay, eglConfig, surface, null);
        //绑定EGLContext和Surface到显示设备中
        if (!mEGL.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent fail");
        }
    }

    private EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                   EGLConfig[] configs) {
        for (EGLConfig config : configs) {
            int d = findConfigAttrib(egl, display, config,
                    EGL10.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttrib(egl, display, config,
                    EGL10.EGL_STENCIL_SIZE, 0);
            if ((d >= mDepthSize) && (s >= mStencilSize)) {
                int r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                        EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                        EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0);
                if ((r == mRedSize) && (g == mGreenSize)
                        && (b == mBlueSize) && (a == mAlphaSize)) {
                    return config;
                }
            }
        }
        return null;
    }

    public EGLContext getEGLContext() {
        return mEglContext;
    }

    //The last step: 刷新数据，显示渲染场景
    public boolean swapBuffers() {
        if (mEGL != null) {
            return mEGL.eglSwapBuffers(mEglDisplay, mEglSurface);
        } else {
            throw new RuntimeException("egl is null");
        }
    }

    public void destroyEGL() {
        if (mEGL != null) {
            if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
                mEGL.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                mEGL.eglDestroySurface(mEglDisplay, mEglSurface);
                mEglSurface = null;
            }
            if (mEglContext != null) {
                mEGL.eglDestroyContext(mEglDisplay, mEglContext);
                mEglContext = null;
            }
            if (mEglDisplay != null) {
                mEGL.eglTerminate(mEglDisplay);
                mEglDisplay = null;
            }
            mEGL = null;
        }
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                 EGLConfig config, int attribute, int defaultValue) {
        int[] value = new int[1];
        if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
            return value[0];
        }
        return defaultValue;
    }

}
