package com.ares.downloader.jarvis.core;

import android.os.Environment;

import com.ares.downloader.jarvis.Jarvis;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2018/4/29 16:34.
 *
 * @author ares
 */

public class DownloadThread extends Thread {

    private static final String DEFAULT_FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "Jarvis";

    private String url;//下载的URL
    private String filePath = DEFAULT_FILE_PATH;//存放的目录
    private String fileName;//保存的名字
    private int threadId;//线程ID
    private long startIndex;//文件的起始位置，用于分片
    private long endIndex;//文件的终止位置
    private ThreadDownloadListener threadDownloadListener;
    //额外的请求头
    private Map<String,String> requestPropertyMap = new HashMap<>();


    private volatile boolean isPause = false;//当前线程的暂停状态

    private volatile DownloadState  downloadState = DownloadState.START;//当前下载状态

    public boolean isPause() {
        return isPause;
    }

    private void setPause(boolean pause) {
        isPause = pause;

        downloadState=DownloadState.PAUSE;

    }



    public DownloadState getDownloadState() {
        return downloadState;
    }

    @Override
    public void interrupt() {

        setPause(true);
        super.interrupt();
    }

    public DownloadThread(String url, int threadId, long startIndex, long endIndex) {
        this.url = url;
        this.threadId = threadId;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }



    public DownloadThread(String url, String filePath, int threadId, long startIndex, long endIndex,Map<String,String> requestPropertyMap, ThreadDownloadListener threadDownloadListener) {
        this(url,filePath, threadId, startIndex, endIndex, threadDownloadListener);
        this.requestPropertyMap = requestPropertyMap;
    }
    public DownloadThread(String url, String filePath, int threadId, long startIndex, long endIndex, ThreadDownloadListener threadDownloadListener) {
        this(url, threadId, startIndex, endIndex, threadDownloadListener);
        this.filePath = filePath;
    }

    public DownloadThread(String url, String filePath, String fileName, int threadId, long startIndex, long endIndex, ThreadDownloadListener threadDownloadListener) {
        this(url, filePath, threadId, startIndex, endIndex, threadDownloadListener);
        this.fileName = fileName;
    }

    public DownloadThread(String url, int threadId, long startIndex, long endIndex, ThreadDownloadListener downloadListener) {
        this(url, threadId, startIndex, endIndex);
        this.threadDownloadListener = downloadListener;
    }


    @Override
    public void run() {
        long total = 0;//记录下载的总量
        HttpURLConnection connection = null;
        int responseCode = 200;
        try {
            URL urlURL = new URL(this.url);
            connection = (HttpURLConnection) urlURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10 * 1000);


            //添加额外头部参数
            if(requestPropertyMap.size()>0){

                for (Map.Entry<String, String> stringStringEntry : requestPropertyMap.entrySet()) {
                    connection.setRequestProperty(stringStringEntry.getKey(),stringStringEntry.getValue());
                }

            }

            connection.setRequestProperty("Connection", "Keep-Alive");

            //加上这个头部，则可以防止getContentLength()为-1
            connection.setRequestProperty("Accept-Encoding", "identity");
            //查询数据库是否存在断点记录
            startIndex = Jarvis.getDownloadRecordDBHelper().getStartIndexOfDownloadRecord(this.url, threadId, startIndex);


            connection.setRequestProperty("Accept-Ranges", "bytes");

            //设置分段下载的头信息  Range:做分段
            connection.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);


            connection.connect();

            responseCode = connection.getResponseCode();



            //不支持断点续传，则从第一个字节的位置读写
            if (responseCode == 200) {
                startIndex = 0;
            }



            if (responseCode == RemoteFile.SUPPORT_RANGE || responseCode == 200) {


                InputStream inputStream = connection.getInputStream();
                File file = new File(filePath);
                if (!file.exists()) {
                    file.mkdirs();
                }

                RandomAccessFile randomAccessFile = new RandomAccessFile(filePath + (fileName == null ? RemoteFileUtil.getRemoteFileName(this.url) : fileName), "rw");

                //设置该分段的文件起点位置
                randomAccessFile.seek(startIndex);


                byte[] buffer = new byte[1024];
                int length = -1;


                while ((length = inputStream.read(buffer)) != -1 && !isPause) {

                    //写入该文件
                    randomAccessFile.write(buffer, 0, length);
                    total += length;
//                    long currentThreadPosition = startIndex + total;

                    if (threadDownloadListener != null) {
                        threadDownloadListener.onDownload(length);
                    }
                }


                randomAccessFile.close();
                inputStream.close();

                if (responseCode == RemoteFile.SUPPORT_RANGE) {
                    //保存当前线程已下载的文件大小
                    Jarvis.getDownloadRecordDBHelper().saveDownloadedFileLength(url, threadId, total);
                    //将当前终止的文件位置插入数据库
//                DownloadHistoryDBHelper.DownloadRecord downloadRecord = new DownloadHistoryDBHelper.DownloadRecord(url, threadId, startIndex + total, endIndex);

                    //将记录保存到数据库
//                Friday.getDownloadRecordDBHelper().updateDownloadRecord(downloadRecord);
                    Jarvis.getDownloadRecordDBHelper().saveOrUpdateDownloadRecord(url, threadId, startIndex + total, endIndex);

                }
                //如果手动暂停，终止时
                if (isPause) {


                    //断开网络连接
                    connection.disconnect();

                    if (threadDownloadListener != null) {
                        threadDownloadListener.onPause();
                    }
//                        break;
                }else{
                    downloadState=DownloadState.FINISH;

                }

                if (threadDownloadListener != null) {

                    threadDownloadListener.onFinish(total);
                }

            } else {
                if (threadDownloadListener != null) {

                    threadDownloadListener.onPause();
                    threadDownloadListener.onFail();
                }
                downloadState =DownloadState.FAIL;

            }


        } catch (Exception e) {
            if (responseCode == RemoteFile.SUPPORT_RANGE) {
                //保存当前线程已下载的文件大小
                Jarvis.getDownloadRecordDBHelper().saveDownloadedFileLength(url, threadId, total);
                //保存当前线程上次下载的位置
//            Friday.getDownloadRecordDBHelper().updateDownloadRecord(new DownloadHistoryDBHelper.DownloadRecord(url, threadId, startIndex, endIndex));
                Jarvis.getDownloadRecordDBHelper().saveOrUpdateDownloadRecord(url, threadId, startIndex + total, endIndex);
            }
            if (threadDownloadListener != null) {

                threadDownloadListener.onPause();
                threadDownloadListener.onFail();

            }

            downloadState =DownloadState.FAIL;
            e.printStackTrace();

        }

    }

    public interface ThreadDownloadListener {

        void onDownload(long currentDownloadSize);

        void onFinish(long total);

        void onPause();

        void onFail();

    }

}
