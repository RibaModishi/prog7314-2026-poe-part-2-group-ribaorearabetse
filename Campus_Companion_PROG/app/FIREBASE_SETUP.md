# Firebase setup

## Android client (`google-services.json`) — in the repo

`Campus_Companion_PROG/app/google-services.json` is **committed** so anyone who
clones GitHub (teammates or the marker) can run Create Account / Sign In without
extra files.

- Package name: `com.example.campus_companion_prog`
- Firebase project: `campus-companion-d4052`
- Email/Password must stay enabled in Firebase Console → Authentication

If Auth fails with “API key not valid”, confirm this file is present after clone,
then Sync / Rebuild in Android Studio.

## API server (`firebase-service-account.json`) — not in the repo

The Ktor backend needs a **service account private key**. That file stays
gitignored (it is a real secret).

1. Firebase Console → Project Settings → Service accounts → Generate new private key
2. Save as `firebase-service-account.json` at the **repo root** (same folder as
   root `build.gradle.kts`)
3. Or run the API with `AUTH_ENABLED=false` for local/debug testing only

Until `google-services.json` is present, the Google Services Gradle plugin is
skipped automatically so the project still syncs.
