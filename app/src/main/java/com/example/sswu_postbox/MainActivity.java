package com.example.sswu_postbox;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    Button signIn, signUp;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn = findViewById(R.id.signIn);
        signUp = findViewById(R.id.signUp);


        signIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        signIn.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    signIn.setBackgroundColor(Color.TRANSPARENT);
                    signIn.setTextColor(Color.BLACK);

                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    signIn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.main_btn));
                    signIn.setTextColor(Color.WHITE);

            }

                return false;
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SignUpActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        signUp.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    signUp.setBackgroundColor(Color.TRANSPARENT);
                    signUp.setTextColor(Color.BLACK);

                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    signUp.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.main_btn));
                    signUp.setTextColor(Color.WHITE);
                }
                return false;
            }
        });
    }
}