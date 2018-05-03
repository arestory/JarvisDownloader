package com.ares.downloader.jarvis.core;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created on 2018/4/29 17:57.
 * 一个无界面的fragment，用来监听activity的销毁声明周期
 *
 * @author ares
 */

public class InvisibleFragment extends Fragment {


    private LifeCallBack lifeCallBack;

    public static InvisibleFragment newInstance(String url) {

        Bundle args = new Bundle();
        args.putString("url", url);
        InvisibleFragment fragment = new InvisibleFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public void setLifeCallBack(LifeCallBack lifeCallBack) {
        this.lifeCallBack = lifeCallBack;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (lifeCallBack != null) {
            lifeCallBack.onDestroy();
        }
    }

    private volatile boolean hadStop = false;


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();


        if (hadStop) {


            if (lifeCallBack != null) {

                lifeCallBack.onRestart();
            }

        }
        hadStop = false;
    }


    @Override
    public void onStop() {
        super.onStop();

        if (lifeCallBack != null) {
            lifeCallBack.onStop();
        }
        hadStop = true;

    }


    public interface LifeCallBack {


        void onDestroy();

        void onStop();

        void onRestart();


    }


}
