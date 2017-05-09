package com.airmap.airmapsdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.LoginCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.AuthService;
import com.airmap.airmapsdk.util.AirMapAuthenticationCallback;
import com.airmap.airmapsdk.util.PreferenceUtils;
import com.airmap.airmapsdk.util.SecuredPreferenceException;
import com.airmap.airmapsdk.util.Utils;
import com.auth0.android.Auth0;
import com.auth0.android.lock.Lock;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;

/**
 * Created by Vansh Gandhi on 8/10/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class Auth {

    public enum ErrorType {
        DomainBlackList,
        EmailVerification,
        Unknown,
    }

    public static void loginOrSignup(Activity activity, AirMapAuthenticationCallback callback) {
        Auth0 auth0 = new Auth0(Utils.getClientId(), "sso.airmap.io");

        Lock lock = Lock.newBuilder(auth0, callback)
                .hideMainScreenTitle(true)
                .setTermsURL("https://www.airmap.com/terms")
                .setPrivacyURL("https://www.airmap.com/privacy")
                .withScope("openid offline_access")
                .withScheme("airmap")
                .closable(true)
                .build(activity);

        callback.setLock(lock);

        activity.startActivity(lock.newIntent(activity));
    }

    /**
     * Refreshes the saved access token
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
                callback.onError(new AirMapException("Invalid Refresh Token"));
            }
            return;
        }

        AuthService.refreshAccessToken(refreshToken, callback);
    }


    public static class AuthCredential implements Serializable {

        private String accessToken;
        private String refreshToken;
        private Date expiresAt;
        private String tokenType;
        private String userId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public Date getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(Date expiresAt) {
            this.expiresAt = expiresAt;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }
    }

    public static class AuthErrors implements Serializable {
        ErrorType type;
        String resendLink;

        public AuthErrors(org.json.JSONObject jsonObject) {
            this.type = getErrorTypeFromString(jsonObject.optString("type"));
            this.resendLink = jsonObject.optString("resend_link", null);
        }

        public AuthErrors(ErrorType type, String resendLink) {
            this.type = type;
            this.resendLink = resendLink;
        }

        public ErrorType getType() {
            return type;
        }

        public void setType(ErrorType type) {
            this.type = type;
        }

        public String getResendLink() {
            return resendLink;
        }

        public void setResendLink(String resendLink) {
            this.resendLink = resendLink;
        }
    }

    private static ErrorType getErrorTypeFromString(String type) {
        switch (type) {
            case "domain_blacklist":
                return ErrorType.DomainBlackList;
            case "email_verification":
                return ErrorType.EmailVerification;
            default:
                return ErrorType.Unknown;
        }
    }
}
