# GITHUB SETUP (Isolated Account)

## 1) Local git identity for this repo only

```bash
cd /Users/snn/fyi/dev/trade-bridge
./scripts/setup-git-identity.sh "YOUR_NAME" "your-email@example.com"
```

## 2) SSH key for isolated account

```bash
ssh-keygen -t ed25519 -C "trade-bridge" -f ~/.ssh/id_ed25519_tradebridge
./scripts/setup-ssh-alias.sh
```

## 3) Add key to dedicated GitHub account

- GitHub account: dedicated for `trade-bridge`
- Add `~/.ssh/id_ed25519_tradebridge.pub` to account SSH keys.

## 4) Create repository and set remote

```bash
cd /Users/snn/fyi/dev/trade-bridge
git remote add origin git@github-tradebridge:<account>/trade-bridge.git
```

## 5) GHCR publishing

- Use account-scoped GHCR namespace:
- `ghcr.io/<account>/trade-bridge/backend`
- `ghcr.io/<account>/trade-bridge/admin-web`
