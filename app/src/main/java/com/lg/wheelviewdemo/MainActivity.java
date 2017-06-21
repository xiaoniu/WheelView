package com.lg.wheelviewdemo;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private PickerView pickerView;
    private PickerView pickerView2;
    private List<String> mDatas;

    private Timer timer;
    private RefreshTimeTask mTask;

    Handler refreshHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            if (pickerView2.getSelected() < mDatas.size() - 1) {
                pickerView2.next();
            } else {
                pickerView2.moveToHead();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickerView = (PickerView) findViewById(R.id.pickerview);
        pickerView2 = (PickerView) findViewById(R.id.pickerview2);
        mDatas=new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            mDatas.add(""+i);
        }
        pickerView.setData(mDatas);
        pickerView2.setData(mDatas);
        pickerView.setSelected(0);
        pickerView2.setSelected(0);

        timer = new Timer();
        mTask = new RefreshTimeTask(refreshHandler);
        timer.schedule(mTask,1000,1000);
    }

    public void onClick(View view){
        if(mTask!=null){
            mTask.cancel();
            mTask = null;
        }

        pickerView.next();
        pickerView2.moveToHead();

        mTask = new RefreshTimeTask(refreshHandler);
        timer.schedule(mTask,1000,1000);
    }

    class RefreshTimeTask extends TimerTask{
        Handler handler;

        public RefreshTimeTask(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage());
        }
    }
}
