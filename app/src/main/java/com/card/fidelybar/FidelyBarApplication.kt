package com.card.fidelybar

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.card.fidelybar.data.AppSettings
import com.card.fidelybar.data.CardFileStore
import com.card.fidelybar.data.StorePreset
import com.card.fidelybar.data.StoreCatalog
import com.card.fidelybar.ui.components.LogoBitmapCache

class FidelyBarApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        AppSettings.init(this)
        warmLogosForSavedCards()
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
    }

    private fun warmLogosForSavedCards() {
        Thread(
            {
                try {
                    val cards = CardFileStore(this).load()
                    val presetIds = cards.mapNotNull { it.presetId }
                        .filter { it != StorePreset.CUSTOM && StoreCatalog.byId.containsKey(it) }
                        .distinct()
                    LogoBitmapCache.warm(this, presetIds)
                } catch (_: Throwable) {
                    // Se la lettura delle carte fallisce lo splash non deve bloccarsi.
                }
            },
            "logo-warmup-load-cards"
        ).start()
    }
}