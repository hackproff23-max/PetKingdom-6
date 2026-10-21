#!/usr/bin/env bash
# Baixa o J2ME Loader na versao fixa (upstream-commit.txt) e aplica o port por cima.
# Uso: bash port/apply.sh [pasta-destino]      (padrao: ./build/J2ME-Loader)
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEST="${1:-$HERE/../build/J2ME-Loader}"
COMMIT="$(tr -d '[:space:]' < "$HERE/upstream-commit.txt")"

rm -rf "$DEST"
mkdir -p "$DEST"
cd "$DEST"

git init -q
git remote add origin https://github.com/nikita36078/J2ME-Loader.git
git fetch -q --depth 1 origin "$COMMIT"
git checkout -q FETCH_HEAD
git submodule update --init --recursive   # TinySoundFont (sintetizador MIDI)

# 1) alteracoes em arquivos existentes (build.gradle, proguard, MicroActivity)
git apply --whitespace=nowarn "$HERE/patch/pk6-port.patch"

# 2) remove o midlet de exemplo do projeto
rm -rf app/src/midlet/java app/src/midlet/resources/com

# 3) copia os arquivos do jogo, configuracao, shader, fonte e o conversor
cp -r "$HERE/overlay/." .

echo "OK: projeto pronto em $DEST"
