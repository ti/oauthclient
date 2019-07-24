package cm.lnx.app;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cm.lnx.auth.oauth.Config;
import cm.lnx.auth.oauth.Token;

public class LoginActivity extends BaseActivity {
    private UserLoginTask mAuthTask;
    private static final String EXTRA_ON_LOGIN = "extra_on_login";
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private Config mOauthConfig;
    private Intent onLoginIntent;

    public static Intent makeIntent(Context context, Intent onLogin) {
        Intent intent = new Intent(context, LoginActivity.class);
        if(onLogin != null) {
            intent.putExtra(LoginActivity.EXTRA_ON_LOGIN, onLogin);
        }
        return intent;
    }

    public static void start(Activity fromActivity, Intent onLogin) {
        Intent intent = makeIntent(fromActivity, onLogin);
        fromActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.content_login);
        initViews();
    }

    private void initViews() {
        onLoginIntent = getIntent()
                .getParcelableExtra(EXTRA_ON_LOGIN);
        mWebView = (WebView) findViewById(R.id.webView);
        mProgressBar = (ProgressBar) this.findViewById(R.id.progress);
        mOauthConfig = OauthClient.getInstance().getOauthConfig();
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
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
                    if (authCode != null ) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mAuthTask = new UserLoginTask();
                        mAuthTask.execute(authCode);
                    } else if (error != null) {
                        Toast.makeText(getApplicationContext(),"ERROR: " + error, Toast.LENGTH_SHORT).show();
                    }
                    mWebView.setVisibility(View.GONE);
                    return;
                }
                super.onPageStarted(view, url, favicon);
            }

        });

        CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
                Map<String, String> params = new HashMap<>();
                params.put("access_type","offline");
                String url = OauthClient.getInstance().getOauthConfig().getAuthCodeUrl("oauth-android-login", params);
                mWebView.loadUrl(url);
            }
        });
    }

    /**
     * 异步获取token
     */
    public class UserLoginTask extends AsyncTask<String, String, Boolean> {

        Token token;

        @Override
        protected void onProgressUpdate(String... values) {
            //显示错误
            Toast.makeText(getApplicationContext(),  values[0], Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                token = mOauthConfig.exchangeToken(params[0]);
            } catch (IOException e) {
                publishProgress("通讯错误 - " + e.getMessage());
                e.printStackTrace();
                return false;
            }
            //保存token
            PrefUtils.saveToken(LoginActivity.this, token);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success) {
                //跳转到新页面
                if (onLoginIntent == null) {
                    onLoginIntent = MainActivity.makeIntent(LoginActivity.this);
                }
                onLoginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
                startActivity(onLoginIntent);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}
