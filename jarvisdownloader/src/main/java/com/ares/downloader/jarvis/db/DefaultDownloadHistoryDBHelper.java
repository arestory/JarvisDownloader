package com.ares.downloader.jarvis.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ares.downloader.jarvis.core.DataCallBack;
import com.ares.downloader.jarvis.core.LocalFileRecordBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2018/4/26 10:17.
 *
 * @author ares
 */

public class DefaultDownloadHistoryDBHelper extends AbsDownloadHistoryDBHelper {


    private static final String DB_NAME = "download_record.db";
    private static final int VERSION = 1;
    private static final String TABLE_RECORD_NAME = "record";
    private static final String TABLE_RECORD_FILE = "record_file_length";
    private static final String TABLE_ORIGIN_FILE = "file_origin_length";
    private static final String CREATE_TABLE_SQL = "create table  if not exists " + TABLE_RECORD_NAME + " ( id integer ,url TEXT,start_index LONG,end_index LONG)";
    private static final String CREATE_TABLE_PROGRESS_SQL = "create table  if not exists " + TABLE_RECORD_FILE + " (url TEXT,thread_id integer,downloaded_file_length LONG)";
    private static final String CREATE_TABLE_ORIGIN_SQL = "create table  if not exists " + TABLE_ORIGIN_FILE + " (url TEXT,file_length LONG)";

