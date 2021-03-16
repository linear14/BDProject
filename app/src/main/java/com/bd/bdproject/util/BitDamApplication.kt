package com.bd.bdproject.util

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.bd.bdproject.di.appModule
import com.bd.bdproject.lifecycle.AppLifecycleObserver
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BitDamApplication: Application() {

    companion object {
        lateinit var pref: BitdamSharedPreferences
        private var instance: BitDamApplication? = null
        var isObserverAlreadyEnrolled = false
        private val observer by lazy {
            AppLifecycleObserver(applicationContext())
        }

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        fun enrollLifecycleObserver() {
            if (!isObserverAlreadyEnrolled) {
                isObserverAlreadyEnrolled = true
                ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
            }
        }

        fun removeLifecycleObserver() {
            if (isObserverAlreadyEnrolled) {
                isObserverAlreadyEnrolled = false
                ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
            }
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