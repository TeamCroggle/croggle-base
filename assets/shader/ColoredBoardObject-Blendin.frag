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
uniform sampler2D u_blendin;
uniform float u_blendin_priority;

void main() {
	vec4 mask = texture2D(u_mask, v_maskCoords);
	vec4 background = texture2D(u_background, v_backgroundCoords);
	vec4 foreground = texture2D(u_foreground, v_foregroundCoords);
	vec4 blendin = texture2D(u_blendin, v_backgroundCoords);

	gl_FragColor = mask.a * (u_blendin_priority * background + (1.f - u_blendin_priority) * blendin);
	gl_FragColor = foreground * foreground.a + gl_FragColor * (1.f - foreground.a);
	gl_FragColor *= v_color;
}