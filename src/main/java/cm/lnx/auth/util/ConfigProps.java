package cm.lnx.auth.util;


import java.io.*;
import java.util.Properties;


public class ConfigProps {
    private static Properties props = new Properties();

    static {
        String propFileName = "oauthconfig.properties";
        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getValue(String key) {
        return props.getProperty(key);
    }

    public static void updateProperties(String key, String value) {
        props.setProperty(key, value);
    }

}
