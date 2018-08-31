package com.example.hp.safeselfie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class zeroActivity extends AppCompatActivity {

    Button btn1, btn2, btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zero_activity);
        btn1 = findViewById(R.id.button4);
        btn2 = findViewById(R.id.button5);
        btn3 = findViewById(R.id.button6);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myintent = new Intent(zeroActivity.this,MainActivity.class);
                startActivity(myintent);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myintent = new Intent(zeroActivity.this,gallery.class);
                startActivity(myintent);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myintent = new Intent(zeroActivity.this,impose.class);
                startActivity(myintent);
            }
        });
    }

}
