package ml.peter_volkov.patcher5;

import android.util.Log;
import java.lang.reflect.Array;

public class Agent {
    static String loggerTag = "no_root_privacy";

    public static void log(String paramString) {
        Log.v(Agent.loggerTag, paramString.replaceAll("\\r?\\n", "\\\\n"));
    }

    public static String toString(Object object) {
        if (object == null) {
            return "null";
        } else if (object.getClass().isArray()) {
            StringBuilder stringBuilder = new StringBuilder("{");
            int arrayLength = Array.getLength(object);
            for (int index = 0; index < arrayLength; index++) {
                stringBuilder.append(toString(Array.get(object, index))).append(", ");
            }
            return stringBuilder.append("}").toString();
        } else {
            return object.getClass().toString();
        }
    }
}
