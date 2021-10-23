package com.example.sswu_postbox;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    String TAG = LoginActivity.class.getSimpleName();

    Button go_sign_up, sign_in;
    EditText id, password;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        go_sign_up = findViewById(R.id.signup_btn);
        sign_in = findViewById(R.id.login_btn);

        go_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();

                FirebaseMessaging.getInstance().subscribeToTopic("test")
                        .addOnCompleteListener( task -> {
                            if (task.isComplete()) Log.d(TAG, "구독 성공");
                            else Log.d(TAG, "구독 실패");
                        });
            }
        });


        sign_in.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sign_in.setBackgroundColor(Color.TRANSPARENT);
                    sign_in.setTextColor(Color.WHITE);

                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    sign_in.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.crystal_blue));
                    sign_in.setTextColor(Color.BLACK);
                }

                return false;
            }
        });

        sign_in.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sign_in.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.crystal_blue));
                sign_in.setTextColor(Color.BLACK);
                return false;
            }
        });

    }

    void login() {
        // 로그인하고 jwt token 받아서 저장하기
        String url = "http://3.37.68.242:8000/login/";

        id = findViewById(R.id.login_id_edit);
        password = findViewById(R.id.login_pwd_edit);

        HashMap<String, String> login_json = new HashMap<>();
        login_json.put("username", id.getText().toString());
        login_json.put("password", password.getText().toString());

        JSONObject parameter = new JSONObject(login_json);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url,
                parameter,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(i);

                        try {
                            String access_token = response.getString("access");

                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            Log.d(TAG, "access_token : " + access_token);

                            editor.putString("access_token", access_token).apply();
                            Log.d(TAG, "Shared test : " + sharedPreferences.getString("access_token", "null"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Toast toast = Toast.makeText(getApplicationContext(), "환영합니다.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(getApplicationContext(), "로그인에 실패했습니다.\n잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG);
                        toast.show();

                        error.printStackTrace();
                        Log.d(TAG, "login fail");
                    }
                });


        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}