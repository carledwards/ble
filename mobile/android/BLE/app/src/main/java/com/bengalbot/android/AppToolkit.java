package com.bengalbot.android;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by jmendez on 12/20/14.
 */
public enum AppToolkit {

    INSTANCE;

    private Bus mBus;
    private Handler mHandler;

    private  AppToolkit() {
        mBus = new Bus(ThreadEnforcer.ANY);
    }

    public void register(Object registerObject) {
        mBus.register(registerObject);
    }

    public void unregister(Object unregisterObject) {
        mBus.unregister(unregisterObject);
    }


    public void post(final Object event) {

        // if we're on the main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // then we deliver directly
            mBus.post(event);
        } else {

            // post from a background thread to the main thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBus.post(event);
                }
            });
        }

    }
}
