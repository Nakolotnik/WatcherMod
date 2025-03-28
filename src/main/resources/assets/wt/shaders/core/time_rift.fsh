#version 150

#moj_import <matrix.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;
uniform float HueShift;

in vec4 texProj0;
in vec4 vertexColor;
out vec4 fragColor;

const vec3[] COLORS = vec3[](
vec3(0.9, 0.9, 1.0),
vec3(0.7, 0.7, 1.0),
vec3(0.55, 0.55, 1.0),
vec3(0.4, 0.4, 0.95),
vec3(0.3, 0.3, 0.85),
vec3(0.2, 0.2, 0.7),
vec3(0.15, 0.15, 0.5),
vec3(0.1, 0.1, 0.3),
vec3(0.05, 0.05, 0.15)
);

const mat4 SCALE_TRANSLATE = mat4(
0.5, 0.0, 0.0, 0.25,
0.0, 0.5, 0.0, 0.25,
0.0, 0.0, 1.0, 0.0,
0.0, 0.0, 0.0, 1.0
);

vec3 rgb2hsv(vec3 c) {
vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
float d = q.x - min(q.w, q.y);
float e = 1.0e-10;
return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c) {
vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float lightningMod(float layer, vec2 uv) {
float ripple = sin(GameTime * 12.0 + uv.y * 80.0 + layer * 4.0);
float flash = smoothstep(0.0, 1.0, fract(GameTime * 1.5 + layer * 0.3));
float discharge = abs(sin(GameTime * 20.0 + uv.x * 15.0 + uv.y * 30.0 + layer * 2.0)) * 0.5;
return 0.65 + 0.35 * (ripple * 0.5 + flash + discharge);
}

mat4 rift_layer(float layer) {
float shift = (1.5 + layer * 0.5) * GameTime;
mat4 translate = mat4(
1.0, 0.0, 0.0, 8.5 / layer,
0.0, 1.0, 0.0, shift,
0.0, 0.0, 1.0, 0.0,
0.0, 0.0, 0.0, 1.0
);

mat2 rotate = mat2_rotate_z(radians((layer * 13.0 + GameTime * 45.0)));
float scaleFactor = (layer * layer - 16.0 * layer + 64.0) / 14.0 + 0.5;
mat2 scale = mat2(scaleFactor);

return mat4(scale * rotate) * translate * SCALE_TRANSLATE;
}

void main() {
vec4 base = vertexColor * ColorModulator;
if (base.a < 0.05) discard;

vec3 finalColor = vec3(0.0);
for (int i = 0; i < 9; i++) {
float layer = float(i + 1);
vec4 tex = textureProj(Sampler0, texProj0 * rift_layer(layer));
float mod = lightningMod(layer, texProj0.xy);

vec3 hsv = rgb2hsv(COLORS[i]);
hsv.x = hsv.x + HueShift - 1.0 * floor((hsv.x + HueShift) / 1.0);
vec3 shiftedColor = hsv2rgb(hsv);

finalColor += tex.rgb * shiftedColor * mod;
}

fragColor = vec4(finalColor * base.rgb, base.a);
}