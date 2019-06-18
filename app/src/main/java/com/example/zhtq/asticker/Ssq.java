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
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class Ssq extends AppCompatActivity {
    private static final String TAG = "SsqActivity";

    private AppCompatCheckBox mSpecialChecker;
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
        mSpecialChecker = findViewById(R.id.special_check1);
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
               boolean checked = mSpecialChecker.isChecked();
               if (checked) {
                   generatezSequently(count);
               } else {
                   generate(count);
               }
            }
        });
    }

    private void generate(final int count) {
        final SsqAlgo algo = new SsqAlgo();
        Disposable disposable = Observable.just(count)
                .map(new Function<Integer, List<byte[]>>() {
                    @Override
                    public List<byte[]> apply(Integer integer) throws Exception {
                        LogUtil.d(TAG, "map, count:%s.", integer);
                        List<byte[]> stringList = new ArrayList<>();
                        for (int i = 0; i < integer; i++) {
                            byte[] results = new byte[7];
                            algo.generate2(results);
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

    private int mGenCount;
    private void generatezSequently(final int count) {
        mAdapter.clear();
        final SsqAlgo algo = new SsqAlgo();
        mGenCount = 0;
        Disposable disposable = Observable.just(count)
                .flatMap(new Function<Integer, ObservableSource<byte[]>>() {
                     @Override
                     public ObservableSource<byte[]> apply(Integer integer) throws Exception {
                         LogUtil.d(TAG, "flatMap, count:%s.", integer);
                         List<byte[]> stringList = new ArrayList<>();
                         for (int i = 0; i < integer; i++) {
                             byte[] results = new byte[7];
                             algo.generate2(results);
                             stringList.add(results);
                         }
                         return Observable.fromIterable(stringList);
                     }
                 }
//                ).interval(1, 1, java.util.concurrent.TimeUnit.SECONDS
                ).observeOn(AndroidSchedulers.mainThread()
                ).subscribeOn(Schedulers.io()
                ).subscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] o) throws Exception {
                        LogUtil.d(TAG, "generate:%s.", o);
                        final byte[] data = o;
                        mRcv.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.add(data);
                            }
                        }, 100 * mGenCount++);

                    }
                });
        mDisposable.add(disposable);
    }

}
