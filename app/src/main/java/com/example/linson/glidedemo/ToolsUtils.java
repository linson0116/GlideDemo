package com.example.linson.glidedemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by linson on 2017/2/4.
 */

public class ToolsUtils {
    public static String getStringValue(Context context, String key, String defaultValue) {
        SharedPreferences sp = context.getSharedPreferences("setting", Context.MODE_APPEND);
        String value = sp.getString(key, defaultValue);
        return value;
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences("setting", Context.MODE_APPEND);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }
}
