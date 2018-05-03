package com.ares.downloader.jarvis;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.ares.downloader.jarvis.core.DataCallBack;
import com.ares.downloader.jarvis.core.DownloadListener;
import com.ares.downloader.jarvis.core.DownloadState;
import com.ares.downloader.jarvis.core.DownloadThread;
import com.ares.downloader.jarvis.core.DownloadThread.ThreadDownloadListener;
import com.ares.downloader.jarvis.core.IDownloader;
import com.ares.downloader.jarvis.core.InvisibleFragment;
import com.ares.downloader.jarvis.core.LocalFileRecordBean;
import com.ares.downloader.jarvis.core.RemoteFile;
import com.ares.downloader.jarvis.core.RemoteFileUtil;
import com.ares.downloader.jarvis.core.ThreadCallBack;
import com.ares.downloader.jarvis.core.UrlException;
import com.ares.downloader.jarvis.db.AbsDownloadHistoryDBHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2018/4/25 16:58.
 *
 * @author ares
 */

public class Jarvis {


    private static Jarvis mFriday;

    private ExecutorService threadPool;


    private LinkedHashMap<String, Downloader> downloaderList;


    private static AbsDownloadHistoryDBHelper downloadRecordDBHelper;//全局的数据库helper

    //保存url以及其对于的下载线程列表
    private LinkedHashMap<String, List<DownloadThread>> downloadThreadLinkedHashMap;


    private LinkedHashMap<String, List<DownloadThread>> getDownloadThreadLinkedHashMap() {
        return downloadThreadLinkedHashMap;
    }

    /**
     * 建议在Application中初始化
     *
     * @param downloadRecordDBHelper
     */
    public static void init(AbsDownloadHistoryDBHelper downloadRecordDBHelper) {


        Jarvis.downloadRecordDBHelper = downloadRecordDBHelper;
    }


    public static AbsDownloadHistoryDBHelper getDownloadRecordDBHelper() {

        if (downloadRecordDBHelper == null) {

            throw new RuntimeException("请首先初始化一个继承于AbsDownloadHistoryDBHelper的SQLiteOpenHelper,请参考DefaultDownloadHistoryDBHelper");
        }

        return downloadRecordDBHelper;
    }


    private Jarvis() {
        threadPool = Executors.newFixedThreadPool(20);
        downloadThreadLinkedHashMap = new LinkedHashMap<>();
    }

    public static Jarvis getInstance() {

        if (mFriday == null) {
            synchronized (Jarvis.class) {
                if (mFriday == null) {
                    mFriday = new Jarvis();
                }
            }
        }
        return mFriday;
    }


    /**
     * 设置线程池空间
     *
     * @param threadPoolLength 线程池空间
     */
    public void initThreadPoolLength(int threadPoolLength) {

        threadPool = Executors.newFixedThreadPool(threadPoolLength);//线程池

    }

    /**
     * 添加下载器
     *
     * @param downloader
     * @return
     */
    private void addDownloader(Downloader downloader) {


        if (downloaderList == null) {

            downloaderList = new LinkedHashMap<>();
        }


        downloaderList.put(downloader.url, downloader);


    }


    /**
     * 暂停下载
     *
     * @param downloader
     * @return
     */
    public Jarvis pauseDownloader(@NonNull Downloader downloader) {


        if (downloader.getDownloadState() == DownloadState.START) {

            downloader.pause();
        }


        return this;
    }


    /**
     * 全部开始下载
     */
    public void startAllDownload() {

        if (downloaderList != null) {

            for (Map.Entry<String, Downloader> stringDownloaderEntry : downloaderList.entrySet()) {

                Downloader downloader = stringDownloaderEntry.getValue();
                if (downloader.getDownloadState() == DownloadState.PAUSE || downloader.getDownloadState() == DownloadState.FAIL) {
                    stringDownloaderEntry.getValue().download();

                }
            }

        }


    }

    /**
     * 获取下载列表
     *
     * @param callBack
     */
    public void getDownloadedList(DataCallBack<List<LocalFileRecordBean>> callBack) {


        if (callBack != null) {

            callBack.onData(Jarvis.getDownloadRecordDBHelper().getRecordList());
        }
    }

