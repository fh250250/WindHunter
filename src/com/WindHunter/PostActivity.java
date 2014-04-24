package com.WindHunter;


import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
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
import android.widget.ProgressBar;
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

    @ViewInject(R.id.post_progress)
    ProgressBar post_progress;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

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
                                post_progress.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoading(long total, long current, boolean isUploading) {
                                if (isUploading){
                                    Long l = current/total * 100;
                                    post_progress.setProgress(l.intValue());
                                }
                            }

                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                if (responseInfo.result.equals("0")){
                                    Toast.makeText(PostActivity.this, "发表失败", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(PostActivity.this, "发表成功", Toast.LENGTH_SHORT).show();

                                    finish();
                                }

                                post_progress.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(PostActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                                post_progress.setVisibility(View.GONE);
                            }
                        });
            }
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.post);

        ViewUtils.inject(this);

        post_progress.setVisibility(View.GONE);

    }



    // 用于存储照相后的照片
    private Uri photoUri = null;

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
                        Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        ContentValues values = new ContentValues();
                        photoUri = PostActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        capture.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(capture, REQUEST_CAPTURE);
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
        if (resultCode == RESULT_OK){

            if (requestCode == REQUEST_IMAGE_CONTENT && null != data){
                // 图库
                Uri imgUri = data.getData();
                ContentResolver cr = this.getContentResolver();
                Cursor cursor = null;
                if (imgUri != null) {
                    cursor = cr.query(imgUri, null, null, null, null);
                }
                if (cursor != null) {
                    cursor.moveToFirst();
                    postImgPath = cursor.getString(1);
                    cursor.close();
                }
            }else if (requestCode == REQUEST_CAPTURE){
                // 相机
                ContentResolver cr = this.getContentResolver();
                Cursor cursor = cr.query(photoUri, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    postImgPath = cursor.getString(1);
                    cursor.close();
                }
            }

            bitmapUtils.display(post_img_preview, postImgPath);
        }else{
            postImgPath = null;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
