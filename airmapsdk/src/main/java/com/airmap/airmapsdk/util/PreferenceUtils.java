package com.airmap.airmapsdk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;

/**
 * Created by collin@airmap.com on 12/16/16.
 */

public class PreferenceUtils {

    private static final String TAG = "PreferenceUtils";

    public static SharedPreferences getPreferences(Context context) {
        try {
            return SecuredPreferenceStore.getSharedInstance(context);
        } catch (IOException | CertificateException | InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | UnrecoverableEntryException | NoSuchProviderException | NoSuchPaddingException | KeyStoreException e) {
            e.printStackTrace();

            return PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    /**
     *  Migrate shared_prefs from public to private, if necessary
     *
     *  @param context
     */
    public static void migrateSecurePreferences(Context context) throws SecuredPreferenceStoreException {

        try {
            SharedPreferences oldPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences securePreferences = SecuredPreferenceStore.getSharedInstance(context);

            if (securePreferences.getAll().isEmpty()) {
                Log.e("PreferencesUtils", "no preferences to migrate");
                return;
            }

            Log.e("PreferencesUtils", "migrateSharePrefs");

            SharedPreferences.Editor secureEditor = securePreferences.edit();

            SharedPreferences.Editor oldEditor = oldPreferences.edit();

            Map<String, ?> oldPrefsMap = oldPreferences.getAll();
            for (String key : oldPrefsMap.keySet()) {
                Object value = oldPrefsMap.get(key);

                if (value instanceof String) {
                    secureEditor.putString(key, (String) value);
                    Log.e("PreferencesUtils", "migrate string: " + value);
                } else if (value instanceof Integer) {
                    secureEditor.putInt(key, (Integer) value);
                    Log.e("PreferencesUtils", "migrate int: " + value);
                } else if (value instanceof Boolean) {
                    secureEditor.putBoolean(key, (Boolean) value);
                    Log.e("PreferencesUtils", "migrate boolean: " + value);
                } else if (value instanceof Float) {
                    secureEditor.putFloat(key, (Float) value);
                    Log.e("PreferencesUtils", "migrate float: " + value);
                } else if (value instanceof Long) {
                    secureEditor.putLong(key, (Long) value);
                    Log.e("PreferencesUtils", "migrate value: " + value);
                } else if (value instanceof Set) {
                    secureEditor.putStringSet(key, (Set<String>) value);
                    Log.e("PreferencesUtils", "migrate set: " + value);
                }

                switch (key.toLowerCase()) {
                    case "":
                    case "3": {
                        oldEditor.remove(key);
                        Log.e(TAG, "remove old key");
                        break;
                    }
                }
            }

            // save to new prefs
            secureEditor.apply();

            oldEditor.apply();

        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException | CertificateException |
                InvalidAlgorithmParameterException | InvalidKeyException | IOException | NoSuchPaddingException | NoSuchProviderException e) {

            e.printStackTrace();
            throw new SecuredPreferenceStoreException(e.getMessage());
        }
    }

    public static class SecuredPreferenceStoreException extends RuntimeException {

        public SecuredPreferenceStoreException(String message) {
            super(message);
        }
    }
}
