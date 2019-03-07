package com.kongzue.basebanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/3/6 12:49
 */
public class SimpleBanner<V extends View> extends RelativeLayout {
    
    public static final int GRAVITY_CENTER = 0;                             //居中
    public static final int GRAVITY_LEFT = 1;                               //居左
    public static final int GRAVITY_RIGHT = 2;                              //居右
    private int indicatorGravity = GRAVITY_CENTER;                          //指示器对齐方式
    private int indicatorMargin = 15;                                       //指示器与边框的距离（单位：dp）
    
    private int indicatorFocusResId;
    private int indicatorNormalResId;
    
    private int DELAY = 4000;                                               //自动轮播延时（单位：毫秒）
    private int PERIOD = 4000;                                              //自动轮播周期（单位：毫秒）
    
    private boolean autoPlay = true;                                        //自动轮播开关
    
    private Handler mainHandler = new Handler(Looper.getMainLooper());      //主线程
    
    private Context context;
    private ViewPager viewPager;
    private List<String> imageUrls;
    private LinearLayout indicatorBox;
    private BindData<V> bindData;                                                  //数据绑定器
    
    public void setData(List<String> imageUrls, BindData<V> bindData) {
        this.imageUrls = imageUrls;
        this.bindData = bindData;
        init();
    }
    
    public SimpleBanner(Context context) {
        super(context);
        this.context = context;
        init();
    }
    
    public SimpleBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        loadAttrs(attrs);
        init();
    }
    
    public SimpleBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        loadAttrs(attrs);
        init();
    }
    
    private void loadAttrs(AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Banner);
        indicatorFocusResId = typedArray.getResourceId(R.styleable.Banner_indicatorFocus, R.drawable.rect_white_alpha90);
        indicatorNormalResId = typedArray.getResourceId(R.styleable.Banner_indicatorNormal, R.drawable.rect_white_alpha50);
        indicatorGravity = typedArray.getInt(R.styleable.Banner_indicatorGravity, GRAVITY_CENTER);
        indicatorMargin = typedArray.getDimensionPixelOffset(R.styleable.Banner_indicatorMargin, dip2px(15));
        DELAY = typedArray.getInt(R.styleable.Banner_delay, 4000);
        PERIOD = typedArray.getInt(R.styleable.Banner_period, 4000);
        autoPlay = typedArray.getBoolean(R.styleable.Banner_autoPlay, true);
    }
    
    private void init() {
        removeAllViews();
        
        if (imageUrls != null) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            viewPager = new ViewPager(context);
            viewPager.setLayoutParams(lp);
            viewPager.setOverScrollMode(OVER_SCROLL_NEVER);
            addView(viewPager);
            
            initPages();
            initIndicator();
        }
    }
    
    private List<View> views;
    private BannerPagerAdapter bannerPagerAdapter;
    private int nowPageIndex;
    
    private void initPages() {
        views = new ArrayList<>();
        addItem(imageUrls.get(imageUrls.size() - 1));
        for (String url : imageUrls) {
            addItem(url);
        }
        addItem(imageUrls.get(0));
        bannerPagerAdapter = new BannerPagerAdapter(views);
        viewPager.setAdapter(bannerPagerAdapter);
        
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            
            }
            
            @Override
            public void onPageSelected(int i) {
                nowPageIndex = i;
                setIndicatorFocus(nowPageIndex);
            }
            
            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == 0) {
                    if (nowPageIndex == views.size() - 1) {
                        viewPager.setCurrentItem(1, false);
                    }
                    if (nowPageIndex == 0) {
                        viewPager.setCurrentItem(views.size() - 2, false);
                    }
                }
            }
        });
        
        viewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (timer != null) timer.cancel();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (timer != null) timer.cancel();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    startAutoPlay();
                }
                return false;
            }
        });
        
        viewPager.setCurrentItem(1, false);
        
        startAutoPlay();
    }
    
    private List<ImageView> indicatorImageViews;
    
    private void initIndicator() {
        indicatorBox = new LinearLayout(context);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        
        switch (indicatorGravity) {
            case GRAVITY_CENTER:
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lp.setMargins(0, 0, 0, indicatorMargin);
                break;
            case GRAVITY_LEFT:
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                lp.setMargins(indicatorMargin, 0, 0, indicatorMargin);
                break;
            case GRAVITY_RIGHT:
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                lp.setMargins(0, 0, indicatorMargin, indicatorMargin);
                break;
        }
        indicatorBox.setLayoutParams(lp);
        
        addView(indicatorBox);
        
        indicatorImageViews = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(dip2px(8), dip2px(8));
            itemLp.setMargins(dip2px(10), 0, 0, 0);
            imageView.setLayoutParams(itemLp);
            imageView.setImageResource(indicatorNormalResId);
            indicatorImageViews.add(imageView);
            indicatorBox.addView(imageView);
        }
        setIndicatorFocus(1);
    }
    
    private void setIndicatorFocus(int index) {
        if (indicatorImageViews != null && !indicatorImageViews.isEmpty()) {
            index = index - 1;
            if (index < 0) index = indicatorImageViews.size() - 1;
            if (index >= indicatorImageViews.size()) index = 0;
            for (ImageView imageView : indicatorImageViews) {
                imageView.setImageResource(indicatorNormalResId);
            }
            indicatorImageViews.get(index).setImageResource(indicatorFocusResId);
        }
    }
    
    private void addItem(String url) {
        V item;
        try {
            Constructor con = bindData.getEntityClass().getConstructor(Context.class);
            item = (V) con.newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (item != null) {
            bindData.bind(url, item);
            views.add(item);
        }
    }
    
    public class BannerPagerAdapter extends PagerAdapter {
        private List<View> views;
        
        public BannerPagerAdapter(List<View> views) {
            this.views = views;
        }
        
        @Override
        public int getCount() {
            return views.size();
        }
        
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = views.get(position);
            container.addView(view);
            return view;
        }
        
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }
    }
    
    private Timer timer;
    
    public void startAutoPlay() {
        if (!autoPlay) return;
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int nextPageIndex = nowPageIndex + 1;
                        if (nextPageIndex >= views.size()) {
                            nextPageIndex = 0;
                        }
                        viewPager.setCurrentItem(nextPageIndex);
                    }
                });
            }
        }, DELAY, PERIOD);
    }
    
    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    
    public boolean isAutoPlay() {
        return autoPlay;
    }
    
    public SimpleBanner setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
        return this;
    }
    
    public abstract static class BindData<V> {
        
        public abstract void bind(String url, V imageView);
        
        public Class<V> getEntityClass() {
            Type type = getClass().getGenericSuperclass();
            ParameterizedType pType = (ParameterizedType) type;
            Type[] params = pType.getActualTypeArguments();
            @SuppressWarnings("unchecked")
            Class<V> c = (Class<V>) params[0];
            return c;
        }
    }
    
    public SimpleBanner setIndicatorGravity(int indicatorGravity) {
        this.indicatorGravity = indicatorGravity;
        return this;
    }
    
    public SimpleBanner setIndicatorMargin(int indicatorMarginInPx) {
        this.indicatorMargin = indicatorMargin;
        return this;
    }
}
