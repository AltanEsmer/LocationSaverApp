# Location Saver App - Architecture Documentation

## Overview
The Location Saver App is an Android application built with Kotlin and Jetpack Compose that allows users to save their current location with custom names and view them in Google Maps. The app follows modern Android development practices with MVVM architecture, Room database, and reactive UI updates.

## Architecture Pattern
The app follows the **MVVM (Model-View-ViewModel)** architecture pattern with the following layers:

### 1. Data Layer
- **LocationEntity**: Room entity representing a saved location
- **LocationDao**: Data Access Object for database operations
- **LocationDatabase**: Room database configuration
- **LocationRepository**: Repository pattern for data access abstraction

### 2. Service Layer
- **LocationService**: Handles location operations using Google Play Services

### 3. Presentation Layer
- **MainViewModel**: ViewModel managing UI state and business logic
- **MainScreen**: Main UI composable
- **SaveLocationDialog**: Dialog for naming locations
- **PermissionHandler**: Handles location permission requests

## Key Components

### Database (Room)
```kotlin
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)
```

### Location Service
- Uses `FusedLocationProviderClient` for accurate location retrieval
- Handles permission checks
- Provides both current location and last known location methods

### UI Components
- **Material 3 Design**: Modern Material Design components
- **Jetpack Compose**: Declarative UI framework
- **Reactive Updates**: Flow-based state management
- **Permission Handling**: User-friendly permission requests

## Data Flow

1. **User Interaction**: User taps "Save Location" button
2. **Permission Check**: App verifies location permissions
3. **Location Retrieval**: LocationService gets current location
4. **User Input**: Dialog prompts for location name
5. **Data Persistence**: Repository saves to Room database
6. **UI Update**: Compose UI reactively updates with new data

## Dependencies

### Core Android
- `androidx.core:core-ktx`
- `androidx.lifecycle:lifecycle-runtime-ktx`
- `androidx.activity:activity-compose`

### UI Framework
- `androidx.compose.bom`
- `androidx.compose.ui`
- `androidx.compose.material3`
- `androidx.navigation:navigation-compose`

### Database
- `androidx.room:room-runtime`
- `androidx.room:room-ktx`
- `androidx.room:room-compiler` (kapt)

### Location Services
- `com.google.android.gms:play-services-location`
- `com.google.android.gms:play-services-maps`

### Permissions
- `com.google.accompanist:accompanist-permissions`

## Security & Privacy

### Data Storage
- All location data is stored locally on the device
- No data is transmitted to external servers
- Database is encrypted by default on Android

### Permissions
- `ACCESS_FINE_LOCATION`: For precise location data
- `ACCESS_COARSE_LOCATION`: For approximate location data
- `INTERNET`: For Google Maps integration

## Error Handling

### Location Errors
- Permission denied scenarios
- Location unavailable errors
- Network connectivity issues

### UI Error States
- Loading indicators during location retrieval
- Error messages with user-friendly descriptions
- Retry mechanisms for failed operations

## Testing Strategy

### Unit Tests
- Repository layer testing
- ViewModel logic testing
- Service layer testing

### Integration Tests
- Database operations
- Location service integration
- UI component testing

## Performance Considerations

### Database
- Room database with efficient queries
- Background thread operations
- Reactive updates with Flow

### Location Services
- High accuracy location requests
- Efficient permission checking
- Proper lifecycle management

### UI
- LazyColumn for efficient list rendering
- State management with StateFlow
- Compose recomposition optimization

## Future Enhancements

### Phase 3 - Background Location Tracking
- Foreground service implementation
- Periodic location updates
- Notification management
- Battery optimization

### Additional Features
- Location categories/tags
- Export/import functionality
- Cloud backup integration
- Location sharing capabilities

## Build Configuration

### Gradle Setup
- Kotlin 1.9.0+
- Compose BOM for version management
- Kapt for Room annotation processing
- Target SDK 36, Min SDK 24

### ProGuard
- Release builds include ProGuard rules
- Room database obfuscation
- Location service optimization

## Deployment

### APK Generation
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

## Troubleshooting

### Common Issues
1. **Location not found**: Check device location settings
2. **Permission denied**: Guide user through settings
3. **Maps not opening**: Verify Google Maps installation
4. **Database errors**: Check Room configuration

### Debug Information
- Enable location logging in debug builds
- Database inspection tools
- Compose preview for UI testing
