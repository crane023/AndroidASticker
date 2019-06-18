package com.example.zhtq.asticker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.zhtq.asticker.data.MainContentHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRcv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initData();
    }

    private void initViews() {
        mRcv = findViewById(R.id.content_list_view);
        mRcv.setLayoutManager(new GridLayoutManager(this, 3));
//        mRcv.setAdapter();
    }

    private void initData() {
        MainContentHelper helper = new MainContentHelper();
        ArrayList<String> content = helper.getContent(MainContentHelper.TYPE_DEFAULT);

    }
}
