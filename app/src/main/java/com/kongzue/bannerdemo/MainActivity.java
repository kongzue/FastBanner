package com.kongzue.bannerdemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.kongzue.basebanner.CustomBanner;
import com.kongzue.basebanner.SimpleBanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    
    private SimpleBanner<SimpleDraweeView,String> simpleBanner;
    private CustomBanner customBanner;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SimpleBanner.DEBUGMODE=true;
    
        simpleBanner = findViewById(R.id.simpleBanner);
        customBanner = findViewById(R.id.customBanner);
    
        ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(this).setDownsampleEnabled(true).build();
        Fresco.initialize(this, imagePipelineConfig);
    
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("http://kongzue.com/test/fs/1.jpg");
        imageUrls.add("http://kongzue.com/test/fs/2.jpg");
        imageUrls.add("http://kongzue.com/test/fs/3.jpg");
        simpleBanner.setData(imageUrls, new SimpleBanner.BindData<SimpleDraweeView,String>(){
            @Override
            public void bind(String url, SimpleDraweeView imageView, int index) {
                imageView.setImageURI(url);
            }
        });
    
        List<Map<String,Object>> bannerData = new ArrayList<>();
        Map<String,Object> data = new HashMap<>();
        data.put("title","场馆标题A");
        data.put("tip","北京市朝阳区某某东大街135号");
        data.put("tip2","距离500M 1680人光顾");
        data.put("img","http://kongzue.com/test/fs/1.jpg");
        bannerData.add(data);
        data = new HashMap<>();
        data.put("title","场馆标题B");
        data.put("tip","北京市朝阳区某某东大街135号");
        data.put("tip2","距离500M 1680人光顾");
        data.put("img","http://kongzue.com/test/fs/2.jpg");
        bannerData.add(data);
        data = new HashMap<>();
        data.put("title","场馆标题C");
        data.put("tip","北京市朝阳区某某东大街135号");
        data.put("tip2","距离500M 1680人光顾");
        data.put("img","http://kongzue.com/test/fs/3.jpg");
        bannerData.add(data);
        customBanner.setData(bannerData, R.layout.item_banner, new CustomBanner.BindView<Map<String,String>>() {
            @Override
            public void bind(Map<String, String> data, View rootView, int index) {
                SimpleDraweeView imgBkg = rootView.findViewById(R.id.img_bkg);
                TextView txtTitle = rootView.findViewById(R.id.txt_title);
                TextView txtAddress = rootView.findViewById(R.id.txt_address);
                TextView txtInfo = rootView.findViewById(R.id.txt_info);
    
                imgBkg.setImageURI(data.get("img")+"");
                txtTitle.setText(data.get("title")+"");
                txtAddress.setText(data.get("tip")+"");
                txtInfo.setText(data.get("tip2")+"");
            }
        });
        
    }
}
