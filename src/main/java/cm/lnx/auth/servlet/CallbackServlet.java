package cm.lnx.auth.servlet;

import cm.lnx.auth.oauth.Config;
import cm.lnx.auth.oauth.Oauth2;
import cm.lnx.auth.oauth.Token;
import cm.lnx.auth.util.Oauth2Utils;
import cm.lnx.auth.model.GenericResponse;
import cm.lnx.auth.model.UserInfo;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class CallbackServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse w) throws IOException {
        String authorizationCode = r.getParameter("code");
        String error = r.getParameter("error");
        w.setContentType("text/html;charset=UTF-8");
        //未能获取到code，处理错误, 一般情况下，是用户拒绝授权，服务器端给出具体提示。
        if (authorizationCode == null || authorizationCode.length() == 0 || error != null) {
            String errorDescription = r.getParameter("error_description");
            String outTemplate = "<!DOCTYPE html><head><title>%s</title></head><body><div>%s</div></body></html>";
            w.getWriter().print(String.format(outTemplate, "用户授权失败 - " + error, "用户授权失败，无法访问该系统，请<a href='" + Config.getInstance().getLoginURL()+ "'>重新授权</a><br>详情:" + errorDescription));
            return;
        }
        //判断state是否合法
        String state = (String) r.getSession().getAttribute(ServletContract.OAUTH_CONNECT_STATE);
        r.getSession().removeAttribute(ServletContract.OAUTH_CONNECT_STATE);
        if (state == null || state.length() == 0 || !state.equals(r.getParameter("state"))) {
            w.setStatus(400);
            w.getWriter().print("Error Bad Request: 请求可能被篡改");
            return;
        }
        //获取token
        Token token = Config.getInstance().exchangeToken(authorizationCode);
        getServletContext().log("GET Oauth AccessToken: " + token.getAccessToken());
        r.getSession().setAttribute(ServletContract.OAUTH_CONNECT_TOKEN, token);
        //TODO: 判断用户是否已存在于您的系统，如果存在，触发您当前用户的登录逻辑（可选）
        String myUser = getUserByOauthUid(token.getUserId());
        if (myUser != null) {
            //TODO: 系统中已经存在该用户，直接触发查找到用户的逻辑。
        } else {
            //创建您的系统用户
            Oauth2 oauth2 = Oauth2Utils.userServletClient(r);
            GenericResponse<UserInfo> userInfo = oauth2.getUserInfo();
            System.out.println("TODO：关联用户信息到系统" + userInfo.getData().getUserId() + "姓名：" + userInfo.getData().getName() + " 数据:" + userInfo.toString());
        }
        //用户登录成功，触发业务逻辑
        String redirectUri = r.getParameter("redirect_uri");
        if (redirectUri == null || redirectUri.length() == 0) {
            redirectUri = "/";
        }
        w.sendRedirect(redirectUri);
    }

    /**
     * 使用Oauth获取到的用户ID查找您的系统用户
     *
     * @param oauthUserId Oauth获取到的用户ID
     * @return 您的系统用户
     */
    private String getUserByOauthUid(String oauthUserId) {
        return "也许你需要关联你的系统用户（可选）";
    }


}
