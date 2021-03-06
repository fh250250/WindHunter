package com.WindHunter;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.WindHunter.tools.FaceUtils;
import com.WindHunter.tools.WHActivity;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class CommentActivity extends WHActivity {

    private String feed_id;

    @ViewInject(R.id.comment_content)
    EditText comment_content;

    @ViewInject(R.id.comment_check)
    CheckBox comment_check;

    @ViewInject(R.id.comment_submit)
    BootstrapButton comment_submit;

    @ViewInject(R.id.comment_words_limit)
    TextView comment_words_limit;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("评论");

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment);

        ViewUtils.inject(this);

        feed_id = getIntent().getStringExtra("feed_id");

        comment_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                comment_words_limit.setText("还可以输入140个字");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 140){
                    comment_submit.setEnabled(false);
                    comment_words_limit.setTextColor(Color.RED);
                    comment_words_limit.setText("已超过" + (editable.length() - 140) + "个字");
                }else{
                    comment_submit.setEnabled(true);
                    comment_words_limit.setTextColor(Color.BLUE);
                    comment_words_limit.setText("还可以输入" + (140 - editable.length()) + "个字");
                }
            }
        });
    }

    @OnClick(R.id.comment_submit)
    public void commentClick(View view){
        String content = comment_content.getText().toString();

        if(content.isEmpty()){
            Toast.makeText(this, "还没有输入内容...", Toast.LENGTH_SHORT).show();
            return;
        }

        // 组装 评论API 请求参数
        String commentApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=comment";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("row_id", feed_id);

        if (comment_check.isChecked())
            requestParams.addQueryStringParameter("ifShareFeed", "1");

        requestParams.addBodyParameter("content", content);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.POST,
                commentApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String state = responseInfo.result;

                        if (state.equals("1")){
                            Toast.makeText(CommentActivity.this, "评论成功", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(CommentActivity.this, WeiboActivity.class);
                            intent.putExtra("feed_id", feed_id);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            startActivity(intent);
                        }else{
                            Toast.makeText(CommentActivity.this, "评论失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(CommentActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @OnClick(R.id.comment_face)
    public void faceClick(View view){
        FaceUtils.getFaceToEdit(this, comment_content);
    }

    @Override
    public void onBackPressed() {
        if (comment_content.getText().toString().isEmpty()){
            super.onBackPressed();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("还有内容，确定退出?");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    CommentActivity.super.onBackPressed();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            builder.create().show();
        }
    }
}
