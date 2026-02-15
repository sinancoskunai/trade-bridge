# trade-bridge admin-web

Admin and broker web panel (React + Vite).

## Run

```bash
npm install
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

Default local API is `http://localhost:8080` if `VITE_API_BASE_URL` is not set.

## Current MVP screen

- Login with backend credentials
- List categories
- Create category (`POST /admin/categories`)
- Add category attribute (`POST /admin/categories/{id}/attributes`)
- Broker demo register + approve + login
- Broker RFQ intervention (`POST /broker/rfqs/{id}/interventions`)

## Scripts

- `npm run dev`
- `npm run build`
- `npm run lint`
- `npm run test`
