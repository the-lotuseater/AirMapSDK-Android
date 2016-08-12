package com.airmap.airmapsdk.UI.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Auth;
import com.airmap.airmapsdk.Models.Pilot.AirMapPilot;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapCallback;
import com.airmap.airmapsdk.Networking.Callbacks.LoginCallback;
import com.airmap.airmapsdk.Networking.Services.AirMap;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.Utils;

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
        setContentView(R.layout.activity_login);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        webView = (WebView) findViewById(R.id.web_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.airmap_title_activity_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar.setMax(100);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                final boolean shouldOverrideUrlLoading = Auth.login(url, LoginActivity.this, new LoginCallback() {
                    @Override
                    public void onSuccess(Auth.AuthCredential authCredential) {
                        Toast.makeText(LoginActivity.this, "You are now logged in", Toast.LENGTH_SHORT).show();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(Utils.REFRESH_TOKEN_KEY, authCredential.getRefreshToken());
                        AirMap.getInstance().setAuthToken(authCredential.getAccessToken());
                        editor.apply();
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
                        new AlertDialog.Builder(LoginActivity.this).setTitle("Email Verification Required")
                                .setMessage("Please verify your email and try logging in again")
                                .setPositiveButton("Resend Email", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        OkHttpClient client = new OkHttpClient();
                                        client.newCall(new Request.Builder().get().url(resendLink).build()).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                toast("Error re-sending verification email");
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    toast("Sent email");
                                                } else {
                                                    toast("Error re-sending verification email");
                                                }
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Ok", null)
                                .show();
                    }

                    @Override
                    public void onErrorDomainBlackList() {
                        // show toast, then reload login url
                        webView.loadUrl(Auth.getLoginUrl());
                        Toast.makeText(LoginActivity.this, "Invalid Email Domain. Please Sign-Up using a different email address.", Toast.LENGTH_LONG).show();
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