package com.WindHunter;


import android.os.Bundle;
import com.WH.xListView.XListView;
import com.WindHunter.tools.PostsList;
import com.WindHunter.tools.WeibaBaseActivity;


public class WeibaPostsActivity extends WeibaBaseActivity {

    private String weiba_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weiba_posts);

        weiba_id = getIntent().getStringExtra("weiba_id");

        XListView xListView = (XListView)findViewById(R.id.weiba_posts_list);
        PostsList postsList = new PostsList(this, xListView);
        postsList.setCount(10).setWeibaID(weiba_id).run();
    }

}
