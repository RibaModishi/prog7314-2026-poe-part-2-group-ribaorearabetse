# Firebase setup (required before Sign-In works)

Place these files **locally** (they are gitignored / must not be committed):

1. `Campus_Companion_PROG/app/google-services.json`
   - From Firebase Console → Project settings → Your Android app
   - Package name must be `com.example.campus_companion_prog`

2. `firebase-service-account.json` at the API repo root
   - Needed to run `./gradlew run` for the Ktor backend

Until `google-services.json` is present, the Google Services Gradle plugin is
skipped automatically so the project still syncs. Firebase Auth will only work
after that file is added and Gradle is synced again.
