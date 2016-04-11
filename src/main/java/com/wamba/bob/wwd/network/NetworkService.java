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

package com.wamba.bob.wwd.network;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wamba.bob.wwd.data.DatingContract;
import com.wamba.bob.wwd.data.model.Profile;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class NetworkService extends Service {

    private final String LOG_TAG = NetworkService.class.getSimpleName();

    public static final String ACTION_LOAD_MY_PROFILE = "my_profile";
    public static final String ACTION_LOAD_PROFILE = "profile";
    public static final String KEY_FIRST_LONG_PARAMETER = "param_1";
    public static final String KEY_SECOND_LONG_PARAMETER = "param_2";


    private static int STATUS_FINISHED = 0;

    // Sets the threadpool size
    private static final int CORE_POOL_SIZE = 1;
    // Sets the threadpool size
    private static final int MAX_POOL_SIZE = 4;

    // Sets the amount of time an idle thread will wait for a task before terminating
    private static final int KEEP_ALIVE_TIME = 5;

    // A queue of Runnables for the network commands pool
    private BlockingQueue<Runnable> mNetworkWorkersQueue;

    // A managed pool of background network comnmand threads
    private ThreadPoolExecutor mNetworkWorkersThreadPool;

    private static NetworkService sInstance;

    // An object that manages Messages in a Thread
    private Handler mHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mNetworkWorkersQueue = new LinkedBlockingQueue<Runnable>();
        mNetworkWorkersThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS, mNetworkWorkersQueue);

        mHandler = new Handler() {

            /*
             * handleMessage() defines the operations to perform when the
             * Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message inputMessage) {

                if (inputMessage.what == STATUS_FINISHED) {
                    recycleTask((NetworkCommandTask) inputMessage.obj);
                    Log.v(LOG_TAG, "Task recycled");
                }
                super.handleMessage(inputMessage);
            }
        };

        mNetworkWorkersThreadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r,
                                          ThreadPoolExecutor executor) {
                Log.v(LOG_TAG, "DemoTask Rejected : ");
                Log.v(LOG_TAG, "Waiting for a second !!");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.v(LOG_TAG, "Lets add another time : ");
                executor.execute(r);
            }
        });
    }

    @Override
    public void onDestroy() {
        sInstance = null;
        super.onDestroy();
    }

    public static NetworkService getInstance() {
        return sInstance;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.v(LOG_TAG, "On start - service");


        // If we have to do smth
        if (null != intent) {

            // Gets a task from the pool of tasks, returning null if the pool is empty
            NetworkCommandTask networkTask = (NetworkCommandTask) mNetworkWorkersQueue.poll();
            // If the queue was empty, create a new task instead.
            if (null == networkTask) {
                networkTask = new NetworkCommandTask();
            }
            // Initializes the task
            networkTask.initialize(intent, sInstance);
            mNetworkWorkersThreadPool.execute(networkTask);

        }


        return super.onStartCommand(intent, flags, startId);
    }

    public void taskFinished(NetworkCommandTask task) {
        // Gets a Message object, stores the state in it, and sends it to the Handler
        Message completeMessage = mHandler.obtainMessage(STATUS_FINISHED, task);
        completeMessage.sendToTarget();
    }


    void recycleTask(NetworkCommandTask task) {

        task.recycle();
        // Puts the task object back into the queue for re-use.
        mNetworkWorkersQueue.offer(task);

    }

    /**
     * Worker class to handle api calls
     */
    class NetworkCommandTask implements Runnable {
        private Intent mIntent;
        private NetworkService mNetworkService;
        private WambaApiClient mClient;
        private final String LOG_TAG = NetworkCommandTask.class.getSimpleName();

        public NetworkCommandTask() {
            mClient = new WambaApiClient();
        }

        public void initialize(Intent intent, NetworkService networkService) {
            if (null != intent) {
                mIntent = (Intent) intent.clone();
                mNetworkService = networkService;
            }
        }

        /*
         * Defines the code to run for this task.
         */
        @Override
        public void run() {

            if (null == mIntent) {
                Log.v(LOG_TAG, "Task runned again");
                return;
            }
            // Moves the current Thread into the background
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);


            String command = mIntent.getAction();

            switch (command) {
                case ACTION_LOAD_PROFILE: {
                    long uid = mIntent.getLongExtra(KEY_FIRST_LONG_PARAMETER, 0);
                    loadUserAnketa(uid);
                    break;
                }
                case ACTION_LOAD_MY_PROFILE: {
                    loadUserProfile();
                    break;
                }

            }
        }

        public void recycle() {
            mIntent = null;
        }

        private void loadUserProfile() {

            Profile profile = mClient.getProfile();
            if (null != profile) {
                profile.contacts_count = 0;
                profile.albums_count = 0;
            }
            updateProfile(profile);
            NetworkService.getInstance().taskFinished(this);

        }

        private void loadUserAnketa(long uid) {

            Profile profile = mClient.getUser(uid);

            updateProfile(profile);
            NetworkService.getInstance().taskFinished(this);

        }

        public void updateProfiles(ArrayList<Profile> profiles) {
            Vector<ContentValues> cVVector = new Vector<ContentValues>(profiles.size());
            for (int i = 0; i < profiles.size(); i++) {
                Profile profile = profiles.get(i);
                if (null != profile && profile.uid != -1) {
                    cVVector.add(
                            getContentValuesFromProfile(profile)
                    );
                }
            }

            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getApplicationContext().getContentResolver().bulkInsert(DatingContract.ProfileEntry.CONTENT_URI, cvArray);
            }


        }

        public void updateProfile(Profile profile) {
            ArrayList<Profile> profiles = new ArrayList<>(0);
            profiles.add(profile);
            updateProfiles(profiles);
        }

        private ContentValues getContentValuesFromProfile(Profile profile) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(DatingContract.ProfileEntry.COLUMN_PROFILE_ID, profile.uid);
            contentValues.put(DatingContract.ProfileEntry.COLUMN_PROFILE_PHOTO_BIG, profile.big_photo);
            contentValues.put(DatingContract.ProfileEntry.COLUMN_PROFILE_PHOTO, profile.photo);
            contentValues.put(DatingContract.ProfileEntry.COLUMN_PROFILE_AGE, profile.age);
            contentValues.put(DatingContract.ProfileEntry.COLUMN_PROFILE_NAME, profile.name);
            contentValues.put(DatingContract.ProfileEntry.COLUMN_PROFILE_UPDATED_TS, profile.updated_ts);
            if (profile.about != null) {
                contentValues.put(DatingContract.ProfileEntry.COLUMN_PROFILE_ABOUT, profile.about);
            }
            if (profile.interests != null) {
                contentValues.put(DatingContract.ProfileEntry.COLUMN_PROFILE_INTERESTS, profile.interests);
            }
            if (profile.contacts_count != -1) {
                contentValues.put(DatingContract.ProfileEntry.COLUMN_PROFILE_CONTACTS_COUNT, profile.albums_count);
            }
            if (profile.albums_count != -1) {
                contentValues.put(DatingContract.ProfileEntry.COLUMN_PROFILE_ALBUMS_COUNT, profile.albums_count);
            }
            return contentValues;
        }

    }


}