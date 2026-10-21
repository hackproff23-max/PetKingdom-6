# Pet Kingdom 6 (PT-BR) — APK Android standalone

APK próprio, baseado no **J2ME Loader** (modo oficial "midlet port"), com o jogo embutido.
Ao tocar no ícone o jogo abre direto, sem lista de apps nem tela de configuração.

## Como gerar o APK (GitHub Actions, sem instalar nada)

1. Crie um repositório no GitHub (pode ser privado) e envie **todo o conteúdo desta pasta**,
   incluindo `.github/` e `port/`.
2. Vá em **Actions → Build APK (Pet Kingdom 6) → Run workflow**.
3. Quando terminar (uns 10–20 min na primeira vez), baixe o artefato **PetKingdom6-apk**,
   extraia o `.apk` e instale no celular.

Também dá para compilar no seu PC: `bash port/build-local.sh` (precisa de JDK 21 e Android SDK).

## O que vem configurado

- **Abre direto no jogo**: na primeira execução o app copia `config.json`, `VirtualKeyboardLayout`
  e o shader `J5.vsh/J5.fsh` (os que você mandou) para a pasta interna e inicia o jogo.
- **Tela cheia + recorte da câmera**: sempre ligado neste APK (sem barra de status/navegação, usando
  a área do notch/furo). Código em `MicroActivity` (arquivo `port/patch/pk6-port.patch`).
- **Botões virtuais**: cores, contorno, formato, transparência e posições vêm do seu
  `config.json` + `VirtualKeyboardLayout`; a **fonte** é o `Roboto-Regular.ttf` extraído do seu `J2ME.apk`.
- **Mesmo conversor do app**: as classes do jogo passam pela mesma instrumentação ASM que o J2ME Loader
  aplica ao importar um `.jar` (charset de `getBytes`, `Timer`, `System.getProperty`,
  `getResourceAsStream`). Testei isso nas 121 classes do jogo.
- **Só o necessário**: apenas ARM (`armeabi-v7a` + `arm64-v8a`), R8 com shrink de código e recursos.

## Assinatura (importante para atualizar o app depois)

Sem configurar nada, cada build gera uma chave nova — instalar por cima de um APK de outro build falha
(assinaturas diferentes). Para fixar a chave:

1. `keytool -genkeypair -keystore keystore.jks -alias pk6 -keyalg RSA -keysize 2048 -validity 36500`
2. Crie `keystore.properties` com `keyAlias=pk6`, `keyPassword=...`, `storeFile=keystore.jks`, `storePassword=...`
3. No GitHub: *Settings → Secrets and variables → Actions* → crie `SIGNING_KEY` (= `base64 -w0 keystore.jks`)
   e `KEYSTORE_PROPERTIES` (= `base64 -w0 keystore.properties`).

## Trocar configuração depois

Edite os arquivos em `port/overlay/app/src/midlet/assets/port/` e gere o APK de novo. A configuração só é
copiada se ainda não existir; ao testar mudanças, **limpe os dados do app** (ou desinstale) antes.

## Limitações conhecidas (leia)

- **Eu não consegui compilar nem rodar o APK aqui** (o sandbox bloqueia Google Maven/Android SDK).
  Verifiquei por partes: o patch aplica limpo sobre o J2ME Loader (commit fixo `afa9daa`), a sintaxe do
  `build.gradle` é válida, o conversor roda nas 121 classes com 0 falhas e nenhum recurso do jogo é
  descartado no empacotamento. O build completo e o teste no aparelho ainda são o primeiro teste real.
- **Fonte/contorno "do seu APK"**: usei a fonte, as cores/contorno/formato/posições que estão nos arquivos
  que você mandou. Não consegui comparar o *código de desenho* dos botões do seu APK com o do código-fonte
  (sem decompilador aqui). Se o seu APK tinha alguma modificação própria nisso, me diga que replico.
- **Tamanho**: cortes mais agressivos (remover ffmpeg-kit e CameraX) exigem alterar código do emulador e
  podem afetar áudio; ficam para uma segunda etapa, depois de confirmar que o jogo abre.
- Ícone do app é o padrão do J2ME Loader (o `icon.png` do jogo não foi aplicado).