    /**
     * 删除所有下载记录
     */
    public void forceDeleteAll() {

        if (downloaderList != null) {

            for (Map.Entry<String, Downloader> stringDownloaderEntry : downloaderList.entrySet()) {

                stringDownloaderEntry.getValue().deleteCacheFile();
            }

        }


    }

    /**
     * 暂停所有下载
     */
    public void pauseAllDownloader() {

        if (downloaderList != null) {

            for (Map.Entry<String, Downloader> stringDownloaderEntry : downloaderList.entrySet()) {

                stringDownloaderEntry.getValue().pause();

            }
        }

    }


    /**
     * 构建
     *
     * @param url 下载链接
     * @return
     */
    private Downloader withUrl(String url) {


        return new Downloader(url);

    }

    /**
     * 回调到主线程
     *
     * @param context
     * @return
     */
    public static Friday with(Context context) {
        WeakReference<Context> contextWeakReference = new WeakReference<Context>(context);

        return new Friday(contextWeakReference);
    }


    public static class Friday {


        private WeakReference<Context> contextWeakReference;

        public Friday(WeakReference<Context> contextWeakReference) {
            this.contextWeakReference = contextWeakReference;
        }

        public Downloader withUrl(String url) {


            return new Downloader(this.contextWeakReference.get(), url);

        }
    }


    public static class Downloader implements IDownloader {

        private String url;//资源路径
        private String filePath;//存放路径
        private String fileName;//文件名
        private int threadCount = 3;//默认3个线程，最大只能4个
        private int pauseThreadCount = 0;//中断的线程数
        private int failThreadCount = 0;//失败的线程数

        private volatile long debrisSize = 0L;//累计每个线程当前下载长度的总和
        private volatile long totalDownloadedSize = 0L;//每个线程完成后累加的下载长度总和，由于debrisSize有几率不能统计出进度为100%的情况，所以totalDownloadedSize用于显示进度为100%的情况


        private WeakReference<Context> contextWeakReference;//软引用context
        private List<DownloadThread> downloadThreadList;//存放各个下载线程，用于暂停的情况


        private volatile DownloadState downloadState = DownloadState.PAUSE;//下载状态,默认为暂停

        //用于保存远端文件的长度及是否支持断点续传
        private RemoteFile remoteFile;


        //一个不可见的fragment，用于监听页面消失
        private InvisibleFragment fragment;

        private long refreshTime = 200;//刷新频率

        //上一次刷新的时间
        private long lastRefreshTime = 0L;

        private volatile boolean uiVisible = true;//UI是否可见

        private boolean allowBackgroundDownload = true;//UI不可见时，是否继续下载


        private float currentProgress = 0f;//当前进度


        private Map<String,String> requestPropertyMap = new HashMap<>();

        /**
         * 添加额外的请求头
         * @param key
         * @param value
         * @return
         */
        public Downloader addExtraRequestProperty(String key, String value){

            requestPropertyMap.put(key,value);
            return this;
        }

        /**
         * 添加额外的请求头
         * @param map
         * @return
         */
        public Downloader addExtraRequestPropertyMap(Map<String,String> map){


            requestPropertyMap.putAll(map);
            return this;
        }


        /**
         * @param context
         * @param url
         */
        public Downloader(Context context, String url) {

            this(url);
            this.contextWeakReference = new WeakReference<>(context);

            if (this.contextWeakReference.get() != null
                    && this.contextWeakReference.get() instanceof Activity) {

                Activity activity = ((Activity) this.contextWeakReference.get());

                Fragment cacheFragment = activity.getFragmentManager().findFragmentByTag(url);
                if (cacheFragment != null) {
                    fragment = (InvisibleFragment) cacheFragment;
                } else {
                    fragment = InvisibleFragment.newInstance(url);

                }

                final FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();


                transaction.add(fragment, url);
                transaction.commit();

                fragment.setLifeCallBack(new InvisibleFragment.LifeCallBack() {
                    @Override
                    public void onDestroy() {

                        transaction.remove(fragment);
                        pause();
                    }

                    @Override
                    public void onStop() {

                        //如果进行中则将其停止
                        if (downloadState == DownloadState.START) {

//                            pause();
                            uiVisible = false;

                            if (!allowBackgroundDownload) {

                                pause();
                            }

                        }


                    }

                    @Override
                    public void onRestart() {

                        System.out.println("current state = " + downloadState);
                        if (!uiVisible) {

                            recovery();
                            uiVisible = true;
                        }
                    }
                });
            }
            Jarvis.getInstance().addDownloader(this);

        }


