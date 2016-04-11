package com.wamba.bob.wwd;

import android.database.Cursor;
import android.net.Uri;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wamba.bob.wwd.data.DatingContract;

/**
 * A placeholder fragment containing a profile view.
 */
public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int PROFILE_LOADER = 0;

    public static final String LOG_TAG = ProfileFragment.class.getSimpleName();

    private static final String[] PROFILE_COLUMNS = {
            DatingContract.ProfileEntry.COLUMN_PROFILE_ID,
            DatingContract.ProfileEntry.COLUMN_PROFILE_PHOTO_BIG,
            DatingContract.ProfileEntry.COLUMN_PROFILE_PHOTO,
            DatingContract.ProfileEntry.COLUMN_PROFILE_ABOUT
    };
    static final int COLUMN_PROFILE_ID = 0;
    static final int COLUMN_PROFILE_PHOTO_BIG = 1;
    static final int COLUMN_PROFILE_PHOTO = 2;
    static final int COLUMN_PROFILE_ABOUT = 3;

    public static final String PROFILE_URI = "URI";


    public ImageView mProfileImageView;
    public TextView mAboutTextView;
    public TextView mInterestsView;

    private Uri mUri;

    //private long mCurrentUid = -1;


    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
//        refreshcurrentUid();
        if (arguments != null) {
            mUri = arguments.getParcelable(ProfileFragment.PROFILE_URI);
        }
        /*
        else {
            if (mCurrentUid != -1) {
                getMyProfileUri();
                refreshcurrentUid();
            }
        }
        */
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mProfileImageView = (ImageView) rootView.findViewById(R.id.profile_image);
        mAboutTextView = (TextView) rootView.findViewById(R.id.about_text);
        mInterestsView = (TextView) rootView.findViewById(R.id.interests_text);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PROFILE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    PROFILE_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "loader reset");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mAboutTextView.setText(data.getString(COLUMN_PROFILE_ABOUT));
            Log.v(LOG_TAG, data.getString(COLUMN_PROFILE_ABOUT));
            mInterestsView.setText(String.valueOf(data.getString(COLUMN_PROFILE_ID)));
        }
    }
/*
    private Uri getMyProfileUri() {
        return DatingContract.ProfileEntry.buildProfileUri(mCurrentUid);
    }

    private void refreshcurrentUid() {
        mCurrentUid = ((WwdApplication) getActivity().getApplication()).getCurrentUserId();
    }

    public void showProfile() {
        refreshcurrentUid();
        mUri = getMyProfileUri();
        getLoaderManager().restartLoader(PROFILE_LOADER, null, this);
    }
    */
}
