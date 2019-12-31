package com.xiangweixin.openglstudy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Texture2DShapeRender extends ViewGLRender {

    private static final float COORDS[] = {
            //X,Y
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    private static final float TEX_COORDS[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    private static final int COORDS_PER_VERTEX = 2;
    private static final int COORDS_PER_ST = 2;
    private static final int TOTAL_COMPONENT_COUNT = COORDS_PER_VERTEX + COORDS_PER_ST;
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * Constants.BYTES_PER_FLOAT;

    private Context mContext;
    private FloatBuffer mVertexFloatBuffer;
    private FloatBuffer mTexFloatBuffer;
    private int mProgramObjectId;
    private int uMatrix;
    private int uTexture;
    private int mTextureId;

    private float[] mProjectionMatrix = new float[16];

    public Texture2DShapeRender(Context context) {
        this.mContext = context;
        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(COORDS.length * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(COORDS);
        mTexFloatBuffer = ByteBuffer
                .allocateDirect(TEX_COORDS.length * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEX_COORDS);
        mVertexFloatBuffer.position(0);
        mTexFloatBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);
        String vertexShaderCode = GLESUtils.readAssetShaderCode(mContext, "texture_vertex_shader.glsl");
        String fragmentShaderCode = GLESUtils.readAssetShaderCode(mContext, "texture_fragment_shader.glsl");
        int vertexShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgramObjectId = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgramObjectId, vertexShaderObjectId);
        GLES20.glAttachShader(mProgramObjectId, fragmentShaderObjectId);
        GLES20.glLinkProgram(mProgramObjectId);
        GLES20.glUseProgram(mProgramObjectId);

        int aPosition = GLES20.glGetAttribLocation(mProgramObjectId, "a_Position");
        GLES20.glVertexAttribPointer(aPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 8, mVertexFloatBuffer);
        GLES20.glEnableVertexAttribArray(aPosition);
        int aCoordinate = GLES20.glGetAttribLocation(mProgramObjectId, "a_TextureCoordinates");
        GLES20.glVertexAttribPointer(aCoordinate, COORDS_PER_ST, GLES20.GL_FLOAT, false, 8, mTexFloatBuffer);
        GLES20.glEnableVertexAttribArray(aCoordinate);

        uMatrix = GLES20.glGetUniformLocation(mProgramObjectId, "u_Matrix");
        uTexture = GLES20.glGetUniformLocation(mProgramObjectId, "u_TextureUnit");

        mTextureId = createTexture();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        super.onSurfaceChanged(gl10, width, height);
        float aspectRatio = width > height ? (float) width / height : (float) height / width;
        if (width > height) {
            //横屏。需要设置的就是左右。
            Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1, 1f, -1.f, 1f);
        } else {
            //竖屏。需要设置的就是上下
            Matrix.orthoM(mProjectionMatrix, 0, -1, 1f, -aspectRatio, aspectRatio, -1.f, 1f);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        super.onDrawFrame(gl10);
        //传递给着色器
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mProjectionMatrix, 0);
        //激活和重新绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        //设置纹理坐标
        GLES20.glUniform1i(uTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    //使用mip贴图生成纹理，相当于把图片复制到OpenGL里面
    private int createTexture() {
        //生成Bitmap
        final Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.lenna, options);
        //生成纹理
        int[] textureObjectId = new int[1];
        if (bitmap != null && !bitmap.isRecycled()) {
            //生成一个纹理
            GLES20.glGenTextures(1, textureObjectId, 0);
            //将生成的纹理绑定
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectId[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            //因为已经复制成功了，所以为了防止修改先unbind
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            return textureObjectId[0];
        }
        return 0;
    }

}
