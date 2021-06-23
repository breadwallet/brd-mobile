/**
 * BreadWallet
 *
 * Created by Shivangi Gandhi on <shivangi@brd.com> 6/7/18.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.app

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

typealias ApplicationLifecycleListener = (Lifecycle.Event) -> Unit

/**
 * Use the [ApplicationLifecycleObserver] to listen for application lifecycle events.
 */
class ApplicationLifecycleObserver : LifecycleObserver {
    /**
     * Called when the application is first created.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        Log.d(TAG, "onCreate")
        onLifeCycleEvent(Lifecycle.Event.ON_CREATE)
    }

    /**
     * Called when the application is brought into the foreground.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        Log.d(TAG, "onStart")
        onLifeCycleEvent(Lifecycle.Event.ON_START)
    }

    /**
     * Called when the application is put into the background.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Log.d(TAG, "onStop")
        onLifeCycleEvent(Lifecycle.Event.ON_STOP)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        Log.d(TAG, "onDestroy")
        onLifeCycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    /**
     * Passes a lifecycle event to all registered listeners.
     *
     * @param event The lifecycle event that has just occurred.
     */
    private fun onLifeCycleEvent(event: Lifecycle.Event) {
        listeners.forEach { it.invoke(event) }
    }

    companion object {
        private val TAG = ApplicationLifecycleObserver::class.java.simpleName
        private val listeners = mutableListOf<ApplicationLifecycleListener>()

        /**
         * Registers an application lifecycle listener to receive lifecycle events.
         *
         * @param listener The listener to register.
         */
        fun addApplicationLifecycleListener(listener: ApplicationLifecycleListener) {
            if (!listeners.contains(listener)) {
                listeners.add(listener)
            }
        }

        /**
         * Unregisters an application lifecycle listener from receiving lifecycle events.
         *
         * @param listener The listener to unregister.
         */
        fun removeApplicationLifecycleListener(listener: ApplicationLifecycleListener?) {
            if (listeners.contains(listener)) {
                listeners.remove(listener)
            }
        }
    }
}