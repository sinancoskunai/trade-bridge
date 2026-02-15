# API SURFACE (Current MVP Baseline)

## Auth

- `POST /auth/register-company`
- `POST /auth/login`
- `POST /auth/refresh`
- `GET /users/me`
- `POST /admin/companies/{companyId}/approve`

## Categories

- `GET /categories`
- `POST /admin/categories`
- `POST /admin/categories/{id}/attributes`

## Product Draft & Listing

- `POST /seller/products/drafts/upload` (multipart: categoryId, file)
- `GET /seller/products/drafts/{draftId}`
- `PUT /seller/products/drafts/{draftId}`
- `POST /seller/products/drafts/{draftId}/confirm`
- `GET /buyer/products`

Draft response now includes:

- `parseJobId`
- `lastError`

## Parse Operations (Admin)

- `GET /admin/parse-jobs?status=...`
- `POST /admin/parse-jobs/{parseJobId}/requeue`

## AI Search (Stub)

- `POST /buyer/search/qa`

## RFQ/Offer/Broker

- `POST /buyer/rfqs`
- `POST /seller/rfqs/{id}/offers`
- `POST /buyer/offers/{id}/counter`
- `POST /buyer/offers/{id}/accept`
- `POST /buyer/offers/{id}/reject`
- `POST /broker/rfqs/{id}/interventions`

## Notifications

- `GET /notifications`
- `POST /notifications/device-token`
