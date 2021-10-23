package com.example.sswu_postbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class KeywordSettingActivity extends AppCompatActivity {
    String TAG = KeywordSettingActivity.class.getSimpleName();

    EditText keyword_add_text, keyword_del_text, keyword_search_text;
    Button keyword_add_btn, keyword_del_btn, keyword_search_btn;

    GridView my_keyword_list;
    MyGridAdapter gridAdapter;

    private BottomNavigationView bottomNavigationView;

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
        setContentView(R.layout.activity_keyword_setting);

        my_keyword_list = (GridView)findViewById(R.id.my_keyword_list);
        gridAdapter = new MyGridAdapter(this);
        my_keyword_list.setAdapter(gridAdapter);

        keyword_list();

        keyword_add_btn = findViewById(R.id.keyword_adding_btn);
        keyword_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyword_add();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                notice_list_post();
            }
        });


        keyword_del_btn = findViewById(R.id.keyword_delete_btn);
        keyword_del_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyword_del();
                notice_del();
            }
        });

        keyword_search_btn = findViewById(R.id.keyword_searching_btn);
        keyword_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyword_search();
            }
        });

        ImageButton back_btn = (ImageButton)findViewById(R.id.keyword_setting_back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.all_posts_btn:
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

    void keyword_add() {
        // 키워드 추가하기
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        keyword_add_text = findViewById(R.id.keyword_adding);

        HashMap<String, String> keywordAdd_json = new HashMap<>();
        keywordAdd_json.put("keyword", keyword_add_text.getText().toString());
        keywordAdd_json.put("user", null);

        JSONObject parameter = new JSONObject(keywordAdd_json);
        String url = "http://3.37.68.242:8000/keywords/";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url,
                parameter,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        gridAdapter.user_keyword_list.clear();
                        keyword_list();

                        Log.d(TAG, gridAdapter.user_keyword_list.toString());

                        try {
                            if (response.getString("keyword").equals("duplicate")) {
                                Toast toast = Toast.makeText(getApplicationContext(), "중복된 키워드 입니다.", Toast.LENGTH_LONG);
                                toast.show();
                            }
                            else {
                                Toast toast = Toast.makeText(getApplicationContext(), "키워드 등록에 성공했습니다.", Toast.LENGTH_LONG);
                                toast.show();

                                // 푸쉬 알림을 위한 FCM topic 구독
                                try {
                                    String encoding_keyword = URLEncoder.encode(keyword_add_text.getText().toString(), "UTF-8");
                                    Log.d(TAG, "keyword " + encoding_keyword);

                                    FirebaseMessaging.getInstance().subscribeToTopic(encoding_keyword)
                                            .addOnCompleteListener( task -> {
                                                if (task.isComplete()) Log.d(TAG, "구독 성공");
                                                else Log.d(TAG, "구독 실패");
                                            });
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(getApplicationContext(), "키워드가 정상적으로 추가되지 않았습니다.\n잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT);
                        toast.show();

                        error.printStackTrace();
                        Log.d(TAG, "keyword add fail " + token);
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

    void keyword_del() {
        // 키워드 삭제하기
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        keyword_del_text = findViewById(R.id.keyword_delete);

        if (!gridAdapter.user_keyword_list.contains(keyword_del_text.getText().toString())) {
            Toast.makeText(getApplicationContext(), "존재하지 않는 키워드입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://3.37.68.242:8000/detail/keywords/?keyword=" + keyword_del_text.getText().toString();

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        gridAdapter.user_keyword_list.clear();
                        keyword_list();

                        Toast toast = Toast.makeText(getApplicationContext(), "키워드를 삭제했습니다.", Toast.LENGTH_SHORT);
                        toast.show();

                        try {
                            String encoding_keyword = URLEncoder.encode(keyword_del_text.getText().toString(), "UTF-8");
                            Log.d(TAG, "keyword " + encoding_keyword);

                            // 더 이상 해당 주제를 구독할 필요가 없으므로 구독 해제
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(encoding_keyword)
                                    .addOnCompleteListener( task -> {
                                        if (task.isComplete()) Log.d(TAG, "구독 해제 성공");
                                        else Log.d(TAG, "구독 해제 실패");
                                    });
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(getApplicationContext(), "키워드가 정상적으로 삭제되지 않았습니다.\n잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG);
                        toast.show();

                        error.printStackTrace();
                        Log.d(TAG, "delete keyword fail");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return give_token(token);
            }
        };


        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    void keyword_list() {
        // 로그인한 사용자의 키워드 목록 가져오기
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

    void keyword_search() {
        // 키워드 검색하기
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        keyword_search_text = findViewById(R.id.keyword_searching);

        String keyword = keyword_search_text.getText().toString();
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

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    Map<String, String> give_token(String token) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        return headers;
    }

    void notice_del() {
        // 키워드 삭제 시 해당 키워드가 제목에 들어간 유저 별 공지사항도 함께 삭제
        keyword_del_text = findViewById(R.id.keyword_delete);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        String url = "http://3.37.68.242:8000/destroy/notice/?keyword=" + keyword_del_text.getText().toString();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "공지사항 삭제 성공");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "공지사항 삭제 실패");
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




