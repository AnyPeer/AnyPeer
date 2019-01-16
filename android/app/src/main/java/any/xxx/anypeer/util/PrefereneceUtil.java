package any.xxx.anypeer.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefereneceUtil {
    private static final String PERFERENCE_DFC = "PERFERENCE_DFC";

    public static SharedPreferences getDefaultPreference(Context context) {
        return context.getSharedPreferences(PERFERENCE_DFC, Context.MODE_PRIVATE);
    }

    public static synchronized String getString(Context context, String key) {
        return getDefaultPreference(context).getString(key, null);
    }

    public static synchronized boolean getBoolean(Context context, String key) {
        return getDefaultPreference(context).getBoolean(key, false);
    }

    public static synchronized boolean getBoolean(Context context, String key, boolean value) {
        return getDefaultPreference(context).getBoolean(key, value);
    }

    public static synchronized void saveString(Context context, String key, String value) {
        SharedPreferences share = getDefaultPreference(context);
        SharedPreferences.Editor editor = share.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static synchronized void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences share = getDefaultPreference(context);
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static synchronized void saveStringList(Context context, String[] key, String[] value) {
        SharedPreferences share = getDefaultPreference(context);
        SharedPreferences.Editor editor = share.edit();
        for (int i = 0; i < key.length; i++) {
            editor.putString(key[i], value[i]);
        }
        editor.commit();
    }

    public static synchronized Long getLong(Context context, String key) {
        return getDefaultPreference(context).getLong(key, -1);
    }

    public static synchronized void saveLong(Context context, String key, Long value) {
        SharedPreferences share = getDefaultPreference(context);
        SharedPreferences.Editor editor = share.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static synchronized int getInt(Context context, String key) {
        return getDefaultPreference(context).getInt(key, -1);
    }

    public static synchronized void saveInt(Context context, String key, int value) {
        SharedPreferences share = getDefaultPreference(context);
        SharedPreferences.Editor editor = share.edit();
        editor.putInt(key, value);
        editor.commit();
    }
}
