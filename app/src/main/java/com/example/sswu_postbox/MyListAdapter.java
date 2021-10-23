package com.example.sswu_postbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;




public class MyListAdapter extends BaseAdapter {
    String TAG = MyListAdapter.class.getSimpleName();

    Context context;
    LayoutInflater layoutInflater;
    ArrayList<String> post_title;
    ArrayList<String> post_date;
    ArrayList<Boolean> post_saved;
    ArrayList<String> post_url;


    public MyListAdapter(Context context, ArrayList<String> post_title, ArrayList<String> post_date, ArrayList<Boolean> post_saved, ArrayList<String> post_url) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.post_title = post_title;
        this.post_date = post_date;
        this.post_saved = post_saved;
        this.post_url = post_url;
    }

    @Override
    public int getCount() {
        return post_title.size();
    }

    @Override
    public Object getItem(int position) {
        return post_title.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = layoutInflater.inflate(R.layout.post_listview_layout2, null);


        TextView contents_postTitle = view.findViewById(R.id.contents_postTitle);
        contents_postTitle.setText(post_title.get(position));


        TextView contents_date = view.findViewById(R.id.contents_date);
        contents_date.setText(post_date.get(position));


        //url 받기 코드 들어 온 후에 사용
        String url = post_url.get(position);

        String titleText = contents_postTitle.getText().toString();
        String dateText = contents_date.getText().toString();


        // 제목 클릭 이벤트 ( 웹뷰 )
        contents_postTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read_modify(post_title.get(position));

                Intent intent = new Intent(context.getApplicationContext(), PostClickActivity.class);
                intent.putExtra("webView_title", titleText);
                intent.putExtra("webView_date", dateText);
                intent.putExtra("url", url);
                intent.putExtra("store_state", post_saved.get(position));
                context.startActivity(intent);
            }
        });



        ImageButton post_share_btn = view.findViewById(R.id.post_share_btn);

        // 보관함 저장 버튼
        ImageButton save_post = (ImageButton) view.findViewById(R.id.save_btn);
        save_post.setSelected(post_saved.get(position));
        save_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());

                if (v.isSelected()) {
                    store_modify(post_title.get(position), v.isSelected());
                }
                else{
                    store_modify(post_title.get(position), v.isSelected());
                }

            }
        });


        /* 공유 버튼
        post_share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                Sharing_intent.setType("text/plain");

                String Test_Message = "["+titleText+"]" + "  " + url;
                Sharing_intent.putExtra(Intent.EXTRA_TEXT, Test_Message);

                Intent Sharing =Intent.createChooser(Sharing_intent, "공유하기");
                context.startActivity(Sharing);
            }
        });
    */

        return view;
    }

    void store_modify(String title, boolean state) {
        // 보관, 보관 취소
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        String url = "http://3.37.68.242:8000/update/notice/";

        HashMap<String, String> store_json = new HashMap<>();
        store_json.put("title", title);
        store_json.put("store", Boolean.toString(state));

        JSONObject parameter = new JSONObject(store_json);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH,
                url,
                parameter,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "보관 상태 수정 성공" + title + state);
                        if (state) Toast.makeText(context, "보관함 저장", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(context, "보관함 저장 취소", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d(TAG, "보관 상태 수정 실패");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return give_token(token);
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        queue.add(request);
    }

    void read_modify(String title) {
        // 읽음 상태 수정 (클릭 이벤트 시 읽음으로 취급)
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        String url = "http://3.37.68.242:8000/update/notice/";

        HashMap<String, String> read_json = new HashMap<>();
        read_json.put("title", title);
        read_json.put("read", "true");

        JSONObject parameter = new JSONObject(read_json);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH,
                url,
                parameter,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "읽음 상태 수정 성공 " + title + " " + true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d(TAG, "읽음 상태 수정 실패 " + title);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return give_token(token);
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        queue.add(request);
    }

    Map<String, String> give_token(String token) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        return headers;
    }
}
