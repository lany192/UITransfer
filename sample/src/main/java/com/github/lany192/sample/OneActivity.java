package com.github.lany192.sample;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.lany192.transfer.UIHelper;
import com.github.lany192.transfer.annotaion.RequestParam;


public class OneActivity extends AppCompatActivity {
    @RequestParam("name")
    String mName;
    @RequestParam
    int age;

    private TextView showText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one);

        UIHelper.bind(this);

        showText = (TextView) findViewById(R.id.my_text_view);
        showText.setText(mName + "  " + age);
    }
}
