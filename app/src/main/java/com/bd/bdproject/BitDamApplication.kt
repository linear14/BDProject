package com.bd.bdproject

import android.app.Application
import android.content.Context
import com.bd.bdproject.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BitDamApplication: Application() {

    companion object {
        private var instance: BitDamApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        startKoin {
            androidLogger()
            androidContext(this@BitDamApplication)
            modules(appModule)
        }
    }
}