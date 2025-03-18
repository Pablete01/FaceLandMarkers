package com.app.facelandmarkers.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.facelandmarkers.MainScreen
import com.app.facelandmarkers.MainViewModel
import com.app.facelandmarkers.camera.Camera
import com.app.facelandmarkers.permissions.PermissionsScreen
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationGraph(
    mainViewModel: MainViewModel,
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val context = LocalContext.current
    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    Scaffold(
        topBar = {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically

                        ) {
                            Text(
                                text = "FaceLandMarkers",
                                modifier = Modifier.weight(1f)
                            )

                        }
                    }
                )
        },
        containerColor = MaterialTheme.colorScheme.onPrimary,
//                    bottomBar = {
//
//
//                    }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (hasCameraPermission) Camera else MainScreen
        ) {
            composable<MainScreen> {
               MainScreen(
                    paddingValues = paddingValues,
                    mainViewModel = mainViewModel,
                   navController = navController
                )

            }
            composable<Camera> {
                Camera(
                    mainViewModel = mainViewModel,
                    paddingValues = paddingValues
                )
            }
            composable<Permissions> {
                PermissionsScreen(navController = navController)
            }

        }
    }
}


@Serializable
object MainScreen

@Serializable
object Camera

@Serializable
object Permissions

