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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DatingContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.wamba.bob.wwd";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_PROFILE = "profile";
    public static final String PATH_CONTACT = "contact";
    public static final String PATH_ALBUM = "album";

    /* Inner class that defines the table contents of the profile table */
    public static final class ProfileEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROFILE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;

        public static final String TABLE_NAME = "profiles";

        public static final String COLUMN_PROFILE_ID = "uid";
        public static final String COLUMN_PROFILE_NAME = "name";
        public static final String COLUMN_PROFILE_AGE = "age";
        public static final String COLUMN_PROFILE_ABOUT = "about";
        public static final String COLUMN_PROFILE_INTERESTS = "interests";
        public static final String COLUMN_PROFILE_PHOTO = "photo";
        public static final String COLUMN_PROFILE_PHOTO_BIG = "big_photo";
        public static final String COLUMN_PROFILE_ALBUMS_COUNT = "albums_count";
        public static final String COLUMN_PROFILE_CONTACTS_COUNT = "contacts_count";
        public static final String COLUMN_PROFILE_UPDATED_TS = "updated_ts";

        public static Uri buildProfileUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Long getProfileIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    /* Inner class that defines the table contents of the contacts table */
    public static final class ContactEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTACT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACT;

        public static final String TABLE_NAME = "contacts";


        public static final String COLUMN_PROFILE_ID = "uid";
        public static final String COLUMN_MESSAGES_COUNT = "messages_count";
        public static final String COLUMN_UPDATED_TS = "updated_ts";

        public static Uri buildContactsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class AlbumEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTACT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALBUM;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALBUM;

        public static final String TABLE_NAME = "albums";


        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHOTO = "photo";

        public static Uri buildAlbumsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
