# JarvisDownloader

熟悉漫威电影的人都知道Jarvis，他是钢铁侠的智能管家，帮助钢铁侠制造装甲、分析大量数据、协助建模等各种智能工作，可惜在复联2中，Jarvis与心灵之石结合成Vision，钢铁侠失去了这位如亲人一般的智能AI，后来Tony用F.R.I.D.A.Y代替了Jarvis的工作，但从钢铁侠与Friday的互动来看，他只把Friday当做一般的AI，并没有投入如对待Jarvis的感情。

最近复联3上映，中国没有同步上映，真的太可惜了，五一本来还想过去香港看，但想到到时会人生人海还是算了，所以假期闲余时间就撸了个**JarvisDownloader**，之所以以Jarvis命名，也是因为我太喜欢Jarvis这个AI了，它属于人工智能的顶端啊！**JarvisDownloader**虽然没有Jarvis那么强大，但在下载文件方面，具备了很多的优点。
 

## 主要功能

- 支持断点续传下载
- 支持自定义文件夹路径、文件名。
- 支持自定义SQLiteOpenHelper，用于保存下载进度等
- 下载进度回调时自动切换到UI线程，方便更新UI。
- 自动与activity绑定生命周期，无需手动释放
- activity不可见时，不会更新UI，可见时会自动恢复状态
- 自定义请求头
- 支持查询下载历史列表

## 使用**JarvisDownloader**
### 依赖远程库

gradle


```
repositories {
    
    maven{
        url "https://jitpack.io"
    }
}

dependencies {

	api 'com.github.yuwenque:JarvisDownloader:0.4.0'

}


```



### 初始化数据库


```Java
Jarvis.init(new DefaultDownloadHistoryDBHelper(applicationContext));
```

你也可以自定义一个继承于AbsDownloadHistoryDBHelper的管理类，用于保存下载进度等操作，详细操作请参考**DefaultDownloadHistoryDBHelper**


### Jarvis.Downloader构建下载任务

>请在activity或service中调用


```Java

//构成下载器
Jarvis.Downloader downloader = Jarvis.with(this).withUrl("http://pic1.win4000.com/wallpaper/2017-10-11/59dde2bca944f.jpg");

//是否允许ui不可见时继续下载
downloader.allowBackgroundDownload(true);

//多线程下载数量
downloader.threadCount(3);
//设置下载目录，非必须，默认目录为 Environment.getExternalStorageDirectory()+File.separator+"Jarvis"
downloader.filePath(Environment.getExternalStorageDirectory() + File.separator + "Jarvis");

//设置文件名，非必须,但建议手动设置,默认为服务器文件的名字
downloader.fileName("test.jpg");


//刷新进度的频率（毫秒），最小值为100
downloader.refreshTime(1000);

//设置状态监听
//假如当前context为activity的话Jarvis已经自动帮你回调到主线程了，不需要调用activity.runOnUiThread
downloader.setDownloadListener(new DownloadListener() {

    /**
     * 文件下载完毕回调
     * @param file
     */
    @Override
    public void onSuccess(File file) {
    }

    /**
     * 进度回调
     * @param downloadedSize 当前下载的文件大小
     * @param progress 当前进度 0-1.0
     */
    @Override
    public void onProgress(long downloadedSize, float progress) {


    }

    /**
     * 开始下载时
     */
    @Override
    public void onStart() {

    }

    /**
     * 下载暂停时
     */
    @Override
    public void onPause() {

    }

    /**
     * 下载失败时
     */
    @Override
    public void onFail() {

    }

    /**
     * 被删除时
     * @param b 删除成功标志位
     */
    @Override
    public void onDelete(boolean b) {

    }
});

//增加额外的请求头
downloader.addExtraRequestProperty("test-key","test-value");

//多个请求头时，可添加map
Map<String,String> map = new HashMap<>();
map.put("test1","value1");
map.put("test2","value2");
downloader.addExtraRequestPropertyMap(map);

//开始下载
downloader.download();

//手动暂停
downloader.pause();

//恢复下载
downloader.recovery();

//手动删除本地记录,但不删除文件,文件下载过程中不允许删除
downloader.delete();

//删除下载记录以及文件，文件下载过程中不允许删除
downloader.deleteCacheFile();

//获取当前的下载状态
downloader.getDownloadState();

//同步获取上次下载的进度,由于是查询数据库的操作，所以建议新开线程来获取
downloader.getDownloadedProgress();
//异步获取下载进度
downloader.getDownloadedProgress(new DataCallBack<Float>() {
    @Override
    public void onData(Float progress) {

    }
});


```

上述代码也可以简化成以下形式


```java
Jarvis.with(this)
        .withUrl("http://pic1.win4000.com/wallpaper/2017-10-11/59dde2bca944f.jpg")
        .allowBackgroundDownload(true)
        .threadCount(3)
        .filePath(Environment.getExternalStorageDirectory() + File.separator + "Jarvis")
        .fileName("test.jpg")
        .refreshTime(1000)
        .setDownloadListener(listener).download();
```


### Jarvis管理下载任务

```java

//异步获取下载历史记录，下载进度请自己计算
Jarvis.getInstance().getDownloadedList(new DataCallBack<List<LocalFileRecordBean>>() {
    @Override
    public void onData(List<LocalFileRecordBean> localFileRecordBeans) {

        LocalFileRecordBean fileRecordBean=  localFileRecordBeans.get(0);
        //下载进度
        float progress = fileRecordBean.getDownloadedLength()*1.0f/fileRecordBean.getFileTotalLength();

    }
});

//停止所有下载任务
Jarvis.getInstance().pauseAllDownloader();

//开始所有下载任务
Jarvis.getInstance().startAllDownload();

//删除所有下载任务及文件
Jarvis.getInstance().forceDeleteAll();

//设置下载的线程池长度，如果当前有10个下载任务，
//每个任务开启3个线程进行断点续传下载，那么此时只设置20个的话，将有部分下载任务需等待其他线程执行完任务才能开始
Jarvis.getInstance().initThreadPoolLength(20);

```
