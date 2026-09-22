# Campus Companion API Documentation

Owner: Khumo-Thato Chabeli (Backend, Database, Authentication & API)

Base URL (local): `http://localhost:8090` (port configurable via the `PORT` env var; default `8090`)

## Authentication

Endpoints marked **Auth: Required** need a Firebase ID token from the signed-in
user, sent as:

```
Authorization: Bearer <firebase-id-token>
```

The Android client obtains this token from `FirebaseAuth.getInstance().currentUser.getIdToken(false)`
after email/password sign-in. The API verifies it server-side with the Firebase Admin SDK
(`FirebaseAuth.verifyIdToken`) — the app never sends a raw password or manages sessions itself.

Endpoints marked **Auth: Public** are intentionally open (no token needed) so the Campus
Navigator and Events feed can be browsed before sign-in, matching the MyUoY "visitor access"
behaviour noted in our research.

## Standard error shape

Every error, from every endpoint, has this shape:

```json
{
  "error": "bad_request",
  "message": "Human-readable explanation",
  "status": 400
}
```

| HTTP status | `error` value       | Meaning                                            |
|-------------|----------------------|-----------------------------------------------------|
| 400         | `bad_request`        | Invalid/missing field, or malformed JSON body       |
| 401         | `unauthorized`       | Missing/invalid/expired Firebase ID token           |
| 403         | `forbidden`          | Valid token, but not allowed to touch this resource |
| 404         | `not_found`          | Resource does not exist                             |
| 500         | `service_unavailable`| Firebase/Firestore not configured (see Setup)       |
| 500         | `internal_server_error` | Unexpected server error                          |

## Endpoints

### Health

| Method | Endpoint  | Auth   | Purpose |
|--------|-----------|--------|---------|
| GET    | `/health` | Public | Reports server + Firebase status. Returns `200` when Firebase is initialised, `503` otherwise. |

### Auth & Profile

| Method | Endpoint                     | Auth     | Purpose |
|--------|------------------------------|----------|---------|
| POST   | `/api/auth/profile`          | Required | Create (first login) or update the signed-in user's profile. |
| GET    | `/api/users/me`              | Required | Fetch the signed-in user's profile. `404` if `POST /api/auth/profile` was never called. |
| PUT    | `/api/users/me/settings`     | Required | Update settings (currently `preferredLanguage`). |

**POST /api/auth/profile**
```json
// Request
{ "name": "Khumo-Thato Chabeli", "email": "student@example.ac.za", "preferredLanguage": "en" }

// 200 Response
{ "uid": "abc123", "name": "Khumo-Thato Chabeli", "email": "student@example.ac.za",
  "preferredLanguage": "en", "createdAt": 1730000000000, "updatedAt": 1730000000000 }
```

### Campus Locations (Campus Navigator)

| Method | Endpoint                            | Auth   | Purpose |
|--------|--------------------------------------|--------|---------|
| GET    | `/api/locations`                     | Public | All campus locations (map markers). |
| GET    | `/api/locations?category=STUDY_SPACE`| Public | Filtered by category. |
| GET    | `/api/locations/{id}`                | Public | One location's full details. |
| POST   | `/api/locations`                     | Public | Create a location (used for seeding/admin tooling; lock down before a public demo if desired). |

Valid `category` values: `LIBRARY`, `STUDY_SPACE`, `LAB`, `CAFETERIA`, `PARKING`,
`STUDENT_SUPPORT`, `LECTURE_VENUE`, `CHILL_SPOT`.

```json
// CampusLocation shape
{
  "id": "loc123", "name": "Main Library - Quiet Study Floor", "category": "LIBRARY",
  "description": "...", "latitude": -29.8587, "longitude": 31.0218,
  "openingHours": "Mon-Fri 07:00-22:00", "isIndoor": true, "isCovered": true,
  "hasAirConditioning": true, "hasWifi": true, "quietLevel": 5, "createdAt": 1730000000000
}
```

### Events & Opportunities

| Method | Endpoint                          | Auth   | Purpose |
|--------|------------------------------------|--------|---------|
| GET    | `/api/events`                      | Public | Combined events/opportunities feed. |
| GET    | `/api/events?category=BURSARY`     | Public | Filtered by type. |
| GET    | `/api/events?date=2026-10-01T00:00:00Z` | Public | Only items starting on/after this ISO-8601 date. |
| GET    | `/api/events/{id}`                 | Public | One event/opportunity's full details. |
| POST   | `/api/events`                      | Public | Create an event/opportunity (seeding/admin tooling). |

Valid `type` values: `CAMPUS_EVENT`, `BURSARY`, `INTERNSHIP`, `WORKSHOP`, `SOCIETY_ACTIVITY`.

