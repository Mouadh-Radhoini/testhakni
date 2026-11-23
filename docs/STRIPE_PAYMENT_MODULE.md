# Stripe Payment Module

## Scope

The payment feature is isolated under `com.testhakni.backend.payment`. It does not change booking state or existing collections.

## Architecture

- `domain/model`: MongoDB payment entity and legal status transitions.
- `domain/repository`: persistence port.
- `application`: booking ownership, server-side amount validation, Checkout orchestration, and webhook business rules.
- `application/port`: Stripe-independent `CheckoutGateway`.
- `infrastructure/stripe`: official Stripe Java SDK implementation.
- `infrastructure/persistence`: MongoDB adapter for the payment repository.
- `presentation`: JWT customer endpoints and unauthenticated, signature-verified webhook endpoint.

The application layer uses `BookingRepository` only as a read-only dependency.

## Lifecycle

`PENDING -> PAID`, `FAILED`, `CANCELLED`, or `EXPIRED`. `PAID`, `FAILED`, `CANCELLED`, and `EXPIRED` do not allow later transitions. MongoDB also has partial unique indexes for one pending and one paid payment per booking.

## Checkout

1. Authenticate the customer.
2. Load the booking from MongoDB.
3. Verify `booking.customerId == authenticated user id`.
4. Use `booking.price` and `STRIPE_CURRENCY`; the browser supplies no amount or currency.
5. Reject missing, nonpositive, unsupported, or incorrectly scaled amounts.
6. Reject existing paid or active pending payments.
7. Create a pending `payments` document.
8. Create Stripe-hosted Checkout with server metadata `bookingId` and `paymentId`.
9. Save and expose the Checkout session ID.

TND is treated as a three-decimal currency, converted to 1000 minor units per 1.000 TND. USD and EUR use 2 decimal places. The implementation never converts between currencies. Stripe account capabilities can still reject TND or the configured currency.

## Webhooks

The webhook controller reads the raw request body and verifies `Stripe-Signature` using `Stripe.Webhook.constructEvent`. Invalid or missing signatures receive `400`. Missing Stripe configuration receives `503`.

The webhook service validates:

- checkout session ID,
- payment ID,
- booking ID,
- booking existence,
- currency,
- amount from Stripe against the server-saved payment amount,
- Stripe `payment_status=paid`,
- PAID terminal status protection.

Frontend success redirects are not payment confirmation.

## MongoDB

The new collection is `payments`. No existing collection or entity is changed.

```json
{
  "id": "ObjectId/string",
  "bookingId": "booking id",
  "customerId": "customer id",
  "amount": "BigDecimal",
  "currency": "tnd",
  "status": "PENDING",
  "stripeCheckoutSessionId": "cs_...",
  "stripePaymentIntentId": "pi_...",
  "createdAt": "Instant",
  "updatedAt": "Instant",
  "paidAt": "Instant"
}
```

No card or bank details are persisted.

## Environment variables

- `STRIPE_SECRET_KEY`: Stripe test/live secret key.
- `STRIPE_WEBHOOK_SECRET`: endpoint signing secret.
- `STRIPE_CURRENCY`: default `tnd`.
- `FRONTEND_BASE_URL`: default `http://localhost:4200`.

No secrets are stored in source or logged.

## API

Customer JWT is required:

```text
POST /api/payments/bookings/{bookingId}/checkout
GET  /api/payments/bookings/{bookingId}
```

Checkout response:

```json
{
  "paymentId": "payment id",
  "bookingId": "booking id",
  "checkoutUrl": "https://checkout.stripe.com/c/pay/...",
  "checkoutSessionId": "cs_...",
  "status": "PENDING"
}
```

Status response:

```json
{
  "paymentId": "payment id",
  "bookingId": "booking id",
  "amount": "125.500",
  "currency": "tnd",
  "status": "PAID",
  "paidAt": "2026-..."
}
```

Webhook:

```text
POST /api/payments/stripe/webhook
Stripe-Signature: t=...,v1=...
```

## Security

The existing `SecurityConfig` is not modified. It currently has no permit rule for the payment webhook and therefore treats it as authenticated. Separately approved activation requires adding, before broad authenticated rules:

```text
.requestMatchers(HttpMethod.POST, "/api/payments/stripe/webhook").permitAll()
```

This permit is safe only because raw-body Stripe signature verification remains mandatory in `StripeWebhookController`.

Customer payment endpoints use the existing `@AuthenticationPrincipal AuthenticatedUser` and server-side ownership checks.

## Tests

New tests are isolated in `backend/src/test/java/com/testhakni/backend/payment/`:

- `PaymentServiceTest`
- `PaymentWebhookServiceTest`
- `PaymentControllerTest`
- `StripeWebhookControllerTest`
- `PaymentRepositoryAdapterTest`

Run:

```bash
cd backend
./mvnw test
./mvnw clean verify
```

Tests mock Stripe and Mongo behavior and make no real Stripe API calls.

## Stripe CLI

Forward and trigger test events after Stripe configuration and security activation:

```bash
stripe listen --forward-to localhost:8081/api/payments/stripe/webhook
stripe trigger checkout.session.completed
```

Use a real Checkout session for amount and metadata validation; synthetic CLI events may not contain the server-created booking metadata.

## Known blockers

- `SecurityConfig` needs a separately approved one-line permit for the verified webhook.
- `STRIPE_SECRET_KEY` and `STRIPE_WEBHOOK_SECRET` must be supplied.
- Stripe Checkout supports only currencies enabled for the merchant account.
- TND availability in Tunisia and test-mode account capabilities must be confirmed in Stripe Dashboard.
- The frontend currently has no success/cancel payment routes.
- This module does not change booking status or existing marketplace behavior.

## Activation status

The following minimal existing-file changes have now been applied:

- `SecurityConfig.java` permits only `POST /api/payments/stripe/webhook` without JWT, while signature verification remains mandatory.
- `application.yml` maps Stripe environment variables to `app.stripe.*`.
- The bookings Angular page provides a `Payer maintenant` action.
- Angular routes include `/payments/success` and `/payments/cancel`.
- A frontend payment service calls the payment checkout and status APIs.

The Stripe SDK dependency was already present in `pom.xml`; no dependency change was necessary. No Stripe secret is stored in source.

## Actual validation

- Backend `clean verify`: 105 tests passed, no failures.
- Payment backend tests: 22 tests passed.
- Angular build: passed.
- Angular unit tests: 32 tests passed.
- Stripe API calls: none. Test mode secrets were not created or configured.
