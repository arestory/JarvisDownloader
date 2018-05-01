package com.ares.downloaderdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
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
    String url9 = "http://dianlipai.com/download/1517546283735097624dianlipai_v2.0.0_ali_2018-02-02_12_31.apk";
    String url10 = "https://gzcu01.baidupcs.com/file/1cf1fd9a434944c2c733f2c2a224a189?bkt=p3-000045733e811f5e69109fcedda7c24ce1bf&fid=220969561-250528-411052751306431&time=1525008937&sign=FDTAXGERLQBHSKa-DCb740ccc5511e5e8fedcff06b081203-TXI0%2Ff2R7rr8Oqniic6xEckIQVI%3D&to=86&size=8065435&sta_dx=8065435&sta_cs=0&sta_ft=JPG&sta_ct=6&sta_mt=6&fm2=MH%2CYangquan%2CAnywhere%2C%2Cguangdong%2Ccnc&vuk=220969561&iv=0&newver=1&newfm=1&secfm=1&flow_ver=3&pkey=000045733e811f5e69109fcedda7c24ce1bf&sl=76480590&expires=8h&rt=sh&r=211508206&mlogid=2757620471471505982&vbdid=4070687391&fin=IMG_0016.JPG&fn=IMG_0016.JPG&rtype=1&dp-logid=2757620471471505982&dp-callid=0.1.1&hps=1&tsl=80&csl=80&csign=THHv6DG%2FPicTjcfY%2F6GKSQjzL3o%3D&so=0&ut=6&uter=4&serv=0&uc=2361768026&ic=3018533560&ti=42c2e66164287fda923b976fad67d20d2cc040f6418d99e5305a5e1275657320&by=themis";
    String url11 = "http://t2.hddhhn.com/uploads/tu/201804/9999/ff28b2f8bc.jpg";
    String url12 = "http://t2.hddhhn.com/uploads/tu/201804/9999/f7abe3f7b7.jpg";
    String url13 = "http://t2.hddhhn.com/uploads/tu/201804/9999/978d773843.jpg";
    String url14 = "http://t2.hddhhn.com/uploads/tu/201804/9999/09a0713fb9.jpg";
    String url15 = "http://t2.hddhhn.com/uploads/tu/201804/9999/d4e5eeb6fc.jpg";
    String url16 = "http://t2.hddhhn.com/uploads/tu/201804/9999/7d9367fa9e.jpg";
    String url17 = "http://t2.hddhhn.com/uploads/tu/201804/9999/2f32e5e42e.jpg";
    String url18 = "http://t2.hddhhn.com/uploads/tu/201804/9999/1642e3e00c.jpg";
    String url19 = "http://t2.hddhhn.com/uploads/tu/201804/9999/e8e1af091d.jpg";
    String url20 = "http://t2.hddhhn.com/uploads/tu/201804/9999/e840e062f1.jpg";


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
        Debug.stopMethodTracing();
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, DownloadActivity.class);
        context.startActivity(starter);
    }



}
