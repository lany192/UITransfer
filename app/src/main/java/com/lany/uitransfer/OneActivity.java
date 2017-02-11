package com.lany.uitransfer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.lany.uitransfer.annotaion.TransferField;
import com.lany.uitransfer.annotaion.TransferTarget;

@TransferTarget
public class OneActivity extends AppCompatActivity {
    @TransferField("MY_DATA")
    String mydata;

    private TextView showText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one);
        Bundle bundle = this.getIntent().getExtras();
        mydata = bundle.getString("MY_DATA");
        showText = (TextView) findViewById(R.id.my_text_view);
        showText.setText(mydata + "");
    }
}
