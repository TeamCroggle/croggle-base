attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoordMask;
attribute vec2 a_texCoordForeground;
attribute vec2 a_texCoordBackground;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_maskCoords;
varying vec2 v_foregroundCoords;
varying vec2 v_backgroundCoords;

void main() {
	v_color = a_color;
	v_maskCoords = a_texCoordMask;
	v_foregroundCoords = a_texCoordForeground;
	v_backgroundCoords = a_texCoordBackground;
	gl_Position =  u_projTrans * a_position;
}