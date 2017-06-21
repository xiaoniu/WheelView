package com.lg.wheelviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class WheelActivity extends AppCompatActivity {
    private PickerView pickerView;
    private List<String> mDatas;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel);
        pickerView = (PickerView) findViewById(R.id.pickerview);
        mDatas=new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            mDatas.add(""+i);
        }
        pickerView.setData(mDatas);
        pickerView.setSelected(8);
    }

    public void onClick(View view){
        pickerView.moveToHead();
    }
}
