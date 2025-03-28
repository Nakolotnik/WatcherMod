#version 150

uniform sampler2D Sampler0; // Текстура (CLOCK_TEXTURE)
uniform float time; // Передаём время анимации
uniform vec2 center; // Центр циферблата (не используется напрямую в этом примере)
uniform float radius; // Радиус циферблата

in vec4 vertexColor;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord);
    float pulse = 1.0 + sin(time * 0.2) * 0.2;
    vec3 color = vertexColor.rgb * texColor.rgb;

    // Пример эффекта пульсации
    float dist = length(texCoord - vec2(0.5, 0.5));
    if (dist < radius * pulse) {
        color = mix(color, vec3(1.0, 0.5, 1.0), smoothstep(radius * pulse - 0.1, radius * pulse, dist));
    }

    fragColor = vec4(color, vertexColor.a * texColor.a);
}