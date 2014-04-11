package com.WindHunter;


import android.os.Bundle;
import android.widget.TextView;
import com.WindHunter.tools.WHActivity;

public class WeiboActivity extends WHActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weibo);


        TextView textView = (TextView)findViewById(R.id.weibo_text);
        textView.setText(getIntent().getStringExtra("feed_id"));
    }

}
