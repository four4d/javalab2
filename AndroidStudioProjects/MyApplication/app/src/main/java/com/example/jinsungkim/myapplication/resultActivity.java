package com.example.jinsungkim.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class resultActivity extends AppCompatActivity {

    // 레이아웃 객체
    TextView mTvResult;
    Button mBtReturn;

    ArrayList<Question> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();

        mTvResult = (Button) findViewById(R.id.bt_result);
        //인텐트로 넘겨받은 결과 값 받아서 TextView에 입력.
        mTvResult.setText(intent.getStringExtra("result"));

        // 돌아가기 버튼 클릭시 현재 액티비티 종료하여 이전 화면으로.
        mBtReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
