package com.example.locationtrackerapp.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationtrackerapp.R
import com.example.locationtrackerapp.data.LocationEntity
import com.example.locationtrackerapp.ui.theme.*
import com.example.locationtrackerapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * The app's single screen: a branded header (location test + search) above a
 * clean, scrollable list of saved locations. Tapping a location opens it
 * directly in Google Maps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenAdvanced(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var showAddChooser by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showAddFromLinkDialog by remember { mutableStateOf(false) }
    var showLocationTestDialog by remember { mutableStateOf(false) }
    var locationPendingDelete by remember { mutableStateOf<LocationEntity?>(null) }
    var locationPendingRename by remember { mutableStateOf<LocationEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Subtle, non-intrusive feedback for saves.
    LaunchedEffect(uiState.lastSavedLocationId) {
        if (uiState.lastSavedLocationId != null) {
            scope.launch { snackbarHostState.showSnackbar("Location saved") }
            viewModel.clearLastSavedLocationId()
        }
    }

    // Concise, non-technical error feedback.
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(friendlyErrorMessage(message)) }
            viewModel.clearError()
        }
    }

    fun closeSearch() {
        isSearchActive = false
        searchQuery = ""
        viewModel.searchLocations("")
        keyboardController?.hide()
    }

    Scaffold(
        containerColor = BackgroundWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddChooser = true },
                shape = CircleShape,
                containerColor = BrandRed700,
                contentColor = Color.White,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Save current location")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppHeader(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = {
                    searchQuery = it
                    viewModel.searchLocations(it)
                },
                onOpenSearch = { isSearchActive = true },
                onCloseSearch = { closeSearch() },
                onLocationTestClick = { showLocationTestDialog = true }
            )

            if (savedLocations.isEmpty()) {
                EmptyLocationsState(isSearching = isSearchActive && searchQuery.isNotBlank())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = savedLocations,
                        key = { _, location -> location.id }
                    ) { index, location ->
                        LocationRow(
                            index = index,
                            location = location,
                            onClick = {
                                viewModel.openLocationInMaps(location)?.let { intent ->
                                    context.startActivity(intent)
                                }
                            },
                            onRename = { locationPendingRename = location },
                            onDelete = { locationPendingDelete = location }
                        )
                    }
                }
            }
        }
    }

    if (showAddChooser) {
        AddLocationChooserDialog(
            onDismiss = { showAddChooser = false },
            onUseCurrentLocation = {
                showAddChooser = false
                showSaveDialog = true
            },
            onPasteLink = {
                showAddChooser = false
                showAddFromLinkDialog = true
            }
        )
    }

    if (showSaveDialog) {
        SaveLocationDialog(
            isSaving = uiState.isLoading,
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                viewModel.saveCurrentLocation(name)
                showSaveDialog = false
            }
        )
    }

    if (showAddFromLinkDialog) {
        AddLocationFromLinkDialog(
            onDismiss = { showAddFromLinkDialog = false },
            onSave = { name, link ->
                viewModel.saveLocationFromLink(name, link)
                showAddFromLinkDialog = false
            }
        )
    }

    if (showLocationTestDialog) {
        LocationTestDialog(
            viewModel = viewModel,
            onDismiss = { showLocationTestDialog = false }
        )
    }

    locationPendingRename?.let { location ->
        RenameLocationDialog(
            currentName = location.name,
            onDismiss = { locationPendingRename = null },
            onConfirm = { newName ->
                viewModel.renameLocation(location.id, newName)
                locationPendingRename = null
            }
        )
    }

    locationPendingDelete?.let { location ->
        DeleteLocationDialog(
            locationName = location.name,
            onDismiss = { locationPendingDelete = null },
            onConfirm = {
                viewModel.deleteLocation(location.id)
                locationPendingDelete = null
            }
        )
    }
}

/**
 * Turns a raw/technical error message into a short, user-facing one.
 */
private fun friendlyErrorMessage(raw: String): String {
    return when {
        raw.contains("permission", ignoreCase = true) -> "Location permission is needed"
        raw.contains("find a location in that link", ignoreCase = true) -> raw
        raw.contains("link", ignoreCase = true) -> "Couldn't read that link"
        raw.contains("location", ignoreCase = true) -> "Unable to get location"
        else -> "Something went wrong"
    }
}

