package com.jitambulishe.app

import android.app.Application
import com.jitambulishe.app.data.AppDatabase

/**
 * Application class. Kept minimal: Room is lazily initialized through
 * [AppDatabase.get]. Add any app-wide setup here.
 */
class JitambulisheApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
}
