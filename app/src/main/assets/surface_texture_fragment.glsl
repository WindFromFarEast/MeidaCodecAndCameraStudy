#extension GL_OES_EGL_image_external : require
//↑申明使用扩展纹理
precision mediump float;
varying vec2 v_TexPosition;
uniform samplerExternalOES s_Texture;//加载流数据(摄像头数据)

void main() {
    gl_FragColor = texture2D(s_Texture, v_TexPosition);
}