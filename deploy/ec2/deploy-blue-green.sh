#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <jar-path> [release-id]"
  exit 1
fi

JAR_PATH="$1"
RELEASE_ID="${2:-$(date +%Y%m%d%H%M%S)}"

APP_ROOT="/opt/xparience"
BLUE_DIR="$APP_ROOT/blue"
GREEN_DIR="$APP_ROOT/green"
RELEASES_DIR="$APP_ROOT/releases"
UPSTREAM_FILE="/etc/nginx/conf.d/xparience-upstream.inc"

BLUE_PORT=9091
GREEN_PORT=9092

if [[ ! -f "$JAR_PATH" ]]; then
  echo "JAR file not found: $JAR_PATH"
  exit 1
fi

mkdir -p "$BLUE_DIR" "$GREEN_DIR" "$RELEASES_DIR"

if [[ ! -f "$UPSTREAM_FILE" ]]; then
  echo "server 127.0.0.1:$BLUE_PORT;" > "$UPSTREAM_FILE"
fi

ACTIVE_SLOT="blue"
ACTIVE_PORT="$BLUE_PORT"

if grep -q "$GREEN_PORT" "$UPSTREAM_FILE"; then
  ACTIVE_SLOT="green"
  ACTIVE_PORT="$GREEN_PORT"
fi

if [[ "$ACTIVE_SLOT" == "blue" ]]; then
  INACTIVE_SLOT="green"
  INACTIVE_PORT="$GREEN_PORT"
else
  INACTIVE_SLOT="blue"
  INACTIVE_PORT="$BLUE_PORT"
fi

TARGET_DIR="$APP_ROOT/$INACTIVE_SLOT"
TARGET_JAR="$TARGET_DIR/app.jar"
RELEASE_JAR="$RELEASES_DIR/app-$RELEASE_ID.jar"

cp "$JAR_PATH" "$RELEASE_JAR"
ln -sfn "$RELEASE_JAR" "$TARGET_JAR"

echo "Deploying release $RELEASE_ID to slot $INACTIVE_SLOT (port $INACTIVE_PORT)..."
systemctl restart "xparience@$INACTIVE_SLOT"

echo "Waiting for health check on inactive slot..."
for _ in {1..90}; do
  if ! systemctl is-active --quiet "xparience@$INACTIVE_SLOT"; then
    echo "Service xparience@$INACTIVE_SLOT is not active during startup"
    journalctl -u "xparience@$INACTIVE_SLOT" -n 150 --no-pager || true
    exit 1
  fi
  if curl -fsS "http://127.0.0.1:$INACTIVE_PORT/api-docs" >/dev/null; then
    break
  fi
  sleep 2
done

if ! curl -fsS "http://127.0.0.1:$INACTIVE_PORT/api-docs" >/dev/null; then
  echo "Health check failed on $INACTIVE_SLOT"
  systemctl status "xparience@$INACTIVE_SLOT" --no-pager -l || true
  journalctl -u "xparience@$INACTIVE_SLOT" -n 150 --no-pager || true
  exit 1
fi

PREV_UPSTREAM="server 127.0.0.1:$ACTIVE_PORT;"
echo "server 127.0.0.1:$INACTIVE_PORT;" > "$UPSTREAM_FILE"

if nginx -t; then
  systemctl reload nginx
else
  echo "$PREV_UPSTREAM" > "$UPSTREAM_FILE"
  nginx -t
  systemctl reload nginx
  echo "Nginx config test failed, rollback applied"
  exit 1
fi

echo "Traffic switched to $INACTIVE_SLOT"
sleep 15
systemctl stop "xparience@$ACTIVE_SLOT" || true

echo "Deployment successful"
