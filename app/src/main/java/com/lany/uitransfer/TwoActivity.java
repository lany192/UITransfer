package com.lany.uitransfer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.lany192.Transfer;
import com.lany.uitransfer.annotaion.IntentExtra;

public class TwoActivity extends AppCompatActivity {
    @IntentExtra("myData")
    int demoData;
    @IntentExtra("isShow")
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
