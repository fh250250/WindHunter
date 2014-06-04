package com.WindHunter;


import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import com.WindHunter.tools.WeibaBaseActivity;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

public class CreatePostActivity extends WeibaBaseActivity {

    private CreatePostActivity context;
    private String weiba_id;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("发布帖子");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post);
        context = this;

        weiba_id = getIntent().getStringExtra("weiba_id");

        BootstrapButton submit = (BootstrapButton)findViewById(R.id.create_post_submit);
        final BootstrapEditText titleEdit = (BootstrapEditText)findViewById(R.id.create_post_title);
        final BootstrapEditText contentEdit = (BootstrapEditText)findViewById(R.id.create_post_content);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEdit.getText().toString();
                String content = contentEdit.getText().toString();

                if (title.isEmpty() || content.isEmpty()){
                    Toast.makeText(context, "标题或内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                String api = "http://" + host + "index.php?app=api&mod=Weiba&act=create_post";
                RequestParams requestParams = new RequestParams();
                requestParams.addQueryStringParameter("id", weiba_id);
                requestParams.addQueryStringParameter("user_id", uid);
                requestParams.addBodyParameter("title", title);
                requestParams.addBodyParameter("content", content);
                requestParams.addQueryStringParameter("oauth_token", oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                httpUtils.send(HttpRequest.HttpMethod.POST,
                        api,
                        requestParams,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String state = responseInfo.result;

                                if (state.equals("1")){
                                    Toast.makeText(context, "发布成功", Toast.LENGTH_SHORT).show();
                                    finish();
                                }else {
                                    Toast.makeText(context, "发布失败", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                                Log.e("net error", e.toString() + s);
                            }
                        });
            }
        });
    }


}
