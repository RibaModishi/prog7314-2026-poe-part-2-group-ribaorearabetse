# Demo video checklist (Part 2 — physical phone)

Record on a **physical phone** with voice-over. Embed the unlisted YouTube link in the README.

## Before recording
- [ ] API deployed or reachable on the same Wi‑Fi (`BASE_URL` = PC LAN IP + `:8090`)
- [ ] `google-services.json` installed; Firebase Email/Password enabled
- [ ] App installed via Android Studio Run (USB debugging)
- [ ] Seed data present (`./gradlew seed` on API if needed)

## Must show on camera
1. **SSO / Auth**
   - [ ] Get Started → Create Account (name, email, password)
   - [ ] Sign out → I already have an account → Sign In
   - [ ] Optional: Biometric prompt (if enabled in Settings)

2. **Settings**
   - [ ] Open Settings — profile name/email from API
   - [ ] Change language (en / zu / af) — UI updates + preference saved
   - [ ] Toggle notifications / biometric

3. **REST API round-trips (with visual proof)**
   - [ ] Navigate — locations load from API
   - [ ] Events — events/opportunities load from API
   - [ ] Save a location + event → appear under Saved
   - [ ] Remove a saved item
   - [ ] (Optional) Show Postman/Firebase console briefly for DB evidence

4. **Custom features**
   - [ ] Home → Check Weather → pick Rain (or Live)
   - [ ] Recommended spaces update
   - [ ] Navigate smart marker colours (green/blue/orange/grey)
   - [ ] Outdoor event weather banner

5. **Offline (if demoing)**
   - [ ] Airplane mode → save item → toast about queued sync
   - [ ] Reconnect → sync runs

## Closing
- [ ] Mention GitHub repo + green CI Actions run
- [ ] Mention team roles briefly (UI / API / business logic)
