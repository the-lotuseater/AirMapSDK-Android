package com.airmap.airmapsdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.airmap.airmapsdk.networking.callbacks.AirMapAuthenticationCallback;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.AuthService;
import com.airmap.airmapsdk.util.AirMapConfig;
import com.airmap.airmapsdk.util.AirMapConstants;
import com.airmap.airmapsdk.util.PreferenceUtils;
import com.airmap.airmapsdk.util.SecuredPreferenceException;
import com.airmap.airmapsdk.util.Utils;
import com.auth0.android.Auth0;
import com.auth0.android.lock.Lock;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

public class Auth {

    private static final String TAG = "Auth";

    public enum ErrorType {
        DomainBlackList,
        EmailVerification,
        Unknown,
    }

    public static boolean isUserLoggedIn(Context context) {
        try {
            SharedPreferences preferences = PreferenceUtils.getPreferences(context);
            return preferences.getBoolean(AirMapConstants.LOGGED_IN, false);
        } catch (SecuredPreferenceException e) {
            Log.e("Auth", "SecurePref", e);
            return false;
        }
    }

    public static void loginOrSignup(Activity activity, AirMapAuthenticationCallback callback) {
        Auth0 auth0 = new Auth0(AirMapConfig.getAuth0ClientId(), AirMapConfig.getAuth0Host());
        auth0.setOIDCConformant(false);

        Lock lock = Lock.newBuilder(auth0, callback)
                .hideMainScreenTitle(true)
                .setTermsURL(AirMapConfig.getTermsUrl())
                .setPrivacyURL(AirMapConfig.getPrivacyUrl())
                .withScope("openid offline_access")
                .withScheme("airmap")
                .closable(true)
                .build(activity);

        callback.setLock(lock);

        activity.startActivity(lock.newIntent(activity));
    }

    /**
     * Refreshes the saved access token. Non-blocking
     */
    public static void refreshAccessToken(final Context context, final AirMapCallback<Void> callback) {
        AirMapLog.i("AuthServices", "Trying to refresh token");

        String refreshToken = null;
        try {
            SharedPreferences preferences = PreferenceUtils.getPreferences(context);
            refreshToken = preferences.getString(Utils.REFRESH_TOKEN_KEY, "");
        } catch (SecuredPreferenceException e) {
            AirMapLog.e("Auth", "Unable to get refresh token from secure prefs", e);
        }

        // return if refresh token is empty
        if (TextUtils.isEmpty(refreshToken)) {
            if (callback != null) {
                callback.error(new AirMapException("Invalid Refresh Token"));
            }
            return;
        }

        AuthService.refreshAccessToken(refreshToken, callback);
    }

    /**
     * Refresh Access Token. Blocking
     */
    public static void refreshAccessToken(final Context context) {
        AirMapLog.i("AuthServices", "Trying to refresh token");

        String refreshToken = null;
        try {
            SharedPreferences preferences = PreferenceUtils.getPreferences(context);
            refreshToken = preferences.getString(Utils.REFRESH_TOKEN_KEY, "");
        } catch (SecuredPreferenceException e) {
            AirMapLog.e("Auth", "Unable to get refresh token from secure prefs", e);
        }

        // return if refresh token is empty
        if (TextUtils.isEmpty(refreshToken)) {
            // throw ?
            return;
        }

        AuthService.refreshAccessToken(refreshToken);
    }

    public static void clearRefreshToken(final Context context) {
        try {
            SharedPreferences preferences = PreferenceUtils.getPreferences(context);
            preferences.edit().putString(Utils.REFRESH_TOKEN_KEY, "").apply();
        } catch (SecuredPreferenceException e) {
            AirMapLog.e("Auth", "Unable to get clear refresh token from secure prefs", e);
        }
    }

    public static boolean isTokenExpired() {
        String token = AirMap.getAuthToken();
        if (TextUtils.isEmpty(token)) {
            AirMapLog.d(TAG, "No auth token");
            return true;
        }

        try {
            JwtConsumer consumer = new JwtConsumerBuilder()
                    .setSkipAllValidators()
                    .setDisableRequireSignature()
                    .setSkipSignatureVerification()
                    .build();
            JwtClaims claims = consumer.processToClaims(token);
            return claims.getExpirationTime().isBefore(NumericDate.now());
        } catch (InvalidJwtException | MalformedClaimException e) {
            AirMapLog.e(TAG, "Unable to process token", e);
            return true;
        }
    }
}
