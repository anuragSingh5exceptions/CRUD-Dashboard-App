# Cliently — Android CRUD Dashboard

A small, modern customer-management Android app built with Kotlin and Jetpack Compose.

## Features

- Login with input validation and a persisted session
- Dashboard totals for customers, active accounts, and leads
- Create, read, update, delete, and search customer records
- Duplicate-email protection and required-field validation
- Local persistence with `SharedPreferences` and JSON (no server required)
- Confirmation prompts for delete and logout actions

## Demo login

Use any valid email address and any password containing at least 6 characters. The login form is prefilled for convenience:

- Email: `admin@cliently.app`
- Password: `password`

## Run

Open this folder in Android Studio and run the `app` configuration, or build from a terminal:

```bash
./gradlew assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Notes

Authentication is intentionally local/demo-only. For production, replace `AppController.login` with a backend or identity provider and store tokens using encrypted storage. Customer data is stored only on the device.
