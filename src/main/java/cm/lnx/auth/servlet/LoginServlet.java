package cm.lnx.auth.servlet;


import cm.lnx.auth.oauth.Config;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse w) throws IOException {
        //使用一个随机到字符串作为state，并写入到session，你也可以改为自己到逻辑
        String state = UUID.randomUUID().toString();
        r.getSession().setAttribute(ServletContract.OAUTH_CONNECT_STATE, state);
        Map<String, String> extarParams = null;
        //可选代码，callcak 回调到任何页面
        if (r.getParameter("redirect_uri") != null) {
            extarParams = new HashMap<>();
            extarParams.put("redirect_uri", r.getParameter("redirect_uri"));
            //如果需要其他参数，可参考文档，在这里添加
        }
        w.sendRedirect(Config.getInstance().getAuthCodeUrl(state,extarParams));
    }
}
