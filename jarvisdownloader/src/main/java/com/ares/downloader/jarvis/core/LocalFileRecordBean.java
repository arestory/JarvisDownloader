package com.ares.downloader.jarvis.core;

/**
 * Created on 2018/4/30 21:02.
 * 本地下载文件记录实体
 * @author ares
 */

public class LocalFileRecordBean {


    private String url;//链接
    private long fileTotalLength;//文件总长度
    private long downloadedLength;//已下载的长度

    public LocalFileRecordBean() {
    }

    public LocalFileRecordBean(String url, long fileTotalLength, long downloadedLength) {
        this.url = url;
        this.fileTotalLength = fileTotalLength;
        this.downloadedLength = downloadedLength;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getFileTotalLength() {
        return fileTotalLength;
    }

    public void setFileTotalLength(long fileTotalLength) {
        this.fileTotalLength = fileTotalLength;
    }

    public long getDownloadedLength() {
        return downloadedLength;
    }

    public void setDownloadedLength(long downloadedLength) {
        this.downloadedLength = downloadedLength;
    }

    @Override
    public String toString() {
        return "FileRecordBean{" +
                "url='" + url + '\'' +
                ", fileTotalLength=" + fileTotalLength +
                ", downloadedLength=" + downloadedLength +
                '}';
    }
}
