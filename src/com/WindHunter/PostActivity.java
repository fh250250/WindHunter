package com.WindHunter;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.io.File;

public class PostActivity extends WHActivity {

    private static final int REQUEST_IMAGE_CONTENT = 100;
    private static final int REQUEST_CAPTURE = 101;
    private String postImgPath;

    @ViewInject(R.id.post_edit_text)
    EditText post_edit_text;

    @ViewInject(R.id.post_img_preview)
    ImageView post_img_preview;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // 默认不显示进度条
        setProgressBarIndeterminateVisibility(false);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        // actionBar.setIcon();
        actionBar.setDisplayShowTitleEnabled(false);

        menu.add("submit")
//                .setIcon()
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // 发微博按钮逻辑
        if (item.getTitle().equals("submit")){
            String content = post_edit_text.getText().toString();

            if (content.isEmpty()){
                Toast.makeText(this, "还没有输入内容哦", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }else{

                // 组装发微博API 请求参数
                String postType;
                RequestParams requestParams = new RequestParams();

                if (postImgPath == null){
                    postType = "update";
                }
                else{
                    postType = "upload";

                    requestParams.addBodyParameter("img", new File(postImgPath));
                }

                String postApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=" + postType;
                requestParams.addQueryStringParameter("from", "2");
                requestParams.addBodyParameter("content", content);
                requestParams.addQueryStringParameter("oauth_token", oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                httpUtils.send(HttpRequest.HttpMethod.POST,
                        postApi,
                        requestParams,
                        new RequestCallBack<String>() {
                            @Override
                            public void onStart() {
                                // 显示进度条
                                setProgressBarIndeterminateVisibility(true);
                            }

                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                if (responseInfo.result.equals("0")){
                                    Toast.makeText(PostActivity.this, "发表失败", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(PostActivity.this, "发表成功", Toast.LENGTH_SHORT).show();

                                    finish();
                                }

                                // 不显示进度条
                                setProgressBarIndeterminateVisibility(false);
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(PostActivity.this, s, Toast.LENGTH_SHORT).show();
                                // 不显示进度条
                                setProgressBarIndeterminateVisibility(false);
                            }
                        });
            }
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取进度条
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.post);

        ViewUtils.inject(this);

    }

    @OnClick(R.id.post_add_img)
    public void addImgClick(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("图片来源");
        String[] options = {"从图库", "从相机"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, REQUEST_IMAGE_CONTENT);
                        break;
                    case 1:
                        Intent creamer = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(creamer, REQUEST_CAPTURE);
                        break;
                    default:
                        break;
                }
            }
        });

        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && null != data){
            Uri uri = data.getData();

            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            postImgPath = cursor.getString(column_index);

            bitmapUtils.display(post_img_preview, postImgPath);
        }else{
            postImgPath = null;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
