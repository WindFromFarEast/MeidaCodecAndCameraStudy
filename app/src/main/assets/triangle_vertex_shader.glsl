attribute vec4 aPosition;
uniform mat4 u_Matrix;
void main() {
    gl_Position = u_Matrix * aPosition;
}