```json
// EventOrOpportunity shape
{
  "id": "evt123", "title": "Welcome Week Braai & Social", "description": "...",
  "type": "CAMPUS_EVENT", "organiser": "SRC", "startDateTime": "2026-10-05T16:00:00Z",
  "endDateTime": "2026-10-05T20:00:00Z", "locationId": null, "externalUrl": null,
  "isOutdoor": true, "tags": ["social", "welcome"], "createdAt": 1730000000000
}
```

### Favourites (Saved Items)

| Method | Endpoint                    | Auth     | Purpose |
|--------|------------------------------|----------|---------|
| GET    | `/api/favourites`            | Required | List the signed-in user's saved locations/events. |
| POST   | `/api/favourites`            | Required | Save a location or event. Duplicate saves of the same item are idempotent (no duplicate rows). |
| DELETE | `/api/favourites/{id}`       | Required | Remove a saved item. `403` if it belongs to another user. |
| POST   | `/api/favourites/sync`       | Required | Replay a batch of offline-queued save/remove actions (see "Offline sync" below). |

```json
// POST /api/favourites request
{ "itemType": "LOCATION", "itemId": "loc123" }

// 201 Response
{ "id": "fav123", "userId": "abc123", "itemType": "LOCATION", "itemId": "loc123", "savedAt": 1730000000000 }
```

**Offline sync contract** — this is the server side of the Android app's Room-based
offline queue (see the Planning & Design doc's "Offline Save with Background Sync"
sequence diagram). When the device saves/removes an item with no connectivity, it queues
the action locally with a client timestamp; once connectivity returns, it POSTs the whole
queue here in one batch:

```json
// POST /api/favourites/sync request
{
  "actions": [
    { "action": "SAVE",   "itemType": "LOCATION", "itemId": "loc123", "clientTimestamp": 1730000000000 },
    { "action": "REMOVE", "itemType": "EVENT",     "itemId": "evt456", "clientTimestamp": 1730000005000 }
  ]
}

// 200 Response - one result per submitted action, in the order they were applied
{
  "syncResults": [
    { "itemType": "LOCATION", "itemId": "loc123", "action": "SAVE",   "success": true },
    { "itemType": "EVENT",    "itemId": "evt456", "action": "REMOVE", "success": true }
  ]
}
```
Actions are applied in `clientTimestamp` order. A failed individual action (e.g. the
referenced item was deleted server-side while offline) is reported with
`"success": false` and a `message`, without failing the whole batch.

### Weather Recommendations

| Method | Endpoint                                          | Auth   | Purpose |
|--------|----------------------------------------------------|--------|---------|
| GET    | `/api/recommendations/weather`                      | Public | Current weather + campus locations suited to it. |
| GET    | `/api/recommendations/weather?mockCondition=RAIN`   | Public | Force a condition for demoing/testing without a real weather key. |

```json
// 200 Response
{
  "condition": "RAIN",
  "temperatureCelsius": 18.0,
  "message": "Rain is expected (...). Indoor study spaces and libraries are recommended.",
  "recommendedLocationIds": ["loc1", "loc2"],
  "recommendedLocations": [ /* full CampusLocation objects */ ]
}
```
`condition` is one of `RAIN`, `HOT`, `COLD`, `WIND`, `CLEAR`. This endpoint calls
OpenWeather when `OPENWEATHER_API_KEY` is set; otherwise it returns a clearly-labelled
mock reading so it stays testable before a real key is obtained. **This first cut of the
scoring rule (`suitableFor()` in `RecommendationRoutes.kt`) is intentionally simple —
Riba owns refining it further per the team's role split.**

## Firestore data model

| Collection   | Doc ID            | Fields |
|--------------|--------------------|--------|
| `users`      | Firebase Auth UID  | `uid, name, email, preferredLanguage, createdAt, updatedAt` |
| `locations`  | auto-generated     | `name, category, description, latitude, longitude, openingHours, isIndoor, isCovered, hasAirConditioning, hasWifi, quietLevel, createdAt` |
| `events`     | auto-generated     | `title, description, type, organiser, startDateTime, endDateTime, locationId, externalUrl, isOutdoor, tags, createdAt` |
| `favourites` | auto-generated     | `userId, itemType, itemId, savedAt` |

## Testing evidence

A ready-to-import Postman collection covering every endpoint above (success cases +
400/401/403/404 failure cases) lives at [`postman/CampusCompanion.postman_collection.json`](../postman/CampusCompanion.postman_collection.json).
See the main [README](../README.md#testing-the-api) for how to run it and capture screenshots.
