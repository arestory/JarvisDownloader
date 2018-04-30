package com.ares.downloader.jarvis.core;

/**
 * Created on 2018/4/29 16:15.
 *
 * @author ares
 */

public class RemoteFile {


    //是否支持断点续传的响应码
   public static final int SUPPORT_RANGE = 206;

    private boolean supportRange = false;//是否支持断点续传
    private long length;

    public RemoteFile(int code, long length) {

        this.supportRange = (code == SUPPORT_RANGE);
        this.length = length;
    }

    public RemoteFile() {
    }

    public RemoteFile(boolean supportRange, long length) {
        this.supportRange = supportRange;
        this.length = length;
    }

    public boolean isSupportRange() {
        return supportRange;
    }

    public void setSupportRange(boolean supportRange) {
        this.supportRange = supportRange;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
