package com.ofamilymedia.trumpet.classes;

import twitter4j.TwitterException;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.internal.http.HttpClientWrapper;
import twitter4j.internal.http.HttpParameter;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.logging.Logger;
import twitter4j.media.ImageUpload;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class CustomImageUpload implements ImageUpload {
    public static final String TWITTER_VERIFY_CREDENTIALS_JSON = "https://api.twitter.com/1/account/verify_credentials.json";
    public static final String TWITTER_VERIFY_CREDENTIALS_XML = "https://api.twitter.com/1/account/verify_credentials.xml";

    private HttpClientWrapper client;

    protected Configuration conf = null;
    protected String apiEndpoint = null;
    protected OAuthAuthorization oauth = null;
    protected String uploadUrl = null;
    protected HttpParameter[] postParameter = null;
    protected HttpParameter[] appendParameter = null;
    protected HttpParameter image = null;
    protected HttpParameter message = null;
    protected Map<String, String> headers = new HashMap<String, String>();
    protected HttpResponse httpResponse = null;
    protected static final Logger logger = Logger.getLogger(CustomImageUpload.class);

    CustomImageUpload(Configuration conf) {
        //this.oauth = oauth;
        this.conf = conf;
        client = new HttpClientWrapper(conf);
    }

    public CustomImageUpload(Configuration conf, String apiEndpoint) {
        this(conf);
        this.apiEndpoint = apiEndpoint;
    }

    public String upload(String imageFileName, InputStream imageBody) throws TwitterException {
        this.image = new HttpParameter("media", imageFileName, imageBody);
        return upload();
    }

    public String upload(String imageFileName, InputStream imageBody, String message) throws TwitterException {
        this.image = new HttpParameter("media", imageFileName, imageBody);
        this.message = new HttpParameter("message", message);
        return upload();
    }

    public String upload(File file, String message) throws TwitterException {
        this.image = new HttpParameter("media", file);
        this.message = new HttpParameter("message", message);
        return upload();
    }

    public String upload(File file) throws TwitterException {
        this.image = new HttpParameter("media", file);
        return upload();
    }

    public String upload() throws TwitterException {
        if (conf.getMediaProviderParameters() != null) {
            Set set = conf.getMediaProviderParameters().keySet();
            HttpParameter[] params = new HttpParameter[set.size()];
            int pos = 0;
            for (Object k : set) {
                String v = conf.getMediaProviderParameters().getProperty((String) k);
                params[pos] = new HttpParameter((String) k, v);
                pos++;
            }
            this.appendParameter = params;
        }
        preUpload();

        Log.w("UPLOAD_URL", uploadUrl);

        if (this.postParameter == null) {
            throw new AssertionError("Incomplete implementation. postParameter is not set.");
        }
        if (this.uploadUrl == null) {
            throw new AssertionError("Incomplete implementation. uploadUrl is not set.");
        }
        if (conf.getMediaProviderParameters() != null && this.appendParameter.length > 0) {
            this.postParameter = appendHttpParameters(this.postParameter, this.appendParameter);
        }
        httpResponse = client.post(uploadUrl, postParameter, headers);

        String mediaUrl = postUpload();
        logger.debug("uploaded url [" + mediaUrl + "]");

        return mediaUrl;
    }

    protected String postUpload() throws TwitterException {
        int statusCode = httpResponse.getStatusCode();
        if (statusCode != 200)
            throw new TwitterException("Custom image upload returned invalid status code", httpResponse);

        String response = httpResponse.asString();

        try {
            JSONObject json = new JSONObject(response);
            if (!json.isNull("url"))
                return json.getString("url");
        } catch (JSONException e) {
            if (-1 != response.indexOf("<mediaurl>")) {
                return response.substring(response.indexOf("<mediaurl>") + "<mediaurl>".length(), response.indexOf("</mediaurl>"));
            }

        	throw new TwitterException("Invalid Server response: " + response, e);
        }

        throw new TwitterException("Unknown Server response", httpResponse);
    }

    protected void preUpload() throws TwitterException {
        uploadUrl = apiEndpoint; //"https://twitpic.com/api/2/upload.json";
        String verifyCredentialsAuthorizationHeader = generateVerifyCredentialsAuthorizationHeader(TWITTER_VERIFY_CREDENTIALS_JSON);

        headers.put("X-Auth-Service-Provider", TWITTER_VERIFY_CREDENTIALS_JSON);
        headers.put("X-Verify-Credentials-Authorization", verifyCredentialsAuthorizationHeader);

        HttpParameter[] params = {this.image};
        if (message != null) {
            params = appendHttpParameters(new HttpParameter[]{
                    this.message
            }, params);
        }
        this.postParameter = params;
    }
    
    protected HttpParameter[] appendHttpParameters(HttpParameter[] src, HttpParameter[] dst) {
        int srcLen = src.length;
        int dstLen = dst.length;
        HttpParameter[] ret = new HttpParameter[srcLen + dstLen];
        System.arraycopy(src, 0, ret, 0, srcLen);
        System.arraycopy(dst, 0, ret, srcLen, dstLen);
        return ret;
    }

    protected String generateVerifyCredentialsAuthorizationHeader(String verifyCredentialsUrl) {
        if(oauth == null) return "";
    	List<HttpParameter> oauthSignatureParams = oauth.generateOAuthSignatureHttpParams("GET", verifyCredentialsUrl);
        return "OAuth realm=\"http://api.twitter.com/\"," + OAuthAuthorization.encodeParameters(oauthSignatureParams, ",", true);
    }

    protected String generateVerifyCredentialsAuthorizationURL(String verifyCredentialsUrl) {
    	if(oauth == null) return "";
    	List<HttpParameter> oauthSignatureParams = oauth.generateOAuthSignatureHttpParams("GET", verifyCredentialsUrl);
        return verifyCredentialsUrl + "?" + OAuthAuthorization.encodeParameters(oauthSignatureParams);
    }
}
