package com.ares.downloader.jarvis.core;

import java.io.File;

/**
 * Created on 2018/4/29 16:41.
 *
 * @author ares
 */

public interface DownloadListener {

    void onSuccess(File file);

    void onProgress(long downloadSize, float progress);

    void onStart();
    void onPause();

    void onFail();


    void onDelete(boolean delete);


}
