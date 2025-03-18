package com.app.facelandmarkers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.app.facelandmarkers.navigation.NavigationGraph
import com.app.facelandmarkers.ui.theme.FaceLandMarkersTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.annotation.KoinExperimentalAPI

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by inject()

    @OptIn(KoinExperimentalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceLandMarkersTheme {
                    KoinAndroidContext {
                        NavigationGraph(
                            mainViewModel = mainViewModel,
                        )
                    }
            }
        }
    }
}
