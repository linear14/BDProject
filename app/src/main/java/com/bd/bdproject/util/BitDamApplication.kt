package com.bd.bdproject.util

import android.app.Application
import android.content.Context
import com.bd.bdproject.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BitDamApplication: Application() {

    companion object {
        lateinit var pref: BitdamSharedPreferences
        private var instance: BitDamApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        pref = BitdamSharedPreferences(applicationContext)
        instance = this

        startKoin {
            androidLogger()
            androidContext(this@BitDamApplication)
            modules(appModule)
        }
    }
}