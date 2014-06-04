package com.WindHunter;


import android.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import com.WH.xListView.XListView;
import com.WindHunter.tools.PostsList;
import com.WindHunter.tools.WeibaBaseActivity;

public class MyWeibaPostsActivity extends WeibaBaseActivity {

    private String type;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);

        if (type.equals("following_posts")){
            actionBar.setTitle("关注微吧的帖子");
        }else if (type.equals("posteds")){
            actionBar.setTitle("发布的帖子");
        }else if (type.equals("commenteds")){
            actionBar.setTitle("回复的帖子");
        }else if (type.equals("favorite_list")){
            actionBar.setTitle("收藏的帖子");
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_weiba_posts);

        type = getIntent().getStringExtra("type");

        XListView xListView = (XListView)findViewById(R.id.my_weiba_posts_list);
        PostsList postsList = new PostsList(this, xListView);
        postsList.setCount(10).setType(type).setUserID(uid).run();
    }
}
