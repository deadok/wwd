package com.wamba.bob.wwd.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Bob on 03.04.2016.
 */
public class Profile implements Parcelable {
    public static final String LOG_TAG = Profile.class.getSimpleName();

    public Profile() {

    }

    public Profile(JSONObject profileJson) {
        try {
            uid = profileJson.getLong("id");
            name = profileJson.getString("name");
            big_photo = profileJson.getString("squarePhotoUrl");
            photo = profileJson.getString("smallPhotoUrl");
            age = profileJson.getInt("age");
            if (profileJson.has("aboutmeBlock")) {
                JSONArray fields = profileJson.getJSONObject("aboutmeBlock").getJSONArray("fields");
                for (int i = 0; i < fields.length(); i++) {
                    JSONObject field = fields.getJSONObject(i);
                    if (field.getString("key").equals("aboutme")) {
                        about = field.getString("value");
                    }
                }
            }

            //interests

            if (profileJson.has("interests")) {
                JSONArray fields = profileJson.getJSONObject("interests").getJSONArray("items");
                interests = "";
                for (int i = 0; i < fields.length(); i++) {
                    JSONObject field = fields.getJSONObject(i);
                    if(i > 0) {
                        interests += ", ";
                    }
                    interests += field.getString("title");
                }
            }
            updated_ts = Math.round(System.currentTimeMillis()/1000);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Invalid profile json object");
        }
    }

    public long uid = -1;

    public String name;

    public int age;

    public String about;

    public String interests;

    public String photo;

    public String big_photo;

    public int albums_count = -1;

    public int contacts_count = -1;

    public long updated_ts;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.uid);
        dest.writeString(this.name);
        dest.writeInt(this.age);
        dest.writeString(this.about);
        dest.writeString(this.interests);
        dest.writeString(this.photo);
        dest.writeString(this.big_photo);
        dest.writeInt(this.albums_count);
        dest.writeInt(this.contacts_count);
        dest.writeLong(this.updated_ts);
    }

    protected Profile(Parcel in) {
        this.uid = in.readLong();
        this.name = in.readString();
        this.age = in.readInt();
        this.about = in.readString();
        this.interests = in.readString();
        this.photo = in.readString();
        this.big_photo = in.readString();
        this.albums_count = in.readInt();
        this.contacts_count = in.readInt();
        this.updated_ts = in.readLong();
    }

    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel source) {
            return new Profile(source);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}