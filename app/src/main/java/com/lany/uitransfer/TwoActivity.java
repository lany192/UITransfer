package com.lany.uitransfer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.lany192.UIHelper;
import com.lany.uitransfer.annotaion.RequestParam;

public class TwoActivity extends AppCompatActivity {
    @RequestParam("myData")
    int demoData;
    @RequestParam("isShow")
    boolean isShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);
        UIHelper.bind(this);
        TextView showText = (TextView) findViewById(R.id.textView);
        showText.setText("demoData==" + demoData + "  isShow==" + isShow);
    }
}
