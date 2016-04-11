package com.wamba.bob.wwd;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wamba.bob.wwd.data.DatingContentProvider;
import com.wamba.bob.wwd.data.DatingContract;
import com.wamba.bob.wwd.network.NetworkService;

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
            DatingContract.ProfileEntry.COLUMN_PROFILE_ABOUT,
            DatingContract.ProfileEntry.COLUMN_PROFILE_INTERESTS,
            DatingContract.ProfileEntry.COLUMN_PROFILE_UPDATED_TS,
            DatingContract.ProfileEntry.COLUMN_PROFILE_NAME,
            DatingContract.ProfileEntry.COLUMN_PROFILE_AGE


    };
    static final int COLUMN_PROFILE_ID = 0;
    static final int COLUMN_PROFILE_PHOTO_BIG = 1;
    static final int COLUMN_PROFILE_PHOTO = 2;
    static final int COLUMN_PROFILE_ABOUT = 3;
    static final int COLUMN_PROFILE_INTERESTS = 4;
    static final int COLUMN_PROFILE_UPDATED_TS = 5;
    static final int COLUMN_PROFILE_NAME = 6;
    static final int COLUMN_PROFILE_AGE = 7;

    public static final String PROFILE_URI = "URI";


    public ImageView mProfileImageView;
    public TextView mAboutTextView;
    public TextView mInterestsView;
    public Button mContactsButton;
    public Button mAlbumsButton;

    private Uri mUri;

    private long mCurrentUid = -1;
    private boolean mIsOwn = false;


    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(ProfileFragment.PROFILE_URI);
            updateCurrentUser();
        }
        */
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mProfileImageView = (ImageView) rootView.findViewById(R.id.profile_image);
        mAboutTextView = (TextView) rootView.findViewById(R.id.about_text);
        mInterestsView = (TextView) rootView.findViewById(R.id.interests_text);
        mContactsButton = (Button) rootView.findViewById(R.id.contacts_button);
        mAlbumsButton = (Button) rootView.findViewById(R.id.albums_button);
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
            mInterestsView.setText(data.getString(COLUMN_PROFILE_INTERESTS));
            String photoUrl = data.getString(COLUMN_PROFILE_PHOTO_BIG);
            if (null == photoUrl || photoUrl.equals("")) {
                photoUrl = data.getString(COLUMN_PROFILE_PHOTO);
            }
            Picasso.with(getContext())
                    .load(photoUrl)
                    .into(mProfileImageView);



            String title = String.format(getContext().getString(R.string.title_activity_main_template),
                    data.getString(COLUMN_PROFILE_NAME),
                    data.getInt(COLUMN_PROFILE_AGE),
                    mIsOwn ? getContext().getString(R.string.own_profile) : ""
            );

            getActivity().setTitle(title);

            long currentTs = Math.round(System.currentTimeMillis()/1000);
            if (currentTs - data.getLong(COLUMN_PROFILE_UPDATED_TS) > 300) {
               onRefreshData();
            }

        } else {
            mAboutTextView.setText("");
            mInterestsView.setText("");
            mProfileImageView.setImageBitmap(null);
            getActivity().setTitle("");
            onRefreshData();
        }
    }

    void onUriChanged(Uri newUri) {

        if (null == mUri || !mUri.equals(newUri)) {
            mUri = newUri;
            updateCurrentUser();
            getLoaderManager().restartLoader(PROFILE_LOADER, null, this);
        }
    }

    void onRefreshData() {
        Intent intent = new Intent(getContext(), NetworkService.class);
        int buttonsVisibility;
        if (mIsOwn) {
            intent.setAction(NetworkService.ACTION_LOAD_MY_PROFILE);
        } else {
            intent.setAction(NetworkService.ACTION_LOAD_PROFILE);
            intent.putExtra(NetworkService.KEY_FIRST_LONG_PARAMETER, mCurrentUid);

        }
        getActivity().startService(intent);
    }


    /**
     * updates current user id
     */
    private void updateCurrentUser() {
        mCurrentUid = DatingContract.ProfileEntry.getProfileIdFromUri(mUri);
        mIsOwn = mCurrentUid == ((WwdApplication) getActivity().getApplication()).getCurrentUserId();
        int buttonsVisibility = mIsOwn ? View.VISIBLE : View.GONE;
        mContactsButton.setVisibility(buttonsVisibility);
        mAlbumsButton.setVisibility(buttonsVisibility);
    }
}
