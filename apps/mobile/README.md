# trade-bridge mobile

React Native Expo mobile client for buyer and seller journeys.

## Current MVP flow

- Demo buyer register + admin approve
- Buyer login
- Category listing
- AI Q&A search (`POST /buyer/search/qa`)
- RFQ create (`POST /buyer/rfqs`)

## Scripts

- `npm run start`
- `npm run lint`
- `npm run test`

## API Base URL

Set backend URL for Expo runtime:

```bash
export EXPO_PUBLIC_API_BASE_URL="http://<your-lan-ip>:8080"
```
