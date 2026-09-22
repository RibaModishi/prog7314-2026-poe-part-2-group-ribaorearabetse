# Android Integration Kit (for Khensani)

Drop these three files into your Android app (adjust the package declaration at the
top of each to match your project's package name):

| File | Purpose |
|------|---------|
| `ApiModels.kt` | Data classes matching every API request/response shape exactly |
| `ApiService.kt` | Retrofit interface with one function per endpoint |
| `RetrofitClient.kt` | Builds the Retrofit client + attaches the Firebase ID token automatically to every request |

See [`../HANDOVER.md`](../HANDOVER.md) for the full explanation, auth flow, and how to
point this at a locally-running backend.

## Required Gradle dependencies (app-level `build.gradle.kts`)

```kotlin
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.1.0") // or via the Firebase BoM
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0") // for Tasks.await()
}
```

## Quick usage example

```kotlin
// Inside a ViewModel or repository, in a coroutine (viewModelScope.launch { ... })
try {
    val locations = RetrofitClient.api.getLocations(category = "CHILL_SPOT")
    // update UI state with locations
} catch (ex: HttpException) {
    val error = ex.toApiError()
    // show error?.message to the user
}
```
