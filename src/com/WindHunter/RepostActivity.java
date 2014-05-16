package com.WindHunter;


import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class RepostActivity extends WHActivity {

    @ViewInject(R.id.repost_content)
    EditText repost_content;

    @ViewInject(R.id.repost_comment)
    CheckBox repost_comment;

    @ViewInject(R.id.repost_words_limit)
    TextView repost_words_limit;

    @ViewInject(R.id.repost_submit)
    BootstrapButton repost_submit;

    private String feed_id;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("转发");

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repost);

        ViewUtils.inject(this);

        feed_id = getIntent().getStringExtra("feed_id");

        repost_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                repost_words_limit.setText("还可以输入140个字");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 140){
                    repost_submit.setEnabled(false);
                    repost_words_limit.setTextColor(Color.RED);
                    repost_words_limit.setText("已超过" + (editable.length() - 140) + "个字");
                }else{
                    repost_submit.setEnabled(true);
                    repost_words_limit.setTextColor(Color.BLUE);
                    repost_words_limit.setText("还可以输入" + (140 - editable.length()) + "个字");
                }
            }
        });
    }

    @OnClick(R.id.repost_submit)
    public void repostClick(View view){
        String content = repost_content.getText().toString();

        // 组装 转发API 请求参数
        String repostApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=repost";
        RequestParams requestParams = new RequestParams();

        if (!content.isEmpty())
            requestParams.addBodyParameter("content", content);

        if (repost_comment.isChecked())
            requestParams.addQueryStringParameter("comment", "1");
        else
            requestParams.addQueryStringParameter("comment", "0");

        requestParams.addQueryStringParameter("id", feed_id);
        requestParams.addQueryStringParameter("from", "2");
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.POST,
                repostApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String state = responseInfo.result;

                        if (state.equals("1")){
                            Toast.makeText(RepostActivity.this, "转发成功", Toast.LENGTH_SHORT).show();
                            RepostActivity.this.finish();
                        }else{
                            Toast.makeText(RepostActivity.this, "转发失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(RepostActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @OnClick(R.id.repost_face)
    public void faceClick(View view){
        FaceUtils.getFaceToEdit(this, repost_content);
    }


    @Override
    public void onBackPressed() {
        if (repost_content.getText().toString().isEmpty()){
            super.onBackPressed();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("还有内容，确定退出?");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    RepostActivity.super.onBackPressed();
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
