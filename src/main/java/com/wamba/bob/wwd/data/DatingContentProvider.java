/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wamba.bob.wwd.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

public class DatingContentProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DatingDbHelper mDbHelper;

    public static final int PROFILES_MATCH = 100;
    public static final int PROFILE_MATCH = 101;
    public static final int CONTACTS_MATCH = 200;
    public static final int CONTACT_MATCH = 201;
    public static final int ALBUMS_MATCH = 300;
    public static final int ALBUM_MATCH = 301;

    public static final String LOG_TAG = DatingContentProvider.class.getSimpleName();

    private static final SQLiteQueryBuilder sContactsWithProfiles;

    static{
        sContactsWithProfiles = new SQLiteQueryBuilder();
        
        sContactsWithProfiles.setTables(
                DatingContract.ContactEntry.TABLE_NAME + " INNER JOIN " +
                        DatingContract.ProfileEntry.TABLE_NAME +
                        " ON " + DatingContract.ContactEntry.TABLE_NAME +
                        "." + DatingContract.ContactEntry.COLUMN_PROFILE_ID +
                        " = " + DatingContract.ProfileEntry.TABLE_NAME +
                        "." + DatingContract.ProfileEntry.COLUMN_PROFILE_ID);
    }

    public static final String sProfileSelection =
            DatingContract.ProfileEntry.TABLE_NAME+
                    "." + DatingContract.ProfileEntry.COLUMN_PROFILE_ID + " = ? ";

    static UriMatcher buildUriMatcher() {


        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = DatingContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DatingContract.PATH_PROFILE + "/#", PROFILES_MATCH);
        matcher.addURI(authority, DatingContract.PATH_PROFILE, PROFILE_MATCH);
        matcher.addURI(authority, DatingContract.PATH_CONTACT, CONTACTS_MATCH);
        matcher.addURI(authority, DatingContract.PATH_CONTACT + "/#", CONTACT_MATCH);
        matcher.addURI(authority, DatingContract.PATH_ALBUM, ALBUMS_MATCH);
        matcher.addURI(authority, DatingContract.PATH_ALBUM + "/#", ALBUM_MATCH);
        return matcher;

    }


    public static UriMatcher getUriMatcher() {
        return sUriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DatingDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case PROFILES_MATCH:
                return DatingContract.ProfileEntry.CONTENT_TYPE;
            case PROFILE_MATCH:
                return DatingContract.ProfileEntry.CONTENT_TYPE;
            case ALBUMS_MATCH:
                return DatingContract.AlbumEntry.CONTENT_TYPE;
            case CONTACTS_MATCH:
                return DatingContract.ContactEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Log.v(LOG_TAG, "query: " + uri.toString());

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            case PROFILE_MATCH:
                if (null == selection) {
                    selection = sProfileSelection;
                    Long uid = DatingContract.ProfileEntry.getProfileIdFromUri(uri);
                    selectionArgs = new String[]{uid.toString()};
                }
            case PROFILES_MATCH: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        DatingContract.ProfileEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case CONTACTS_MATCH: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        DatingContract.ContactEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ALBUMS_MATCH: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        DatingContract.ContactEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PROFILES_MATCH: {
                long id = db.insert(DatingContract.ProfileEntry.TABLE_NAME, null, values);
                if ( id > 0 )
                    returnUri = DatingContract.ProfileEntry.buildProfileUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ALBUM_MATCH: {
                long id = db.insert(DatingContract.AlbumEntry.TABLE_NAME, null, values);
                if ( id > 0 )
                    returnUri = DatingContract.AlbumEntry.buildAlbumsUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted =0;

        return rowsDeleted;
    }


    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PROFILES_MATCH:
            case PROFILE_MATCH:
                db.beginTransaction();
                int returnCount = 0;
                ArrayList<Long> notifyIds = new ArrayList<>(0);
                try {
                    for (ContentValues value : values) {
                        long id = db.insertWithOnConflict(DatingContract.ProfileEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (id != -1) {
                            returnCount++;
                            notifyIds.add(id);
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                ContentResolver contentResolver = getContext().getContentResolver();
                contentResolver.notifyChange(uri, null);
                for (int i = 0; i < notifyIds.size(); i++) {
                    contentResolver.notifyChange(
                            DatingContract.ProfileEntry.buildProfileUri(notifyIds.get(i)),
                            null
                    );
                }
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    public void clearDB() {
        mDbHelper.resetDatabase();
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mDbHelper.close();
        super.shutdown();
    }



}