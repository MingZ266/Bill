package com.mingz.billing.utils;

import android.content.Context;
import android.util.Log;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import kotlin.reflect.KClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MyLog {
    public final String tag;
    public final boolean debug;
    // 日志文件相关
    private File logFile = null;
    private SimpleDateFormat format = null;

    public static final MyLog TEMP = new MyLog("TEMP");

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

    public void setLogFile(Context context, String fileName) {
        try {
            File dir = context.getExternalFilesDir("");
            if (dir == null) {
                dir = context.getFilesDir();
            }
            dir = new File(dir, "log");
            logFile = new File(dir, fileName);
            // 创建日志文件
            if (dir.exists() || dir.mkdirs()) {
                if (logFile.exists() || logFile.createNewFile()) {
                    format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",
                            Locale.getDefault());
                    return;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        logFile = null;
        format = null;
    }

    @Nullable
    public File getLogFile() {
        return logFile;
    }

    public void v(Object msg) {
        core(Log.VERBOSE, msg);
    }

    public void v(Object errorInfo, Throwable e) {
        core(Log.VERBOSE, errorInfo, e);
    }

    public void d(Object msg) {
        core(Log.DEBUG, msg);
    }

    public void d(Object errorInfo, Throwable e) {
        core(Log.DEBUG, errorInfo, e);
    }

    public void i(Object msg) {
        core(Log.INFO, msg);
    }

    public void i(Object errorInfo, Throwable e) {
        core(Log.INFO, errorInfo, e);
    }

    public void w(Object msg) {
        core(Log.WARN, msg);
    }

    public void w(Object errorInfo, Throwable e) {
        core(Log.WARN, errorInfo, e);
    }

    public void e(Object msg) {
        core(Log.ERROR, msg);
    }

    public void e(Object errorInfo, Throwable e) {
        core(Log.ERROR, errorInfo, e);
    }

    private void core(@Level int priority, Object msg) {
        if (!debug) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }
        }
        String msgStr = msg.toString();
        Log.println(priority, tag, msgStr);
        if (logFile != null) {
            String content = format.format(System.currentTimeMillis()) + " "
                    + tag + " : " + msgStr + "\n\n";
            try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void core(@Level int priority, Object errorInfo, Throwable e) {
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
