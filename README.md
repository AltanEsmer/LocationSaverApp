# 📍 Location Saver App

A simple and intuitive Android application that allows you to save your current location with custom names and view them in Google Maps.

## ✨ Features

- **Save Current Location**: Save your current location with a custom name
- **View Saved Locations**: See all your saved locations in a clean, organized list
- **Open in Google Maps**: Tap any location to open it directly in Google Maps
- **Delete Locations**: Remove locations you no longer need
- **Offline Storage**: All data is stored locally on your device
- **Privacy First**: Your location data never leaves your device

## 🚀 Getting Started

### Prerequisites
- Android 8.0 (API level 26) or higher
- Google Play Services (for Maps integration)
- Location services enabled

### Installation
1. Download the APK file
2. Enable "Install from unknown sources" in your device settings
3. Install the APK
4. Grant location permission when prompted

### First Use
1. Open the app
2. Grant location permission when prompted
3. Tap the **+** button to save your first location
4. Enter a name for your location
5. Tap "Save"

## 📱 Screenshots

*Screenshots would be added here showing the main interface, save dialog, and location list*

## 🏗️ Architecture

This app follows modern Android development practices:

- **MVVM Architecture**: Clean separation of concerns
- **Jetpack Compose**: Modern declarative UI
- **Room Database**: Local data persistence
- **Google Play Services**: Location and Maps integration
- **Material 3 Design**: Modern, accessible UI

## 📚 Documentation

- [User Guide](USER_GUIDE.md) - Complete user documentation
- [Developer Guide](DEVELOPER_GUIDE.md) - Technical documentation
- [Architecture Guide](ARCHITECTURE.md) - System architecture details

## 🔧 Development

### Building from Source
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build the project

### Dependencies
- Kotlin 1.9.0+
- Android SDK 24+
- Jetpack Compose
- Room Database
- Google Play Services

## 🔒 Privacy & Security

- **Local Storage Only**: All data is stored on your device
- **No Internet Required**: App works offline (except for Maps)
- **No Data Collection**: We don't collect any personal information
- **Encrypted Storage**: Data is protected using Android's built-in security

## 🐛 Troubleshooting

### Common Issues
- **Location not found**: Ensure GPS is enabled and you have a clear view of the sky
- **Permission denied**: Grant location permission in device settings
- **Maps not opening**: Install Google Maps from the Play Store

### Getting Help
- Check the [User Guide](USER_GUIDE.md) for detailed troubleshooting
- Review the [Developer Guide](DEVELOPER_GUIDE.md) for technical issues

## 🚧 Future Features

- Background location tracking
- Location categories and tags
- Export/import functionality
- Cloud backup integration
- Location sharing capabilities

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Support

For support, please check the documentation or open an issue in the repository.

## 🏆 Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Uses [Room Database](https://developer.android.com/training/data-storage/room)
- Integrates with [Google Play Services](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary)
- Follows [Material Design](https://material.io/design) guidelines

---

**Note**: This app is designed for personal use and stores all data locally on your device. No data is transmitted to external servers.