        /**
         * 是否允许应用界面不可见时仍然继续下载，context为service可忽略
         *
         * @param allow true 允许， false 界面不可见时暂停
         * @return
         */
        public Downloader allowBackgroundDownload(boolean allow) {

            this.allowBackgroundDownload = allow;
            return this;
        }

        /**
         * 刷新频率
         *
         * @param refreshTime
         * @return
         */
        public Downloader refreshTime(long refreshTime) {

            if (refreshTime <= 100) {

                refreshTime = 100;
            }
            this.refreshTime = refreshTime;
            return this;
        }

        /**
         * 获取下载状态
         *
         * @return
         */
        public DownloadState getDownloadState() {


            return downloadState;
        }

        /**
         * 获取已下载的进度
         *
         * @return
         */
        public float getDownloadedProgress() {


            if (currentProgress > 0) {

                return currentProgress;
            }

            long lastDownloadedFileLength = Jarvis.downloadRecordDBHelper.getDownloadedFileLength(url);

            long fileLength = 0;
            if (remoteFile != null) {
                fileLength = remoteFile.getLength();

            } else {
                fileLength = Jarvis.downloadRecordDBHelper.getFileLengthRecord(url);

            }

            long downloadSize = debrisSize + lastDownloadedFileLength;
            float progress = 0;
            if (fileLength == 0) {
                progress = 0;
            } else {
                progress = downloadSize * 1.0f / fileLength;
            }
            if (progress == 1) {
                downloadState = DownloadState.FINISH;
            }


            return progress;
        }


        public void getDownloadedProgress(final DataCallBack<Float> callback) {


            Jarvis.getInstance().threadPool.execute(new Runnable() {
                @Override
                public void run() {

                    Log.e("getDownloadedProgress", "Thread = " + Thread.currentThread().getName());
                    runThread(new ThreadCallBack() {
                        @Override
                        public void runOnUiThread() {
                            callback.onData(getDownloadedProgress());

                        }

                        @Override
                        public void runOnThread() {
                            callback.onData(getDownloadedProgress());

                        }
                    });
                }
            });


        }


        /**
         * 初始化构造
         *
         * @param url 请求的url
         */
        public Downloader(String url) {
            this.url = url;
            if (TextUtils.isEmpty(url)) {


                throw new UrlException("url不能为空");

            }

            if (!(url.startsWith("https://") || url.startsWith("http://"))) {

                throw new UrlException("url不合法");
            }

        }

        /**
         * 设置文件下载的目录
         *
         * @param filePath 文件下载的目录
         * @return
         */
        public Downloader filePath(String filePath) {
            if (!filePath.endsWith("/")) {
                filePath = filePath + File.separator;
            }
            this.filePath = filePath;

            return this;
        }

        /**
         * 设置文件名，非必须
         *
         * @param fileName 文件名
         * @return
         */
        public Downloader fileName(String fileName) {

            this.fileName = fileName;
            return this;
        }

        /**
         * 设置下载线程数
         *
         * @param threadCount 线程数 最大
         * @return
         */
        public Downloader threadCount(int threadCount) {

            //设置有效值
            if (threadCount <= 0) {

                throw new ZeroThreadException();
            }

            if (threadCount >= 5) {
                threadCount = 5;
            }
            this.threadCount = threadCount;
            return this;
        }

        /**
         * 获取分段下载的线程数
         *
         * @return
         */
        public int getThreadCount() {

            return this.threadCount;
        }

        private class ZeroThreadException extends RuntimeException {

            public ZeroThreadException() {
                super("线程数量不能设置为0");
            }
        }

        private DownloadListener downloadListener;

        /**
         * 下载监听
         *
         * @param downloadListener
         * @return
         */
        public Downloader setDownloadListener(DownloadListener downloadListener) {

            this.downloadListener = downloadListener;

            return this;
        }


        private boolean isDownloadListenerExist() {


            return this.downloadListener != null;
        }


