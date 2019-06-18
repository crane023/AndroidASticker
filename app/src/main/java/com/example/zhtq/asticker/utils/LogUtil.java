package com.example.zhtq.asticker.utils;

import android.util.Log;

public class LogUtil {
    private static LogUtil sInstance;
    private static final String PREFIX = "AndroidASticker";
//    private static final boolean SHOW_DEBUG = "eng".equals(Build.TYPE) || "userdebug".equals(Build.TYPE);

    public static final int V = 1;
    public static final int D = 2;
    public static final int I = 3;
    public static final int W = 4;
    public static final int E = 5;

    private final int FILTER_LEVEL;
    private static int sInternalLevel = D;

    public LogUtil(int level) {
        FILTER_LEVEL = level;
    }

    public static void initLogUtil(int level) {
        if (null == sInstance) {
            synchronized (LogUtil.class) {
                if (null == sInstance) {
                    sInternalLevel = level;
                    sInstance = new LogUtil(level);
                }
            }
        }
    }

    private static void checkInstance() {
        initLogUtil(sInternalLevel);
    }

    public static void e(String TAG, String msg, Throwable tr) {
        checkInstance();
        if (E >= sInstance.FILTER_LEVEL) {
            Log.e(PREFIX.concat("/").concat(TAG), msg, tr);
        }
    }

    public static void e(String TAG, String msg) {
        checkInstance();
        if (E >= sInstance.FILTER_LEVEL) {
            Log.e(PREFIX.concat("/").concat(TAG), msg);
        }
    }

    public static void e(String TAG, Throwable tr, String msg, Object... args) {
        checkInstance();
        if (E >= sInstance.FILTER_LEVEL) {
            String fmMsg;
            if (null != args && args.length > 0) {
                fmMsg = String.format(msg, args);
            } else {
                fmMsg = msg;
            }
            Log.e(PREFIX.concat("/").concat(TAG), fmMsg, tr);
        }
    }

    public static void w(String TAG, String msg, Throwable tr) {
        checkInstance();
        if (W >= sInstance.FILTER_LEVEL) {
            Log.w(PREFIX.concat("/").concat(TAG), msg, tr);
        }
    }

    public static void w(String TAG, String msg, Object... args) {
        checkInstance();
        if (W >= sInstance.FILTER_LEVEL) {
            String fmMsg;
            if (null != args && args.length > 0) {
                fmMsg = String.format(msg, args);
            } else {
                fmMsg = msg;
            }
            Log.w(PREFIX.concat("/").concat(TAG), fmMsg);
        }
    }

    public static void i(String TAG, String msg) {
        checkInstance();
        if (I >= sInstance.FILTER_LEVEL) {
            Log.i(PREFIX.concat("/").concat(TAG), msg);
        }
    }

    public static void i(String TAG, String msg, Object... args) {
        checkInstance();
        if (I >= sInstance.FILTER_LEVEL) {
            String fmMsg;
            if (null != args && args.length > 0) {
                fmMsg = String.format(msg, args);
            } else {
                fmMsg = msg;
            }
            Log.i(PREFIX.concat("/").concat(TAG), fmMsg);
        }
    }

    public static void d(String TAG, String msg) {
        checkInstance();
        if (D >= sInstance.FILTER_LEVEL) {
            Log.d(PREFIX.concat("/").concat(TAG), msg);
        }
    }

    public static void d(String TAG, String msg, Object... args) {
        checkInstance();
        if (D >= sInstance.FILTER_LEVEL) {
            String fmMsg;
            if (null != args && args.length > 0) {
                fmMsg = String.format(msg, args);
            } else {
                fmMsg = msg;
            }
            Log.d(PREFIX.concat("/").concat(TAG), fmMsg);
        }
    }

    public static void v(String TAG, String msg) {
        checkInstance();
        if (V >= sInstance.FILTER_LEVEL) {
            Log.v(PREFIX.concat("/").concat(TAG), msg);
        }
    }

    public static void v(String TAG, String msg, Object... args) {
        checkInstance();
        if (V >= sInstance.FILTER_LEVEL) {
            String fmMsg;
            if (null != args && args.length > 0) {
                fmMsg = String.format(msg, args);
            } else {
                fmMsg = msg;
            }
            Log.v(PREFIX.concat("/").concat(TAG), fmMsg);
        }
    }

    public static String getMethodName() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//        for(int i = 0; i < elements.length; i++) {
//            LogUtil.i(PREFIX, "index:%d; method name:%s.", i, elements[i].getMethodName());
//        }
        return elements[3].getMethodName();
    }

    public static void dumpStack(String TAG, String msg){
        checkInstance();
        if (D >= sInstance.FILTER_LEVEL) {
            java.util.Map<Thread, StackTraceElement[]> ts = Thread.getAllStackTraces();
            StackTraceElement[] ste = ts.get(Thread.currentThread());
            if (ste != null && ste.length > 0) {
                for (StackTraceElement s : ste) {
                    Log.i(PREFIX.concat("/").concat(TAG) + "/" + msg, s.toString());
                }
            } else {
                Log.i(PREFIX.concat("/").concat(TAG) + "/" + msg, "EMPTY Stack array!");
            }
        }
    }
}

