precision mediump float;
//在片元着色器里面sampler2D表示我们要添加2D贴图
uniform sampler2D u_TextureUnit;
varying vec2 v_TextureCoordinates;
void main() {
    //渲染2D纹理 交给FragColor
    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);
}