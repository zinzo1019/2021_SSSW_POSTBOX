package com.example.sswu_postbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PostClickActivity extends AppCompatActivity {
    String TAG = PostClickActivity.class.getSimpleName();

    private WebView webview;

    ImageButton save_btn;
    TextView webView_title;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_click);


        Intent intent = getIntent();
        String titleText = intent.getStringExtra("webView_title");
        String dateText = intent.getStringExtra("webView_date");
        Boolean store_state = intent.getBooleanExtra("store_state", false);
        String url = intent.getStringExtra("url");

        webView_title = findViewById(R.id.webview_title);
        webView_title.setText(titleText);


        webview = (WebView) findViewById(R.id.webview);
        webview.loadUrl(url);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webview.setWebChromeClient(new WebChromeClient());

        //공유 기능
        ImageButton webview_share_btn = findViewById(R.id.webview_share_btn);
        webview_share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                Sharing_intent.setType("text/plain");

                String Test_Message = "["+titleText+"]" + "  " + url;
                Sharing_intent.putExtra(Intent.EXTRA_TEXT, Test_Message);

                Intent Sharing =Intent.createChooser(Sharing_intent, "공유하기");
                startActivity(Sharing);
            }
        });

        save_btn = findViewById(R.id.webview_save_btn);
        save_btn.setSelected(store_state);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());

                if (v.isSelected()) {
                    store_modify(webView_title.getText().toString(), v.isSelected());
                }
                else{
                    store_modify(webView_title.getText().toString(), v.isSelected());
                }
            }
        });


        ImageButton activity_post_click_close_btn = findViewById(R.id.activity_post_click_close_btn);
        activity_post_click_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    void store_modify(String title, boolean state) {
        // 보관, 보관 취소
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
                        if (state) Toast.makeText(getApplicationContext(), "보관함 저장", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(getApplicationContext(), "보관함 저장 취소", Toast.LENGTH_SHORT).show();
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

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    Map<String, String> give_token(String token) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        return headers;
    }

}

