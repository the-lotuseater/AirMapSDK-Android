package com.airmap.airmapsdk.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Auth;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.LoginCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.util.PreferenceUtils;
import com.airmap.airmapsdk.util.SecuredPreferenceException;
import com.airmap.airmapsdk.util.Utils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    public static final String PILOT = "pilot";

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airmap_activity_login);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        webView = (WebView) findViewById(R.id.web_view);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar.setMax(100);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                final boolean shouldOverrideUrlLoading = Auth.login(url, LoginActivity.this, new LoginCallback() {
                    @Override
                    public void onSuccess(Auth.AuthCredential authCredential) {
                        try {
                            PreferenceUtils.getPreferences(LoginActivity.this).edit()
                                    .putString(Utils.REFRESH_TOKEN_KEY, authCredential.getRefreshToken())
                                    .apply();
                        } catch (SecuredPreferenceException e) {
                            e.printStackTrace();
                        }

                        clearCookies();

                        AirMap.getInstance().setAuthToken(authCredential.getAccessToken());
                        AirMap.getPilot(new AirMapCallback<AirMapPilot>() {
                            @Override
                            public void onSuccess(AirMapPilot response) {
                                Intent data = new Intent();
                                data.putExtra(PILOT, response);
                                setResult(RESULT_OK, data);
                                finish();
                            }

                            @Override
                            public void onError(AirMapException e) {
                                e.printStackTrace();
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onEmailVerificationNeeded(final String resendLink) {
                        // redirect to login
                        webView.loadUrl(Auth.getLoginUrl());
                        new AlertDialog.Builder(LoginActivity.this).setTitle(R.string.email_verification_required)
                                .setMessage(R.string.verify_email_log_in_again)
                                .setPositiveButton(R.string.resend_email, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        OkHttpClient client = new OkHttpClient();
                                        client.newCall(new Request.Builder().get().url(resendLink).build()).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                toast(getString(R.string.error_resending_email));
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    toast(getString(R.string.sent_email));
                                                } else {
                                                    toast(getString(R.string.error_resending_email));
                                                }
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton(android.R.string.ok, null)
                                .show();
                    }

                    @Override
                    public void onErrorDomainBlackList() {
                        // show toast, then reload login url
                        webView.loadUrl(Auth.getLoginUrl());
                        Toast.makeText(LoginActivity.this, R.string.invalid_email_domain, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onContinue() {
                        webView.loadUrl(url);
                    }
                });
                return shouldOverrideUrlLoading;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(Auth.getLoginUrl());
    }

    private void clearCookies() {
        if (webView != null) {
            webView.clearCache(true);
            webView.clearHistory();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(LoginActivity.this);
            cookieSyncManager.startSync();

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();

            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}