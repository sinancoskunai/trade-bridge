#!/usr/bin/env bash
set -euo pipefail

SSH_CONFIG="${HOME}/.ssh/config"
ALIAS_NAME="github-tradebridge"

cat <<CFG >> "$SSH_CONFIG"

Host ${ALIAS_NAME}
  HostName github.com
  User git
  IdentityFile ~/.ssh/id_ed25519_tradebridge
  IdentitiesOnly yes
CFG

echo "SSH alias ${ALIAS_NAME} appended to ${SSH_CONFIG}"
