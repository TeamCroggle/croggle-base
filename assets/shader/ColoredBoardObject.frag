#ifdef GL_ES
	precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_maskCoords;
varying vec2 v_foregroundCoords;
varying vec2 v_backgroundCoords;

uniform sampler2D u_mask;
uniform sampler2D u_background;
uniform sampler2D u_foreground;

void main() {
	vec4 mask = texture2D(u_mask, v_maskCoords);
	vec4 background = texture2D(u_background, v_backgroundCoords);
	vec4 foreground = texture2D(u_foreground, v_foregroundCoords);

	background.a = mask.a;
	
	float ac = foreground.a + (1.f - foreground.a) * background.a;
	
	gl_FragColor = foreground * foreground.a + background * (1.f - foreground.a);
	gl_FragColor.a = ac;
	gl_FragColor *= v_color;
}