# Campus Companion API

Custom Kotlin **Ktor** REST API + **Firebase Firestore** database for the *Campus
Companion* Android app (PROG7314). This repo covers **Khumo-Thato Chabeli's part**
of the project: Backend, Database, Authentication, and API — the Android app never
talks to Firestore directly, it only talks to this API.

- **Language/framework:** Kotlin, [Ktor](https://ktor.io) 3.5.2 (Netty engine)
- **Database:** Firebase Firestore (NoSQL, cloud-hosted)
- **Auth:** Firebase Authentication (email/password); every user-scoped request is
  verified server-side via a Firebase ID token
- **Full endpoint reference:** [`docs/API_DOCUMENTATION.md`](docs/API_DOCUMENTATION.md)
- **Handover doc for the rest of the team:** [`docs/HANDOVER.md`](docs/HANDOVER.md) (Riba: business logic/testing, Khensani: Android/Retrofit integration kit)

## Architecture at a glance

```
Android App (Kotlin)
   │  Retrofit + Firebase Auth ID token
   ▼
Campus Companion API (this repo - Ktor / Kotlin)
   │  Firebase Admin SDK
   ▼
Firebase (Firestore + Authentication)
```

The Android app authenticates users directly against Firebase Auth (client SDK),
then sends the resulting ID token to this API on every request that needs to know
*who* is calling. This API is the **only** thing that reads/writes Firestore — the
app never holds Firestore credentials.

## Project layout

```
src/main/kotlin/com/campuscompanion/api/
├── Application.kt          - entry point (embeddedServer + plugin wiring)
├── config/                 - env-driven config, Firebase Admin SDK init
├── plugins/                - Ktor plugins: serialization, CORS, error handling, auth helper, routing
├── models/                 - data classes (domain models + request DTOs)
├── repositories/           - Firestore reads/writes, one per collection
├── routes/                 - route handlers, grouped by feature area
├── services/               - WeatherService (OpenWeather integration)
└── data/                   - SeedData.kt, demo data for locations/events
docs/API_DOCUMENTATION.md   - full endpoint reference
postman/                    - Postman collection covering every endpoint
```

## Setup

### 1. Prerequisites

- JDK 17+ (any recent JDK works; the wrapper pins Gradle 9.7.1 which supports JDK 17-26)
- A Firebase project (this repo is configured for project ID `campus-companion-d4052`
  — see [Firebase Console](https://console.firebase.google.com/))

### 2. Get Firebase credentials

1. Firebase Console → your project → ⚙️ **Project Settings** → **Service accounts**
2. **Generate new private key** → downloads a JSON file
3. Save it as `firebase-service-account.json` in the **project root** (same folder as
   `build.gradle.kts`). This file is already git-ignored — **never commit it**.
4. In the same Firebase project, enable:
   - **Firestore Database** (production or test mode is fine for coursework)
   - **Authentication → Sign-in method → Email/Password**

A placeholder showing the expected shape is at
[`firebase-service-account.example.json`](firebase-service-account.example.json).

### 3. Environment variables (all optional, sensible defaults provided)

| Variable                  | Default                          | Purpose |
|----------------------------|-----------------------------------|---------|
| `PORT`                    | `8080`                            | Server port |
| `FIREBASE_CREDENTIALS_PATH`| `firebase-service-account.json`  | Path to the service-account JSON |
| `FIREBASE_PROJECT_ID`     | `campus-companion-d4052`          | Firebase project ID (diagnostics/logging) |
| `ALLOWED_CORS_ORIGINS`    | `*`                                | Comma-separated allowed origins, or `*` |
| `AUTH_ENABLED`            | `true`                            | Set `false` **only** for local testing without real tokens (see below) |
| `OPENWEATHER_API_KEY`     | *(unset → mock weather)*          | [OpenWeather](https://openweathermap.org/api) key for live weather recommendations |
| `CAMPUS_LATITUDE` / `CAMPUS_LONGITUDE` | Durban coords | Coordinates used for the weather lookup |

## Running locally

```bash
# Windows
.\gradlew.bat run

# macOS/Linux
./gradlew run
```

The server starts on `http://localhost:8080` (or `$PORT`). Confirm it's healthy:

```bash
curl http://localhost:8080/health
```

- `"status": "ok"` → Firebase is configured correctly.
- `"status": "degraded"` (HTTP 503) → credentials are missing/invalid; every other
  endpoint will return a clear `500 service_unavailable` JSON error until this is fixed.
  This is intentional: the server still boots so teammates can pull the repo and see
  it run before Firebase is wired up.

### Testing without real Firebase ID tokens

Firebase ID tokens are short-lived and normally come from the Android app's sign-in
flow. For quick backend-only testing, set `AUTH_ENABLED=false` and send any string as
an `X-Debug-Uid` header instead of `Authorization: Bearer ...` — **this must stay
`true` (default) for anything resembling a real/graded deployment.**

To test with a **real** token instead, sign in via the Firebase Auth REST API using your
project's Web API key (Firebase Console → Project Settings → General → Web API Key):

```bash
curl -X POST "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=YOUR_WEB_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@student.ac.za","password":"Password123!","returnSecureToken":true}'
```
Copy the `idToken` from the response into the `idToken` collection variable in Postman.

## Seeding sample data

Populates Firestore with 10 campus locations (all 8 categories) and 8 events/opportunities
(all 5 types), so the API is demo-ready immediately:

```bash
./gradlew seed
```

Safe to re-run — it skips seeding a collection that's already non-empty.

## Testing the API

1. Import [`postman/CampusCompanion.postman_collection.json`](postman/CampusCompanion.postman_collection.json) into Postman.
2. Set the collection variables `baseUrl` (e.g. `http://localhost:8080`) and `idToken`
   (see above).
3. Run requests top-to-bottom; save a screenshot of each folder's results (or use
   Postman's "Run Collection" for one combined report) as your API-testing evidence.
4. The collection includes deliberate failure cases (missing auth, invalid category,
   etc.) so you can show `400`/`401`/`404` responses too, not just happy paths.

## Deployment

Any container-friendly host works since this is a plain Ktor/Netty app. Two straightforward
options for this module:

**Option A — Google Cloud Run (recommended, same ecosystem as Firebase)**
```bash
# from the project root
gcloud run deploy campus-companion-api \
  --source . \
  --region europe-west1 \
  --allow-unauthenticated \
  --set-env-vars FIREBASE_PROJECT_ID=campus-companion-d4052
```
Cloud Run builds the included `Dockerfile` automatically. Upload the real
`firebase-service-account.json` as a Secret Manager secret and mount it at
`/app/firebase-service-account.json` (Cloud Run → Edit & Deploy → Variables & Secrets),
rather than baking it into the image.

**Option B — Render / Railway (simplest free-tier option)**
- New Web Service → connect this repo → it detects the `Dockerfile` automatically.
- Add `firebase-service-account.json` as a "Secret File" and set
  `FIREBASE_CREDENTIALS_PATH` to wherever the host mounts it.
- Set `PORT` if the platform requires a specific value (most inject it automatically).

## Team ownership (from the Revised Task 1 Plan)

This repo implements the **Backend, Database, Authentication & API** column below;
see the Planning & Design / Revised Task 1 Plan documents for the full breakdown.

| Owner    | Area |
|----------|------|
| Khumo-Thato Chabeli | Kotlin Ktor backend, Firestore, Firebase Auth token validation, all REST endpoints, seed data, API docs & Postman evidence *(this repo)* |
| Khensani Maluleke   | Android UI/UX, screens, navigation, design system, README/screenshots (Android app repo) |
| Orearabetse Riba    | Domain models/DTOs, weather-recommendation business rules, validation rules, unit tests (Android app repo / shared logic) |
