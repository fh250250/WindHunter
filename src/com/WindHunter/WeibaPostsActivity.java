package com.WindHunter;


import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.WH.xListView.XListView;
import com.WindHunter.tools.PostsList;
import com.WindHunter.tools.WeibaBaseActivity;


public class WeibaPostsActivity extends WeibaBaseActivity {

    private String weiba_id;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("帖子列表");

        menu.add("post").setIcon(R.drawable.bar_post).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getTitle().equals("post")){
            Intent intent = new Intent(this, CreatePostActivity.class);
            intent.putExtra("weiba_id", weiba_id);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

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