        @Override
        public void pause() {

            List<DownloadThread> downloadThreadList = Jarvis.getInstance().getDownloadThreadLinkedHashMap().get(url);

            if (downloadThreadList != null && downloadThreadList.size() > 0) {

                for (int i = 0; i < downloadThreadList.size(); i++) {

                    DownloadThread downloadThread = downloadThreadList.get(i);
                    //停止写入数据
                    //中断该线程
                    downloadThread.interrupt();
                }

                Jarvis.getInstance().getDownloadThreadLinkedHashMap().remove(url);
            }


        }


        @Override
        public void delete() {

            pause();

            downloadRecordDBHelper.clearDownloadRecordOfThisUrl(url);


        }


        @Override
        public boolean deleteCacheFile() {


            //不允许在下载过程中删除
            if (downloadState != DownloadState.START) {

                downloadRecordDBHelper.clearDownloadRecordOfThisUrl(url);
                File localFile = new File(filePath + (fileName == null ? RemoteFileUtil.getRemoteFileName(url) : fileName));
                final boolean delete = localFile.delete();


                System.out.println("deleteCacheFile onPauseDone ");
                if (isDownloadListenerExist()) {
                    runThread(new ThreadCallBack() {
                        @Override
                        public void runOnUiThread() {

                            downloadListener.onDelete(delete);


                        }

                        @Override
                        public void runOnThread() {
                            downloadListener.onDelete(delete);

                        }
                    });

                }
                downloadState = DownloadState.PAUSE;

                return delete;

            } else {
                downloadState = DownloadState.PAUSE;


                return false;
            }


        }


        @Override
        public void recovery() {


            download();

        }


        private void resetRefreshTime() {

            lastRefreshTime = 0L;
        }

