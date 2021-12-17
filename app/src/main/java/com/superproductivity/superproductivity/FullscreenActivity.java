package com.superproductivity.superproductivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    CommonJavaScriptInterface jsi;
    WebView wv;
    public final static String INTERFACE_PROPERTY = "SUPAndroid";
    public final static String INTERFACE_PROPERTY_F_DROID = "SUPFDroid";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("TW", "FullScreenActivity: onCreate");
        if (savedInstanceState == null) {
            Log.v("TW", "FullScreenActivity: onCreate reeeeeeeeeeeeeeeeeeload");
            boolean IS_DEBUG = 0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);

            // hide action bar
            getSupportActionBar().hide();
            setContentView(R.layout.activity_fullscreen);

            // init web view
            wv = WebHelper.getWebView();
            FrameLayout frameLayout = findViewById(R.id.webview_wrapper);
            frameLayout.addView(wv);

            // init JS here, as it needs an activity to work
            jsi = new JavaScriptInterface(this, wv, IS_DEBUG);
            wv.addJavascriptInterface(jsi, INTERFACE_PROPERTY);
            if (BuildConfig.FLAVOR.equals("fdroid")) {
                wv.addJavascriptInterface(jsi, INTERFACE_PROPERTY_F_DROID);
            }

            // also needs to be done here, because new Intent otherwise will crash the app
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.v("TW", url);

                    if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                        if (url.contains("super-productivity.com") || url.contains("localhost")) {
                            return false;
                        } else {
                            FullscreenActivity.this.startActivity(
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
            });

            wv.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    return super.onJsAlert(view, url, message, result);
                }

                @Override
                public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                    new AlertDialog.Builder(FullscreenActivity.this)
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            result.confirm();
                                        }
                                    })
                            .setNegativeButton(android.R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            result.cancel();
                                        }
                                    })
                            .create()
                            .show();

                    return true;
                }
            });

            // In case we want to make sure the most recent version is loaded
            // wv.clearCache(true);
            // wv.clearHistory();
        }
    }

    @Override
    protected void onDestroy() {
        Log.v("TW", "FullScreenActivity: onDestroy ");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("TW", "FullScreenActivity: onStop ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("TW", "FullScreenActivity: onPause");
        callJSInterfaceFunctionIfExists("onPause$.next");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("TW", "FullScreenActivity: onResume");
        callJSInterfaceFunctionIfExists("onResume$.next");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("TW", "FullScreenActivity: onStart");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Toast.makeText(this, "Google onActivityResult", Toast.LENGTH_SHORT).show();
        jsi.onActivityResult(requestCode, resultCode, data);
    }

    public void callJSInterfaceFunctionIfExists(final String fnName) {
        final String fnFullName = "window." + INTERFACE_PROPERTY + "." + fnName;
        callJavaScriptFunction("if(" + fnFullName + ")" + fnFullName + "();");
    }

    public void callJavaScriptFunction(final String script) {
        if (wv == null) {
            return;
        }
        wv.post(new Runnable() {
            @Override
            public void run() {
                wv.evaluateJavascript(script, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                    }
                });
            }
        });
    }
}
