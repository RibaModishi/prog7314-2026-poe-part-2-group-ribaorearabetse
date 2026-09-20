# Campus Companion 

Campus Companion is a multi-platform mobile and backend solution designed to streamline student life. It provides students with real-time campus navigation, event discovery, weather-aware recommendations, and personalized saved items through a Kotlin-based Android interface and a high-performance Ktor REST API backend.

---

## Table of Contents
- [Project Overview](#-project-overview)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Backend & API Architecture](#-backend--api-architecture)
- [Android Frontend Architecture](#-android-frontend-architecture)
- [Offline Capability & Synchronization](#-offline-capability--synchronization)
- [Automated Testing & CI/CD](#-automated-testing--cicd)
- [Setup & Installation](#-setup--installation)
- [Firebase & Environment Setup](#-firebase--environment-setup)
- [Team Roles & Responsibilities](#-team-roles--responsibilities)
- [Rubric & Submission Evidence](#-rubric--submission-evidence)

---

## Project Overview

Campus Companion bridges the gap between campus resources and students by providing a central digital hub. The system consists of:
1. **Android Application**: Built with Kotlin and Jetpack Compose, offering an intuitive UI for navigation, weather updates, and event tracking.
2. **Ktor REST API**: A lightweight backend built in Kotlin providing endpoints for user management, event data, and campus location endpoints. Android app never  [Ktor](https://ktor.io) 3.5.2 (Netty engine)
talks to Firestore directly, it only talks to this API.
3. **Database & Auth**: Firebase Firestore for real-time cloud persistence and Firebase Authentication for secure user sessions.
4.  **Full endpoint reference:** [`docs/API_DOCUMENTATION.md`]

---
##  Key Features

* ** Campus Navigator**: Smart map markers showing key locations, lecture halls, and facilities across campus.
* ** Events & Opportunities**: Browse, search, and save upcoming campus events, academic deadlines, and social gatherings.
* ** Weather-Aware Recommendations**: Integrates live weather data to give context-aware suggestions (e.g., indoor vs. outdoor activity recommendations).
* ** Saved Items**: Allows students to bookmark key events and places for quick offline access.
* ** Secure Authentication**: Firebase Auth integration supporting email/password sign-in and session security.

---

##  System Architecture

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

---

##  Tech Stack

* **Mobile Frontend**: Kotlin, Jetpack Compose, Material Design 3, Retrofit, Coroutines & Flow.
* **Backend API**: Kotlin, Ktor 3.5.2 (Netty engine), ContentNegotiation, Kotlinx Serialization.
* **Database & Auth**: Firebase Firestore (NoSQL), Firebase Authentication (Email/Password), Firebase Admin SDK.
* **External Services**: OpenWeather API (Live weather data).
* **CI/CD Pipeline**: GitHub Actions (`.github/workflows/ci.yml`) targeting Android and Ktor modules.

---

##  Backend & API Architecture

The Ktor API manages user data, event persistence, and location queries via JSON endpoints.

### Key API Endpoints

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/health` | Server health and Firebase connection diagnostics | No |
| `GET` | `/api/events` | Retrieves all upcoming campus events | No |
| `GET` | `/api/events/{id}` | Retrieves detailed information for a specific event | No |
| `GET` | `/api/locations` | Fetches campus map coordinates and markers | No |
| `POST` | `/api/saved` | Saves an event or location to the user's saved list | Yes (Bearer Token) |
| `GET` | `/api/weather` | Proxies weather condition requests | No |

*Full endpoint specifications: [`docs/API_DOCUMENTATION.md`](docs/API_DOCUMENTATION.md)*  
*Backend handover guidelines: [`docs/HANDOVER.md`](docs/HANDOVER.md)*

---


## Repository Structure
```
.
├── .github/
│   └── workflows/
│       └── ci.yml                             # GitHub Actions CI pipeline
├── Campus_Companion_PROG/                     # Android App (Mobile Frontend Module)
│   ├── app/
│   │   └── src/
│   │       ├── main/java/                     # Kotlin source code & Jetpack Compose UI
│   │       └── res/                           # UI resources & assets
│   ├── build.gradle.kts
│   └── gradlew
├── src/main/kotlin/com/campuscompanion/api/    # Ktor Backend API Module
│   ├── Application.kt                         # Entry point (embeddedServer & plugin wiring)
│   ├── config/                                # Environment-driven config & Firebase Admin setup
│   ├── plugins/                               # Serialization, CORS, error handling, routing
│   ├── models/                                # Domain models & request DTOs
│   ├── repositories/                          # Firestore reads/writes
│   ├── routes/                                # Feature route handlers
│   ├── services/                              # OpenWeather API integration
│   └── data/                                  # SeedData.kt for locations & events
├── docs/                                      # API documentation & handover notes
├── postman/                                   # Postman test collection
├── build.gradle.kts                           # Root build script
├── gradlew                                    # Gradle wrapper
└── README.md                                  # Combined technical documentation
```
---

##  Offline Capability(POE) & Synchronization

To ensure smooth operation during network loss:
1. **Local Caching**: Critical UI details (saved events and cached location markers) are stored locally on the Android device.
2. **Graceful Fallbacks**: Offline states trigger non-intrusive banner notifications while serving cached content.
3. **Optimistic Updates**: Bookmarks and user selections update locally on the UI immediately and synchronize with the Ktor backend when connection is restored.

---

##  Automated Testing & CI/CD

Continuous Integration is executed using **GitHub Actions** (`.github/workflows/ci.yml`).

The workflow automatically executes on pushes and pull requests to `main`, `master`, and `develop`:
* **Backend Tests Job**: Configures JDK 17, sets `GRADLE_OPTS`, compiles Ktor sources, and runs server-side unit tests (`./gradlew test`).
* **Android Tests Job**: Configures JDK 17, sets up Android SDK (`cmdline-tools-version: 11076708`), compiles Android modules, executes unit tests (`./gradlew testDebugUnitTest`), and compiles the debug APK (`./gradlew assembleDebug`).

---

##  Setup & Prerequisites

### Prerequisites
- **JDK 17+** (The included Gradle wrapper pins Gradle 9.7.1, supporting JDK 17–26).
- **Android Studio Ladybug** or newer.
- **Firebase Project** (Configured for project ID `campus-companion-d4052`).

### Firebase Credentials Setup
1. Open [Firebase Console](https://console.firebase.google.com/) → **Project Settings** → **Service accounts**.
2. Click **Generate new private key** to download your JSON credentials.
3. Save the key as `firebase-service-account.json` in the project root directory (same folder as root `build.gradle.kts`). *Note: This file is git-ignored for security.*
4. Enable **Firestore Database** and **Authentication** (Email/Password sign-in method) in Firebase Console.
*(Refer to `firebase-service-account.example.json` for structure details).*

### Environment Variables

| Variable | Default | Purpose |
| :--- | :--- | :--- |
| `PORT` | `8090` | Ktor HTTP server port |
| `FIREBASE_CREDENTIALS_PATH` | `firebase-service-account.json` | Path to service-account JSON |
| `FIREBASE_PROJECT_ID` | `campus-companion-d4052` | Firebase project ID |
| `ALLOWED_CORS_ORIGINS` | `*` | Allowed CORS origins |
| `AUTH_ENABLED` | `true` | Set `false` **only** for local debug testing without tokens |
| `OPENWEATHER_API_KEY` | *(unset)* | OpenWeather API key for live context recommendations |

---


## Running locally & Backend Server

```bash
# Windows
.\gradlew.bat run

# macOS/Linux
./gradlew run
```

The server starts on `http://localhost:8090` (or `$PORT`). Confirm it's healthy:

```bash
curl http://localhost:8090/health
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
2. Set the collection variables `baseUrl` (e.g. `http://localhost:8090`) and `idToken`
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

---

##  App Screenshots & UI Evidence

Below is a breakdown of the core user interface and features implemented in the Campus Companion Android application:

| Feature / Screen | Preview | Description |
| :--- | :---: | :--- |
| **Authentication & Sign-In** | ![Sign In Screen]<img width="1080" height="2424" alt="image" src="https://github.com/user-attachments/assets/06b7f5ae-dcf5-4e78-a6cd-80ab1d1d7e22" />
 | Secure student login and registration backed by Firebase Auth tokens. |
| **User Profile** | <img width="1080" height="2424" alt="image" src="https://github.com/user-attachments/assets/d55fa015-856d-4cf1-9e40-9ff0e952d450" />
 | Displays student details, account information, and personal preferences. |
| **Campus Navigator (Map)** | *(Paste image here)* | Interactive map view displaying campus landmarks, lecture halls, and facilities with custom markers. |
| **Events & Opportunities** | *(Paste image here)* | List of upcoming campus events and academic deadlines with filtering by category. |
| **Saved Items (Bookmarks)** | *(Paste image here)* | Personal bookmarking view with local offline caching support for quick access without network. |
| **App Settings** | <img width="1080" height="2424" alt="image" src="https://github.com/user-attachments/assets/dd72c00b-643c-434b-8e14-859d6915d1ce" />
 | Application configuration options including language options, theme toggles, and notification settings. |
| **GitHub Actions CI/CD** | *(Paste image here)* | Automated build and test pipeline status showing green builds for Android and Backend jobs. |
## Demo & Rubric Evidence 

- Demo Video: [link]
- APL Documentation: [`docs/API_DOCUMENTATION.md`]
- Postman Collection: 
