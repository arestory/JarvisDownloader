package com.ares.downloader.jarvis.core;

import com.ares.downloader.jarvis.Jarvis;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created on 2018/4/29 16:29.
 *
 * @author ares
 */

public class RemoteFileUtil {



    /**
     * 获取服务端的文件名
     *
     * @param fileUrl
     * @return
     */
    public static String getRemoteFileName(String fileUrl) {


        int index = fileUrl.lastIndexOf("/");
        //找到最后一个?的位置，进行字符串分割
        int endIndex = fileUrl.indexOf("?");
        if (endIndex >= 0) {
            return fileUrl.substring(index, endIndex);

        } else {

            return fileUrl.substring(index, fileUrl.length());

        }

    }


    /**
     * 获取服务器的文件长度以及是否支持断点续传
     *
     * @param fileUrl
     * @return
     */
    public static RemoteFile getRemoteFileLength(String fileUrl) {

       RemoteFile remoteFile = new RemoteFile(false, 0);

        long length = Jarvis.getDownloadRecordDBHelper().getFileLengthRecord(fileUrl);
        System.out.println("RemoteFile length = "+length);
        if (length != 0) {

            remoteFile.setSupportRange(true);
            remoteFile.setLength(length);
            return remoteFile;
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();


            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10 * 1000);
            connection.setRequestProperty("Accept-Ranges", "bytes");
            connection.setRequestProperty("Connection", "Keep-Alive");

            //加上这个头部，则可以防止出现getContentLength()为-1的问题
            connection.setRequestProperty("Accept-Encoding", "identity");
            //必须加这个头部，否则无法返回正常支持断点续传的响应码206
            connection.setRequestProperty("Range", "bytes=0-");
            connection.connect();
            System.out.println("connection code = " + connection.getResponseCode());

            if (connection.getResponseCode() == 200 || connection.getResponseCode() == 206) {

                Jarvis.getDownloadRecordDBHelper().saveFileLengthOfThisUrl(fileUrl, connection.getContentLength());
                remoteFile.setLength(connection.getContentLength());
                //设置该文件是否支持断点续传
                remoteFile.setSupportRange(connection.getResponseCode() == 206);
                return remoteFile;
            } else {

                return remoteFile;
            }
        } catch (IOException e) {

            e.printStackTrace();
            return remoteFile;
        }


    }



}
