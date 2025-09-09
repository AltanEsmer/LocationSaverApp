# Location Saver App - Developer Guide

## Project Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Kotlin 1.9.0+
- Android SDK 24+ (Android 8.0)
- Google Play Services SDK

### Initial Setup
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build the project

### Dependencies Overview
```kotlin
// Core Android
implementation("androidx.core:core-ktx")
implementation("androidx.lifecycle:lifecycle-runtime-ktx")
implementation("androidx.activity:activity-compose")

// Compose
implementation(platform("androidx.compose:compose-bom"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Navigation
implementation("androidx.navigation:navigation-compose")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")

// Room Database
implementation("androidx.room:room-runtime")
implementation("androidx.room:room-ktx")
kapt("androidx.room:room-compiler")

// Location Services
implementation("com.google.android.gms:play-services-location")
implementation("com.google.android.gms:play-services-maps")

// Permissions
implementation("com.google.accompanist:accompanist-permissions")
```

## Code Structure

### Package Organization
```
com.example.locationtrackerapp/
├── data/                    # Data layer
│   ├── LocationEntity.kt   # Room entity
│   ├── LocationDao.kt      # Database access
│   └── LocationDatabase.kt # Database configuration
├── repository/              # Repository layer
│   └── LocationRepository.kt
├── service/                 # Service layer
│   └── LocationService.kt  # Location operations
├── viewmodel/              # Presentation layer
│   └── MainViewModel.kt    # UI state management
├── ui/                     # UI components
│   ├── MainScreen.kt       # Main screen
│   ├── SaveLocationDialog.kt
│   └── PermissionHandler.kt
└── MainActivity.kt         # Entry point
```

## Key Components

### 1. Data Layer

#### LocationEntity
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

#### LocationDao
```kotlin
@Dao
interface LocationDao {
    @Insert
    suspend fun insertLocation(location: LocationEntity): Long
    
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<LocationEntity>>
    
    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun deleteLocation(id: Long)
}
```

### 2. Service Layer

#### LocationService
```kotlin
class LocationService(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    suspend fun getCurrentLocation(): Location
    suspend fun getLastKnownLocation(): Location?
    fun hasLocationPermissions(): Boolean
}
```

### 3. Presentation Layer

#### MainViewModel
```kotlin
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    fun saveCurrentLocation(name: String)
    fun openLocationInMaps(location: LocationEntity): Intent?
    fun deleteLocation(locationId: Long)
}
```

## Development Workflow

### 1. Adding New Features

#### Database Changes
1. Update `LocationEntity` if needed
2. Update `LocationDao` with new queries
3. Increment database version in `LocationDatabase`
4. Add migration if needed

#### UI Changes
1. Create new Composable functions
2. Update `MainScreen` to include new UI
3. Add state management in `MainViewModel`
4. Test with different screen sizes

#### Service Changes
1. Update `LocationService` for new functionality
2. Update `LocationRepository` if needed
3. Update `MainViewModel` to use new services
4. Test permission handling

### 2. Testing

#### Unit Tests
```kotlin
@Test
fun `saveLocation should insert location with correct data`() = runTest {
    // Given
    val name = "Test Location"
    val latitude = 40.7128
    val longitude = -74.0060
    
    // When
    val result = repository.saveLocation(name, latitude, longitude)
    
    // Then
    assertThat(result).isGreaterThan(0)
}
```

#### UI Tests
```kotlin
@Test
fun saveLocationButton_click_opensDialog() {
    composeTestRule.setContent { MainScreen() }
    
    composeTestRule.onNodeWithText("Save Location").performClick()
    
    composeTestRule.onNodeWithText("Enter a name for this location:")
        .assertIsDisplayed()
}
```

### 3. Debugging

#### Location Issues
- Check device location settings
- Verify GPS is enabled
- Test with different accuracy settings
- Check permission status

#### Database Issues
- Use Room Inspector in Android Studio
- Check database file in device storage
- Verify entity annotations
- Test queries manually

#### UI Issues
- Use Compose Preview
- Test on different screen sizes
- Check state management
- Verify recomposition

## Build Configuration

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### APK Analysis
```bash
./gradlew analyzeDebugApk
```

## Performance Optimization

### Database
- Use `@Query` annotations for complex queries
- Implement proper indexing
- Use background threads for database operations
- Consider pagination for large datasets

### Location Services
- Use appropriate accuracy levels
- Implement proper lifecycle management
- Cache last known location
- Handle location updates efficiently

### UI
- Use `LazyColumn` for large lists
- Implement proper state management
- Avoid unnecessary recompositions
- Use `remember` for expensive calculations

## Security Considerations

### Data Protection
- All data stored locally
- No network transmission
- Encrypted storage (Android default)
- Proper permission handling

### Code Security
- No hardcoded secrets
- Proper input validation
- Secure database queries
- Error handling without data leakage

## Deployment

### APK Generation
1. Build release version
2. Sign with release key
3. Generate APK
4. Test on target devices

### Play Store Preparation
1. Create app listing
2. Add screenshots
3. Write description
4. Set up privacy policy
5. Configure app signing

## Common Issues & Solutions

### Build Issues

#### Gradle Sync Failed
- Check internet connection
- Update Android Studio
- Clear Gradle cache
- Check dependency versions

#### Room Compilation Error
- Verify entity annotations
- Check DAO method signatures
- Ensure proper imports
- Check database version

### Runtime Issues

#### Location Not Found
- Check device location settings
- Verify GPS status
- Test with different accuracy
- Check permission grants

#### Database Errors
- Verify Room configuration
- Check entity relationships
- Test database operations
- Review migration scripts

#### UI Crashes
- Check Compose version compatibility
- Verify state management
- Test on different devices
- Review error logs

## Future Enhancements

### Phase 3 - Background Location
```kotlin
class LocationTrackingService : Service() {
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            // Save location to database
        }
    }
}
```

### Additional Features
- Location categories
- Export/import functionality
- Cloud backup
- Location sharing
- Offline maps integration

## Code Style Guidelines

### Kotlin
- Use `val` over `var` when possible
- Prefer data classes
- Use extension functions
- Follow naming conventions

### Compose
- Use `@Composable` functions
- Implement proper state hoisting
- Use `remember` for state
- Follow Material Design guidelines

### Architecture
- Follow MVVM pattern
- Use dependency injection
- Implement proper error handling
- Write comprehensive tests

## Resources

### Documentation
- [Android Developer Guide](https://developer.android.com/guide)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Location Services](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary)

### Tools
- Android Studio
- Room Inspector
- Layout Inspector
- Compose Preview
- Device File Explorer

### Testing
- JUnit for unit tests
- Espresso for UI tests
- Compose Testing
- Mockito for mocking
