package cm.lnx.auth.oauth;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by leenanxi on 2017/3/7.
 */
public class Token {

    private static final long expiryDelta = 60;

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("expires_in")
    private long expiresIn;
    @SerializedName("uid")
    private String uid;

    private Date expiryAt;

    @SerializedName("user_id")
    private String userId;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    private String getTokenType() {
        return tokenType;
    }


    public String getUserId() {
        if (uid != null) {
            return uid;
        }
        return userId;
    }

    public String getAuthorization() {
        return this.tokenType + " " + this.accessToken;
    }


    public Date initExpiryAt() {
        if (this.expiryAt != null) {
            return this.expiryAt;
        }
        this.expiryAt = new Date((new Date().getTime() / 1000 + this.expiresIn) * 1000);
        return this.expiryAt;
    }

    public Date getExpiryAt() {
        return this.expiryAt;
    }

    public boolean isExpired() {
        return  (new Date().getTime() + (expiryDelta * 1000)) > this.initExpiryAt().getTime();
    }

    public boolean isValid() {
        return this.accessToken != null && !this.isExpired();
    }

}
