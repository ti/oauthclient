package cm.lnx.auth.oauth;

import org.junit.Test;

/**
 * Created by leenanxi on 2017/3/15.
 */
public class ConfigTest {

    @Test
    public void getUserAgent() throws Exception {
        Config config = Config.getInstance();

        System.out.println(config.getUserAgent());

    }

}