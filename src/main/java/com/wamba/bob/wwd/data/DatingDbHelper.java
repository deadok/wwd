package com.wamba.bob.wwd.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manages a local database for dating data.
 */
public class DatingDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 8;

    static final String DATABASE_NAME = "dating.db";

    public DatingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_PROFILE_TABLE = "CREATE TABLE " + DatingContract.ProfileEntry.TABLE_NAME + " (" +
                DatingContract.ProfileEntry.COLUMN_PROFILE_ID + " INTEGER PRIMARY KEY," +
                DatingContract.ProfileEntry.COLUMN_PROFILE_NAME + " TEXT NOT NULL, " +
                DatingContract.ProfileEntry.COLUMN_PROFILE_AGE + " INTEGER NOT NULL, " +
                DatingContract.ProfileEntry.COLUMN_PROFILE_ABOUT + " TEXT, " +
                DatingContract.ProfileEntry.COLUMN_PROFILE_INTERESTS + " TEXT, " +
                DatingContract.ProfileEntry.COLUMN_PROFILE_PHOTO + " TEXT NOT NULL, " +
                DatingContract.ProfileEntry.COLUMN_PROFILE_PHOTO_BIG + " TEXT, " +
                DatingContract.ProfileEntry.COLUMN_PROFILE_ALBUMS_COUNT + " INTEGER, " +
                DatingContract.ProfileEntry.COLUMN_PROFILE_CONTACTS_COUNT + " INTEGER, " +
                DatingContract.ProfileEntry.COLUMN_PROFILE_UPDATED_TS + " INTEGER NOT NULL " +
                " );";

        final String SQL_CREATE_CONTACT_TABLE = "CREATE TABLE " + DatingContract.ContactEntry.TABLE_NAME + " (" +
                DatingContract.ContactEntry.COLUMN_PROFILE_ID + " INTEGER PRIMARY KEY," +
                DatingContract.ContactEntry.COLUMN_MESSAGES_COUNT + " INTEGER NOT NULL, " +
                DatingContract.ContactEntry.COLUMN_UPDATED_TS + " INTEGER NOT NULL " +
                " );";

        final String SQL_CREATE_ALBUM_TABLE = "CREATE TABLE " + DatingContract.AlbumEntry.TABLE_NAME + " (" +
                DatingContract.AlbumEntry.COLUMN_ID + " INTEGER PRIMARY KEY," +
                DatingContract.AlbumEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                DatingContract.AlbumEntry.COLUMN_PHOTO + " INTEGER NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_PROFILE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CONTACT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ALBUM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        resetDatabase(sqLiteDatabase);
    }

    public void resetDatabase() {
        resetDatabase(getWritableDatabase());
    }

    public void resetDatabase(SQLiteDatabase sqLiteDatabase) {
        clearDatabase(sqLiteDatabase);
        onCreate(sqLiteDatabase);
    }

    private void clearDatabase(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatingContract.ProfileEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatingContract.AlbumEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatingContract.ContactEntry.TABLE_NAME);
    }
}
