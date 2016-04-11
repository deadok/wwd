package com.wamba.bob.wwd;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.wamba.bob.wwd.account.DatingAccount;
import com.wamba.bob.wwd.data.DatingContentProvider;
import com.wamba.bob.wwd.data.DatingContract;
import com.wamba.bob.wwd.data.DatingDbHelper;
import com.wamba.bob.wwd.network.NetworkService;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int REQ_SIGNIN = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        long userIdToshow = -1;
        if (null == savedInstanceState) {
            Uri uri = getIntent().getData();
            if (uri != null) {
                userIdToshow = DatingContract.ProfileEntry.getProfileIdFromUri(uri);
            }
        }

        String authToken = ((WwdApplication)getApplication()).getAuthToken();
        if (authToken == null) {
            AccountManager accountManager = AccountManager.get(this);
            Account[] accounts = accountManager.getAccountsByType(DatingAccount.TYPE);
            if (accounts.length == 0) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, REQ_SIGNIN);
            } else {
                long uid = Long.parseLong(accountManager.getUserData(accounts[0], DatingAccount.KEY_UESR_ID));
                ((WwdApplication)getApplication()).setCurrentUserId(uid);
                accountManager.getAuthToken(accounts[0], DatingAccount.TOKEN_TYPE, null, this, new GetAuthTokenCallback(), null);
                if (userIdToshow == -1) {
                    userIdToshow = uid;
                }
            }
        }

        //If we have user to show - let's show
        if (userIdToshow != -1) {
            updateFragment(userIdToshow);
        }
    }

    protected void updateFragment(long uid) {

        ProfileFragment pf = (ProfileFragment)getSupportFragmentManager().findFragmentById(R.id.profile_fragment);
        pf.onUriChanged(
                DatingContract.ProfileEntry.buildProfileUri(uid)
        );
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQ_SIGNIN) {
            ((WwdApplication)getApplication()).setAuthToken(data.getStringExtra(AccountManager.KEY_AUTHTOKEN));
            updateFragment(data.getLongExtra(DatingAccount.KEY_UESR_ID, 0));
        }
        Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_sign_out:

                AccountManager accountManager = AccountManager.get(this);
                Account[] accounts = accountManager.getAccountsByType(DatingAccount.TYPE);
                if (accounts.length != 0) {

                    accountManager.removeAccount(accounts[0], null, null);
                    WwdApplication.invalidateAuthToken();
                    DatingDbHelper dBHelper = new DatingDbHelper(this);
                    dBHelper.resetDatabase();
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivityForResult(intent, REQ_SIGNIN);
                }
                break;
            case R.id.action_show_somebody:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setData(DatingContract.ProfileEntry.buildProfileUri(437420475));
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    protected void loadData() {

        /*
        updateFragment(DatingContract.ProfileEntry.buildProfileUri(
                ((WwdApplication)getApplication()).getCurrentUserId()
        ));
        */

    }

    private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            Bundle bundle;

            try {
                bundle = result.getResult();

                final Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (null != intent) {
                    startActivityForResult(intent, REQ_SIGNIN);
                } else {
                    ((WwdApplication)getApplication()).setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
                    loadData();

                }
            } catch(OperationCanceledException e) {
                Log.e(LOG_TAG, e.getMessage());
            } catch(Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }

        }

    }

}
