package com.kongzue.basebanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
public class SimpleBanner<V extends View, D> extends RelativeLayout {

    private static final String TAG = "SimpleBanner";
    public static boolean DEBUGMODE;
    
    public static final int GRAVITY_CENTER = 0;                             //居中
    public static final int GRAVITY_LEFT = 1;                               //居左
    public static final int GRAVITY_RIGHT = 2;                              //居右
    private int indicatorGravity = GRAVITY_CENTER;                          //指示器对齐方式
    private int indicatorMargin = 15;                                       //指示器与边框的距离（单位：dp）
    private boolean indicatorVisibility = true;                             //指示器可见性
    
    private int indicatorFocusResId;
    private int indicatorNormalResId;
    private int indicatorWidth = dip2px(8);
    private int indicatorHeight = dip2px(8);
    
    private int DELAY = 4000;                                               //自动轮播延时（单位：毫秒）
    private int PERIOD = 4000;                                              //自动轮播周期（单位：毫秒）
    
    private boolean autoPlay = true;                                        //自动轮播开关
    
    private Handler mainHandler = new Handler(Looper.getMainLooper());      //主线程
    
    private ViewPager viewPager;
    private List dataList;
    private LinearLayout indicatorBox;
    private BindData<V, D> bindData;                                                  //数据绑定器
    private Runnable nextRunnable = new Runnable() {
        @Override
        public void run() {
            int nextPageIndex = nowPageIndex + 1;
            if (nextPageIndex >= views.size()) {
                nextPageIndex = 0;
            }
            viewPager.setCurrentItem(nextPageIndex);
        }
    };
    
    public void setData(List dataList, BindData<V, D> bindData) {
        this.dataList = dataList;
        this.bindData = bindData;
        init();
    }
    
    public SimpleBanner(Context context) {
        super(context);
        init();
    }
    
    public SimpleBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttrs(attrs);
        init();
    }
    
    public SimpleBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttrs(attrs);
        init();
    }
    
    private void loadAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Banner);
        indicatorVisibility = typedArray.getBoolean(R.styleable.Banner_indicatorVisibility, true);
        indicatorFocusResId = typedArray.getResourceId(R.styleable.Banner_indicatorFocus, R.drawable.rect_white_alpha90);
        indicatorNormalResId = typedArray.getResourceId(R.styleable.Banner_indicatorNormal, R.drawable.rect_white_alpha50);
        indicatorWidth = typedArray.getDimensionPixelSize(R.styleable.Banner_indicatorWidth, dip2px(8));
        indicatorHeight = typedArray.getDimensionPixelSize(R.styleable.Banner_indicatorHeight, dip2px(8));
        indicatorGravity = typedArray.getInt(R.styleable.Banner_indicatorGravity, GRAVITY_CENTER);
        indicatorMargin = typedArray.getDimensionPixelOffset(R.styleable.Banner_indicatorMargin, dip2px(15));
        DELAY = typedArray.getInt(R.styleable.Banner_delay, 4000);
        PERIOD = typedArray.getInt(R.styleable.Banner_period, 4000);
        autoPlay = typedArray.getBoolean(R.styleable.Banner_autoPlay, true);
    }
    
    private void init() {
        removeAllViews();
        
        if (dataList != null) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            viewPager = new ViewPager(getContext());
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
        if (dataList.size() > 1) {
            addItem((D) dataList.get(dataList.size() - 1), dataList.size() - 1);
            
            for (int i = 0; i < dataList.size(); i++) {
                D data = (D) dataList.get(i);
                addItem(data, i);
            }
        }
        addItem((D) dataList.get(0), 0);
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
        if (!indicatorVisibility || dataList == null || dataList.size() <= 1) {
            if (indicatorBox != null) removeView(indicatorBox);
            return;
        }
        if (indicatorBox != null) removeView(indicatorBox);
        indicatorBox = new LinearLayout(getContext());
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
        for (int i = 0; i < dataList.size(); i++) {
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(indicatorWidth, indicatorHeight);
            itemLp.setMargins(dip2px(10), 0, 0, 0);
            imageView.setLayoutParams(itemLp);
            imageView.setImageResource(indicatorNormalResId);
            indicatorImageViews.add(imageView);
            indicatorBox.addView(imageView);
        }
        setIndicatorFocus(1);
    }
    
    public LinearLayout getIndicatorBox() {
        return indicatorBox;
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
    
    private void addItem(D data, int index) {
        if (DEBUGMODE){
            Log.i(TAG, "addItem: " + data+" index: "+index);
        }
        V item;
        try {
            Constructor con = bindData.getEntityClass().getConstructor(Context.class);
            item = (V) con.newInstance(getContext());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (item != null) {
            bindData.bind(data, item, index);
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
        if (!autoPlay || dataList == null || dataList.size() <= 1) return;
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(nextRunnable);
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
    
    public abstract static class BindData<V, D> {
        
        public abstract void bind(D data, V imageView, int index);
        
        public Class<V> getEntityClass() {
            Type genType = getClass().getGenericSuperclass();
            if (!(genType instanceof ParameterizedType)) {
                return (Class<V>) Object.class;
            }
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            if (0 >= params.length || 0 < 0) {
                return (Class<V>) Object.class;
            }
            if (!(params[0] instanceof Class)) {
                return (Class<V>) Object.class;
            }
            return (Class) params[0];
        }
    }
    
    public SimpleBanner setIndicatorGravity(int indicatorGravity) {
        this.indicatorGravity = indicatorGravity;
        invalidate();
        return this;
    }
    
    public SimpleBanner setIndicatorMargin(int indicatorMarginInPx) {
        this.indicatorMargin = indicatorMargin;
        invalidate();
        return this;
    }
    
    public boolean isIndicatorVisibility() {
        return indicatorVisibility;
    }
    
    public SimpleBanner<V, D> setIndicatorVisibility(boolean indicatorVisibility) {
        this.indicatorVisibility = indicatorVisibility;
        invalidate();
        return this;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        if (timer != null) timer.cancel();
        if (mainHandler != null && nextRunnable != null) mainHandler.removeCallbacks(nextRunnable);
        super.onDetachedFromWindow();
    }
    
    public ViewPager getViewPager() {
        return viewPager;
    }
    
    public BannerPagerAdapter getBannerPagerAdapter() {
        return bannerPagerAdapter;
    }
}
