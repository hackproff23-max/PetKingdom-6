#!/usr/bin/env bash
# Cria app/keystore.jks e keystore.properties no projeto (exigidos pelo build "release").
# Ordem de preferencia:
#   1) variaveis SIGNING_KEY e KEYSTORE_PROPERTIES (base64) -> chave fixa (secrets do GitHub)
#   2) port/signing/keystore.jks + port/signing/keystore.properties -> chave fixa local
#   3) gera uma chave nova (APKs de builds diferentes NAO atualizam um ao outro)
# Uso: bash port/sign-setup.sh <pasta-do-projeto>
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJ="${1:?informe a pasta do projeto}"

if [[ -n "${SIGNING_KEY:-}" && -n "${KEYSTORE_PROPERTIES:-}" ]]; then
  printf '%s' "$SIGNING_KEY" | base64 -di > "$PROJ/app/keystore.jks"
  printf '%s' "$KEYSTORE_PROPERTIES" | base64 -di > "$PROJ/keystore.properties"
  echo "Assinatura: usando chave fixa dos secrets."
elif [[ -f "$HERE/signing/keystore.jks" && -f "$HERE/signing/keystore.properties" ]]; then
  cp "$HERE/signing/keystore.jks" "$PROJ/app/keystore.jks"
  cp "$HERE/signing/keystore.properties" "$PROJ/keystore.properties"
  echo "Assinatura: usando port/signing/."
else
  PASS="$(head -c 24 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 20)"
  keytool -genkeypair -keystore "$PROJ/app/keystore.jks" -alias pk6 -keyalg RSA -keysize 2048 \
    -validity 36500 -storepass "$PASS" -keypass "$PASS" -dname "CN=Pet Kingdom 6 Port" >/dev/null 2>&1
  printf 'keyAlias=pk6\nkeyPassword=%s\nstoreFile=keystore.jks\nstorePassword=%s\n' "$PASS" "$PASS" \
    > "$PROJ/keystore.properties"
  echo "Assinatura: chave NOVA gerada (guarde-a se quiser atualizar o app depois por cima)."
  echo "Para fixar a chave: veja o README (secao Assinatura)."
fi
