package com.WindHunter;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import com.lidroid.xutils.BitmapUtils;

import java.util.ArrayList;

public class BigImageActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.big_image);

        ArrayList<String> attachUrls = getIntent().getStringArrayListExtra("attachUrls");

        BitmapUtils bitmapUtils = new BitmapUtils(this);
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);
        bitmapUtils.configDefaultImageLoadAnimation(animation);
        // TODO: 需添加载失败图片
        bitmapUtils.configDefaultLoadingImage(R.drawable.loadingimg);
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.icon);
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);

        ViewPager bigImage = (ViewPager)findViewById(R.id.big_image);

        if ( (attachUrls != null) && (!attachUrls.isEmpty()) ){

            ImageView imageView;
            final ArrayList<View> imgViews = new ArrayList<View>();
            for (String attachUrl : attachUrls) {
                imageView = new ImageView(this);
                bitmapUtils.display(imageView, attachUrl);
                imgViews.add(imageView);
            }

            bigImage.setAdapter(new PagerAdapter() {
                @Override
                public int getCount() {
                    return imgViews.size();
                }

                @Override
                public boolean isViewFromObject(View view, Object o) {
                    return view == o;
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView(imgViews.get(position));
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    container.addView(imgViews.get(position));
                    return imgViews.get(position);
                }
            });
        }
    }
}
