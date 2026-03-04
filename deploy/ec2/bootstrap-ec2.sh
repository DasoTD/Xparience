#!/usr/bin/env bash
set -euo pipefail

if [[ $EUID -ne 0 ]]; then
  echo "Run as root (sudo)"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

id -u xparience >/dev/null 2>&1 || useradd --system --create-home --shell /bin/bash xparience

mkdir -p /opt/xparience/blue /opt/xparience/green /opt/xparience/releases
mkdir -p /etc/xparience /var/log/xparience

touch /etc/xparience/common.env
touch /etc/xparience/blue.env
touch /etc/xparience/green.env

if ! grep -q '^SERVER_PORT=' /etc/xparience/blue.env; then
  echo 'SERVER_PORT=9091' >> /etc/xparience/blue.env
fi

if ! grep -q '^SERVER_PORT=' /etc/xparience/green.env; then
  echo 'SERVER_PORT=9092' >> /etc/xparience/green.env
fi

chown -R xparience:xparience /opt/xparience /var/log/xparience
chmod 750 /opt/xparience /var/log/xparience
chmod 640 /etc/xparience/*.env

cp "$ROOT_DIR/deploy/systemd/xparience@.service" /etc/systemd/system/xparience@.service
cp "$ROOT_DIR/deploy/nginx/xparience.conf" /etc/nginx/conf.d/xparience.conf

if [[ ! -f /etc/nginx/conf.d/xparience-upstream.inc ]]; then
  echo 'server 127.0.0.1:9091;' > /etc/nginx/conf.d/xparience-upstream.inc
fi

systemctl daemon-reload
nginx -t
systemctl enable nginx
systemctl restart nginx

echo "Bootstrap complete."
echo "Update /etc/xparience/common.env with production secrets before first deployment."
