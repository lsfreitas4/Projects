# CG 2025/2026

## Group T09G08

## TP5 Notes

### Exercício 1.1 - Teapot colorido por posição em clip-space

Foram criados os shaders `positionColor.vert` e `positionColor.frag`.

- No vertex shader, a posição transformada (`gl_Position`) é passada para o fragment shader por uma variável `varying`.
- No fragment shader, os fragmentos com `y > 0.5` ficam amarelos e os restantes ficam azuis.

### Exercício 1.2 - Animação sinusoidal no eixo X

O shader `texture3anim.vert` foi alterado para aplicar uma translação ao longo do eixo X usando uma onda sinusoidal.

- A animação usa `timeFactor`.
- A amplitude depende de `normScale` (valor controlado por `scaleFactor` na interface).

### Exercício 1.3 - Grayscale

Foi criado o fragment shader `grayscale.frag` com base no shader sépia.

- Conversão para tons de cinza feita por:
	`L = 0.299R + 0.587G + 0.114B`

### Exercício 2 - Water shader no plano

Foram criados os shaders `water.vert` e `water.frag` (baseados em `texture2.vert` / `texture2.frag`) e adicionados à interface como `Water effect`.

- Textura base de água: `textures/waterTex.jpg`
- Mapa de alturas/filtro: `textures/waterMap.jpg` (ligado em `uSampler2`)
- No vertex shader, os vértices são deslocados pela normal em função do mapa de alturas.
- No vertex e fragment shader, as coordenadas de textura variam com o tempo (`timeFactor`) para criar animação de água.

