#!/usr/bin/env bash
#
# Refreshes the pinned auth-service certificate in app/src/main/res/raw/auth_service.pem.
#
# The deployment presents a self-signed certificate, so the app ships a copy and trusts it
# alongside the system CAs (see network/TlsConfig.java). Whenever the server's keystore is
# regenerated the copy here has to be replaced, or every call fails the handshake.
#
#   usage: tools/fetch-server-cert.sh [host] [port]
#
# Defaults match ApiConfig. Requires openssl on PATH.
#
# Alternative, straight from the server's keystore:
#   keytool -exportcert -rfc -alias auth-service \
#           -keystore <auth-service>/src/main/resources/keystore.p12 \
#           -storetype PKCS12 -storepass <password> \
#           -file app/src/main/res/raw/auth_service.pem

set -euo pipefail

HOST="${1:-3.108.167.166}"
PORT="${2:-8443}"
OUT="$(cd "$(dirname "$0")/.." && pwd)/app/src/main/res/raw/auth_service.pem"

echo "Fetching the certificate presented by ${HOST}:${PORT} ..."

# -servername sends SNI; harmless for an IP, needed once the service sits behind a name.
if ! openssl s_client -connect "${HOST}:${PORT}" -servername "${HOST}" -showcerts </dev/null 2>/dev/null \
        | openssl x509 -outform PEM > "${OUT}.tmp"; then
    rm -f "${OUT}.tmp"
    echo "Could not retrieve a certificate from ${HOST}:${PORT}." >&2
    echo "Check that the port is reachable (EC2 security group, VPN) and try again." >&2
    exit 1
fi

if [ ! -s "${OUT}.tmp" ]; then
    rm -f "${OUT}.tmp"
    echo "The server returned no certificate." >&2
    exit 1
fi

mv "${OUT}.tmp" "${OUT}"

echo "Written to ${OUT}:"
openssl x509 -in "${OUT}" -noout -subject -issuer -dates -fingerprint -sha256
echo
echo "Compare that SHA-256 fingerprint with the one on the server before shipping a build."
