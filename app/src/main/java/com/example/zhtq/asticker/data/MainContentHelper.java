package com.example.zhtq.asticker.data;

import android.util.SparseArray;

import java.util.ArrayList;

public class MainContentHelper {
    public static final int TYPE_DEFAULT = 1;

    public static final String CONTENT_SSQ = "com.ex.zh.ssq";
    private SparseArray<ArrayList<String>> mContent;

    public MainContentHelper() {
        init();
    }

    private void init() {
        if (mContent == null) {
            mContent = new SparseArray<>();
        } else {
            mContent.clear();
        }

        ArrayList<String> defaultList = new ArrayList<>();
        defaultList.add(CONTENT_SSQ);
        mContent.put(TYPE_DEFAULT, defaultList);
    }

    public ArrayList<String> getContent(int type) {
        return mContent.get(type);
    }
}
