package com.WindHunter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;


public class WelcomeActivity extends Activity {

    // 延时时间
    final int JUMP_DELAY = 3 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        // 设置延时跳转
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent().setClass(WelcomeActivity.this, LoginActivity.class));
                WelcomeActivity.this.finish();
            }
        };
        timer.schedule(timerTask, JUMP_DELAY);
    }
}
