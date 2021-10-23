package com.example.sswu_postbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class HomeActivity extends AppCompatActivity {
    String TAG = HomeActivity.class.getSimpleName();

    TextView unread_count;

    String user_major, user_major2, user_major3;
    String user_major_url;

    //Recyclerview
    private RecyclerView recyclerView;
    private MyRecyclerAdapter adapter;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        notice_list_post();
        unread_count();


        // recyclerView
        home_keyword_list();


        // listView
        postList = findViewById(R.id.home_post_list);
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


        // 사용자 전공
        user_major();


        ImageButton portal_shortcut = findViewById(R.id.portal_shortcut);
        portal_shortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://portal.sungshin.ac.kr/sso/login.jsp"));
                startActivity(browserIntent);

            }
        });

        ImageButton edu_sys_shortcut = findViewById(R.id.edu_sys_shortcut);
        edu_sys_shortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://lms.sungshin.ac.kr/ilos/m/main/login_form.acl"));
                startActivity(browserIntent);

            }
        });

        ImageButton sungshin_main_shortcut = findViewById(R.id.sungshin_main_shortcut);
        sungshin_main_shortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sungshin.ac.kr/sites/main_kor/main.jsp"));
                startActivity(browserIntent);

            }
        });

        ImageButton major_shortcut = findViewById(R.id.major_shortcut);
        major_shortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(user_major_url));
                startActivity(browserIntent);

            }
        });


        Button plus_keyword_btn = findViewById(R.id.my_keyword_list_plus_btn);
        plus_keyword_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), KeywordSettingActivity.class);
                startActivity(intent);
            }
        });


        Button check_keyword_post_btn = findViewById(R.id.check_keyword_post_btn);
        check_keyword_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CheckKeywordPostActivity.class);
                startActivity(intent);
            }
        });


        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        Menu menu = bottomNavigationView.getMenu();
        bottomNavigationView.setSelectedItemId(R.id.home_btn);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
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
                        menuItem.setIcon(R.drawable.home_btn_big_click);

                        menu.findItem(R.id.all_posts_btn).setIcon(R.drawable.noti_btn);
                        menu.findItem(R.id.locker_btn).setIcon(R.drawable.store_btn);
                        menu.findItem(R.id.setting_btn).setIcon(R.drawable.setting_btn);
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


    //Recyclerview
    private void home_keyword_list(){

        recyclerView = findViewById(R.id.home_keyword_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<String> itemList = new ArrayList<>();

        adapter = new MyRecyclerAdapter(this, itemList, onClickItem);
        recyclerView.setAdapter(adapter);

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
                                itemList.add(response.getJSONObject(i).getString("keyword"));
                                adapter.notifyDataSetChanged();
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

    private View.OnClickListener onClickItem = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String str = (String) v.getTag();
            Toast.makeText(HomeActivity.this, str, Toast.LENGTH_SHORT).show();
        }
    };

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

    void notice_list_add(int count) {
        // 유저별 공지사항 response 의 두번째 페이지 이후  (공지사항 더보기)
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


    public void user_major() {
        // 현재 로그인한 사용자의 전공 가져오기
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        String url = "http://3.37.68.242:8000/detail/user/";

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                JSONObject userInfo = response.getJSONObject(0);

                                user_major = userInfo.getString("user_major");
                                user_major2 = userInfo.getString("user_major2");
                                user_major3 = userInfo.getString("user_major3");

                                // user_major -> 주전공 , user_major2 -> 복수전공, user_major3 -> 부전공
                                if (major_url_map.containsKey(user_major)) {
                                        user_major_url = major_url_map.get(user_major);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Log.d(TAG, "user major call fail");
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

    void unread_count() {
        // 읽지 않은 공지사항의 개수 가져오기
        unread_count = findViewById(R.id.unread_count);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("access_token", "null");

        String url = "http://3.37.68.242:8000/unread/notice/";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            unread_count.setText(String.valueOf(response.getInt("count")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

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

    private Map<String, String> major_url_map = new HashMap<String, String>(){
        {
            put("컴퓨터공학과","https://www.sungshin.ac.kr/ce/index..do");
            put("청정융합에너지공학과", "https://www.sungshin.ac.kr/clean/index..do");
            put("바이오생명공학과", "https://www.sungshin.ac.kr/bte/index..do");
            put("바이오식품공학과", "https://www.sungshin.ac.kr/bif/index..do");
            put("융합보안공학과", "https://www.sungshin.ac.kr/cse/index..do");
            put("정보시스템공학과", "https://www.sungshin.ac.kr/infosys/index..do");
            put("서비스디자인공학과", "https://www.sungshin.ac.kr/serdesign/index..do");
            put("AI융합학부", "https://www.sungshin.ac.kr/aiot/index..do");
            put("국어국문학과", "https://www.sungshin.ac.kr/sites/korean/index.do");
            put("영어영문학과", "https://www.sungshin.ac.kr/sites/english/index.do");
            put("독일어문ㆍ문화학과", "https://www.sungshin.ac.kr/sites/german/index.do");
            put("프랑스어문ㆍ문화학과", "https://www.sungshin.ac.kr/sites/france/index.do");
            put("일본어문ㆍ문화학과", "https://www.sungshin.ac.kr/sites/japanese/index.do");
            put("중국어문ㆍ문화학과", "https://www.sungshin.ac.kr/sites/chinese/index.do");
            put("사학과", "https://www.sungshin.ac.kr/sites/history/index.do");
            put("정치외교학과", "https://www.sungshin.ac.kr/sites/politics/index.do");
            put("심리학과", "https://www.sungshin.ac.kr/sites/psy/index.do");
            put("지리학과", "https://www.sungshin.ac.kr/sites/geographic/index.do");
            put("경제학과", "https://www.sungshin.ac.kr/sites/economic/index.do");
            put("경영학과", "https://www.sungshin.ac.kr/sites/business/index.do");
            put("경영학부", "https://www.sungshin.ac.kr/sites/bizadm/index.do");
            put("미디어커뮤니케이션학과", "https://www.sungshin.ac.kr/sites/mediacomm/index.do");
            put("사회복지학과(운정)", "https://www.sungshin.ac.kr/sites/welfare/index.do");
            put("법학부", "https://www.sungshin.ac.kr/sites/solaw/index.do");
            put("수학과", "https://www.sungshin.ac.kr/sites/math/index.do");
            put("통계학과", "https://www.sungshin.ac.kr/sites/statistics/index.do");
            put("IT학부", "https://www.sungshin.ac.kr/sites/it/index.do");
            put("화학과", "https://www.sungshin.ac.kr/sites/chm/index.do");
            put("생명과학·화학부", "https://www.sungshin.ac.kr/sites/bio/index.do");
            put("수리통계데이터사이언스학부", "https://www.sungshin.ac.kr/sites/math-statistics/index.d");
            put("화학·에너지융합학부", "https://www.sungshin.ac.kr/sites/chem-energy/index.do");
            put("스포츠레저학과(수정)", "https://www.sungshin.ac.kr/sites/sport/index.do");
            put("운동재활복지학과(수정)", "https://www.sungshin.ac.kr/sites/exercise/index.do");
            put("글로벌의과학과(운정)", "https://www.sungshin.ac.kr/sites/gms/index.do");
            put("식품영양학과(운정)", "https://www.sungshin.ac.kr/sites/nutrition/index.do");
            put("사회복지학과(운정)", "https://www.sungshin.ac.kr/sites/welfare/index.do");
            put("바이오신약의과학부", "https://www.sungshin.ac.kr/sites/biopharm/index.do");
            put("바이오헬스융합학부", "https://www.sungshin.ac.kr/sites/biohealth/index.do");
            put("스포츠과학부", "https://www.sungshin.ac.kr/sites/sportsscience/index.do");
            put("글로벌비즈니스학과", "https://www.sungshin.ac.kr/sites/globiz/index.do");
            put("의류산업학과", "https://www.sungshin.ac.kr/sites/cloth/index.do");
            put("뷰티산업학과", "https://www.sungshin.ac.kr/sites/insbeauty/index.do");
            put("소비자생활문화산업학과", "https://www.sungshin.ac.kr/sites/family/index.do");
            put("교육학과", "https://www.sungshin.ac.kr/sites/education/index.do");
            put("사회교육과", "https://www.sungshin.ac.kr/sites/edusociety/index.do");
            put("윤리교육과", "https://www.sungshin.ac.kr/sites/eduethics/index.do");
            put("한문교육과", "https://www.sungshin.ac.kr/sites/educhinese/index.do");
            put("유아교육과", "https://www.sungshin.ac.kr/sites/edukids/index.d");
            put("동양화과", "https://www.sungshin.ac.kr/sites/orient/index.do");
            put("서양화과", "https://www.sungshin.ac.kr/sites/western/index.do");
            put("조소과", "https://www.sungshin.ac.kr/sites/carving/index.do");
            put("공예과", "https://www.sungshin.ac.kr/sites/indusdesign/index.do");
            put("성악과", "https://www.sungshin.ac.kr/vocal/index.do");
            put("기악과", "https://www.sungshin.ac.kr/instrumental/index.do");
            put("작곡과", "https://www.sungshin.ac.kr/composition/index.do");
            put("문화예술경영학과", "https://www.sungshin.ac.kr/cultureart/12695/subview.do");
            put("미디어영상연기학과", "https://www.sungshin.ac.kr/vmacting/index..do");
            put("현대실용음악학과", "https://www.sungshin.ac.kr/ctpmusic/index");
            put("무용예술학과", "https://www.sungshin.ac.kr/danceart/index..do");
            put("간호학과", "https://www.sungshin.ac.kr/sites/nurse/index.do");
        }
    };
}