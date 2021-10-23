package com.example.sswu_postbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckKeywordPostActivity extends AppCompatActivity {
    String TAG = CheckKeywordPostActivity.class.getSimpleName();

    EditText notice_search_text;
    Button notice_search_btn;

    GridView my_keyword_list;
    MyGridAdapter gridAdapter;

    //listView
    ListView postList;
    ArrayList<String> post_title = new ArrayList<>();
    ArrayList<String> post_date = new ArrayList<>();
    ArrayList<Boolean> post_saved = new ArrayList<>();
    ArrayList<String> post_url = new ArrayList<>();

    MyListAdapter myListAdapter;
    View footer;
    Button add_notice;

    int total_count = 0;

    private BottomNavigationView bottomNavigationView;

    private FragmentManager fm;
    private FragmentTransaction ft;

    //    키워드 이외의 곳을 클릭하면 키보드가 내려가도록
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_keyword_post);

        notice_list_post();

        my_keyword_list = (GridView)findViewById(R.id.my_keyword_list2);
        gridAdapter = new MyGridAdapter(this);
        my_keyword_list.setAdapter(gridAdapter);

        keyword_list();


        // listView
        postList = findViewById(R.id.keyword_post_listView);
        footer = getLayoutInflater().inflate(R.layout.listview_footer, null, false);
        postList.addFooterView(footer);
        myListAdapter = new MyListAdapter(this, post_title, post_date, post_saved, post_url);
        postList.setAdapter(myListAdapter);

        notice_list();

        add_notice = footer.findViewById(R.id.add);
        add_notice.setOnClickListener(new View.OnClickListener() {
            int count = 1;

            @Override
            public void onClick(View v) {
                count++;
                notice_list_add(count);
            }
        });

        ImageButton back_btn = (ImageButton)findViewById(R.id.check_keyword_post_back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Button add_keyword_btn = (Button)findViewById(R.id.my_keyword_list_plus_btn);
        add_keyword_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), KeywordSettingActivity.class);
                startActivity(intent);
            }
        });

        notice_search_btn = findViewById(R.id.post_searching_btn);
        notice_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notice_search();
            }
        });


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        bottomNavigationView.setSelectedItemId(R.id.all_posts_btn);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.all_posts_btn:
                        menuItem.setIcon(R.drawable.noti_btn_big_click);
                        menu.findItem(R.id.home_btn).setIcon(R.drawable.home_btn_bttom);
                        menu.findItem(R.id.locker_btn).setIcon(R.drawable.store_btn);
                        menu.findItem(R.id.setting_btn).setIcon(R.drawable.setting_btn);
                        setFrag(0);
                        break;
                    case R.id.locker_btn:
                        setFrag(1);
                        break;
                    case R.id.home_btn:
                        setFrag(2);
                        break;
                    case R.id.setting_btn:
                        setFrag(3);
                        break;
                }

                return true;
            }
        });

    }

    void keyword_list() {
        // 유저별 키워드 목록 가져오기
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        String url = "http://3.37.68.242:8000/detail/keywords/";


        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                gridAdapter.user_keyword_list.add(response.getJSONObject(i).getString("keyword"));
                                gridAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "keyword list get fail");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return give_token(token);
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    void notice_list_add(int count) {
        // 유저별 공지사항 response 의 두번째 페이지 이후 가져오기 (공지사항 더보기)
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        // 한 페이지당 100개의 Object 가 response 에 담겨져 옴
        if (count <= (total_count / 100) + 1) {
            String url = "http://3.37.68.242:8000/userNotice/?page=" + count;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray results = response.getJSONArray("results");
                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject notice = results.getJSONObject(i).getJSONObject("notice");
                                    post_title.add(notice.getString("title"));
                                    post_date.add(notice.getString("date"));
                                    post_url.add(notice.getString("url"));

                                    JSONObject user_notice = results.getJSONObject(i);
                                    post_saved.add(user_notice.getBoolean("store"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            myListAdapter.notifyDataSetChanged();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Log.d(TAG, "공지사항 " + count + " 페이지 로딩 실패");
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return give_token(token);
                }
            };

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);
        }
        else {
            Toast.makeText(getApplicationContext(), "더 이상 공지사항이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void notice_list() {
        // 유저별 공지사항 response 의 첫 페이지 가져오기
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        String url = "http://3.37.68.242:8000/userNotice/";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            total_count = response.getInt("count");

                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject notice = results.getJSONObject(i).getJSONObject("notice");
                                post_title.add(notice.getString("title"));
                                post_date.add(notice.getString("date"));
                                post_url.add(notice.getString("url"));

                                JSONObject user_notice = results.getJSONObject(i);
                                post_saved.add(user_notice.getBoolean("store"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        myListAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "공지사항 첫 페이지 로딩 실패");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return give_token(token);
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    void notice_search() {
        // 공지사항 제목으로 검색
        notice_search_text = findViewById(R.id.post_searching);
        String keyword = notice_search_text.getText().toString();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        String search_url = "http://3.37.68.242:8000/userNotice/?search=" + keyword;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                search_url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        post_title.clear();
                        post_date.clear();
                        post_url.clear();
                        post_saved.clear();

                        try {
                            JSONArray results = response.getJSONArray("results");

                            for (int i = 0; i < results.length(); i++) {
                                try {
                                    JSONObject notice = results.getJSONObject(i).getJSONObject("notice");
                                    post_title.add(notice.getString("title"));
                                    post_date.add(notice.getString("date"));
                                    post_url.add(notice.getString("url"));

                                    JSONObject user_notice = results.getJSONObject(i);
                                    post_saved.add(user_notice.getBoolean("store"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            myListAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "user notice search fail");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return give_token(token);
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    void notice_list_post() {
        // 유저별 공지사항 업데이트(앱에 들어올때마다 업데이트)
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        String url = "http://3.37.68.242:8000/userNotice/";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "notice list post success");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "notice list post fail");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return give_token(token);
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    Map<String, String> give_token(String token) {
        // Request Header 에 token 을 주기 위한 함수
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        return headers;
    }

    private void setFrag(int n) {

        switch (n){
            case 0:
                Intent AllPosts = new Intent(this, CheckKeywordPostActivity.class);
                startActivity(AllPosts);
                break;
            case 1:
                Intent locker = new Intent(this, LockerActivity.class);
                startActivity(locker);
                break;
            case 2:
                Intent home = new Intent(this, HomeActivity.class);
                startActivity(home);
                break;
            case 3:
                Intent setting = new Intent(this, SettingActivity.class);
                startActivity(setting);
                break;
        }
    }
}
