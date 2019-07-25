package cm.lnx.auth.oauth;


import cm.lnx.auth.model.GenericResponse;
import cm.lnx.auth.model.UserInfo;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

public class Oauth2 {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Token token;
    private Config config;
    private OkHttpClient client;
    private TokenChangeListener tokenChangeListener;
    private TokenUnauthorizedListener tokenUnauthorizedListener;

    /**
     * 创建OAUTH 客户端
     *
     * @param config      配置
     * @param tokenSource token源 （TOKEN在使用过程中可能会被刷新，如果需要监听刷新，可以使用setOnTokenChangeListener）
     */
    public Oauth2(Config config, Token tokenSource) {
        this.config = config;
        this.token = tokenSource;
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new RefreshTokenInterceptor()).build();
    }


    /**
     * 获取当前的httpclient，可用于更多自定义请求
     *
     * @return
     */
    public OkHttpClient getHttpClient() {
        return this.client;
    }

    /**
     * 注销当前Token
     *
     * @throws IOException
     */
    public void revoke(Callback callback) {
        if (tokenChangeListener != null) {
            tokenChangeListener.onRevoke(token);
        }
        JsonObject json = new JsonObject();
        json.addProperty("token", this.token.getAccessToken());
        RequestBody requestBody = RequestBody.create(JSON, json.toString());
        this.async("POST", config.getEndpoint().getRevokeURL(), requestBody, callback);
    }

    /**
     * 获取当前TOKEN
     *
     * @return
     */

    public Token getToken() {
        return token;
    }

    /**
     * 获取用户信息
     *
     * @return
     * @throws IOException
     */
    public GenericResponse<UserInfo> getUserInfo() throws IOException {
        if (this.token.getUserId() == null) {
            throw new IOException("Client Credentials Oauth client have on permission to get user data");
        }
        return this.get(this.config.getEndpoint().getUserInfoURL(), UserInfo.class);
    }

    /**
     * 重新从服务器端获取Token的数据
     *
     * @return
     * @throws IOException
     */
    public GenericResponse<Token> getTokenIntrospect() throws IOException {
        GenericResponse<Token> data = this.get(this.config.getEndpoint().getTokenIntrospectURL(), Token.class);
        if (data.isSucceed()) {
            data.getData().initExpiryAt();
        }
        return data;
    }

    /**
     * GET 数据
     *
     * @param apiUrl   API地址
     * @param classOfT
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> GenericResponse<T> get(String apiUrl, Class<T> classOfT) throws IOException {
        return this.execute("GET", apiUrl, null, classOfT);
    }

    /**
     * POST FORM数据
     *
     * @param apiUrl
     * @param params   参数字典
     * @param classOfT
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> GenericResponse<T> postForm(String apiUrl, Map<String, String> params, Class<T> classOfT) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        FormBody.Builder formBody = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBody.add(entry.getKey(), entry.getValue());
        }
        return this.execute("post", apiUrl, formBuilder.build(), classOfT);
    }

    public <T> GenericResponse<T> postJson(String apiUrl, String jsonBody, Class<T> classOfT) throws IOException {
        return this.execute("POST", apiUrl, RequestBody.create(JSON, jsonBody), classOfT);
    }

    /**
     * 获取API的返回对象
     *
     * @param method
     * @param apiUrl
     * @param body
     * @param classOfT
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> GenericResponse<T> execute(String method, String apiUrl, RequestBody body, Class<T> classOfT) throws IOException {
        if (!((apiUrl.startsWith("http://")) || (apiUrl.startsWith("https://")))) {
            apiUrl = config.getBaseURL() + apiUrl;
        }
        Request request = new Request.Builder()
                .header("User-Agent", config.getUserAgent())
                .url(apiUrl)
                .method(method, body)
                .build();
        Response response = client.newCall(request).execute();
        ;
        if (response.code() == 401) {
            if (this.tokenUnauthorizedListener != null) {
                this.tokenUnauthorizedListener.onUnauthorized();
            }
        }
        return new GenericResponse<T>(response.code(), response.body().string(), classOfT);
    }

    /**
     * 获取API的Response
     *
     * @param method
     * @param apiUrl
     * @param body
     * @return
     * @throws IOException
     */
    public Response response(String method, String apiUrl, RequestBody body) throws IOException {
        if (!((apiUrl.startsWith("http://")) || (apiUrl.startsWith("https://")))) {
            apiUrl = config.getBaseURL() + apiUrl;
        }
        Request request = new Request.Builder()
                .header("User-Agent", config.getUserAgent())
                .url(apiUrl)
                .method(method, body)
                .build();
        return client.newCall(request).execute();
    }

    /**
     * 异步请求
     *
     * @param method
     * @param apiUrl
     * @param body
     * @param callback 回调函数
     * @throws IOException
     */
    public void async(String method, String apiUrl, RequestBody body, Callback callback) {
        if (!((apiUrl.startsWith("http://")) || (apiUrl.startsWith("https://")))) {
            apiUrl = config.getBaseURL() + apiUrl;
        }
        if (callback == null) {
            callback = new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                }
            };
        }
        Request request = new Request.Builder()
                .header("User-Agent", config.getUserAgent())
                .url(apiUrl)
                .method(method, body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 异步GET请求
     *
     * @param apiUrl
     * @param callback
     * @throws IOException
     */
    public void asyncGet(String apiUrl, Callback callback) {
        this.async("GET", apiUrl, null, callback);
    }

    /**
     * 设定TOKEN变化时的回调。
     *
     * @param l
     */
    public void setOnTokenChangeListener(TokenChangeListener l) {
        tokenChangeListener = l;
    }

    /**
     * 设定TOKEN失效时的回调。
     * 一般会有两种情况。
     * 1. 用户在其他地方在您的系统登录了
     * 2. 用户后台撤销了对您应用的授权
     * <p>
     * 如果需要，可让用户重新登录授权。
     *
     * @param l
     */
    public void setOnTokenUnauthorizedListener(TokenUnauthorizedListener l) {
        tokenUnauthorizedListener = l;
    }


    public interface TokenChangeListener {
        void onRefresh(Token orgToken, Token newToken);

        void onRevoke(Token token);
    }

    /**
     * On Token Unauthorized
     */
    public interface TokenUnauthorizedListener {
        void onUnauthorized();
    }


    //auto refresh access token when it is isExpired
    private class RefreshTokenInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            if (token.isExpired() && token.getRefreshToken() != null) {
                synchronized (client) {
                    Token newToken = null;
                    try {
                        newToken = config.refreshToken(token.getRefreshToken());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (newToken != null) {
                        if (tokenChangeListener != null) {
                            tokenChangeListener.onRefresh(token, newToken);
                        }
                        token = newToken;
                    }
                }
            }
            Request request = chain.request();
            Request.Builder builder = request.newBuilder();
            builder.header("Authorization", token.getAuthorization());
            request = builder.build();
            Response response = chain.proceed(request);
            //如果TOKEN没有 UserId，则表示token为 Client ClientCredentials， 则需要使用 clientCredential 方式进行授权。
            if (response.code() == 401 && token.getUserId() == null) {
                synchronized (client) {
                    Token newToken = null;
                    try {
                        newToken = config.clientCredentialsToken();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (newToken != null) {
                        if (tokenChangeListener != null) {
                            tokenChangeListener.onRefresh(token, newToken);
                        }
                        token = newToken;
                        builder.header("Authorization", token.getAuthorization());
                        request = builder.build();
                        return chain.proceed(request);
                    }
                }
            }
            return response;
        }
    }
}
