package com.example.zhtq.asticker;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.example.zhtq.asticker.algo.SsqAlgo;
import com.example.zhtq.asticker.utils.FileUtils;
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
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.example.zhtq.asticker.utils.FileUtils.getFileNameByTime;
import static com.example.zhtq.asticker.utils.SizeUtils.dp2px;

public class Ssq extends AppCompatActivity {
    private static final String TAG = "SsqActivity";

    private AppCompatCheckBox mSortCancelChecker;
    private AppCompatCheckBox mMannualCaptureChecker;
    private EditText mInputEt;
    private RecyclerView mRcv;
    private Button mCaptureBtn;
    private Button mGenBtn;

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

    private void enableCaptureBtn(boolean enable) {
        mCaptureBtn.setEnabled(enable);
        mCaptureBtn.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
    }

    private void initViews() {
        mSortCancelChecker = findViewById(R.id.red_repeat_checker);
        mMannualCaptureChecker = findViewById(R.id.blue_unique_checker);
        mInputEt = findViewById(R.id.edit_parameter);
        mRcv = findViewById(R.id.generate_result);
        mRcv.setLayoutManager(new LinearLayoutManager(this));
        mRcv.setAdapter(mAdapter);
        mCaptureBtn = findViewById(R.id.capture_and_save);
        mGenBtn = findViewById(R.id.generate);

        enableCaptureBtn(mMannualCaptureChecker.isChecked());

        mMannualCaptureChecker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableCaptureBtn(isChecked);
            }
        });

        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureAndClear();
            }
        });

        mGenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String para = mInputEt.getText().toString();
               if (TextUtils.isEmpty(para) || !TextUtils.isDigitsOnly(para)) {
                   para = "1";
               }
               int count = Integer.parseInt(para);
               generateAtInterval(count, !mSortCancelChecker.isChecked(), !mMannualCaptureChecker.isChecked());
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
                            algo.generate2(results, true, false);
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

    private void generateAtInterval(final int count, final boolean sort, final boolean autoCapture) {
        mAdapter.clear();
        final SsqAlgo algo = new SsqAlgo();
        Disposable disposable = Observable.intervalRange(1, count, 20, 300, TimeUnit.MILLISECONDS)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        LogUtil.d(TAG, "onSubscribe");
                        mGenBtn.setEnabled(false);
                    }
                }).observeOn(Schedulers.io())
                .map(new Function<Long, byte[]>() {
                             @Override
                             public byte[] apply(Long value) throws Exception {
                                 LogUtil.v(TAG, "flatMap, at interval:%d.", value);
                                 byte[] results = new byte[7];
                                 algo.generate2(results, true, false);
                                 if (sort) {
                                     Arrays.sort(results, 0, results.length - 1);
                                 }
                                 return results;
                             }
                         }
                ).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) throws Exception {
                        LogUtil.v(TAG, "onNext:%s", Arrays.toString(bytes));
                        mAdapter.add(bytes);
                    }
                }).observeOn(Schedulers.io())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        LogUtil.d(TAG, "onComplete:%b!", autoCapture);
                        if (autoCapture) {
                            captureAndClear();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mGenBtn.setEnabled(true);
                            }
                        });
                    }
                }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
        mDisposable.add(disposable);
    }

    @SuppressLint("NewApi")
    private void capture2() {
        Window window = getWindow();
        Bitmap bitmap = Bitmap.createBitmap(mRcv.getWidth(), mRcv.getHeight(), Bitmap.Config.ARGB_8888);
        PixelCopy.request(getWindow(), bitmap, new PixelCopy.OnPixelCopyFinishedListener() {
            @Override
            public void onPixelCopyFinished(int copyResult) {

            }
        }, new Handler(Looper.getMainLooper()));
    }

    private void captureAndClear() {
        mRcv.setDrawingCacheEnabled(true);
        mRcv.buildDrawingCache();
//        Bitmap bitmap = Bitmap.createBitmap(mRcv.getDrawingCache()/*, 0, 0, mRcv.getWidth(), mRcv.getHeight()*/);
        final int textSize = getResources().getDimensionPixelSize(R.dimen.result_rcv_text_size);
        Bitmap bitmap = Bitmap.createBitmap(mRcv.getWidth(), mRcv.getHeight() + (int)(textSize * 1.5f), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        String fileName = getFileNameByTime(-1, false);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(mRcv.getDrawingCache(), dp2px(16), (int)(textSize * 1.5f), paint);
        canvas.drawText(fileName, dp2px(16), textSize * 1.2f, paint);
        mRcv.destroyDrawingCache();
        String path = FileUtils.saveJpeg2ExternalStorage(bitmap, FileUtils.ROOT_DIR, fileName.concat(".jpg"));
        LogUtil.d(TAG, "the result is saved:%s; size:%d.", path, bitmap.getAllocationByteCount());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int[] pos = getRecyclerViewLastPosition();
                LogUtil.d(TAG, "captured, pos:%s", Arrays.toString(pos));
//                mAdapter.clear();
            }
        });
    }


    public int[] getRecyclerViewLastPosition() {

        if (mRcv.getLayoutManager() == null || !(mRcv.getLayoutManager() instanceof LinearLayoutManager)) {
            return null;
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager)mRcv.getLayoutManager();
        int[] pos = new int[3];
        pos[0] = layoutManager.findFirstCompletelyVisibleItemPosition();
        pos[1] = layoutManager.findLastCompletelyVisibleItemPosition();

        OrientationHelper orientationHelper = OrientationHelper.createOrientationHelper(layoutManager, OrientationHelper.VERTICAL);
        int fromIndex = 0;
        int toIndex = mAdapter.getItemCount();
        final int start = orientationHelper.getStartAfterPadding();
        final int end = orientationHelper.getEndAfterPadding();
        final int next = toIndex > fromIndex ? 1 : -1;
        for (int i = fromIndex; i != toIndex; i += next) {
            final View child = mRcv.getChildAt(i);
            final int childStart = orientationHelper.getDecoratedStart(child);
            final int childEnd = orientationHelper.getDecoratedEnd(child);
            if (childStart < end && childEnd > start) {
                if (childStart >= start && childEnd <= end) {
                    pos[1] = childStart;
                    LogUtil.v(TAG, "position = " + pos[0] + " off = " + pos[1]);
                    return pos;
                }
            }
        }
        return pos;
    }
}
