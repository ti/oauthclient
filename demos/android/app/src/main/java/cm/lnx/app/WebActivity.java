package cm.lnx.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;


public class WebActivity extends BaseActivity {
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private static final String EXTRA_WEB_TITLE = "extra_web_title";
    private static final String EXTRA_WEB_URL = "extra_web_url";
    private static final String EXTRA_AUTH_REQUIRED = "extra_auth_required";
    private String webURL;
    private String webTitle;
    private boolean authRequired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webURL = getIntent().getStringExtra(EXTRA_WEB_URL);
        webTitle = getIntent().getStringExtra(EXTRA_WEB_TITLE);
        authRequired = getIntent().getBooleanExtra(EXTRA_AUTH_REQUIRED, false);
        if (authRequired &&!isAuthed(this)) {
            return;
        }
        setContent(R.layout.content_web);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(webTitle);


        mWebView = findViewById(R.id.webView);
        mProgressBar = this.findViewById(R.id.progress);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mProgressBar.setVisibility(View.VISIBLE);
                if (!Uri.parse(url).getScheme().contains("http")) {
                    Uri uri = Uri.parse(url);
                    String authCode = uri.getQueryParameter("code");
                    String error = uri.getQueryParameter("error");
                    if (authCode != null) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    } else if (error != null) {
                        Toast.makeText(getApplicationContext(), "ERROR: " + error, Toast.LENGTH_SHORT).show();
                    }
                    mWebView.setVisibility(View.GONE);
                    return;
                }
                super.onPageStarted(view, url, favicon);
            }
        });
        mWebView.loadUrl(webURL);
    }

    public static void start(Activity fromActivity, String title, String url, boolean authRequired) {
        Intent intent = new Intent(fromActivity, WebActivity.class);
        if (url != null) {
            intent.putExtra(WebActivity.EXTRA_WEB_TITLE, title);
            intent.putExtra(WebActivity.EXTRA_WEB_URL, url);
            intent.putExtra(WebActivity.EXTRA_AUTH_REQUIRED, authRequired);
        }
        fromActivity.startActivity(intent);
    }
}
