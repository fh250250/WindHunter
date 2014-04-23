package com.WindHunter;


import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.WindHunter.tools.WHActivity;
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

    private String feed_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repost);

        ViewUtils.inject(this);

        feed_id = getIntent().getStringExtra("feed_id");
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
}
