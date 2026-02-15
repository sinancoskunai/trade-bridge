#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="${1:-.env.dev}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$SCRIPT_DIR"

docker compose --env-file "$ENV_FILE" -f docker-compose.deploy.yml pull

docker compose --env-file "$ENV_FILE" -f docker-compose.deploy.yml up -d

./smoke-test.sh "$ENV_FILE"
