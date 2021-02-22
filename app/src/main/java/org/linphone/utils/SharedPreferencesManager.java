package org.linphone.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREFS_NAME = "NethPreferences";
    private static final String LOGGED_USERNAME = "neth_username";
    private static final String KEY_AUTHTOKEN = "neth_authToken";
    private static final String DOMAIN = "neth_domain";

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

    public static void setAuthtoken(Context context, String authToken) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().putString(KEY_AUTHTOKEN, authToken).apply();
    }

    public static String getAuthtoken(Context context) {
        return getSharedPreferences(context).getString(KEY_AUTHTOKEN, null);
    }

    public static void removeAuthtoken(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().remove(KEY_AUTHTOKEN).apply();
    }

    public static void setDomain(Context context, String domain) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().putString(DOMAIN, domain).apply();
    }

    public static String getDomain(Context context) {
        return getSharedPreferences(context).getString(DOMAIN, null);
    }

    public static void removeDomain(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().remove(DOMAIN).apply();
    }
}
