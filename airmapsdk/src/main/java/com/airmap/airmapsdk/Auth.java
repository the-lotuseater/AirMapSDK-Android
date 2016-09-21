package com.airmap.airmapsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.airmap.airmapsdk.networking.callbacks.LoginCallback;
import com.airmap.airmapsdk.networking.callbacks.RefreshTokenListener;
import com.airmap.airmapsdk.networking.services.AirMap;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    /**
     * Generates an authCredentials object from a url
     *
     * @param url The url to parse
     * @return AuthCredentials
     */
    private static AuthCredential authCredentialsFromUrl(String url) {
        if (isValidLoginSchema(url)) {
            url = url.replace(Utils.getCallbackUrl() + "#", Utils.getCallbackUrl() + "?"); // Auth0 returns a #
            HttpUrl parsed = HttpUrl.parse(url);

            try {
                String idToken = parsed.queryParameter("id_token");
                AuthCredential authCredentials = new AuthCredential();
                authCredentials.setAccessToken(idToken);
                authCredentials.setTokenType(parsed.queryParameter("token_type"));
                authCredentials.setRefreshToken(parsed.queryParameter("refresh_token"));

                JwtConsumer consumer = new JwtConsumerBuilder()
                        .setSkipAllValidators()
                        .setDisableRequireSignature()
                        .setSkipSignatureVerification()
                        .build();
                JwtClaims claims = consumer.processToClaims(idToken);
                authCredentials.setUserId(claims.getSubject());
                authCredentials.setExpiresAt(new Date(claims.getExpirationTime().getValueInMillis()));
                return authCredentials;
            } catch (InvalidJwtException | MalformedClaimException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Generates an AuthErrors object from a url
     *
     * @param url The url to parse
     * @return AuthErrors
     */
    public static AuthErrors authErrorsFromUrl(String url) {
        if (!isValidLoginSchema(url)) {
            return null;
        }
        String callbackUrl;
        try {
            JSONObject auth0 = AirMap.getConfig().getJSONObject("auth0");
            callbackUrl = auth0.getString("callback_url");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("No callbackUrl found in airmap.config.json");
        }
        url = url.replace(callbackUrl + "#", callbackUrl + "?"); // Auth0 returns a #
        HttpUrl parsed = HttpUrl.parse(url);
        String error = parsed.queryParameter("error"); // check for error == unauthorized
        if (error == null || !error.equals("unauthorized")) {
            return null;
        }
        try {
            String errorDescription = parsed.queryParameter("error_description");
            errorDescription = URLDecoder.decode(errorDescription, "UTF-8");
            JSONObject jsonObject = new JSONObject(errorDescription);
            return new AuthErrors(jsonObject);

        } catch (JSONException | UnsupportedEncodingException e) {
            return null;
        }
    }


    /**
     * Checks for Auth Errors or AuthCredentials, if Valid, Saves AuthCredentials
     */
    public static boolean login(String url, Context context, LoginCallback callback) {
        AuthErrors authError = authErrorsFromUrl(url);
        if (authError != null) {
            switch (authError.type) {
                case EmailVerification:
                    callback.onEmailVerificationNeeded(authError.resendLink);
                    return true;
                case DomainBlackList:
                    callback.onErrorDomainBlackList();
                    return true;
            }
            return false;
        }

        AuthCredential authCredentials = authCredentialsFromUrl(url);
        if (authCredentials != null) {
            AirMap.getInstance().setAuthToken(authCredentials.getAccessToken());
            if (authCredentials.getRefreshToken() != null && !authCredentials.getRefreshToken().isEmpty()) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                preferences.edit().putString(Utils.REFRESH_TOKEN_KEY, authCredentials.getRefreshToken()).apply();
            }
            callback.onSuccess(authCredentials);
            return true;
        }
        callback.onContinue();
        return false;
    }


    /**
     * Refreshes the saved access token
     */
    public static void refreshAccessToken(final Context context, final RefreshTokenListener listener) {
        AirMapLog.i("AuthServices", "Trying to refresh token");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String refreshToken = preferences.getString(Utils.REFRESH_TOKEN_KEY, "");

        // return if refresh token is empty
        if(refreshToken.equals("")){
            if (listener != null) {
                listener.onError(new AirMapException("Invalid Refresh Token"));
            }
            return;
        }

        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sso.airmap.io/delegation").newBuilder();
        urlBuilder.addQueryParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        urlBuilder.addQueryParameter("api_type", "app");
        urlBuilder.addQueryParameter("client_id", Utils.getClientId());
        urlBuilder.addQueryParameter("refresh_token", refreshToken);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AirMapLog.e("AuthServices", e.getMessage());
                if (listener != null) {
                    listener.onError(new AirMapException(e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    response.body().close();
                    JSONObject jsonObject = new JSONObject(json);
                    String idToken = jsonObject.optString("id_token");
                    AirMap.getInstance().setAuthToken(idToken);
                    if (listener != null) {
                        listener.onSuccess();
                    }
                } catch (JSONException e) {
                    AirMapLog.e("AuthServices", e.getMessage());
                    if (listener != null) {
                        listener.onError(new AirMapException(e.getMessage()));
                    }
                }
            }
        });
    }

    /**
     * Concatenates and returns a Login Url
     *
     * @return String
     */
    public static String getLoginUrl() {
        return "https://sso.airmap.io/authorize?response_type=token&client_id=" + Utils.getClientId() + "&redirect_uri=" + Utils.getCallbackUrl() + "&scope=openid+offline_access";
    }

    private static boolean isValidLoginSchema(String url) {
        if (url.equals(getLoginUrl())) {
            return false;
        }
        return url.contains(Utils.getCallbackUrl());
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
