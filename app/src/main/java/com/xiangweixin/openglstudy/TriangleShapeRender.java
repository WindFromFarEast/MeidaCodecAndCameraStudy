package com.xiangweixin.openglstudy;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TriangleShapeRender extends ViewGLRender {

    private static final String VERTEX_SHADER_FILE = "triangle_vertex_shader.glsl";
    private static final String FRAGMENT_SHADER_FILE = "triangle_fragment_shader.glsl";

    private static final String A_POSITION = "aPosition";
    private static final String U_COLOR = "uColor";

    //三角形的三个顶点坐标
    private static float TRIANGLE_COORDS[] = {
            //X,Y,Z
            0.5f, 0.5f, 0.0f, //top
            -0.5f, -0.5f, 0.0f, //bottom left
            0.5f, -0.5f, 0.0f, //bottom right
    };

    //在三角形的顶点坐标数组中，一个顶点需要三个值(x,y,z)来描述其位置，所以需要3个偏移量
    private static final int COORDS_PER_VERTEX = 3;
    private static final int COORDS_PER_COLOR = 0;
    //在数组中，描述一个顶点，总共的顶点需要的偏移量。这里因为只有位置顶点，所以和上面的值一样
    private static final int TOTAL_COMPONENT_COUNT = COORDS_PER_VERTEX + COORDS_PER_COLOR;
    //一个点需要的byte偏移量。
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * Constants.BYTES_PER_FLOAT;

    private Context mContext;
    private final FloatBuffer mVertexFloatBuffer;
    int mProgramObjectId;

    private static float TRIANGLE_COLOR[] = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final int VERTEX_COUNT = TRIANGLE_COORDS.length / TOTAL_COMPONENT_COUNT;

    private static final String U_MATRIX = "u_Matrix";
    private int uMatrix;

    public TriangleShapeRender(Context context) {
        this.mContext = context;
        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(TRIANGLE_COORDS.length * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TRIANGLE_COORDS);
        mVertexFloatBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);
        //先从Assets中获取到Shader Code
        String vertexShaderCode = GLESUtils.readAssetShaderCode(mContext, VERTEX_SHADER_FILE);
        String fragmentShaderCode = GLESUtils.readAssetShaderCode(mContext, FRAGMENT_SHADER_FILE);
        //编译Shader
        int vertexShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        //创建Program
        mProgramObjectId = GLES20.glCreateProgram();
        //将Shader和Program绑定
        GLES20.glAttachShader(mProgramObjectId, vertexShaderObjectId);
        GLES20.glAttachShader(mProgramObjectId, fragmentShaderObjectId);
        //Link Program
        GLES20.glLinkProgram(mProgramObjectId);
        GLES20.glUseProgram(mProgramObjectId);
        //获取我们定义的Position变量的handle
        int aPosition = GLES20.glGetAttribLocation(mProgramObjectId, A_POSITION);
        mVertexFloatBuffer.position(0);
        //将坐标数据传入我们自定义的Position
        GLES20.glVertexAttribPointer(aPosition,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                STRIDE,
                mVertexFloatBuffer);
        //启用我们自定义的Position
        GLES20.glEnableVertexAttribArray(aPosition);
        //获取我们定义的Color变量的handle
        int uColor = GLES20.glGetUniformLocation(mProgramObjectId, U_COLOR);
        //将颜色数传入我们定义的Color
        GLES20.glUniform4fv(uColor, 1, TRIANGLE_COLOR, 0);

        uMatrix = GLES20.glGetUniformLocation(mProgramObjectId, U_MATRIX);
    }

    //投影矩阵
    private float[] mProjectionMatrix = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        super.onSurfaceChanged(gl10, width, height);
        //进行缩放
        float aspectRatio = width > height ? (float) width / (float) height : (float) height / (float) width;
        if (width > height) {
            //横屏
            Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1, 1f, -1.f, 1f);
        } else {
            //竖屏
            Matrix.orthoM(mProjectionMatrix, 0, -1, 1f, -aspectRatio, aspectRatio, -1.f, 1f);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        super.onDrawFrame(gl10);
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mProjectionMatrix, 0);
        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
    }

}
