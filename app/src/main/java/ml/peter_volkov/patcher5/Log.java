package ml.peter_volkov.patcher5;

//import android.util.Log;

//Logger abstraction layer for portability reasons:
// allow the code to be executed at both android and JVM
public class Log {
    static String TAG = "no-root-privacy";

    public static void d(String message) {
        android.util.Log.d(TAG, message);
    }
    public static void d(String TAG, String message) {
        android.util.Log.d(TAG, message);
    }

    public static void e(String message) {
        android.util.Log.e(TAG, message);
    }
    public static void e(String TAG, String message) {
        android.util.Log.e(TAG, message);
    }

    public static void i(String message) {
        android.util.Log.i(TAG, message);
    }
    public static void i(String TAG, String message) {
        android.util.Log.i(TAG, message);
    }

    public static void w(String message) {
        android.util.Log.w(TAG, message);
    }
    public static void w(String TAG, String message) {
        android.util.Log.w(TAG, message);
    }
}
