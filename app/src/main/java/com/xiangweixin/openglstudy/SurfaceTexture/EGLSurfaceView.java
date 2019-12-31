package com.xiangweixin.openglstudy.SurfaceTexture;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

import static android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY;

public class EGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private Render mRender;
    private Surface mSurface;
    private EGLContext mEGLContext;

    private EGLThread mEGLThread;

    private int mRenderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY;

    public EGLSurfaceView(Context context) {
        super(context);
    }

    public EGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mSurface == null) {
            mSurface = holder.getSurface();
        }
        mEGLThread = new EGLThread(new WeakReference<>(this));
        mEGLThread.isCreate = true;
        mEGLThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mEGLThread.width = width;
        mEGLThread.height = height;
        mEGLThread.isChange = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mEGLThread.onDestroy();
        mEGLThread = null;
        mSurface = null;
        mEGLContext = null;
    }

    public void setRender(Render render) {
        mRender = render;
    }

    public void setRenderMode(int renderMode) {
        this.mRenderMode = renderMode;
    }

    public void requestRender() {
        mEGLThread.requestRender();
    }

    public EGLContext getEGLContext() {
        return mEGLThread.getEGLContext();
    }

    /**
     * EGLSurfaceView内部运行的GL线程
     */
    private static class EGLThread extends Thread {

        private WeakReference<EGLSurfaceView> mEGLSurfaceViewWeakRef;
        private EGLHelper mEGLHelper;

        private int width;
        private int height;

        private boolean isCreate;
        private boolean isChange;
        private boolean isStart;
        private boolean isExit;

        private Object object;

        private EGLThread(WeakReference<EGLSurfaceView> eglSurfaceViewWeakReference) {
            this.mEGLSurfaceViewWeakRef = eglSurfaceViewWeakReference;
        }

        @Override
        public void run() {
            super.run();
            try {
                guardedRun();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void guardedRun() throws InterruptedException {
            isExit = false;
            isStart = false;
            object = new Object();
            mEGLHelper = new EGLHelper();
            //初始化EGL环境，绑定当前线程为GL线程
            mEGLHelper.initEGL(mEGLSurfaceViewWeakRef.get().mSurface, mEGLSurfaceViewWeakRef.get().mEGLContext);

            while (true) {
                if (isExit) {
                    release();
                    break;
                }

                if (isStart) {
                    if (mEGLSurfaceViewWeakRef.get().mRenderMode == GLSurfaceView.RENDERMODE_WHEN_DIRTY) {
                        synchronized (object) {
                            object.wait();
                        }
                    } else if (mEGLSurfaceViewWeakRef.get().mRenderMode == RENDERMODE_CONTINUOUSLY){
                        Thread.sleep(1000 / 60);
                    } else {
                        throw new IllegalArgumentException("render Mode!");
                    }
                }

                onCreate();
                onChange(width, height);
                onDraw();
                isStart = true;
            }
        }

        private void onCreate() {
            if (!isCreate || mEGLSurfaceViewWeakRef.get().mRender == null) {
                return;
            }
            isCreate = false;
            mEGLSurfaceViewWeakRef.get().mRender.onSurfaceCreated();
        }

        private void onChange(int width, int height) {
            if (!isChange || mEGLSurfaceViewWeakRef.get().mRender == null) {
                return;
            }
            isChange = false;
            mEGLSurfaceViewWeakRef.get().mRender.onSurfaceChanged(width, height);
        }

        private void onDraw() {
            if (mEGLSurfaceViewWeakRef.get().mRender == null) {
                return;
            }
            mEGLSurfaceViewWeakRef.get().mRender.onDrawFrame();
            if (!isStart) {
                mEGLSurfaceViewWeakRef.get().mRender.onDrawFrame();
            }
            mEGLHelper.swapBuffers();
        }

        private void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        private void release() {
            isExit = true;
            //释放锁
            requestRender();
        }

        void onDestroy() {
            if (mEGLHelper != null) {
                mEGLHelper.destroyEGL();
                mEGLHelper = null;
                object = null;
                mEGLSurfaceViewWeakRef = null;
            }
        }

        EGLContext getEGLContext() {
            return mEGLHelper.getEGLContext();
        }

    }

    public interface Render {
        void onSurfaceCreated();
        void onSurfaceChanged(int width, int height);
        void onDrawFrame();
    }

}
