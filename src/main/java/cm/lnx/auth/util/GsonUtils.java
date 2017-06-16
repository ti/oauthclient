package cm.lnx.auth.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.text.DateFormat;


public class GsonUtils {
    private static final Gson GSON = new GsonBuilder()
            .generateNonExecutableJson()
            .create();

    private GsonUtils() {

    }

    public static Gson get() {
        return GSON;
    }

    public static String toJson(Object o) {
        return GSON.toJson(o);
    }

    public static  <T> T fromJson(String json,  Class<T> classOfT) {
        if (classOfT == null) {
            return null;
        }
        return GSON.fromJson(json, classOfT);
    }

    public static  <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

}
