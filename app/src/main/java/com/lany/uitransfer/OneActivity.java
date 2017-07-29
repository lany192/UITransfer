package com.lany.uitransfer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.lany192.Transfer;
import com.lany.uitransfer.annotaion.RequestParam;

public class OneActivity extends AppCompatActivity {
    @RequestParam
    String name;
    @RequestParam
    int age;

    private TextView showText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one);

        Transfer.inject(this);

        showText = (TextView) findViewById(R.id.my_text_view);
        showText.setText(name + "  " + age);
    }
}
