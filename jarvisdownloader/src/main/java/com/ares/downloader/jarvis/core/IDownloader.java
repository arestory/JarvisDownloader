package com.ares.downloader.jarvis.core;

/**
 * Created on 2018/4/26 17:16.
 *
 * @author ares
 */

public interface IDownloader {


    /**
     * 构建下载
     */
     void download();

    /**
     * 暂停所有下载线程
     */
     void pause();


    /**
     * 恢复
     */
    void recovery();

    /**
     * 删除本地下载记录,但不删除已下载的文件
     */
     void delete();

    /**
     * 删除本地下载记录和已下载的文件
     *
     * @return
     */
    boolean deleteCacheFile();


}
