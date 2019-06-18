package com.example.zhtq.asticker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.example.zhtq.asticker.algo.SsqAlgo;
import com.example.zhtq.asticker.utils.LogUtil;
import com.example.zhtq.asticker.widgets.CustomSimpleAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class Ssq extends AppCompatActivity {
    private static final String TAG = "SsqActivity";

    private AppCompatCheckBox mRedRepeatChecker;
    private AppCompatCheckBox mBlueUniqueChecker;
    private EditText mInputEt;
    private RecyclerView mRcv;

    private CustomSimpleAdapter mAdapter;

    private CompositeDisposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssq);

        initPreBasics();
        initViews();
    }

    private void initPreBasics() {
        mAdapter = new CustomSimpleAdapter();
        mDisposable = new CompositeDisposable();
    }

    private void initViews() {
        mRedRepeatChecker = findViewById(R.id.red_repeat_checker);
        mBlueUniqueChecker = findViewById(R.id.blue_unique_checker);
        mInputEt = findViewById(R.id.edit_parameter);
        mRcv = findViewById(R.id.generate_result);
        mRcv.setLayoutManager(new LinearLayoutManager(this));
        mRcv.setAdapter(mAdapter);

        findViewById(R.id.generate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String para = mInputEt.getText().toString();
               if (TextUtils.isEmpty(para) || !TextUtils.isDigitsOnly(para)) {
                   para = "1";
               }
               int count = Integer.parseInt(para);
               generateAtInterval(count, mRedRepeatChecker.isChecked(), mBlueUniqueChecker.isChecked());
            }
        });
    }

    private void generate(final int count, final boolean redRepeat, final boolean blueUnique) {
        final SsqAlgo algo = new SsqAlgo();
        Disposable disposable = Observable.just(count)
                .map(new Function<Integer, List<byte[]>>() {
                    @Override
                    public List<byte[]> apply(Integer integer) throws Exception {
                        LogUtil.d(TAG, "map, count:%s.", integer);
                        List<byte[]> stringList = new ArrayList<>();
                        for (int i = 0; i < integer; i++) {
                            byte[] results = new byte[7];
                            algo.generate2(results, !redRepeat, blueUnique);
                            stringList.add(results);
                        }
                        return stringList;
                    }
                }).observeOn(AndroidSchedulers.mainThread()
                ).subscribeOn(Schedulers.io()
                ).subscribe(new Consumer<List<byte[]>>() {
                    @Override
                    public void accept(List<byte[]> o) throws Exception {
                        LogUtil.i(TAG, "onComplete:%d", o.size());
                        mAdapter.setData(o);
                    }
                });
        mDisposable.add(disposable);
    }

    private void generateAtInterval(final int count, final boolean redRepeat, final boolean blueUnique) {
        mAdapter.clear();
        final SsqAlgo algo = new SsqAlgo();
        Disposable disposable = Observable.intervalRange(1, count, 20, 300, TimeUnit.MILLISECONDS)
                .map(new Function<Long, byte[]>() {
                             @Override
                             public byte[] apply(Long value) throws Exception {
                                 LogUtil.d(TAG, "flatMap, at interval:%d.", value);
                                 byte[] results = new byte[7];
                                 algo.generate2(results, !redRepeat, blueUnique);
                                 return results;
                             }
                         }
                ).observeOn(AndroidSchedulers.mainThread()
                ).subscribeOn(Schedulers.io()
                ).subscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] o) throws Exception {
                        LogUtil.d(TAG, "generate:%s.", Arrays.toString(o));
                        mAdapter.add(o);
                    }
                });
        mDisposable.add(disposable);
    }
}
