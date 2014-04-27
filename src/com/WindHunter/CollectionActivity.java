package com.WindHunter;

import android.os.Bundle;
import android.view.Menu;
import com.WH.xListView.XListView;
import com.WindHunter.tools.WHActivity;
import com.WindHunter.tools.WeiboList;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class CollectionActivity extends WHActivity{

    @ViewInject(R.id.collection_list)
    XListView collection_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection);

        ViewUtils.inject(this);

        WeiboList weiboList = new WeiboList(this, collection_list);
        weiboList.setCount(10).setType("favorite_feed").run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        title.setText("收藏列表");
        return true;
    }
}
