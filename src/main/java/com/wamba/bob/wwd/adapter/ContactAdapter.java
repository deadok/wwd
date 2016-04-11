package com.wamba.bob.wwd.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wamba.bob.wwd.R;

public class ContactAdapter extends CursorAdapter {

    /**
     * Cache of the children views
     */
    public static class ViewHolder {
            public final ImageView profileImageView;
            public final TextView aboutTextView;
            public final TextView interestsView;

        public ViewHolder(View view) {
            profileImageView = (ImageView) view.findViewById(R.id.profile_image );
            aboutTextView = (TextView) view.findViewById(R.id.about_text );
            interestsView = (TextView) view.findViewById(R.id.interests_text);
        }
    }

    public ContactAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        
        int layoutId = -1;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();


    }

}