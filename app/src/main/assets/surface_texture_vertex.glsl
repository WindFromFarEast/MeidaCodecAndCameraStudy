attribute vec4 a_Position;//顶点坐标
attribute vec2 a_TexCoordinatePosition;//纹理坐标
varying vec2 v_TexPosition;//纹理坐标
uniform mat4 u_Matrix;//变换矩阵

void main() {
    v_TexPosition = a_TexCoordinatePosition;
    gl_Position = u_Matrix * a_Position;
}