package com.ares.downloaderdemo;

import android.app.Application;

import com.ares.downloader.jarvis.Jarvis;
import com.ares.downloader.jarvis.db.DefaultDownloadHistoryDBHelper;
import com.squareup.leakcanary.LeakCanary;

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

        Jarvis.init(new DefaultDownloadHistoryDBHelper(this));
        LeakCanary.install(this);



    }
}