/**
 * Small chooser shown from the "+" button: save the device's current
 * location, or add one by pasting a Google Maps link.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLocationChooserDialog(
    onDismiss: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onPasteLink: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Add Location",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                AddOptionRow(
                    title = "Use current location",
                    subtitle = "Save where you are right now",
                    onClick = onUseCurrentLocation
                )
                Spacer(modifier = Modifier.height(4.dp))
                AddOptionRow(
                    title = "Paste a Maps link",
                    subtitle = "Add a place shared from Google Maps",
                    onClick = onPasteLink
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun AddOptionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

/**
 * Branded header: dark-red gradient surface holding the centered logo, with
 * the location-test control top-left and a compact, expandable search
 * control top-right.
 */
@Composable
private fun AppHeader(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onLocationTestClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = BrandRedGradient,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(120)) },
                label = "header_controls"
            ) { searchActive ->
                if (searchActive) {
                    SearchField(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onClose = onCloseSearch
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderIconButton(
                            icon = Icons.Default.LocationOn,
                            contentDescription = "Test location services",
                            onClick = onLocationTestClick
                        )
                        HeaderIconButton(
                            icon = Icons.Default.Search,
                            contentDescription = "Search saved locations",
                            onClick = onOpenSearch
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_uriel),
                contentDescription = "Uriel Cafe logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 110.dp)
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.16f)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                    cursorBrush = SolidColor(BrandRed700),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                "Search locations",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                        innerTextField()
                    }
                )
            }
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close search",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * A single saved-location row: a small numbered badge, the location name,
 * and a subtle delete control. Tapping opens Google Maps; long-pressing
 * opens the rename dialog.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LocationRow(
    index: Int,
    location: LocationEntity,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction("Rename") { onRename(); true }
                )
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onRename
            ),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCard,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = CircleShape,
                color = BrandRedTint
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandRed700
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = location.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete ${location.name}",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyLocationsState(isSearching: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = BrandRedTint
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = BrandRed700,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSearching) "No matching locations" else "No saved locations yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            if (!isSearching) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap + to save your current location or paste a Maps link",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Dialog for testing location services. Shows the raw result only; no
 * technical instructions or debug detail.
 */
@Composable
fun LocationTestDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var currentLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    PremiumAlertDialog(
        onDismiss = onDismiss,
        title = "Location Test",
        content = {
            when {
                isLoading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = BrandRed700
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Getting location…", color = TextSecondary)
                    }
                }
                currentLocation != null -> {
                    Text(
                        text = "Lat ${String.format("%.5f", currentLocation!!.first)}, " +
                            "Lng ${String.format("%.5f", currentLocation!!.second)}",
                        color = TextPrimary
                    )
                }
                error != null -> {
                    Text(
                        text = "Unable to get location",
                        color = ErrorRed
                    )
                }
                else -> {
                    Text(
                        text = "Check that location services are working correctly.",
                        color = TextSecondary
                    )
                }
            }
        },
        confirmText = "Get Location",
        onConfirm = {
            isLoading = true
            error = null
            viewModel.getCurrentLocationForTesting(
                onSuccess = { lat, lng ->
                    currentLocation = Pair(lat, lng)
                    isLoading = false
                },
                onError = { message ->
                    error = message
                    isLoading = false
                }
            )
        },
        confirmClosesDialog = false,
        dismissText = "Close"
    )
}

/**
 * Rename dialog: matches the app's minimalist dialog style.
 */
@Composable
fun RenameLocationDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var isError by remember { mutableStateOf(false) }

    PremiumAlertDialog(
        onDismiss = onDismiss,
        title = "Rename Location",
        content = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    singleLine = true,
                    isError = isError,
                    shape = RoundedCornerShape(12.dp),
                    colors = premiumTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = "Enter a name",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmText = "Save",
        onConfirm = {
            if (name.trim().isEmpty()) {
                isError = true
            } else {
                onConfirm(name.trim())
            }
        },
        confirmClosesDialog = false,
        dismissText = "Cancel"
    )
}

/**
 * Delete confirmation dialog.
 */
@Composable
fun DeleteLocationDialog(
    locationName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    PremiumAlertDialog(
        onDismiss = onDismiss,
        title = "Delete location?",
        content = {
            Text(
                text = "Are you sure you want to delete \"$locationName\"?",
                color = TextSecondary
            )
        },
        confirmText = "Delete",
        confirmColor = ErrorRed,
        onConfirm = onConfirm,
        dismissText = "Cancel"
    )
}
