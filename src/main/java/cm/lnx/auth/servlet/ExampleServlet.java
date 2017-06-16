package cm.lnx.auth.servlet;


import cm.lnx.auth.util.Oauth2Utils;
import cm.lnx.auth.model.GenericResponse;
import cm.lnx.auth.model.UserInfo;
import cm.lnx.auth.oauth.Oauth2;
import cm.lnx.auth.oauth.Token;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 示例代码，代码包含获取用户ID，和接口调用部分；
 *
 * 基本请求演示：
 * GET 请求：
 * GenericResponse<UserInfo> userInfo = client.get("/apis/user/v1/userinfo",UserInfo.class);
 * POST 请求：
 * 1. GenericResponse<UserInfo> userInfoGenericResponse = client.postForm("/apis/user/v1/userinfo",new HashMap<String, String>(),UserInfo.class);
 * 2, GenericResponse<UserInfo> userInfoGenericResponse = client.postJson("/apis/user/v1/userinfo","{\"name\":\"exp\"}",UserInfo.class);
 * 其他高级请求：
 * GenericResponse<UserInfo> userInfo = client.execute("PATCH", "/apis/user/v1/userinfo", null, UserInfo.class);
 * Response res = client.response("DELETE", "/apis/user/v1/userinfo", null);
 */

public class ExampleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse w) throws IOException {
        Oauth2 client = Oauth2Utils.userServletClient(r);
        if (client == null) {
            //重新获取授权
            Oauth2Utils.reAuthUserServlet(r,w);
            return;
        }
        //判断用户是否是登录态（可选）
        Token token = client.getToken();
        if (!token.isExpired()) {
            //获取到用户信息
            String userId = token.getUserId();
        }
        //普通接口请求示例
        GenericResponse<UserInfo> userInfo = client.get("/apis/user/v1/userinfo",UserInfo.class);
        if(userInfo.getStatus() == 401) {
            //TOKEN过期， 可以重刷
            Oauth2Utils.reAuthUserServlet(r,w);
            return;
        }
        w.setContentType("text/html;charset=UTF-8");
        String outTemplate = "<!DOCTYPE html><head><title>%s</title></head><body><div>%s</div></body></html>";
        w.getWriter().print(String.format(outTemplate, "接口演示", "获取用户信息：<br>UID: " + userInfo.getData().getUserId() + "<br>姓名: " + userInfo.getData().getName()));
    }

}
