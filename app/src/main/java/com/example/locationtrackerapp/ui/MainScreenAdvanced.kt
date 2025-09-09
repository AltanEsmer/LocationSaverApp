package com.example.locationtrackerapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationtrackerapp.viewmodel.MainViewModel

/**
 * Advanced main screen with tabs for different features.
 * Perfect for delivery service management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenAdvanced(
    viewModel: MainViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf(
        TabItem("Konumlar", Icons.Default.LocationOn),
        TabItem("Siparişler", Icons.Default.List),
        TabItem("Müşteriler", Icons.Default.Person),
        TabItem("Rota", Icons.Default.LocationOn)
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uriel Cafe Delivery") },
                actions = {
                    IconButton(onClick = { /* Search functionality */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(
                    onClick = { /* Add location */ }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Location")
                }
                1 -> FloatingActionButton(
                    onClick = { /* Add order */ }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Order")
                }
                2 -> FloatingActionButton(
                    onClick = { /* Add customer */ }
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Add Customer")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> LocationsTab()
                1 -> OrdersTab()
                2 -> CustomersTab()
                3 -> RouteTab()
            }
        }
    }
}

/**
 * Tab item data class.
 */
data class TabItem(
    val title: String,
    val icon: ImageVector
)

/**
 * Locations tab content.
 */
@Composable
fun LocationsTab() {
    // Location management content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Konumlar Tab - Gelişmiş özellikler burada olacak")
    }
}

/**
 * Orders tab content.
 */
@Composable
fun OrdersTab() {
    // Order management content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Siparişler Tab - Sipariş takibi burada olacak")
    }
}

/**
 * Customers tab content.
 */
@Composable
fun CustomersTab() {
    // Customer management content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Müşteriler Tab - Müşteri yönetimi burada olacak")
    }
}

/**
 * Route tab content.
 */
@Composable
fun RouteTab() {
    // Route planning content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Rota Tab - Rota planlama burada olacak")
    }
}
