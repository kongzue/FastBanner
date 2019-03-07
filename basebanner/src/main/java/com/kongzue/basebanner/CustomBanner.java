package com.kongzue.basebanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/3/6 12:50
 */
public class CustomBanner<D> extends RelativeLayout {
    
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
    private List<D> datas;
    private LinearLayout indicatorBox;
    private int customLayoutResId;                                            //用户自定义布局资源id
    private BindView bindView;                                              //布局组件绑定器
    
    //使用以下方法启动CustomBanner
    public void setData(List<D> datas, int layoutId, BindView bindView) {
        this.customLayoutResId = layoutId;
        this.datas = datas;
        this.bindView = bindView;
        init();
    }
    
    public void setData(List<D> datas) {
        this.datas = datas;
        this.bindView = bindView;
        init();
    }
    
    //系统构造方法
    public CustomBanner(Context context) {
        super(context);
        this.context = context;
        init();
    }
    
    public CustomBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        loadAttrs(attrs);
        init();
    }
    
    public CustomBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        loadAttrs(attrs);
        init();
    }
    
    private void init() {
        removeAllViews();
        
        if (datas != null) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            viewPager = new ViewPager(context);
            viewPager.setLayoutParams(lp);
            viewPager.setOverScrollMode(OVER_SCROLL_NEVER);
            addView(viewPager);
            
            initPages();
            initIndicator();
        }
    }
    
    private void loadAttrs(AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Banner);
        indicatorFocusResId = typedArray.getResourceId(R.styleable.Banner_indicatorFocus, R.drawable.rect_white_alpha90);
        indicatorNormalResId = typedArray.getResourceId(R.styleable.Banner_indicatorNormal, R.drawable.rect_white_alpha50);
        indicatorGravity = typedArray.getInt(R.styleable.Banner_indicatorGravity, GRAVITY_CENTER);
        indicatorMargin = typedArray.getInt(R.styleable.Banner_indicatorMargin, 15);
        DELAY = typedArray.getInt(R.styleable.Banner_delay, 4000);
        PERIOD = typedArray.getInt(R.styleable.Banner_period, 4000);
        autoPlay = typedArray.getBoolean(R.styleable.Banner_autoPlay, true);
    }
    
    private List<View> views;
    private BannerPagerAdapter bannerPagerAdapter;
    private int nowPageIndex;
    
    private void initPages() {
        views = new ArrayList<>();
        addItem(datas.get(datas.size() - 1));
        for (D d : datas) {
            addItem(d);
        }
        addItem(datas.get(0));
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
                lp.setMargins(0, 0, 0, dip2px(indicatorMargin));
                break;
            case GRAVITY_LEFT:
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                lp.setMargins(dip2px(indicatorMargin), 0, 0, dip2px(indicatorMargin));
                break;
            case GRAVITY_RIGHT:
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                lp.setMargins(0, 0, dip2px(indicatorMargin), dip2px(indicatorMargin));
                break;
        }
        indicatorBox.setLayoutParams(lp);
        
        addView(indicatorBox);
        
        indicatorImageViews = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
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
    
    private void addItem(D data) {
        View item = LayoutInflater.from(context).inflate(customLayoutResId, null, false);;
        if (item != null) {
            bindView.bind(data, item);
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
    
    public CustomBanner setCustomLayout(int layoutId) {
        this.customLayoutResId = layoutId;
        return this;
    }
    
    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    
    public boolean isAutoPlay() {
        return autoPlay;
    }
    
    public CustomBanner setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
        return this;
    }
    
    public interface BindView<D> {
        void bind(D data, View rootView);
    }
    
    public CustomBanner setIndicatorGravity(int indicatorGravity) {
        this.indicatorGravity = indicatorGravity;
        return this;
    }
    
    public CustomBanner setIndicatorMargin(int indicatorMargin) {
        this.indicatorMargin = indicatorMargin;
        return this;
    }
}
