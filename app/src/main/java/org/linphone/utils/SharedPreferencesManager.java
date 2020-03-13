package org.linphone.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREFS_NAME = "NethPreferences";
    private static final String LOGGED_USERNAME = "neth_username";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getString(Context context, String name) {
        return getSharedPreferences(context).getString(name, null);
    }

    public static void setString(Context context, String name, String value) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().putString(name, value).apply();
    }

    public static void setUsername(Context context, String username) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().putString(LOGGED_USERNAME, username).apply();
    }

    public static String getUsername(Context context) {
        return getSharedPreferences(context).getString(LOGGED_USERNAME, null);
    }

    public static void removeUsername(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().remove(LOGGED_USERNAME).apply();
    }
}
