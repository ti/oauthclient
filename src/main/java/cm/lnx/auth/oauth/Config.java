package cm.lnx.auth.oauth;

import cm.lnx.auth.util.GsonUtils;
import okhttp3.*;
import okhttp3.internal.Version;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置文件
 * 手机端可自行创建
 */
public class Config {
    private String clientID;
    private String clientSecret;
    private String loginURL;
    private String scope;
    private String clientCredentialsScope;
    private String userAgent;
    private Endpoint endpoint;
    private String baseURL;
    private String redirectURI;
    private boolean devMode;
    private final String version = "1.0.0";

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public static class Endpoint {
        private String authURL;
        private String tokenURL;
        private String revokeURL;
        private String tokenIntrospectURL;
        private String userInfoURL;

        public String getAuthURL() {
            return authURL;
        }

        public void setAuthURL(String authURL) {
            this.authURL = authURL;
        }

        public String getTokenURL() {
            return tokenURL;
        }

        public void setTokenURL(String tokenURL) {
            this.tokenURL = tokenURL;
        }

        public String getRevokeURL() {
            return revokeURL;
        }

        public void setRevokeURL(String revokeURL) {
            this.revokeURL = revokeURL;
        }

        public String getTokenIntrospectURL() {
            return tokenIntrospectURL;
        }

        public void setTokenIntrospectURL(String tokenIntrospectURL) {
            this.tokenIntrospectURL = tokenIntrospectURL;
        }

        public String getUserInfoURL() {
            return userInfoURL;
        }

        public void setUserInfoURL(String userInfoURL) {
            this.userInfoURL = userInfoURL;
        }
    }


    public String getUserAgent() {
        if (userAgent == null) {
            userAgent = "Oauth2ApiClient/" + version + " (" + System.getProperty("os.name") + " " + System.getProperty("os.version").replace(".", "_") + ")" + " " +  Version.userAgent() + " (JAVA " + System.getProperty("java.version").replace(".", "_") + ")";
        }
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getLoginURL() {
        return loginURL;
    }

    public void setLoginURL(String loginURL) {
        this.loginURL = loginURL;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getClientCredentialsScope() {
        return clientCredentialsScope;
    }

    public void setClientCredentialsScope(String clientCredentialsScope) {
        this.clientCredentialsScope = clientCredentialsScope;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }


    private static Config instance = null;
    private final OkHttpClient client = new OkHttpClient();

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
            instance.endpoint = new Endpoint();
        }
        return instance;
    }


    //Exchange token by authorizationCode
    public Token clientCredentialsToken() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials");
        if (this.clientCredentialsScope != null) {
            params.put("scope", this.clientCredentialsScope);
        }
        return retrieveToken(params);
    }

    //Exchange token by password
    public Token passwordCredentialsToken(String username, String password, Map<String, String> params) throws IOException {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("grant_type", "password");
        params.put("username", username);
        params.put("password", password);
        if (this.scope != null) {
            params.put("scope", this.scope);
        }
        return retrieveToken(params);
    }


    //Exchange token by authorizationCode
    public Token exchangeToken(String authorizationCode) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", authorizationCode);
        return retrieveToken(params);
    }


    private Token retrieveToken(Map<String, String> params) throws IOException {
        FormBody.Builder formBody = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBody.add(entry.getKey(), entry.getValue());
        }

        String apiUrl = this.endpoint.tokenURL;
        if (!((apiUrl.startsWith("https://")) || (apiUrl.startsWith("https://")))) {
            apiUrl = this.getBaseURL() + apiUrl;
        }
        Request request = new Request.Builder()
                .header("Accpet", "application/json")
                .header("User-Agent", getUserAgent())
                .header("Authorization", Credentials.basic(this.clientID, this.clientSecret))
                .url(apiUrl)
                .post(formBody.build())
                .build();
        Response response = client.newCall(request).execute();
        String respBody = response.body().string();
        if (response.code() != 200) {
            throw new IOException(String.format("Token Retrieve Error response code: %s for response body %s", response.code(), respBody));
        }
        Token token = GsonUtils.fromJson(respBody, Token.class);
        if (!token.isValid()) {
            throw new IOException(String.format("Token Retrieve Error for response body %s", respBody));
        }
        return token;
    }


    //Get auth code url
    public Token refreshToken(String refreshToken) throws IOException {
        if (refreshToken == null) {
            throw new IOException("Oauth refresh token error for refreshToken can not be null");
        }
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", refreshToken);
        return retrieveToken(params);
    }

    public String getAuthCodeUrl(String state, Map<String, String> params) {
        return getRedirectURL("code", state, scope, redirectURI, params);
    }

    public String getRedirectURL(String responseType, String state, String scope, String redirectURL, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.endpoint.authURL + "?client_id=" + this.clientID);
        if (params == null) {
            params = new HashMap<>();
        } else {
            if (params.get("redirect_uri") != null) {
                redirectURL = redirectURL + "?redirect_uri=" + params.get("redirect_uri");
            }
        }
        params.put("redirect_uri", redirectURL);
        params.put("response_type", responseType);
        params.put("state", state);
        params.put("scope", scope);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("&" + entry.getKey() + "=" + entry.getValue());
        }
        return sb.toString();
    }


}
