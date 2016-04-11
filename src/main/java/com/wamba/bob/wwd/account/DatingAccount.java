package com.wamba.bob.wwd.account;

import android.accounts.Account;
import android.os.Parcel;
public class DatingAccount extends Account {

    public static final String TYPE = "com.wamba.bob.wwd";

    public static final String TOKEN_TYPE = "com.wamba.bob.wwd.type_one";

    public static final String KEY_UESR_ID = "com.wamba.bob.wwd.user_id";

    public DatingAccount(Parcel in) {
        super(in);
    }

    public DatingAccount(String name) {
        super(name, TYPE);
    }

}
