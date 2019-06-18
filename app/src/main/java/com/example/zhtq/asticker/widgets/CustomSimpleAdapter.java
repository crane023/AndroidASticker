package com.example.zhtq.asticker.widgets;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.zhtq.asticker.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomSimpleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = CustomSimpleAdapter.class.getSimpleName();
    private List<byte[]> mData;

    public void setData(List<byte[]> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void add(byte[] data) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData.add(data);
        notifyItemInserted(mData.size() - 1);
    }

    public void clear() {
        if (mData != null) {
            mData.clear();

        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        TextView textView = new TextView(viewGroup.getContext());
        return new ViewHolder2(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof ViewHolder2) {
            byte[] itemData = mData.get(i);
            display(viewHolder, itemData);
        }
    }

    private void display(RecyclerView.ViewHolder viewHolder, byte[] itemData) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        final String separator = "   ";
        for (byte b : itemData) {
            builder.append(Byte.toString(b)).append(separator);
        }

        int lastDigitLength = Byte.toString(itemData[itemData.length - 1]).length();
        LogUtil.d(TAG, "display, raw size:%d; processed:%d; last digit length:%d."
                , itemData.length, builder.length(), lastDigitLength);
        builder.setSpan(new ForegroundColorSpan(Color.RED), 0
                , builder.length() - separator.length() - lastDigitLength
                , Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(Color.BLUE), builder.length() - separator.length() - lastDigitLength
                , builder.length() - separator.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ViewHolder2 holder2 = (ViewHolder2) viewHolder;

        holder2.textView.setText(builder.subSequence(0, builder.length() - separator.length()));
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    class ViewHolder2 extends RecyclerView.ViewHolder {
        private TextView textView;

        public ViewHolder2(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
