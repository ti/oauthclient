package cm.lnx.auth.util;


import cm.lnx.auth.oauth.Config;
import cm.lnx.auth.oauth.Oauth2;
import cm.lnx.auth.oauth.Token;
import cm.lnx.auth.servlet.ServletContract;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Oauth2Utils {

    /**
     * 获取当前 Servlet 的Oauth客户端
     * @param request ServletRequest
     * @return 获取当前 Servlet 的Oauth客户端
     * @throws IOException
     */
    public static Oauth2 userServletClient(final HttpServletRequest request) throws IOException {
        Object o = request.getSession().getAttribute(ServletContract.OAUTH_CONNECT_TOKEN);
        if (o==null) {
            return null;
        }
        Config config = Config.getInstance();
        final Oauth2 client = new Oauth2(config, (Token) o);
        client.setOnTokenChangeListener(new Oauth2.TokenChangeListener() {
            @Override
            public void onRefresh(Token orgToken, Token newToken) {
                request.getSession().setAttribute(ServletContract.OAUTH_CONNECT_TOKEN, newToken);
            }
            @Override
            public void onRevoke(Token token) {
                request.getSession().removeAttribute(ServletContract.OAUTH_CONNECT_TOKEN);
            }
        });
        return  client;
    }

    /**
     * 重新对servlet进行授权
     * @param request
     * @param response
     * @throws IOException
     */
    public static void reAuthUserServlet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String redirectPath = request.getRequestURI();
        if (request.getQueryString() != null) {
            redirectPath += "?" + request.getQueryString();
        }
        String uri = Config.getInstance().getLoginURL() + "?redirect_uri=" + redirectPath;
        response.sendRedirect(uri);
    }


    private static Oauth2 clientCredentialsClient = null;

    /**
     * 获取 clientCredentials Oauth2 单例
     * @param config
     * @return
     * @throws IOException
     */
    public static Oauth2 clientCredentialsClient(Config config) throws IOException {
        if (clientCredentialsClient == null) {
            Token token = config.clientCredentialsToken();
            return new Oauth2(config, token);
        }
        return clientCredentialsClient;
    }


    /**
     * 获取 authTokenClient Oauth2
     * 一般可用于手机端，在手机APP初始化时，从配置文件读取并生成, 服务端是多用户的，不适合此接口
     * @param config 配置文件
     * @param tokenSource 已经保存的有效的TOKEN源。
     * @param listener  Token变化监听（如果token变化，需要本地回写TOKEN源数据）
     * @return
     * @throws IOException
     */
    public static Oauth2 newAuthTokenClient(Config config, Token tokenSource, Oauth2.TokenChangeListener listener) {
        Oauth2 authTokenClient =  new Oauth2(config, tokenSource);
        authTokenClient.setOnTokenChangeListener(listener);
        return authTokenClient;
    }
}
