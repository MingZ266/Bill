package com.mingz.share

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat

class MyApplication : Application() {
    private val myLog by lazy(LazyThreadSafetyMode.NONE) {
        MyLog(MyApplication::class)
    }

    companion object {
        private val observers = ArrayList<Observer>()

        /**
         * 订阅应用程序退出事件.
         */
        @JvmStatic
        fun registerObserver(observer: Observer) {
            observers.add(observer)
        }

        /**
         * 取消订阅应用程序退出事件.
         */
        @JvmStatic
        fun unregisterObserver(observer: Observer) {
            observers.remove(observer)
        }

        // 当应用程序正常退出时通知观察者
        private fun applicationExits() {
            for (observer in observers) {
                observer.applicationExits()
            }
        }

        // 当应用程序异常退出时通知观察者
        private fun applicationExitsUnexpectedly(e: Throwable) {
            for (observer in observers) {
                observer.applicationExitsUnexpectedly(e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // 记录主线程崩溃信息
        mainLooper.thread.uncaughtExceptionHandler = object : Thread.UncaughtExceptionHandler {
            private val myLog by lazy { MyLog(null) }

            override fun uncaughtException(t: Thread, e: Throwable) {
                // 当主线程出现未捕获的异常，认为应用程序将异常退出
                applicationExitsUnexpectedly(e)
                // 记录主线程崩溃日志
                myLog.e(t, e, true)
            }
        }
        // 注册生命周期回调
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var activityCount = 0 // 记录栈中Activity的数量

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                myLog.v("onActivityCreated: ${activity::class.simpleName}")
                activityCount++
                // 根据当前应用是否为深色模式以设置状态栏
                val uiMode = activity.resources.configuration.uiMode
                setLightStatusBar(activity, (uiMode and Configuration.UI_MODE_NIGHT_MASK)
                        != Configuration.UI_MODE_NIGHT_YES)
            }

            override fun onActivityStarted(activity: Activity) {
                myLog.v("onActivityStarted: ${activity::class.simpleName}")
            }

            override fun onActivityResumed(activity: Activity) {
                myLog.v("onActivityResumed: ${activity::class.simpleName}")
            }

            override fun onActivityPaused(activity: Activity) {
                myLog.v("onActivityPaused: ${activity::class.simpleName}")
            }

            override fun onActivityStopped(activity: Activity) {
                myLog.v("onActivityStopped: ${activity::class.simpleName}")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                myLog.v("onActivitySaveInstanceState: ${activity::class.simpleName}")
            }

            override fun onActivityDestroyed(activity: Activity) {
                myLog.v("onActivityDestroyed: ${activity::class.simpleName}")
                if (--activityCount <= 0) { // 栈中Activity全部销毁，认为应用程序已退出
                    applicationExits()
                }
            }
        })
    }

    // 将状态栏字体颜色设为在亮色下显示
    private fun setLightStatusBar(activity: Activity, light: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = activity.window.decorView.windowInsetsController
            controller?.setSystemBarsAppearance(
                if (light) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = activity.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller?.isAppearanceLightStatusBars = light
        } else {
            myLog.i("Android SDK ${Build.VERSION.SDK_INT}: 不能变更状态栏字体颜色")
        }
    }

    interface Observer {
        /**
         * 应用程序正常退出.
         */
        fun applicationExits() {}

        /**
         * 应用程序异常退出.
         */
        fun applicationExitsUnexpectedly(e: Throwable) {}
    }
}