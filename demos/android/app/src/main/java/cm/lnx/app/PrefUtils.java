package cm.lnx.app;

import android.content.Context;
import android.content.SharedPreferences;

import cm.lnx.auth.oauth.Token;
import cm.lnx.auth.util.GsonUtils;

/**
 * Created by leenanxi on 2017/3/16.
 */

public class PrefUtils {
    private static final String TOKEN = "token";


    public static void saveToken(final Context context, Token token) {
        String key = context.getResources().getString(R.string.preference_file_key);
        SharedPreferences sp = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        sp.edit().putString(TOKEN, GsonUtils.toJson(token)).apply();
    }

    public static void removeToken(final Context context) {
        String key = context.getResources().getString(R.string.preference_file_key);
        SharedPreferences sp = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        sp.edit().remove(TOKEN).apply();
    }


    public static Token getToken(final Context context) {
        String key = context.getResources().getString(R.string.preference_file_key);
        SharedPreferences sp = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        String tokenJson = sp.getString(TOKEN, "{}");
        return GsonUtils.fromJson(tokenJson, Token.class);
    }

}
