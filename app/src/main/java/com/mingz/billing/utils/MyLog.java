package com.mingz.billing.utils;

import android.util.Log;
import androidx.annotation.IntDef;
import kotlin.reflect.KClass;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MyLog {
    public final String tag;
    public final boolean debug;

    public MyLog(String tag, boolean debug) {
        this.tag = "MyTAG - " + tag;
        this.debug = debug;
    }

    public MyLog(String tag) {
        this(tag, true);
    }

    public MyLog(Class<?> clazz) {
        this(clazz.getSimpleName(), true);
    }

    public MyLog(Class<?> clazz, boolean debug) {
        this(clazz.getSimpleName(), debug);
    }

    public MyLog(Object obj) {
        this(obj.getClass(), true);
    }

    public MyLog(Object obj, boolean debug) {
        this(obj.getClass(), debug);
    }

    public void v(String msg) {
        core(Log.VERBOSE, msg);
    }

    public void v(String errorInfo, Throwable e) {
        core(Log.VERBOSE, errorInfo, e);
    }

    public void d(String msg) {
        core(Log.DEBUG, msg);
    }

    public void d(String errorInfo, Throwable e) {
        core(Log.DEBUG, errorInfo, e);
    }

    public void i(String msg) {
        core(Log.INFO, msg);
    }

    public void i(String errorInfo, Throwable e) {
        core(Log.INFO, errorInfo, e);
    }

    public void w(String msg) {
        core(Log.WARN, msg);
    }

    public void w(String errorInfo, Throwable e) {
        core(Log.WARN, errorInfo, e);
    }

    public void e(String msg) {
        core(Log.ERROR, msg);
    }

    public void e(String errorInfo, Throwable e) {
        core(Log.ERROR, errorInfo, e);
    }

    private void core(@Level int priority, String msg) {
        if (!debug) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }
        }
        Log.println(priority, tag, msg);
    }

    private void core(@Level int priority, String errorInfo, Throwable e) {
        if (!debug) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }
        }
        Log.println(priority, tag, errorInfo + "(" + e.getClass().getSimpleName()
                + "): " + e.getMessage());
    }

    @IntDef({Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Level {}
}
