package com.example.sswu_postbox;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DepartModiActivity extends AppCompatActivity {
    String TAG = DepartModiActivity.class.getSimpleName();

    Button modi_button;
    Spinner after_major;
    String selectedMajor;

    TextView current_major;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_depart_modi);

        modi_button = findViewById(R.id.modi_button);
        after_major = findViewById(R.id.after_major);

        get_current_major();

//      뒤로 가기 버튼
        ImageButton modi_back_btn = findViewById(R.id.modi_back_btn);
        modi_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//      수정 버튼 클릭 시
        modi_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modify();
                get_current_major();
//               설정 창 새로고침
                ((SettingActivity)SettingActivity.CONTEXT).reload();
                finish();
            }
        });
    }

    void get_current_major() {
        // 현재 주전공 가져오기
        current_major = findViewById(R.id.before_major);

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

                            current_major.setText(userInfo.getString("user_major"));
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


    void modify() {
        // 학과 수정
        selectedMajor = after_major.getSelectedItem().toString();

        HashMap<String, String> major_json = new HashMap<>();
        major_json.put("user_major", selectedMajor);

        JSONObject parameter = new JSONObject(major_json);

        String url = "http://3.37.68.242:8000/update/user/";

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = sharedPreferences.getString("access_token", "null");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH,
                url,
                parameter,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "전공 수정 성공");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "전공 수정 실패");
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
}