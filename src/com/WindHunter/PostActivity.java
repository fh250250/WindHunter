package com.WindHunter;


import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
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
    private static final int REQUEST_AT = 102;
    private String postImgPath;

    @ViewInject(R.id.post_edit_text)
    EditText post_edit_text;

    @ViewInject(R.id.post_img_preview)
    FrameLayout post_img_preview;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        // actionBar.setIcon();
        actionBar.setDisplayShowTitleEnabled(false);

        menu.add("submit")
                .setIcon(R.drawable.post_submit)
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
                    File file = new File(postImgPath);

                    if (file.length() > 1 * 1024 * 1024){
                        Toast.makeText(this, "图片不能大于1M", Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    postType = "upload";

                    requestParams.addBodyParameter("img", file);
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
                                setUIEnable(false);
                            }

                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                if (responseInfo.result.equals("0")){
                                    Toast.makeText(PostActivity.this, "发表失败", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(PostActivity.this, "发表成功", Toast.LENGTH_SHORT).show();

                                    finish();
                                }

                                setUIEnable(true);
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(PostActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                                setUIEnable(true);
                            }
                        });
            }
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.post);

        ViewUtils.inject(this);

        setProgressBarIndeterminateVisibility(false);

    }

    // 用于存储照相后的照片
    private Uri photoUri = null;

    @OnClick(R.id.post_img)
    public void addImgClick(View view){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_IMAGE_CONTENT);

    }

    @OnClick(R.id.post_capture)
    public void captureClick(View view){
        Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentValues values = new ContentValues();
        photoUri = PostActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        capture.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(capture, REQUEST_CAPTURE);
    }

    @OnClick(R.id.post_at)
    public void atClick(View view){
        Intent intent = new Intent(this, GetFollowForPostActivity.class);
        intent.putExtra("host", host);
        intent.putExtra("oauth_token", oauth_token);
        intent.putExtra("oauth_token_secret", oauth_token_secret);
        intent.putExtra("uid", uid);

        startActivityForResult(intent, REQUEST_AT);
    }

    @OnClick(R.id.post_topic)
    public void topicClick(View view){
        post_edit_text.append("#请在这里输入自定义话题#");
        post_edit_text.setSelection(post_edit_text.length() - 12, post_edit_text.length() - 1);

        InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
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

            post_img_preview.removeAllViews();
            ImageView preImage = new ImageView(this);
            FrameLayout.LayoutParams imgLayout = new FrameLayout.LayoutParams(200, 200);
            imgLayout.gravity = Gravity.CENTER;
            post_img_preview.addView(preImage, imgLayout);
            bitmapUtils.display(preImage, postImgPath);

            ImageView delImg = new ImageView(this);
            delImg.setImageResource(R.drawable.x);
            FrameLayout.LayoutParams delLayout = new FrameLayout.LayoutParams(50, 50);
            delLayout.gravity = Gravity.RIGHT | Gravity.TOP;
            post_img_preview.addView(delImg, delLayout);

            delImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postImgPath = null;
                    post_img_preview.removeAllViews();
                }
            });

        }else{
            postImgPath = null;
        }

        if (requestCode == REQUEST_AT){
            String[] names = data.getStringArrayExtra("names");

            for (String name : names){
                post_edit_text.append("@" + name + " ");
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setUIEnable(boolean b){
        findViewById(R.id.post_at).setEnabled(b);
        findViewById(R.id.post_img).setEnabled(b);
        findViewById(R.id.post_capture).setEnabled(b);
        findViewById(R.id.post_topic).setEnabled(b);
        setProgressBarIndeterminateVisibility(!b);
    }

    @Override
    public void onBackPressed() {
        if (post_edit_text.getText().toString().isEmpty()){
            super.onBackPressed();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("还有内容，确定退出?");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    PostActivity.super.onBackPressed();
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
