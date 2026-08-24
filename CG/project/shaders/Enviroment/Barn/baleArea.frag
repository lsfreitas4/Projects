#ifdef GL_ES
precision highp float;
#endif

varying vec2 vTextureCoord;

uniform sampler2D uSampler;        // A tua textura de palha/feno (Unit 0)
uniform float uIsIntersecting;    // 1.0 se o vagão estiver lá dentro, 0.0 se fora

void main() {
    // 1. Amostrar a cor original da textura de palha
    vec4 strawColor = texture2D(uSampler, vTextureCoord);
    
    // 2. Calcular a distância do fragmento ao centro do círculo (coordenadas UV vão de 0 a 1)
    vec2 center = vec2(0.5, 0.5);
    float distFromCenter = distance(vTextureCoord, center);
    
    // Como usamos o MyCircle geométrico puro, ele já vem cortado de fábrica,
    // mas podemos usar a distância para desenhar uma borda delimitadora!
    
    vec3 finalRGB = strawColor.rgb;

    // 3. SE HOUVER INTERSEÇÃO: Aplica um filtro dourado radiante (Feedback Ativo)
    if (uIsIntersecting > 0.5) {
        // Mistura a palha com um tom amarelo-alaranjado brilhante nas zonas ativas
        vec3 activeGlow = vec3(1.0, 0.75, 0.1); 
        finalRGB = mix(finalRGB, activeGlow, 0.3);
        
        // Desenha uma linha de contorno amarela néon a piscar/brilhar perto da extremidade
        if (distFromCenter > 0.45) {
            finalRGB = vec3(1.0, 0.9, 0.3);
        }
    } 
    // 4. SE NÃO HOUVER INTERSEÇÃO: Visual normal e calmo
    else {
        // Desenha uma linha de contorno castanha escura discreta para marcar o limite no chão
        if (distFromCenter > 0.47) {
            finalRGB = vec3(0.35, 0.22, 0.12); // Cor de terra escura/madeira velha
        }
    }

    gl_FragColor = vec4(finalRGB, 1.0);
}