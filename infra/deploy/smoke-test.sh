#!/usr/bin/env bash
set -euo pipefail

curl -fsS http://localhost:18080/actuator/health >/dev/null
