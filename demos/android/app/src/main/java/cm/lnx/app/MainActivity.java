package cm.lnx.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;



public class MainActivity extends BaseActivity {
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isAuthed(this)) {
            return;
        }
        initViews();
    }


    public static Intent makeIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //初始化界面
    private void initViews() {
        setContent(R.layout.content_main);
        textView = (TextView) findViewById(R.id.text);
        OauthClient.getInstance().getOauth2Client().asyncGet("/v1/user/me", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final String msg = e.getMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("网络错误： " + msg);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String respBody = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.code() == 200) {
                            textView.setText("获取成功： " + respBody);
                        } else {
                            textView.setText("获取错误： " + respBody);
                        }
                    }
                });
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            OauthClient.getInstance().logout();
            return true;
        } else if (id == R.id.action_settings) {
            WebActivity.start(this, "账户设置", "https://account.nanxi.li/web/pref", true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
