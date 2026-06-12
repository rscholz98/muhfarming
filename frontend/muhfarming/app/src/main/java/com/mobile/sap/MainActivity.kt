package com.mobile.sap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.mobile.sap.ui.navigation.AppNavigation
import com.mobile.sap.ui.theme.MuhfarmingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuhfarmingTheme {
                AppNavigation()
            }
        }
    }
}