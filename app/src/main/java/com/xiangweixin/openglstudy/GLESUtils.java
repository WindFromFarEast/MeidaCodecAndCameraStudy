package com.xiangweixin.openglstudy;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GLESUtils {

    /**
     * 从Assets文件中读取Shader Code
     * @param context
     * @param shaderCodeName
     * @return
     */
    public static String readAssetShaderCode(Context context, String shaderCodeName) {
        StringBuilder body = new StringBuilder();
        InputStream open;
        try {
            open = context.getAssets().open(shaderCodeName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(open));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                body.append(line);
                body.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body.toString();
    }

    /**
     * 编译Shader
     * @param type Shader Type
     * @param shaderCode Shader Code
     * @return
     */
    public static int compileShaderCode(int type, String shaderCode) {
        //获取着色器ID,之后通过ID对着色器进行操作
        int shaderObjectId = GLES20.glCreateShader(type);

        if (shaderObjectId != 0) {
            GLES20.glShaderSource(shaderObjectId, shaderCode);//将GLSL代码和Shader绑定
            GLES20.glCompileShader(shaderObjectId);//编译Shader

            //查询编译状态
            int[] status = new int[1];
            GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, status, 0);
            if (status[0] == 0) { //结果为0表示编译失败
                GLES20.glDeleteShader(shaderObjectId);//编译失败需要释放资源
                Log.w("GLESUtils", "Shader Compile Failed!");
                return 0;
            }
        }

        return shaderObjectId;
    }

}
