package com.WindHunter.tools;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaceUtils {
    public static SpannableString getExpressionString(Context context, String content){
        SpannableString spannableString = new SpannableString(content);
        Pattern pattern = Pattern.compile("\\[([a-z]+)\\]");
        Matcher matcher = pattern.matcher(spannableString);

        while (matcher.find()){
            try {
                InputStream is = context.getAssets().open("face/" + matcher.group(1) + ".png");
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                ImageSpan imageSpan = new ImageSpan(context, bitmap);
                spannableString.setSpan(imageSpan, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (IOException e) {
                Log.e("parse face error", e.toString());
            }
        }


        return spannableString;
    }

    public static void getFaceToEdit(final Context context, final EditText editText){
        final Dialog dialog = new Dialog(context);
        dialog.setTitle("选择表情");

        GridView gridView = new GridView(context);
        gridView.setAdapter(new FaceAdapter(context));
        gridView.setNumColumns(10);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String face = (String)adapterView.getItemAtPosition(i);
                String face_tmp;
                face_tmp = "[" + face + "]";
                face_tmp = face_tmp.replace(".png", "");

                SpannableString spannableString = new SpannableString(face_tmp);

                try {
                    InputStream is = context.getAssets().open("face/" + face);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    ImageSpan imageSpan = new ImageSpan(context, bitmap);

                    spannableString.setSpan(imageSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (IOException e) {
                    Log.e("error: ", e.toString());
                }

                editText.append(spannableString);
                dialog.dismiss();
            }
        });

        dialog.setContentView(gridView);
        dialog.show();
    }

    private static class FaceAdapter extends BaseAdapter {

        private Context context;
        private String[] faces;

        public FaceAdapter(Context context) {
            this.context = context;
            try {
                faces = context.getAssets().list("face");
            } catch (IOException e) {
                Log.e("error: ", e.toString());
            }
        }

        @Override
        public int getCount() {
            return faces.length;
        }

        @Override
        public Object getItem(int i) {
            return faces[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageView imageView;

            if (view == null){
                imageView = new ImageView(context);
            }else{
                imageView = (ImageView)view;
            }

            try {
                InputStream is = context.getAssets().open("face/" + faces[i]);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.e("error", e.toString());
            }

            return imageView;
        }
    }
}
