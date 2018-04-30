package com.ares.downloader.jarvis.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ares.downloader.jarvis.core.DataCallBack;
import com.ares.downloader.jarvis.core.LocalFileRecordBean;

import java.util.List;

/**
 * Created on 2018/4/30 21:26.
 *
 * @author ares
 */

public abstract class AbsDownloadHistoryDBHelper extends SQLiteOpenHelper {


    public AbsDownloadHistoryDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    /**
     * 清除该条下载链接的所有相关记录
     * @param url
     */
    public abstract void clearDownloadRecordOfThisUrl(final String url);

    /**
     * 将文件长度保存下来
     * @param url
     * @param fileLength
     */
    public abstract void saveFileLengthOfThisUrl(String url, long fileLength);

    /**
     * 获取对应链接在本地保存的长度记录
     * @param url
     * @return
     */
    public abstract long getFileLengthRecord(String url);

    /**
     * 获取对应链接在本地保存的长度记录
     * @param url 链接
     * @param dataCallBack 回调
     * @return
     */
    public abstract void getFileLengthRecord(final String url, final DataCallBack<Long> dataCallBack);

    /**
     * 获取本地下载历史列表
     * @return
     */
    public abstract List<LocalFileRecordBean> getRecordList();

    /**
     * 获取对应链接已经下载的文件长度
     * @param url
     * @return
     */
    public abstract long getDownloadedFileLength(String url);
    /**
     * 获取对应链接已经下载的文件长度
     * @param url
     * @param dataCallBack 回调
     * @return
     */
    public abstract void getDownloadedFileLength(final String url, final DataCallBack<Long> dataCallBack);

    /**
     * 保存每个线程下载的文件长度
     * @param url
     * @param threadId 线程ID
     * @param fileLength 文件长度
     */
    public abstract void saveDownloadedFileLength(final String url, final int threadId, final long fileLength);

    /**
     * 获取对应线程上一次的断点记录的位置
     * @param url
     * @param threadId
     * @param defaultIndex 默认位置
     * @return
     */
    public abstract long getStartIndexOfDownloadRecord(String url, int threadId, long defaultIndex);
    /**
     * 获取对应线程上一次的断点记录的位置
     * @param url
     * @param threadId
     * @param defaultIndex
     * @param callBack 回调
     * @return
     */
    public abstract void getStartIndexOfDownloadRecord(final String url, final int threadId, final long defaultIndex, final DataCallBack<Long> callBack);




    /**
     * 保存或更新每个线程的下载记录（断点续传）
     * @param url
     * @param threadId
     * @param startIndex 开始位置
     * @param endIndex 结束位置
     */
    public abstract void saveOrUpdateDownloadRecord(String url,int threadId,long startIndex,long endIndex);

    ;


}
