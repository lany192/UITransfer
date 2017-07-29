package com.lany.uitransfer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.lany192.Transfer;
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
        Transfer.inject(this);
        TextView showText = (TextView) findViewById(R.id.textView);
        showText.setText("demoData==" + demoData + "  isShow==" + isShow);
    }
}
