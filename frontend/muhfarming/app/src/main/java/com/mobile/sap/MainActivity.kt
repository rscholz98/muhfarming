package com.mobile.sap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.mobile.sap.data.api.RetrofitClient
import com.mobile.sap.data.auth.SessionManager
import com.mobile.sap.ui.navigation.AppNavigation
import com.mobile.sap.ui.theme.MuhfarmingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wire the network client to the persisted session so the auth
        // interceptor can attach the JWT to every backend request.
        RetrofitClient.init(SessionManager.get(applicationContext))

        // Workaround for Compose hover event crash on some devices
        try {
            enableEdgeToEdge()
        } catch (e: IllegalStateException) {
            // Silently continue if edge-to-edge fails
        }

        setContent {
            MuhfarmingTheme {
                AppNavigation()
            }
        }
    }
}