package cm.lnx.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;

//
public class BaseAuthedActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        checkCreateAuth(this);
    }

    //登录并初始化
    private boolean isLogin() {
        return OauthClient.getInstance().getOauth2Client() != null;
    }



    protected boolean checkCreateAuth(Activity activity) {
        if (!isLogin()) {
            WelcomeActivity.start(this);
            activity.finish();
            return false;
        }
        return true;
    }
}
