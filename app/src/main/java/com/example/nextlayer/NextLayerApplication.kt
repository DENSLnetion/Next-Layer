package com.example.nextlayer

import android.app.Application

/**
 * File ini wajib ada karena dideklarasikan di AndroidManifest.xml.
 * Menjaga lifecycle aplikasi tetap stabil dari awal boot.
 */
class NextLayerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Tempat inisialisasi library global nantinya jika diperlukan
    }
}
