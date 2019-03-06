package com.kongzue.bannerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.kongzue.basebanner.SimpleBanner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private SimpleBanner<SimpleDraweeView> simpleBanner;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        simpleBanner = findViewById(R.id.simpleBanner);
    
        ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(this).setDownsampleEnabled(true).build();
        Fresco.initialize(this, imagePipelineConfig);
    
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("http://kongzue.com/test/fs/1.jpg");
        imageUrls.add("http://kongzue.com/test/fs/2.jpg");
        imageUrls.add("http://kongzue.com/test/fs/3.jpg");
        simpleBanner.setData(imageUrls, new SimpleBanner.BindData<SimpleDraweeView>(){
            @Override
            public void bind(String url, SimpleDraweeView imageView) {
                imageView.setImageURI(url);
            }
        });
        
    }
}
