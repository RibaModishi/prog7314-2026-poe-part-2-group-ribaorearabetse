# Backend Handover — for Riba & Khensani

**From:** Khumo-Thato Chabeli
**Status:** Backend is built, tested, seeded, and running. Live and verified against
the real `campus-companion-d4052` Firebase project (not just theory — see "What's
already verified working" below).

This doc tells each of you exactly what exists, what you need from it, and what to do
next. Full endpoint-by-endpoint reference: [`API_DOCUMENTATION.md`](API_DOCUMENTATION.md).

---

## ⚠️ Read this first: getting the code

This project currently lives **only on my machine** (`campus-companion-api`), not on
GitHub yet. **You can't clone/pull it until it's pushed somewhere.** I'm holding off on
creating the GitHub repo until we agree on where the whole team's code should live
(one monorepo with the Android app, or a separate repo just for this API). Ping me and
I'll push it the same day we decide — it's a 2-minute job on my end.

Once it's up, `git clone` it and follow the **Setup** section in the main
[`README.md`](../README.md) (get your own copy of `firebase-service-account.json` from
the Firebase console, same steps I did).

---

## What's already verified working

I didn't just write this code — I ran it. Confirmed end-to-end against the real
Firebase project:

- ✅ Server boots and connects to Firestore + Firebase Auth (`GET /health` → `200 ok`)
- ✅ All endpoints respond with the documented JSON shapes
- ✅ Auth is enforced — no token → clean `401` JSON, not a crash
- ✅ Firestore seeded with **10 campus locations** (all 8 categories) and **8
  events/opportunities** (all 5 types) — good enough to demo right now
- ✅ Weather recommendation endpoint correctly filters locations by indoor/covered/
  air-conditioned attributes for a given condition
- ✅ Full Gradle build + unit test run passes (`./gradlew build`, `./gradlew test`)

---

## For Khensani — Android UI & API integration

You need the app to **display data from this API**, never touch Firestore directly.

### 1. Copy-paste-ready Retrofit kit

I've written the Retrofit layer for you already (matches your "Use Retrofit response
models supplied by Khumo" deliverable). It's in [`android-integration/`](android-integration/):

| File | What it is |
|------|------------|
| `ApiModels.kt` | Every data class you need (`CampusLocation`, `EventOrOpportunity`, `Favourite`, `UserProfile`, etc.) — field names match the API exactly |
| `ApiService.kt` | Retrofit interface, one function per endpoint |
| `RetrofitClient.kt` | Builds the Retrofit client and **automatically attaches your Firebase ID token** to every request — you don't manually handle auth headers anywhere in your screens |
| `README.md` | Gradle dependencies to add + a usage example |

Copy all three `.kt` files into your app module, fix the `package` line at the top of
each to match your project, and you're wired up.

### 2. Auth flow you need to implement on your side

This API does **not** issue its own login — it trusts Firebase Auth:

1. Your app signs the user in/up with `FirebaseAuth.getInstance()` (email/password —
   Firebase Console has this enabled already).
2. First time after sign-in, call `RetrofitClient.api.upsertProfile(...)` (hits
   `POST /api/auth/profile`) to create their profile row.
3. From then on, every API call automatically carries their Firebase ID token — the
   interceptor in `RetrofitClient.kt` handles this for you.
4. If a call needing auth comes back as `401`, it means the token expired or the user
   isn't signed in — redirect to your Sign In screen.

### 3. Which screens need which endpoints (matches your screen list from Planning & Design)

| Your screen | Endpoint(s) |
|---|---|
| Dashboard (weather card, recommendation card) | `GET /api/recommendations/weather` |
| Campus Navigator / map | `GET /api/locations`, `GET /api/locations?category=` |
| Location Details | `GET /api/locations/{id}`, `POST /api/favourites` (save button) |
| Events feed | `GET /api/events`, `GET /api/events?category=` |
| Event Details | `GET /api/events/{id}`, `POST /api/favourites` (save button) |
| Saved Items | `GET /api/favourites` — then match each `itemId` against `GET /api/locations` or `GET /api/events` results to show full details |
| Settings (language) | `PUT /api/users/me/settings` |
| Profile | `GET /api/users/me` |

### 4. Handling loading/error states (your deliverable)

Every failure comes back as one of `400/401/403/404/500`, always with the same JSON
shape (`{"error", "message", "status"}`). Use the `ex.toApiError()` helper in
`RetrofitClient.kt` to grab a human-readable `message` for your "no data"/error UI —
you don't need to write custom parsing per screen.

### 5. Running against a live backend while you build

Until this is deployed, run the API locally on my/your machine (`./gradlew run`) and
point `RetrofitClient.BASE_URL` at:
- `http://10.0.2.2:8090/` if you're using the Android **emulator** on the same PC running the API
- `http://<pc-lan-ip>:8090/` if you're testing on a **physical phone** on the same Wi-Fi

I'll update this once the API has a real deployed URL (Cloud Run/Render — see
README "Deployment" section) so you're not dependent on my laptop being on.

