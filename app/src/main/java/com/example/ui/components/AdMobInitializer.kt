package com.example.ui.components

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.initialization.InitializationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AdMobInitializer: Dedicated helper class to initialize Google Mobile Ads SDK
 * safely and asynchronously in MainActivity.onCreate(), preparing the app
 * to serve banner and interstitial ads efficiently.
 */
object AdMobInitializer {
    private const val TAG = "AdMobInitializer"

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    /**
     * Initializes the Google Mobile Ads SDK asynchronously.
     * Safe to invoke on the main thread in MainActivity.onCreate().
     */
    fun initialize(
        context: Context,
        onInitialized: ((InitializationStatus) -> Unit)? = null
    ) {
        if (_isInitialized.value) {
            Log.d(TAG, "AdMob SDK is already initialized.")
            return
        }

        // Initialize on background thread to keep startup smooth and responsive
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Build baseline request configuration
                val requestConfiguration = MobileAds.getRequestConfiguration()
                    .toBuilder()
                    .build()
                MobileAds.setRequestConfiguration(requestConfiguration)

                // Initialize Google Mobile Ads SDK
                MobileAds.initialize(context.applicationContext) { status ->
                    _isInitialized.value = true
                    Log.i(TAG, "Google Mobile Ads SDK initialized successfully.")
                    status.adapterStatusMap.forEach { (adapter, adapterStatus) ->
                        Log.d(TAG, "AdMob Adapter: $adapter -> ${adapterStatus.initializationState}")
                    }
                    onInitialized?.invoke(status)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing AdMob SDK: ${e.message}", e)
            }
        }
    }
}
