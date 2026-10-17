# SalaryMate Android

A native Kotlin Android salary calculator for offline use.

## Features
- Basic salary, allowances, overtime, deductions, and advance/loan deduction
- Calculates gross and take-home salary
- Save one record per salary month
- View and delete saved salary records
- Dark mode
- Local storage using Android SharedPreferences
- No internet permission or account required

## Build an APK using only your phone

This project includes a GitHub Actions workflow to compile an APK in the cloud. You will need a GitHub account and to upload this source project to a repository.

1. Download and extract this ZIP using your phone's Files app.
2. In your browser, open https://github.com and create/sign in to an account.
3. Create a new repository named `SalaryMate`, preferably private.
4. Upload the extracted project files and folders to the repository. Ensure `.github/workflows/android-apk.yml` is included. If GitHub's mobile upload interface hides dot-folders, use the browser's desktop-site option.
5. Open the repository's Actions tab and select `Build SalaryMate APK`.
6. If the workflow has not run automatically, choose `Run workflow`.
7. When the run succeeds, open it and download the `SalaryMate-debug-APK` artifact.
8. Extract the downloaded artifact ZIP; it contains `app-debug.apk`.
9. Tap the APK to install. Android may ask you to allow installs from your browser or Files app.

The cloud workflow uses Gradle to compile the APK. No laptop is required, but the phone-based upload steps can be fiddly. Do not share your GitHub password or recovery codes with anyone.

## Build locally
The project can also be opened in Android Studio and built with Gradle.

Package: `com.codexvision.salarymate`
Minimum Android: Android 6.0 (API 23)
Target SDK: 35
