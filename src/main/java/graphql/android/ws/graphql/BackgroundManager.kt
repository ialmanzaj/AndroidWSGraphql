package graphql.android.ws.graphql

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import graphql.android.ws.graphql.utils.SingletonHolder
import java.util.*
import java.util.logging.Logger

class BackgroundManager private constructor(application: Application):  Application.ActivityLifecycleCallbacks {

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    companion object  : SingletonHolder<BackgroundManager, Application>(::BackgroundManager) {
        val Log: Logger = Logger.getLogger(BackgroundManager::class.java.name)
    }

    private val BACKGROUND_DELAY : Long = 500

     interface Listener {
         fun onBecameForeground()
         fun onBecameBackground()
     }

    private var mInBackground = true
    private var listeners =  ArrayList<Listener>()
    private var mBackgroundDelayHandler =  Handler()
    private var mBackgroundTransition : Runnable? = null


    fun registerListener(listener : Listener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun isInBackground() : Boolean {
        return mInBackground
    }


    override fun onActivityResumed(activity: Activity) {
        if (mBackgroundTransition != null) {
            mBackgroundDelayHandler.removeCallbacks(mBackgroundTransition)
            mBackgroundTransition = null
        }

        if (mInBackground) {
            mInBackground = false
            notifyOnBecameForeground()
            Log.info( "Application went to foreground")
        }
    }

    private fun notifyOnBecameForeground() {
        for (listener in listeners) {
            try {
                listener.onBecameForeground()
            } catch (e:Exception) {
                Log.warning( "Listener threw exception! $e")
            }
        }
    }


    override fun onActivityPaused(activity : Activity?) {
        if (!mInBackground && mBackgroundTransition == null) {
            mBackgroundTransition =  Runnable {
                mInBackground = true
                mBackgroundTransition = null
                notifyOnBecameBackground()
                Log.info( "Application went to background")
            }
            mBackgroundDelayHandler.postDelayed(mBackgroundTransition, BACKGROUND_DELAY)
        }
    }

    private fun notifyOnBecameBackground() {
        for (listener in listeners) {
            try {
                listener.onBecameBackground()
            } catch (e:Exception) {
                Log.warning("Listener threw exception!$e")
            }
        }
    }

    override fun onActivityStopped(activity : Activity?) {}
    override fun onActivityCreated(activity : Activity?, savedInstanceState : Bundle?) {}
    override fun onActivityStarted(activity : Activity?) {}
    override fun onActivitySaveInstanceState(activity : Activity?, outState: Bundle?) {}
    override fun onActivityDestroyed(activity : Activity?) {}


}