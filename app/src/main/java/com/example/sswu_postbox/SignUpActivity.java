package com.example.sswu_postbox;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    String TAG = SignUpActivity.class.getSimpleName();

    EditText id, password, password2;
    Button sign_up;

    Spinner major;
    String selectedMajor;

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
        setContentView(R.layout.activity_sign_up);

        sign_up = findViewById(R.id.signup_check_btn);

        major = findViewById(R.id.signup_major_edit);

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sign_up();
            }
        });

        sign_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch( View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sign_up.setBackgroundColor(Color.TRANSPARENT);
                    sign_up.setTextColor(Color.WHITE);

                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    sign_up.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.crystal_blue));
                    sign_up.setTextColor(Color.BLACK);
                }
                return false;
            }
        });

        sign_up.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sign_up.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.crystal_blue));
                sign_up.setTextColor(Color.BLACK);
                return false;
            }
        });

    }

    void sign_up() {
        // 회원가입
        String url = "http://3.37.68.242:8000/users/";

        id = findViewById(R.id.signup_id_edit);
        password = findViewById(R.id.signup_pwd_edit);
        password2 = findViewById(R.id.signup_pwd2_edit);
        selectedMajor = major.getSelectedItem().toString();
        Log.d(TAG, selectedMajor);

        HashMap<String, String> signup_json = new HashMap<>();
        signup_json.put("username", id.getText().toString());
        signup_json.put("password", password.getText().toString());
        signup_json.put("password2", password2.getText().toString());
        signup_json.put("user_major", selectedMajor);
        signup_json.put("user_major2", "");
        signup_json.put("user_major3", "");
        signup_json.put("user", null);

        JSONObject parameter = new JSONObject(signup_json);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url,
                parameter,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast toast = Toast.makeText(getApplicationContext(), "수룡이의 우편함에 오신 것을 환영합니다!", Toast.LENGTH_LONG);
                        toast.show();

                        Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(i);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(getApplicationContext(), "회원가입에 실패했습니다.\n잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG);
                        toast.show();

                        error.printStackTrace();
                        Log.d(TAG, "sign up fail");
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}