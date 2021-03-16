package com.bd.bdproject.lifecycle

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bd.bdproject.BitdamLog
import com.bd.bdproject.util.SharedUtil
import com.bd.bdproject.view.activity.LockActivity

class AppLifecycleObserver(private val context: Context): LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() {
        if(SharedUtil.isPasswordExist()) {
            BitdamLog.contentLogger("LifecycleObserver: onStart")
            LockActivity.start(context)
        }
    }
}