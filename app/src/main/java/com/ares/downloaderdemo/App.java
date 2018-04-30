package com.ares.downloaderdemo;

import android.app.Application;
import android.content.Context;

import com.ares.downloader.jarvis.Jarvis;
import com.ares.downloader.jarvis.db.DefaultDownloadHistoryDBHelper;
import com.squareup.leakcanary.LeakCanary;

import java.lang.ref.WeakReference;

/**
 * Created on 2018/4/30 20:52.
 *
 * @author ares
 */

public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        Jarvis.init(new DefaultDownloadHistoryDBHelper(new WeakReference<Context>(this)));
        LeakCanary.install(this);



    }
}
