package com.mingz.share;

import android.util.Log;
import androidx.annotation.IntDef;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class MyLog {
    /**
     * 控制所有{@link MyLog}实例是否允许打印{@link Log#DEBUG}和{@link Log#VERBOSE}级别的日志.
     */
    private static final boolean DEBUG = true;
    private static final String PREFIX = "MyTAG-";
    private final String tag;

    /**
     * 是否打印{@link Log#DEBUG}和{@link Log#VERBOSE}级别的日志.<br/>
     * 默认为true.
     */
    public final boolean debug;

    // 字符串
    public MyLog(@Nullable String tag, boolean debug) {
        if (tag == null) {
            this.tag = PREFIX.substring(0, PREFIX.length() - 1)/*MyTAG*/;
        } else {
            this.tag = PREFIX + tag;
        }
        this.debug = debug;
    }

    public MyLog(@Nullable String tag) {
        this(tag, true);
    }

    // Kotlin类
    public MyLog(@NotNull KClass<?> c, boolean debug) {
        this(c.getSimpleName(), debug);
    }

    public MyLog(@NotNull KClass<?> c) {
        this(c.getSimpleName());
    }

    // Java类
    public MyLog(@NotNull Class<?> c, boolean debug) {
        this(c.getSimpleName(), debug);
    }

    public MyLog(@NotNull Class<?> c) {
        this(c.getSimpleName());
    }

    // 使用默认标签
    public MyLog(boolean debug) {
        this((String) null, debug);
    }

    public MyLog() {
        this((String) null);
    }

    // 打印日志
    private void core(@Level int level, Object msg) {
        if (DEBUG || debug || (level != Log.DEBUG && level != Log.VERBOSE)) {
            Log.println(level, tag, msg.toString());
        }
    }

    private void core(@Level int level, Throwable e, boolean printStack) {
        if (DEBUG || debug || (level != Log.DEBUG && level != Log.VERBOSE)) {
            if (printStack) {
                Log.println(level, tag, Log.getStackTraceString(e));
            } else {
                Log.println(level, tag, e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }

    private void core(@Level int level, Object msg, Throwable e, boolean printStack) {
        if (DEBUG || debug || (level != Log.DEBUG && level != Log.VERBOSE)) {
            if (printStack) {
                Log.println(level, tag, msg + "\n" + Log.getStackTraceString(e));
            } else {
                Log.println(level, tag, msg + "(" + e.getClass().getName() + "): " + e.getMessage());
            }
        }
    }

    // Log.VERBOSE
    public void v(Object msg) {
        core(Log.VERBOSE, msg);
    }

    public void v(Throwable e, boolean printStack) {
        core(Log.VERBOSE, e, printStack);
    }

    public void v(Throwable e) {
        core(Log.VERBOSE, e, false);
    }

    public void v(Object msg, Throwable e, boolean printStack) {
        core(Log.VERBOSE, msg, e, printStack);
    }

    public void v(Object msg, Throwable e) {
        core(Log.VERBOSE, msg, e, false);
    }

    // Log.DEBUG
    public void d(Object msg) {
        core(Log.DEBUG, msg);
    }

    public void d(Throwable e, boolean printStack) {
        core(Log.DEBUG, e, printStack);
    }

    public void d(Throwable e) {
        core(Log.DEBUG, e, false);
    }

    public void d(Object msg, Throwable e, boolean printStack) {
        core(Log.DEBUG, msg, e, printStack);
    }

    public void d(Object msg, Throwable e) {
        core(Log.DEBUG, msg, e, false);
    }

    // Log.INFO
    public void i(Object msg) {
        core(Log.INFO, msg);
    }

    public void i(Throwable e, boolean printStack) {
        core(Log.INFO, e, printStack);
    }

    public void i(Throwable e) {
        core(Log.INFO, e, false);
    }

    public void i(Object msg, Throwable e, boolean printStack) {
        core(Log.INFO, msg, e, printStack);
    }

    public void i(Object msg, Throwable e) {
        core(Log.INFO, msg, e, false);
    }

    // Log.WARN
    public void w(Object msg) {
        core(Log.WARN, msg);
    }

    public void w(Throwable e, boolean printStack) {
        core(Log.WARN, e, printStack);
    }

    public void w(Throwable e) {
        core(Log.WARN, e, false);
    }

    public void w(Object msg, Throwable e, boolean printStack) {
        core(Log.WARN, msg, e, printStack);
    }

    public void w(Object msg, Throwable e) {
        core(Log.WARN, msg, e, false);
    }

    // Log.ERROR
    public void e(Object msg) {
        core(Log.ERROR, msg);
    }

    public void e(Throwable e, boolean printStack) {
        core(Log.ERROR, e, printStack);
    }

    public void e(Throwable e) {
        core(Log.ERROR, e, false);
    }

    public void e(Object msg, Throwable e, boolean printStack) {
        core(Log.ERROR, msg, e, printStack);
    }

    public void e(Object msg, Throwable e) {
        core(Log.ERROR, msg, e, false);
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR})
    private @interface Level {}
}
