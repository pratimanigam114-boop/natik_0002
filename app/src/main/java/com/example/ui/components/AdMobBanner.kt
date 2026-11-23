package com.example.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

object AdMobConfig {
    // =====================================================================
    // Google AdMob Configuration
    // ---------------------------------------------------------------------
    // Currently configured with Google's official Test Ad IDs:
    //
    // 1. App ID (defined in AndroidManifest.xml):
    //    Test App ID: "ca-app-pub-3940256099942544~3347511713"
    //    Replace in AndroidManifest.xml when you have your real AdMob App ID!
    //
    // 2. Banner Ad Unit ID (below):
    //    Test Banner ID: "ca-app-pub-3940256099942544/6300978111"
    //    Replace with your real Banner Unit ID when ready for production!
    // =====================================================================

    const val SAMPLE_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val SAMPLE_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val BANNER_AD_UNIT_ID = SAMPLE_BANNER_AD_UNIT_ID
}

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConfig.BANNER_AD_UNIT_ID,
    isProUser: Boolean = false
) {
    // Pro subscription users enjoy an ad-free experience
    if (isProUser) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("admob_banner_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SPONSORED",
            fontSize = 9.sp,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 2.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admob_ad_view"),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                Log.d("AdMobBanner", "Banner ad loaded successfully ($adUnitId).")
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                Log.w("AdMobBanner", "Banner ad failed to load: ${error.message} (code ${error.code})")
                            }
                        }
                        val adRequest = AdRequest.Builder().build()
                        loadAd(adRequest)
                    }
                }
            )
        }
    }
}
