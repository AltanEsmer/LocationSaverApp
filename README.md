<div align="center">
	<img src="icon.png" alt="Location Saver logo" width="180" />
	<h1>Location Saver</h1>
	<p>Save, organize, and open important locations directly in Google Maps.</p>
	<p>
		<img src="https://img.shields.io/badge/version-1.0-931019" alt="Version 1.0" />
		<img src="https://img.shields.io/badge/platform-Android-3DDC84" alt="Android" />
		<img src="https://img.shields.io/badge/license-MIT-555555" alt="MIT License" />
	</p>
</div>

## Overview

Location Saver is a privacy-focused Android application for saving named GPS locations on your device. Saved locations remain available offline and can be opened in Google Maps when navigation is needed.

## Features

- Save the current GPS position with a custom name
- Browse saved locations in a searchable list
- Open a saved location in Google Maps
- Rename or delete saved locations
- Store location data locally with Room
- Use a clean Material 3 interface built with Jetpack Compose

## App Information

| Property | Value |
| --- | --- |
| Version | 1.0 |
| Version code | 1 |
| Application ID | `com.example.locationtrackerapp` |
| Platform | Android |
| Minimum Android version | Android 7.0, API 24 |
| Target Android version | API 36 |
| Compile SDK | API 36 |
| License | MIT |

## Installation

1. Download the debug APK from [app/build/outputs/apk/debug/app-debug.apk](app/build/outputs/apk/debug/app-debug.apk), or build it from source.
2. Allow installation from this source when Android prompts you.
3. Install the APK and open Location Saver.
4. Grant location permission when requested.

The debug APK is intended for testing and is not a production release package.

## Using the App

1. Open Location Saver and grant location access.
2. Select the save-location action.
3. Enter a name for the location and save it.
4. Select a saved location to open it in Google Maps.
5. Use the available location actions to rename or remove an entry.

## Technical Architecture

- **Language:** Kotlin
- **UI:** Jetpack Compose and Material 3
- **Architecture:** MVVM with ViewModel and repository layers
- **Persistence:** Room database
- **Location services:** Google Play Services Location
- **Map integration:** Google Maps intent integration
- **Build system:** Gradle with Android Gradle Plugin

## Build From Source

### Requirements

- Android Studio with Android SDK 36
- JDK 11 or newer
- Android SDK Build-Tools 35 or newer

### Commands

From the project root:

```powershell
.gradlew.bat assembleDebug
```

The generated APK is located at `app/build/outputs/apk/debug/app-debug.apk`.

## Privacy

Location data is stored locally in the app's private database. The app does not provide a cloud sync service or collect personal location data. Google Maps handles any data required when a location is opened for mapping or navigation.

## Troubleshooting

### Location cannot be found

Enable device location services and grant the app location permission. GPS accuracy may be reduced indoors or in areas with limited signal.

### Google Maps does not open

Install Google Maps or choose another compatible mapping application when Android displays the available apps.

### Saved locations are missing after reinstalling

Uninstalling the app deletes its private local database. Keep the existing installation when updating, or create a backup before uninstalling. Backup and import support is planned for a future release.

## Documentation

- [Comprehensive User Guide](USER_GUIDE_COMPREHENSIVE.md)
- [Developer Guide](DEVELOPER_GUIDE.md)
- [Architecture Guide](ARCHITECTURE.md)
- [Location Testing Guide](LOCATION_TESTING_GUIDE.md)
- [Delivery Guide](DELIVERY_GUIDE.md)

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.