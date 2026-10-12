# DeliverLK

Fleet management Android app for Sri Lankan delivery and courier operators.
NIBM HND Software Engineering, Mobile Application Development module project.

## Features

- **Fleet Manager:** live map of all vehicles, delivery assignment, daily reports, driver safety records.
- **Driver:** delivery checklist, turn-by-turn navigation, photo proof of delivery, earnings tracker.
- **Customer:** live order tracking with distance and ETA.
- **Offline support:** driver's daily route cached locally and synced when back online.

## Tech Stack

- Kotlin, Jetpack Compose, Material 3
- MVVM architecture
- Room (local database)
- Firebase (Auth, Firestore, Storage)
- Google Maps Compose
- Route optimization API (OSRM / Google Routes API)
- SMS gateway (Dialog / Twilio)

## Project Structure

```
app/src/main/java/com/deliverlk/
├── core/        # design system, navigation, utilities
├── domain/      # models + repository interfaces
├── data/        # fake and real repository implementations
└── feature/     # auth, manager, driver, customer screens
```

## Getting Started

1. Clone the repository and open it in Android Studio.
2. Create a `local.properties` file in the project root (it is git-ignored) and add:
```
   MAPS_API_KEY=your_google_maps_api_key
```
3. Add your own `google-services.json` to the `app/` folder (not committed).
4. Sync Gradle and run on an emulator or device (min SDK 26).

Demo logins (fake data mode): `manager@deliverlk.lk`, `driver@deliverlk.lk`, `customer@deliverlk.lk` / `123456`

## Branch Rules

- Do not push directly to the default branch.
- Frontend work: `feature/frontend-ui`
- Backend work: `feature/backend-firebase`
- Merge through Pull Requests only.
- Never commit `local.properties` or `google-services.json`.

## Team

- Dinusha Malshan 
- ELISA-0525 
