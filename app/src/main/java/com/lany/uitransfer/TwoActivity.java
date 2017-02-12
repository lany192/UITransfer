package com.lany.uitransfer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lany.uitransfer.annotaion.TransferField;
import com.lany.uitransfer.annotaion.TransferTarget;

@TransferTarget
public class TwoActivity extends AppCompatActivity {
    @TransferField
    int demoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);
    }
}
