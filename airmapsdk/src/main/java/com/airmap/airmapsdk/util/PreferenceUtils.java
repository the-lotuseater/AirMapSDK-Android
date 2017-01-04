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

    public static SharedPreferences getPreferences(Context context) throws SecuredPreferenceException {
        try {
            return SecuredPreferenceStore.getSharedInstance(context);
        } catch (IOException | CertificateException | InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | UnrecoverableEntryException | NoSuchProviderException | NoSuchPaddingException | KeyStoreException e) {
            e.printStackTrace();

            throw new SecuredPreferenceException(e.getMessage());
        }
    }
}