        @Override
        public void download() {


            if (downloadState == DownloadState.START) {
                Log.e("download", "--------正在下载中--------");

                return;
            }

            Log.e("download", "--------开始下载--------");


            downloadState = DownloadState.START;


            if (isDownloadListenerExist()) {
                runThread(new ThreadCallBack() {
                    @Override
                    public void runOnUiThread() {
                        downloadListener.onStart();

                    }

                    @Override
                    public void runOnThread() {
                        downloadListener.onStart();

                    }
                });
            }

            debrisSize = 0;
            final long lastDownloadedFileLength = Jarvis.downloadRecordDBHelper.getDownloadedFileLength(url);
//            threadPool = Executors.newFixedThreadPool(initThreadPoolLength);

            Jarvis.getInstance().threadPool.execute(new Runnable() {
                @Override
                public void run() {


                    downloadThreadList = new ArrayList<>();


                    //避免重复获取
                    if (remoteFile == null || remoteFile.getLength() <= 0) {

                        remoteFile = RemoteFileUtil.getRemoteFileLength(url);
                    }
                    final long fileLength = remoteFile.getLength();
                    System.out.println("文件长度为fileLength = " + fileLength);

                    if (fileLength <= 0) {


                            downloadState = DownloadState.FAIL;

                            if (isDownloadListenerExist()) {

                                runThread(new ThreadCallBack() {
                                    @Override
                                    public void runOnUiThread() {
                                        downloadListener.onFail();
                                    }

                                    @Override
                                    public void runOnThread() {
                                        downloadListener.onFail();

                                    }
                                });

                            }


//                            downloadListener.onPause();

                            resetRefreshTime();

                        return;
                    }


                    final File localFile = new File(filePath + (fileName == null ? RemoteFileUtil.getRemoteFileName(url) : fileName));
                    boolean exist = localFile.exists();
                    System.out.println("local file = " + localFile.getPath() + ", exist = " + exist);
                    if (exist) {

                        //数据库记录的已下载的文件总大小
                        long cacheFileLength = downloadRecordDBHelper.getDownloadedFileLength(url);
                        if (fileLength == cacheFileLength) {

                            System.out.println("本地已存在 file =  " + localFile.getPath());


                            downloadState = DownloadState.FINISH;
                            if (isDownloadListenerExist()) {


                                runThread(new ThreadCallBack() {
                                    @Override
                                    public void runOnUiThread() {

                                        if (uiVisible) {
                                            downloadListener.onSuccess(localFile);
                                            downloadListener.onProgress(fileLength, 1.0f);
                                        }

                                    }

                                    @Override
                                    public void runOnThread() {
                                        downloadListener.onSuccess(localFile);
                                        downloadListener.onProgress(fileLength, 1.0f);
                                    }
                                });


                            }
                            return;
                        }

                    }


                    //分段大小
                    final long div = fileLength / threadCount;
                    long lastIndex = 0;

                    ThreadDownloadListener childDownloadListener = new ThreadDownloadListener() {
                        @Override
                        public void onDownload(long currentDownloadSize) {


                            synchronized (this) {

                                debrisSize += currentDownloadSize;

                                //已下载的长度（加上上次下载的长度）
                                final long downloadSize = debrisSize + lastDownloadedFileLength;
                                final float progress = downloadSize * 1.0f / fileLength;
//                                Log.v("progress", "progress=" + progress + ",current size = " + downloadSize + ",fileLength=" + fileLength);

                                currentProgress = progress;

                                if (isDownloadListenerExist() && downloadState == DownloadState.START) {


                                    runThread(new ThreadCallBack() {
                                        @Override
                                        public void runOnUiThread() {
                                            //回调给UI线程
                                            //控制刷新频率
                                            if (System.currentTimeMillis() - lastRefreshTime > refreshTime) {

                                                if (uiVisible) {

                                                    downloadListener.onProgress(downloadSize, progress);
                                                }

                                                lastRefreshTime = System.currentTimeMillis();
                                            }

                                            if (progress == 1) {
                                                File file = new File(filePath + RemoteFileUtil.getRemoteFileName(url));

                                                if (file.exists()) {
                                                    downloadListener.onSuccess(file);
                                                    downloadState = DownloadState.FINISH;

                                                }
                                                resetRefreshTime();


                                            }
                                        }

                                        @Override
                                        public void runOnThread() {

                                            downloadListener.onProgress(downloadSize, progress);

                                            if (progress == 1) {
                                                File file = new File(filePath + RemoteFileUtil.getRemoteFileName(url));

                                                if (file.exists()) {
                                                    downloadListener.onSuccess(file);
                                                }
                                                downloadState = DownloadState.FINISH;


                                            }
                                        }
                                    });


                                }

                            }
                        }

                        @Override
                        public void onFinish(long total) {


                            synchronized (this) {
                                totalDownloadedSize += total;
                                //曾经中断的话，计算新的进度需要加上上一次已下载的文件长度
                                if (fileLength == (totalDownloadedSize + lastDownloadedFileLength)) {

                                    currentProgress = 1.0f;

                                    Jarvis.getInstance().getDownloadThreadLinkedHashMap().remove(url);

//                                    Downloader.downloadRecordDBHelper.clearDownloadRecordOfThisUrl(url);
                                    if (isDownloadListenerExist()) {

                                        runThread(new ThreadCallBack() {
                                            @Override
                                            public void runOnUiThread() {


                                                if (getDownloadState() != DownloadState.FINISH) {
                                                    if (uiVisible) {

                                                        downloadListener.onProgress(fileLength, 1.0f);

                                                        File file = new File(filePath + RemoteFileUtil.getRemoteFileName(url));
                                                        if (file.exists()) {

                                                            downloadListener.onSuccess(file);

                                                        }
                                                    }


                                                }
                                            }

                                            @Override
                                            public void runOnThread() {

                                                if (getDownloadState() != DownloadState.FINISH) {
                                                    downloadListener.onProgress(fileLength, 1.0f);

                                                    File file = new File(filePath + RemoteFileUtil.getRemoteFileName(url));
                                                    if (file.exists()) {

                                                        downloadListener.onSuccess(file);

                                                    }


                                                }
                                            }
                                        });
                                        resetRefreshTime();

                                        downloadState = DownloadState.FINISH;

                                    }
                                }
                            }
                        }


                        @Override
                        public void onFail() {


                            synchronized (this) {

                                if (downloadState!=DownloadState.FAIL) {




                                        if (isDownloadListenerExist()) {

//                                        downloadListener.onPause();
                                            runThread(new ThreadCallBack() {
                                                @Override
                                                public void runOnUiThread() {
                                                    downloadListener.onFail();

                                                }

                                                @Override
                                                public void runOnThread() {
                                                    downloadListener.onFail();

                                                }
                                            });
                                            totalDownloadedSize = 0;
                                            downloadState = DownloadState.FAIL;




                                    }
                                }


//                                failThreadCount++;
//                                downloadState = DownloadState.FAIL;
//                                if (failThreadCount == threadCount) {
//
//
//                                    if (isDownloadListenerExist()) {
//
////                                        downloadListener.onPause();
//
//                                        runThread(new ThreadCallBack() {
//                                            @Override
//                                            public void runOnUiThread() {
//                                                downloadListener.onFail();
//
//                                            }
//
//                                            @Override
//                                            public void runOnThread() {
//                                                downloadListener.onFail();
//
//                                            }
//                                        });
//
//                                    }
//
//
//                                    failThreadCount = 0;
//
//                                    totalDownloadedSize = 0;
//
//                                }

                            }

                        }

                        @Override
                        public void onPause() {

//                          List<DownloadThread> list =   Jarvis.getInstance().getDownloadThreadLinkedHashMap().get(url);


                            synchronized (this) {


                                boolean allThreadPause = true;
                                if (downloadThreadList != null && downloadThreadList.size() > 0) {
                                    for (DownloadThread downloadThread : downloadThreadList) {

                                        //如果还有线程不是暂停状态
                                        if (downloadThread.getDownloadState() != DownloadState.PAUSE) {

                                            allThreadPause = false;

                                            break;

                                        }
                                    }
                                }

                                if (allThreadPause && (downloadState != DownloadState.PAUSE)) {

                                    Log.e("all thread ", "onPause ? " + allThreadPause);

                                    if (isDownloadListenerExist()) {


                                        runThread(new ThreadCallBack() {
                                            @Override
                                            public void runOnUiThread() {
                                                if (uiVisible) {

                                                    downloadListener.onPause();
                                                }

                                            }

                                            @Override
                                            public void runOnThread() {
                                                downloadListener.onPause();

                                            }
                                        });


                                    }
                                    downloadState = DownloadState.PAUSE;

                                    totalDownloadedSize = 0;

                                }

//                                pauseThreadCount++;
//                                //如果当前暂停的线程数等于所有线程数，则回调停
//                                if (pauseThreadCount == threadCount) {
//
//                                    if (isDownloadListenerExist()) {
//
//
//                                        runThread(new ThreadCallBack() {
//                                            @Override
//                                            public void runOnUiThread() {
//                                                if (uiVisible) {
//
//                                                    downloadListener.onPause();
//                                                }
//
//                                            }
//
//                                            @Override
//                                            public void runOnThread() {
//                                                downloadListener.onPause();
//
//                                            }
//                                        });
//                                        downloadState = DownloadState.PAUSE;
//
//
//                                    }
//                                    pauseThreadCount = 0;
//                                    Log.e("all thread ", "onPause");
//                                    totalDownloadedSize = 0;
//
//                                }

//                                totalDownloadedSize = 0;
//                                downloadState = DownloadState.PAUSE;

                            }
                        }


                    };

                    //判断是否支持断点续传,如果不支持，则只开启一个线程
                    if (!remoteFile.isSupportRange()) {

                        threadCount = 1;
                    }
                    //创建下载线程
                    for (int i = 0; i < threadCount; i++) {


                        DownloadThread downloadThread = null;
                        if (i != threadCount - 1) {

                            downloadThread = new DownloadThread(url, filePath, i, lastIndex, div * (i + 1),requestPropertyMap, childDownloadListener);


                        } else {
                            downloadThread = new DownloadThread(url, filePath, i, lastIndex, fileLength - 1,requestPropertyMap, childDownloadListener);
                        }

                        //添加到线程池中
                        Jarvis.getInstance().threadPool.execute(downloadThread);


                        //保存到列表进行维护
                        downloadThreadList.add(downloadThread);

                        //下一段的开头
                        lastIndex = div * (i + 1) + 1;

                    }

                    //保存到map，绑定url和线程列表
                    Jarvis.getInstance().getDownloadThreadLinkedHashMap().put(url, downloadThreadList);


                }
            });


        }

        private void runThread(final ThreadCallBack threadCallBack) {


            if (contextWeakReference != null && contextWeakReference.get() != null && (contextWeakReference.get() instanceof Activity)) {

                Activity activity = (Activity) contextWeakReference.get();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        threadCallBack.runOnUiThread();


                    }
                });


            } else {

                threadCallBack.runOnThread();
            }


        }


    }


}
