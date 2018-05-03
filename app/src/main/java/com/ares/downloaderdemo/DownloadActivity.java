package com.ares.downloaderdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ares.downloader.jarvis.Jarvis;
import com.ares.downloader.jarvis.core.DataCallBack;
import com.ares.downloader.jarvis.core.LocalFileRecordBean;

import java.util.List;

/**
 * Created on 2018/4/26 11:31.
 *
 * @author ares
 */

public class DownloadActivity extends AppCompatActivity {


    String url = "http://pic2.zhimg.com/80/v2-4bd879d9876f90c1db0bd98ffdee17f0_hd.jpg";
    String url1 = "http://img1.gamersky.com/image2017/01/20170114_zl_91_6/gamersky_01origin_01_201711416272DB.jpg";
    String url2 = "http://pic1.win4000.com/wallpaper/2017-10-11/59dde2bca944f.jpg";
    String url3 = "http://gdown.baidu.com/data/wisegame/d2fbbc8e64990454/wangyiyunyinle_87.apk";
    String url4 = "http://dianlipai.com/download/1524539667804095709dianlipai_v2.0.1_yingyongbao_2018-04-24_09_41_legu_signed_zipalign.apk";
    String url5 = "http://dianlipai.com/download/1497952239876080593dianlipai_v1.0.0_dianlipai.apk";
    String url6 = "http://dianlipai.com/download/1499075088291045643dianlipai_v1.0.1_dianlipai.apk";
    String url7 = "http://dianlipai.com/download/1504593048593012436dianlipai_v1.2.0_dianlipai_2017-09-04_legu_dianlipai_signed_zipalign.apk";
    String url8 = "http://dianlipai.com/download/1517462120914057094dianlipai_v2.0.0_ali_2018-02-01_11_37.apk";
    String url9 = "http://ares-space.com/img/yunnan/daytwo/dianchi/IMG_2944.mov";



    private RecyclerView rv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);

//        rv = findViewById(R.id.rv);
//
//        rv.setLayoutManager(new LinearLayoutManager(this));
//        List<FileBean> list = new ArrayList<>();
//        MultiDownloadAdapter downloadAdapter = new MultiDownloadAdapter(list);
//        list.add(new FileBean(url1));
//        list.add(new FileBean(url2));
//        list.add(new FileBean(url3));
//        list.add(new FileBean(url4));
//        list.add(new FileBean(url5));
//        list.add(new FileBean(url6));
//        list.add(new FileBean(url7));
//        list.add(new FileBean(url8));
//        list.add(new FileBean(url9));
//        list.add(new FileBean(url10));
//        list.add(new FileBean(url11));
//        list.add(new FileBean(url12));
//        rv.setAdapter(downloadAdapter);


        new DownloadLayouter().add(findViewById(R.id.layout1), url9)

                .add(findViewById(R.id.layout2),  url1)
                .add(findViewById(R.id.layout3), url3).add(findViewById(R.id.layout4), url4)
                .add(findViewById(R.id.layout5), url5).add(findViewById(R.id.layout6), url6)
        ;

        Jarvis.getInstance().getDownloadedList(new DataCallBack<List<LocalFileRecordBean>>() {
            @Override
            public void onData(List<LocalFileRecordBean> data) {

                System.out.println(data.size()+"个记录");
            }
        });

    }



    public void startAll(View v) {


        Jarvis.getInstance().startAllDownload();

    }


    public void pauseAll(View v) {


        Jarvis.getInstance().pauseAllDownloader();

    }


    public void deleteAll(View v) {


        Jarvis.getInstance().forceDeleteAll();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, DownloadActivity.class);
        context.startActivity(starter);
    }



}
