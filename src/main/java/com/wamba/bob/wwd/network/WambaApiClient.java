package com.wamba.bob.wwd.network;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonIOException;
import com.wamba.bob.wwd.WwdApplication;
import com.wamba.bob.wwd.data.model.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Bob on 02.04.2016.
 */
public class WambaApiClient {

    public static final String BASE_URL = "https://api.mobile-api.ru/v5.2.1.0";

    public static String EXTRA_PROFILE_PARCEL = "profile_parcel";

    public static final String LOG_TAG = WambaApiClient.class.getSimpleName();

    private Context mContext = null;

    private static String mSid = null;

    private static final String METHOD_GET = "GET";

    private static final String METHOD_POST = "POST";

    public WambaApiClient() {
    }

    public static void setSid(String sid) {
        mSid = sid;
    }

    private Uri.Builder getBaseUriBuilder() {
        Uri.Builder baseUriBuilder = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("lang_id", "en");
        return baseUriBuilder;
    }

    public Profile getProfile() {
        Uri.Builder builder = getBaseUriBuilder()
                .appendEncodedPath("profile");
        return getProfileOrUser(builder);
    }



    public Profile getUser(Integer uid) {
        Uri.Builder builder = getBaseUriBuilder()
                .appendEncodedPath("users")
                .appendEncodedPath(uid.toString());

        return getProfileOrUser(builder);
    }

    private Profile getProfileOrUser(Uri.Builder builder) {
        Profile profile = null;
        JSONObject profileResponse = performRequest(builder, "", METHOD_GET);
        try {
            if (profileResponse.has("anketa")) {
                profile = new Profile(profileResponse.getJSONObject("anketa"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return profile;
    }


    public static String login(String login, String password) {
        WambaApiClient client = new WambaApiClient();
        client.getSid();
        Profile profile = client.internalLogin(login, password);
        if (profile.uid != -1) {
            return mSid;
        }
        return null;
    }

    public static Intent login(String login, String password, boolean returnProfile ) {
        WambaApiClient client = new WambaApiClient();
        client.getSid();
        Intent retIntent = new Intent();
        Profile profile = client.internalLogin(login, password);
        if (profile != null && profile.uid != -1) {
            retIntent.putExtra(AccountManager.KEY_AUTHTOKEN, mSid);
            retIntent.putExtra(EXTRA_PROFILE_PARCEL, profile);
        }

        return retIntent;
    }

    private Profile internalLogin(String login, String password) {

        Profile profile = new Profile();

        Uri.Builder builder = getBaseUriBuilder()
                .appendEncodedPath("login");

        String postData = "";
        try {
            JSONObject params = new JSONObject();
            params.put("login", login);
            params.put("password", password);
            params.put("pushTypes", "");
            params.put("sid", mSid);
            postData = params.toString();
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        JSONObject loginResponse = performRequest(builder, postData, METHOD_POST);
        try {
            if (loginResponse.has("errors") && !loginResponse.getBoolean("isAuth")) {
                return null;
            } else {
                profile = new Profile(loginResponse.getJSONObject("profile"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            //e.printStackTrace();
        }
        return profile;
    }

    public String getSid() {
        Uri.Builder builder = getBaseUriBuilder()
                .appendEncodedPath("session");

        JSONObject dataContainer = performRequest(builder, "", METHOD_GET);

        try {
            mSid = dataContainer.getString("sid");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return mSid;
    }

    @Nullable
    private JSONObject performRequest(Uri.Builder builder, String requestBody, String requestMethod) {

        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        OutputStream writer = null;
        String result = "";
        builder.appendEncodedPath("/");

        try {
            URL url = new URL(builder.build().toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            if (mSid != null) {
                urlConnection.setRequestProperty("Cookie", "sid=" + mSid);
                Log.d(LOG_TAG, "Request sid: " + mSid);
            }
            urlConnection.setRequestMethod(requestMethod);
            urlConnection.connect();

            if (requestMethod != METHOD_GET) {

                try {
                    writer = new BufferedOutputStream(urlConnection.getOutputStream());
                    writer.write(requestBody.getBytes());
                    Log.v(LOG_TAG, "Post body: " + requestBody);
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }

            }

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                //throw new NetworkErrorException();
                return null;
            }


            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            result = buffer.toString();
            Log.v(LOG_TAG, "Response: " + result);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
            }
        }

        JSONObject response = null;
        try {
            response = new JSONObject(result);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            try {
                response = new JSONObject("{}");
            } catch (JSONException ex ) {
            }
        }
        checkResponseStatus(response);
        return response;
    }

    /*
     * Validates sid
     */
    void checkResponseStatus(JSONObject response) {
        try {
            if (response.has("sid")) {
                String responseSid = response.getString("sid");
                if (mSid != null &&
                        ( !responseSid.equals(mSid) ||
                                ( response.has("error") && !response.getBoolean("isAuth") )
                        )
                ) {
                    WwdApplication.invalidateAuthToken(mSid);
                }
            }
        } catch (JSONException e) {

        }
    }
}