---

## For Riba — Business logic & recommendation rules

Your part is about the **rules**, and per your own plan doc's recommendation, those
rules run **inside this Ktor API** (not the Android app) — so you'll be writing Kotlin
in this same backend repo, not just consuming it.

### 1. Where your "first draft" logic already lives (you own refining it)

I built a working v1 of everything you're responsible for, so there's a running
system to improve rather than a blank page:

| Your responsibility | File | What's there now |
|---|---|---|
| Domain models/DTOs | `src/main/kotlin/.../models/Models.kt` + `Requests.kt` | `UserProfile`, `CampusLocation`, `EventOrOpportunity`, `Favourite`, `WeatherRecommendation` |
| Weather integration | `src/main/kotlin/.../services/WeatherService.kt` | Calls OpenWeather, normalises into `RAIN/HOT/COLD/WIND/CLEAR`. Falls back to a mock reading if `OPENWEATHER_API_KEY` isn't set yet |
| Recommendation engine | `src/main/kotlin/.../routes/RecommendationRoutes.kt` — see `suitableFor()` and `buildMessage()` | Simple boolean rule per condition (e.g. RAIN → `isIndoor \|\| isCovered`). **This is the part most worth your time** — it's intentionally basic |
| Location suitability scoring | Same file, `suitableFor()` | Currently a pass/fail filter, not a ranked score — you could add a scoring/sorting system |
| Event suitability / weather warnings | Not yet built | `EventOrOpportunity.isOutdoor` field exists and is seeded on 3 of the 8 sample events — you'd add the "Rain expected, bring an umbrella" style logic, likely as a new field on the `GET /api/events` response or a small new endpoint |
| Favourite business rules (no duplicates) | `src/main/kotlin/.../repositories/FavouriteRepository.kt` — `existsForUser()` | Duplicate-save prevention is already there; extend if you want richer rules |
| Date/time rules (future events first) | `src/main/kotlin/.../repositories/EventRepository.kt` — `findAll()` | Currently sorts by `startDateTime` ascending only; "hide past events" / better formatting is still open |
| Validation rules | `src/main/kotlin/.../util/Validation.kt` | Email, language, category, item type, coordinates, quiet level — add more here as needed |
| Edge cases (no network, empty lists, expired events, etc.) | Spread across `routes/` + `StatusPages.kt` | Empty Firestore queries already return `[]` cleanly rather than erroring; "expired event" handling is not yet special-cased |

### 2. Unit testing — already wired up, just add files

`kotlin.test` + `ktor-server-test-host` are already dependencies in `build.gradle.kts`.
There's a working example at
[`src/test/kotlin/.../util/ValidationTest.kt`](../src/test/kotlin/com/campuscompanion/api/util/ValidationTest.kt)
(6 passing tests) — copy its pattern for your own logic. Suggested files to add:

- `RecommendationRulesTest.kt` — does `suitableFor()` pick the right locations per condition?
- `EventSuitabilityTest.kt` — once you build the outdoor-event-warning logic
- `FavouriteRulesTest.kt` — duplicate prevention, invalid item types
- `EventOrderingTest.kt` — future-events-first sorting

Run everything with:
```bash
./gradlew test
```

### 3. A note on where to put new logic

If a rule is simple and stateless (e.g. "is this location suitable for this weather"),
a plain Kotlin object/function like `RecommendationRoutes.kt`'s `suitableFor()` is fine.
If it gets more involved (e.g. a real scoring algorithm with weights), pull it out into
its own file under a new `src/main/kotlin/.../businessrules/` package so it's easy to
unit test in isolation from the Ktor route itself — routes should stay thin (parse
request → call your logic → respond).

---

## Questions / blockers

If anything here doesn't match what you expected from the Planning & Design doc, or
you need an endpoint that doesn't exist yet, tell me directly rather than working
around it in your own layer — it's a 10-minute add on the API side and keeps
everyone's Firestore schema consistent.
