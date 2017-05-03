package com.ojsb.recyclerviewsimple;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by brady on 2017/5/2.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter <RecyclerViewAdapter.TextViewHolder> {

    public static final int LAST_POSITION = -1 ;
    private final Context mContext;
    private String[] mSkills;
    private final LayoutInflater mLayoutInflater;

    public RecyclerViewAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mSkills = context.getResources().getStringArray(R.array.skills);
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //final View view = LayoutInflater.from(mContext).inflate(R.layout.row, parent, false);
        return new TextViewHolder(mLayoutInflater.inflate(R.layout.row, parent, false));
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, final int position) {
        holder.mTvRowItem.setText(mSkills[position]);
    }

    @Override
    public int getItemCount() {
        return mSkills == null? 0 : mSkills.length;
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder{
        public TextView mTvRowItem;
        public TextViewHolder(View itemView) {
            super(itemView);
            mTvRowItem = (TextView) itemView.findViewById(R.id.tv_rowitem);
        }
    }
}
