package cm.lnx.app;

import android.app.Application;

/**
 * Created by leenanxi on 2017/3/10.
 */

public class DemoApplication extends Application {
    public void onCreate() {
        super.onCreate();
        OauthClient.getInstance().init(this);
    }
}
