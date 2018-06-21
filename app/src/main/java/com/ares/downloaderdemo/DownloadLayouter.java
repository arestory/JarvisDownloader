package com.ares.downloaderdemo;

import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.ares.downloader.jarvis.Jarvis;
import com.ares.downloader.jarvis.core.DataCallBack;
import com.ares.downloader.jarvis.core.DownloadListener;
import com.ares.downloader.jarvis.core.DownloadState;

import java.io.File;

/**
 * Created on 2018/4/30 11:26.
 * 由于使用recyclerView,维护progressBar的进度非常麻烦，所以暂时用这个帮助作为示例
 * @author ares
 */

public class DownloadLayouter {


    public DownloadLayouter add(View layout, String url) {


        final ProgressBar progressBar = layout.findViewById(R.id.pb);
        final Button btn = layout.findViewById(R.id.btn);
        final Button deleteBtn = layout.findViewById(R.id.deleteBtn);

        final Switch sw1 = layout.findViewById(R.id.sw1);
        if (progressBar != null && btn != null) {


//
            final Jarvis.Downloader downloader = Jarvis.with(layout.getContext()).withUrl(url).filePath(Environment.getExternalStorageDirectory() + File.separator + "test").threadCount(3).refreshTime(1000).setDownloadListener(new DownloadListener() {

                @Override
                public void onStart() {
                    btn.setText("暂停");
                }

                @Override
                public void onSuccess(File file) {

                    progressBar.setProgress(100);
                    btn.setText("下载完毕");
                    deleteBtn.setEnabled(true);


                }


                @Override
                public void onProgress(long downloadSize, float progress) {

                    int progressInt = (int) (progress * 100);
                    progressBar.setProgress(progressInt);
//                    btn.setText(String.valueOf(progressInt)+"%");
                }

                @Override
                public void onPause() {

                    btn.setText("继续下载");
                    deleteBtn.setEnabled(true);

                }

                @Override
                public void onFail() {
                    btn.setText("下载失败");
                    deleteBtn.setEnabled(true);

                }

                @Override
                public void onDelete(boolean delete) {
                    btn.setText("下载");
                    progressBar.setProgress(0);
                    deleteBtn.setEnabled(true);


                }
            });


            downloader.addExtraRequestProperty("test","test").allowBackgroundDownload(sw1.isChecked());
            sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    downloader.allowBackgroundDownload(isChecked);
                }
            });

            downloader.getDownloadedProgress(new DataCallBack<Float>() {
                @Override
                public void onData(Float progress) {

                    progressBar.setProgress(((int) (progress * 100)));
                    if (progress == 1) {
                        btn.setText("下载完毕");

                    } else if (progress > 0) {

                        btn.setText("继续下载");
                    }
                }
            });

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (downloader.getDownloadState() == DownloadState.PAUSE||downloader.getDownloadState() == DownloadState.FAIL||downloader.getDownloadState() == DownloadState.DELETE) {

                        downloader.download();

                        btn.setText("暂停");
                        deleteBtn.setEnabled(false);

                    } else if (downloader.getDownloadState() == DownloadState.START) {
                        downloader.pause();
                        btn.setText("继续下载");
                        deleteBtn.setEnabled(true);

                    } else if (downloader.getDownloadState() == DownloadState.FINISH) {

                        deleteBtn.setEnabled(true);

                        Toast.makeText(btn.getContext(), "已下载完毕", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    boolean flag = downloader.deleteCacheFile();

                    System.out.println("删除文件？  " + flag);

                }
            });


        }

        return this;
    }


}