    private ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public DefaultDownloadHistoryDBHelper(WeakReference<Context> context) {
        super(context.get(), DB_NAME, null, VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_SQL);
        db.execSQL(CREATE_TABLE_PROGRESS_SQL);
        db.execSQL(CREATE_TABLE_ORIGIN_SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }

    public void clearDownloadRecordOfThisUrl(final String url) {


        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                getWritableDatabase().delete(TABLE_RECORD_FILE, "url = ?", new String[]{url});
                getWritableDatabase().delete(TABLE_RECORD_NAME, "url = ?", new String[]{url});
                getWritableDatabase().delete(TABLE_ORIGIN_FILE, "url = ?", new String[]{url});


            }
        });


    }

    public void saveFileLengthOfThisUrl(String url, long fileLength) {


        System.out.println("saveFileLengthOfThisUrl fileLength = " + fileLength);

        getWritableDatabase().delete(TABLE_ORIGIN_FILE, "url = ?", new String[]{url});

        ContentValues values = new ContentValues();
        values.put("url", url);
        values.put("file_length", fileLength);
        getWritableDatabase().insert(TABLE_ORIGIN_FILE, null, values);
    }

    public long getFileLengthRecord(String url) {

        long defaultLength = 0L;
        Cursor cursor = getReadableDatabase().query(TABLE_ORIGIN_FILE, new String[]{"file_length"}, "url = ?", new String[]{url}, null, null
                , null);

        System.err.println("lastDownloadRecord size =" + cursor.getCount());
        while (cursor.moveToNext()) {

            defaultLength = cursor.getLong(cursor.getColumnIndex("file_length"));
        }

        System.out.println("getFileLengthRecord fileLength = " + defaultLength);

        cursor.close();
        return defaultLength;

    }


    public void getFileLengthRecord(final String url, final DataCallBack<Long> dataCallBack){


        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                dataCallBack.onData(getFileLengthRecord(url));

            }
        });

    }


    public List<LocalFileRecordBean> getRecordList(){

        List<LocalFileRecordBean> list = new ArrayList<>();


        Cursor cursor = getReadableDatabase().query(TABLE_ORIGIN_FILE, new String[]{"url,file_length"}, null, null, null, null
                , null);

        while (cursor.moveToNext()){


            String url = cursor.getString(cursor.getColumnIndex("url"));
            long file_length = cursor.getLong(cursor.getColumnIndex("file_length"));

            long downloadedLength = getDownloadedFileLength(url);


            LocalFileRecordBean fileRecordBean  = new LocalFileRecordBean(url,file_length,downloadedLength);

            list.add(fileRecordBean);

            Log.e("record",fileRecordBean.toString());
        }


        cursor.close();


        return list;
    }





    public long  getDownloadedFileLength(String url) {
        Cursor cursor = getReadableDatabase().query(TABLE_RECORD_FILE, null, "url = ?", new String[]{url}, null, null, null);

        long downloadedFileLength = 0;


        while (cursor.moveToNext()) {


            downloadedFileLength += cursor.getLong(cursor.getColumnIndex("downloaded_file_length"));


        }
        cursor.close();
        Log.e("tag", "已下载文件的大小 = " + downloadedFileLength);
        return downloadedFileLength;
    }

    public void getDownloadedFileLength(final String url, final DataCallBack<Long> dataCallBack){



        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                dataCallBack.onData(getDownloadedFileLength(url));

            }
        });
    }


    public void saveDownloadedFileLength(final String url, final int threadId, final long fileLength) {


        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put("url", url);
                values.put("thread_id", threadId);
                values.put("downloaded_file_length", fileLength);
                Log.e("tag", "threadId = " + threadId + ",保存已下载文件大小 = " + fileLength);

                long count = getWritableDatabase().insert(TABLE_RECORD_FILE, null, values);

                System.out.println("线程" + threadId + "插入下载的记录数 = " + count);
            }
        });


    }


    public long getStartIndexOfDownloadRecord(String url, int threadId, long defaultIndex) {


        Cursor cursor = getReadableDatabase().query(TABLE_RECORD_NAME, null, "id = ? and url = ?", new String[]{String.valueOf(threadId), url}, null, null, null);


        while (cursor.moveToNext()) {

            defaultIndex = cursor.getLong(cursor.getColumnIndex(DownloadRecord.START_INDEX_COLUMN));

            Log.e("defaultIndex", "存在断点记录，上次的位置defaultIndex=" + defaultIndex);
        }

        cursor.close();


        return defaultIndex;
    }


    public void  getStartIndexOfDownloadRecord(final String url, final int threadId, final long defaultIndex, final DataCallBack<Long> callBack){

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                callBack.onData(getStartIndexOfDownloadRecord(url,threadId,defaultIndex));

            }
        });


    }



    public synchronized void updateDownloadRecord(DownloadRecord downloadRecord) {


        Cursor cursor = getReadableDatabase().query(TABLE_RECORD_NAME, null, "id = ? and url = ?", new String[]{String.valueOf(downloadRecord.id), downloadRecord.url}, null, null, null);


        if (cursor != null && cursor.getCount() > 0) {

            ContentValues values = new ContentValues();
            values.put(DownloadRecord.START_INDEX_COLUMN, downloadRecord.startIndex);

            //do updateDownloadRecord
            getWritableDatabase().update(TABLE_RECORD_NAME, values, "id = ? and url = ?", new String[]{downloadRecord.id + "", downloadRecord.url});


        } else {

            ContentValues values = new ContentValues();
            values.put(DownloadRecord.ID_COLUMN, downloadRecord.id);
            values.put(DownloadRecord.URL_COLUMN, downloadRecord.url);
            values.put(DownloadRecord.START_INDEX_COLUMN, downloadRecord.startIndex);
            values.put(DownloadRecord.END_INDEX_COLUMN, downloadRecord.endIndex);

            getWritableDatabase().insert(TABLE_RECORD_NAME, null, values);

        }

        cursor.close();
    }

    @Override
    public void saveOrUpdateDownloadRecord(String url, int threadId, long startIndex, long endIndex) {

        DownloadRecord downloadRecord = new DownloadRecord(url,threadId,startIndex,endIndex);

        updateDownloadRecord(downloadRecord);
    }

    public static class DownloadRecord {


        public static final String URL_COLUMN = "url";
        public static final String ID_COLUMN = "id";
        public static final String START_INDEX_COLUMN = "start_index";
        public static final String END_INDEX_COLUMN = "end_index";


        private String url;
        private int id;
        private long startIndex;
        private long endIndex;

        public DownloadRecord() {
        }

        public DownloadRecord(String url, int id, long startIndex, long endIndex) {
            this.url = url;
            this.id = id;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public long getStartIndex() {
            return startIndex;
        }

        public void setStartIndex(long startIndex) {
            this.startIndex = startIndex;
        }

        public long getEndIndex() {
            return endIndex;
        }

        public void setEndIndex(long endIndex) {
            this.endIndex = endIndex;
        }
    }


}
