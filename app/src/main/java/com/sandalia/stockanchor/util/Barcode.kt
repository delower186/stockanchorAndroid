package com.sandalia.stockanchor.util

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.tasks.await

/**
 * Barcode scanning (Premium) via Google's code scanner: the camera UI is provided by Google Play services,
 * so the app needs no CAMERA permission. Returns null if the user cancels.
 */
object BarcodeScanner {
    suspend fun scan(context: Context): String? {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39, Barcode.FORMAT_QR_CODE, Barcode.FORMAT_ITF,
            )
            .enableAutoZoom()
            .build()
        return try {
            GmsBarcodeScanning.getClient(context, options).startScan().await()?.rawValue
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            null
        }
    }
}
