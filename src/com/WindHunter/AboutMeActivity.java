package com.WindHunter;


import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.WH.xListView.XListView;
import com.WindHunter.tools.CommentList;
import com.WindHunter.tools.MessageList;
import com.WindHunter.tools.WHActivity;
import com.WindHunter.tools.WeiboList;

import java.util.ArrayList;
import java.util.List;


public class AboutMeActivity extends WHActivity {

    private ViewPager   mPager;         //页卡内容
    private ImageView   cursor;         // 动画图片
    private int         offset = 0;     // 动画图片偏移量
    private int         currIndex = 0;  // 当前页卡编号
    private int         bmpW;           // 动画图片宽度


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_me);

        getSupportActionBar().hide();

        InitImageView();
        InitTextView();
        InitViewPager();

    }


    /**
     * 初始化头标
     */
    private void InitTextView() {
        TextView atMe = (TextView) findViewById(R.id.at_me);
        TextView commentToMe = (TextView) findViewById(R.id.comment_to_me);
        TextView commentByMe = (TextView) findViewById(R.id.comment_by_me);

        atMe.setOnClickListener(new MyOnClickListener(0));
        commentToMe.setOnClickListener(new MyOnClickListener(1));
        commentByMe.setOnClickListener(new MyOnClickListener(2));
    }

    /**
     * 头标点击监听
     */
    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    }

    /**
     * 初始化ViewPager
     */
    private void InitViewPager() {
        mPager = (ViewPager) findViewById(R.id.vPager);
        List<View> listViews = new ArrayList<View>();
        LayoutInflater mInflater = getLayoutInflater();
        listViews.add(mInflater.inflate(R.layout.at_me, null));
        listViews.add(mInflater.inflate(R.layout.comment_about_me, null));
        listViews.add(mInflater.inflate(R.layout.message_list, null));
        mPager.setAdapter(new MyPagerAdapter(listViews));
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());

        initAtMe(listViews.get(0));
        initCommentToMe(listViews.get(1));
        initMessageList(listViews.get(2));
    }

    /**
     * ViewPager适配器
     */
    public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position));

            return mListViews.get(position);
        }
    }


    /**
     * 初始化动画
     */
    private void InitImageView() {
        cursor = (ImageView) findViewById(R.id.cursor);
        bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.about_me_tab)
                .getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 3 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        cursor.setImageMatrix(matrix);// 设置动画初始位置
    }

    /**
     * 页卡切换监听
     */
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
        int two = one * 2;// 页卡1 -> 页卡3 偏移量

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                case 0:
                    if (currIndex == 1) {
                        animation = new TranslateAnimation(one, 0, 0, 0);
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, 0, 0, 0);
                    }
                    break;
                case 1:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, one, 0, 0);
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, one, 0, 0);
                    }
                    break;
                case 2:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, two, 0, 0);
                    } else if (currIndex == 1) {
                        animation = new TranslateAnimation(one, two, 0, 0);
                    }
                    break;
            }
            currIndex = arg0;
            assert animation != null;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            cursor.startAnimation(animation);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }


    private void initAtMe(View atMeView){
        XListView atMeListView = (XListView)atMeView.findViewById(R.id.at_me_list);
        WeiboList weiboList = new WeiboList(this, atMeListView);
        weiboList.setCount(10).setType("mentions_feed").run();
    }

    private void initCommentToMe(View commentToMeView){
        XListView commentToMeList = (XListView)commentToMeView.findViewById(R.id.comment_about_me_list);
        CommentList commentList = new CommentList(this, commentToMeList);
        commentList.setCount(10).setType("comments_to_me").run();
    }

    private void initMessageList(View messageListView){
        XListView messageList = (XListView)messageListView.findViewById(R.id.message_list);
        MessageList message = new MessageList(this, messageList);
        message.setCount(10).run();
    }

}
