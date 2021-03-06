package com.wamba.bob.wwd;

import android.accounts.AccountManager;
import android.app.Application;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.wamba.bob.wwd.account.DatingAccount;
import com.wamba.bob.wwd.network.WambaApiClient;

/**
 * Created by Bob on 05.04.2016.
 */
public class WwdApplication extends Application
{
    private long mUid = -1;

    private String mAuthToken = null;

    private static WwdApplication sInstance;


    @Override
    public void onCreate()
    {
        super.onCreate();
        sInstance = this;


        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }

    public void setCurrentUserId(Long uid) {
        mUid = uid;
    }

    public long getCurrentUserId() {
        return mUid;
    }

    public void setAuthToken(String authToken) {
        mAuthToken = authToken;
        WambaApiClient.setSid(authToken);
    }

    public String getAuthToken() {
        return mAuthToken;
    }


    public static void invalidateAuthToken() {
        invalidateAuthToken(sInstance.getAuthToken());
    }

    public static void invalidateAuthToken (String authToken) {
        sInstance.setAuthToken(null);
        sInstance.invalidateTokenInAM(authToken);
    }

    private void invalidateTokenInAM(String authToken) {
        AccountManager am = AccountManager.get(this);
        am.invalidateAuthToken(DatingAccount.TYPE, authToken);

    }

    @Override
    public void onTerminate() {
        sInstance = null;
        super.onTerminate();
    }

}
