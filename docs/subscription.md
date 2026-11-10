# Mobile subscription flow

The Subscription entry in More opens the current plan. Plan Free can upgrade through plan selection, billing details, card details and payment summary. Plan Control exposes renewal, payment history, scheduled cancellation and reactivation. The existing Orders paywall also links to Subscription.

The module follows the application's Domain / Application / Infrastructure / Interfaces layers. `AppContainer` provides the services; the navigation entry owns the ViewModel. UI text uses English resources and Spanish translations, and the screens reuse the shared cards, top bar and pill buttons.

## Backend integration

The API shapes were checked against the deployed OpenAPI contract and `LernenLabs/adm-entreprenly-backend` source.

- Plans: `GET /api/v1/subscription-plans`.
- Panel and billing: `GET/PUT /api/v1/subscription-dashboard/{userId}`. Fiscal-only saves set `currentPlan` to null so the compatibility endpoint cannot activate or resume a subscription.
- Checkout: `POST /api/v1/subscriptions`; rejected attempts reuse the returned subscription through its `/payments` endpoint.
- Verification and history: `GET /api/v1/subscriptions/{id}` and its `/payments` endpoint. Only an approved latest payment and an active, unexpired subscription show activation.
- Renewal: `POST /api/v1/subscriptions/{id}/renewals` after the user confirms the summary.
- Scheduled cancellation/reactivation: update the dashboard's current plan status. The raw `/cancellations` endpoint cancels immediately and is not used for the paid-plan cancellation dialog.

The backend currently persists Free as an active subscription. Checkout closes only that Free row before creating Control; an existing paid subscription is never cancelled by this operation. Free remains the effective plan until the payment is approved.

## Payment behavior and limits

The deployed backend uses `FakePaymentGateway`; there are no real card charges. Use test card details such as `4111111111111111`, a future expiry and a three-digit CVV. The app sends a generated fake reference, brand and last four digits; it never submits or persists the full card number or CVV. Card entry is held in memory and cleared when leaving the payment form.

A pending or interrupted payment disables submission and offers status verification. Saved state retains the user, plan and subscription identifiers for recovery after recreation. The backend does not expose a lookup for a pending creation whose response was lost before returning its identifier, nor does it accept an idempotency key. That case remains unconfirmed rather than issuing another charge.

**US-22 backend correction required:** the current backend resets renewal to today plus 30 days and changes an active subscription to pending when renewal is rejected. `backend/subscription-renewal.patch` extends from the existing expiration and preserves access on rejected/pending renewal. It includes backend regression tests. Apply and test it in the backend repository, then deploy that change; the patch is not applied to the running service. The mobile summary follows the backend's existing 30-day monthly period and always reloads the actual expiration after payment.

The dashboard exposes Free when no active paid subscription exists; the API cannot distinguish an old cancelled subscription from an account that has always been Free. A separate terminal-status query is needed to show historical Cancelled/Expired badges. The camera card scanner and renewal push notification shown in the references are not part of the seven implemented stories and are not wired to native camera/notification services.

## Validation

```sh
bash ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
bash ./gradlew :app:connectedDebugAndroidTest
```

Unit tests cover fiscal validation, card validation, paid access expiration, billing saves that cannot activate plans, and safe upgrade/cancellation requests. The instrumented test exercises the Spanish UI with controlled API responses: selection, validation errors, declined payment, pending verification, activation, cancelled cancellation dialog, scheduled cancellation, reactivation, renewal and history. It does not charge or modify live backend accounts. Captures are written to the test emulator's `Download/entreprenly-subscription` directory.

To check the accompanying patch in the backend checkout:

```sh
git apply --check /path/to/adm-entreprenly-frontend/docs/backend/subscription-renewal.patch
git apply /path/to/adm-entreprenly-frontend/docs/backend/subscription-renewal.patch
./mvnw -Dtest=SubscriptionTests test
```

The backend patch tests require that repository's configured Java 26 environment and have not been executed as part of the Android checks.
