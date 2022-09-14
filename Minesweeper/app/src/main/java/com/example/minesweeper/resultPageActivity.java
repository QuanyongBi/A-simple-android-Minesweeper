package com.example.minesweeper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class resultPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_page);
        Intent intent = getIntent();
        String message = intent.getStringExtra("msg");
        TextView button_tv = (TextView) findViewById(R.id.return_button);
        button_tv.setOnClickListener(this::onClickReturn);

        TextView textView = (TextView) findViewById(R.id.result_text);
        textView.setText(message);
    }

    public void onClickReturn (View view){
        Intent intent = new Intent(this, loadingPageActivity.class);
        startActivity(intent);
    }
}
