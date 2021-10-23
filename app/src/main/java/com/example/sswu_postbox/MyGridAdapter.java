package com.example.sswu_postbox;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

// Gridview Adapter
public class MyGridAdapter extends BaseAdapter{
    Context context;

    //사용자가 등록한 모든 키워드들을 받아올 변수(임시로 텍스트 넣어둠)
    ArrayList<String> user_keyword_list = new ArrayList<>();

    public MyGridAdapter(Context c){
        context = c;
    }

    @Override
    //보여줄 키워드 개수
    public int getCount() {
        return user_keyword_list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    // 띄우는 부분
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(Dimension.SP, 12);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setBackground(ContextCompat.getDrawable(context, R.drawable.keyword_list));
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        textView.setText(user_keyword_list.get(position));

        return textView;
    }
}

