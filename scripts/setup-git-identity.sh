#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 2 ]; then
  echo "Usage: $0 <git-user-name> <git-user-email>"
  exit 1
fi

git config user.name "$1"
git config user.email "$2"

echo "Local git identity set for trade-bridge repo."
