package com.xiangweixin.openglstudy;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SquareShapeRender extends ViewGLRender {

    private Context mContext;
    private FloatBuffer mVertexFloatBuffer;
    private ShortBuffer mIndexBuffer;

    //使用GL_TRIANGLE_STRIP的方式绘制正方形
//    private static float SQUARE_COLOR_COORDS[] = {
//            //Order of coordinates: X, Y, Z, R,G,B,
//            -0.5f, 0.5f, 0.0f, 1.f, 0f, 0f,  //  0.top left RED
//            -0.5f, -0.5f, 0.0f, 0.f, 0f, 1f, //  1.bottom right Blue
//            0.5f, 0.5f, 0.0f, 1f, 1f, 1f,   //  3.top right WHITE
//            0.5f, -0.5f, 0.0f, 0.f, 1f, 0f,  //  2.bottom left GREEN
//    };

    //使用GL_TRIANGLE_FAN的方式绘制正方形
//    private static float SQUARE_COLOR_COORDS[] = {
//            //Order of coordinates: X, Y, Z, R,G,B,
//            -0.5f, 0.5f, 0.0f, 1.f, 0f, 0f,  //  0.top left RED
//            0.5f, 0.5f, 0.0f, 0.f, 0f, 1f, //  1.bottom right Blue
//            0.5f, -0.5f, 0.0f, 1f, 1f, 1f,   //  3.top right WHITE
//            -0.5f, -0.5f, 0.0f, 0.f, 1f, 0f,  //  2.bottom left GREEN
//    };

    //正方形的点1
    private static float SQUARE_COLOR_COORDS[] = {
            //Order of coordinates: X, Y, Z, R,G,B,
            -0.5f, 0.5f, 0.0f, 1.f, 0f, 0f,  //  0.top left RED
            -0.5f, -0.5f, 0.0f, 0.f, 0f, 1f, //  1.bottom right Blue
            0.5f, 0.5f, 0.0f, 1f, 1f, 1f,   //  3.top right WHITE
            0.5f, -0.5f, 0.0f, 0.f, 1f, 0f,  //  2.bottom left GREEN
    };
    //创建一个遍历的点的顺序 第一个三角形:1,0,2,1 第二个三角形:1,2,3,1
    private static short SQUARE_INDEX[] = {
            1, 0, 2, 1, 2, 3
    };

    //描述一个顶点的位置需要三个偏移量
    private static final int COORDS_PER_VERTEX = 3;
    //描述一个顶点的颜色需要三个偏移量
    private static final int COORDS_PER_COLOR = 3;
    //描述一个顶点需要的总偏移量
    private static final int TOTAL_COMPONENT_COUNT = COORDS_PER_COLOR + COORDS_PER_VERTEX;
    //描述一个顶点需要的byte偏移量
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * Constants.BYTES_PER_FLOAT;
    private static final int VERTEX_COUNT = SQUARE_COLOR_COORDS.length / TOTAL_COMPONENT_COUNT;

    private int mProgramObjectId;
    private int uMatrix;

    private float[] mProjectionMatrix = new float[16];

    public SquareShapeRender(Context context) {
        this.mContext = context;
        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(SQUARE_COLOR_COORDS.length * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(SQUARE_COLOR_COORDS);
        mVertexFloatBuffer.position(0);
        mIndexBuffer = ByteBuffer
                .allocateDirect(SQUARE_INDEX.length * Constants.BYTES_PER_SHORT)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(SQUARE_INDEX);
        mIndexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);
        String vertexShaderCode = GLESUtils.readAssetShaderCode(mContext, "triangle_matrix_color_vertex_shader.glsl");
        String fragmentShaderCode = GLESUtils.readAssetShaderCode(mContext, "triangle_matrix_color_fragment_shader.glsl");
        int vertexShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgramObjectId = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgramObjectId, vertexShaderObjectId);
        GLES20.glAttachShader(mProgramObjectId, fragmentShaderObjectId);
        GLES20.glLinkProgram(mProgramObjectId);
        GLES20.glUseProgram(mProgramObjectId);

        int aPosition = GLES20.glGetAttribLocation(mProgramObjectId, "a_Position");
        mVertexFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(aPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, STRIDE, mVertexFloatBuffer);
        GLES20.glEnableVertexAttribArray(aPosition);

        int aColor = GLES20.glGetAttribLocation(mProgramObjectId, "a_Color");
        mVertexFloatBuffer.position(COORDS_PER_VERTEX);
        GLES20.glVertexAttribPointer(aColor, COORDS_PER_COLOR, GLES20.GL_FLOAT, false, STRIDE, mVertexFloatBuffer);
        GLES20.glEnableVertexAttribArray(aColor);

        uMatrix = GLES20.glGetUniformLocation(mProgramObjectId, "u_Matrix");
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        super.onSurfaceChanged(gl10, width, height);
        float aspectRatio = width > height ? (float) width / height : (float) height / width;
        if (width > height) {
            Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        super.onDrawFrame(gl10);
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mProjectionMatrix, 0);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX_COUNT);
        //count为绘制图元的数量乘上一个图元的顶点数，例如三角形的定点为3，要绘制两个三角形，就得传入6
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, SQUARE_INDEX.length, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);
    }

}
