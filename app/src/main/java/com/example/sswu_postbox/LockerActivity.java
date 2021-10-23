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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LockerActivity extends AppCompatActivity {
    String TAG = LockerActivity.class.getSimpleName();

    EditText search_text;
    Button search_btn;

    GridView my_keyword_list;
    MyGridAdapter gridAdapter;

    //보관된 공지사항 listview
    ListView postList;
    ArrayList<String> post_title = new ArrayList<>();
    ArrayList<String> post_date = new ArrayList<>();
    ArrayList<Boolean> post_saved = new ArrayList<>();
    ArrayList<String> post_url = new ArrayList<>();
    MyListAdapter myListAdapter;

    private BottomNavigationView bottomNavigationView;

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
        setContentView(R.layout.activity_locker);

        my_keyword_list = (GridView)findViewById(R.id.my_keyword_list2);
        gridAdapter = new MyGridAdapter(this);
        my_keyword_list.setAdapter(gridAdapter);

        keyword_list();

        postList = findViewById(R.id.locker_post_listView);
        myListAdapter = new MyListAdapter(this, post_title, post_date, post_saved, post_url);
        postList.setAdapter(myListAdapter);

        notice_list();

        ImageButton locker_back_btn = findViewById(R.id.locker_back_btn);
        locker_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        search_btn = findViewById(R.id.locker_search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyword_search();
            }
        });


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        bottomNavigationView.setSelectedItemId(R.id.locker_btn);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.all_posts_btn:
                        setFrag(0);
                        break;
                    case R.id.locker_btn:
                        menuItem.setIcon(R.drawable.store_btn_big_click);
                        menu.findItem(R.id.home_btn).setIcon(R.drawable.home_btn_bttom);
                        menu.findItem(R.id.all_posts_btn).setIcon(R.drawable. noti_btn);
                        menu.findItem(R.id.setting_btn).setIcon(R.drawable.setting_btn_click);
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
        // 현재 로그인한 사용자의 키워드 목록 가져오기
        String TAG = KeywordSettingActivity.class.getSimpleName();

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

    void notice_list() {
        // 보관된 공지사항 목록 가져오기
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        String url = "http://3.37.68.242:8000/stored/notice/";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject notice = response.getJSONObject(i).getJSONObject("notice");
                                post_title.add(notice.getString("title"));
                                post_date.add(notice.getString("date"));
                                post_url.add(notice.getString("url"));

                                JSONObject user_notice = response.getJSONObject(i);
                                post_saved.add(user_notice.getBoolean("store"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        myListAdapter.notifyDataSetChanged();
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "notice user list error");
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

    void keyword_search() {
        // 키워드 검색하기
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        search_text = findViewById(R.id.ghg);

        String keyword = search_text.getText().toString();
        String url = "http://3.37.68.242:8000/detail/keywords/?search=" + keyword;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            Toast toast = Toast.makeText(getApplicationContext(), "찾으시는 검색 결과가 없습니다.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        ArrayList<String> keyword_list = new ArrayList<>();

                        for(int i = 0; i < response.length(); i++) {
                            try {
                                keyword_list.add(response.getJSONObject(i).getString("keyword"));

                                Log.d(TAG, keyword_list.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        gridAdapter.user_keyword_list = keyword_list;
                        gridAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(getApplicationContext(), "키워드 검색에 실패했습니다.\n잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG);
                        toast.show();

                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return give_token(token);
            }
        };

        // 검색한 키워드에 따라 해당 키워드가 제목이 들어간 공지사항도 함께 검색
        String search_url = "http://3.37.68.242:8000/stored/notice/?search=" + keyword;

        JsonArrayRequest request1 = new JsonArrayRequest(Request.Method.GET,
                search_url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        post_title.clear();
                        post_date.clear();
                        post_url.clear();
                        post_saved.clear();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject notice = response.getJSONObject(i).getJSONObject("notice");
                                post_title.add(notice.getString("title"));
                                post_date.add(notice.getString("date"));
                                post_url.add(notice.getString("url"));

                                JSONObject user_notice = response.getJSONObject(i);
                                post_saved.add(user_notice.getBoolean("store"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        myListAdapter.notifyDataSetChanged();
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "notice user list error");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return give_token(token);
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
        queue.add(request1);
    }

    Map<String, String> give_token(String token) {
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