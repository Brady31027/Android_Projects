package com.ojsb.multithreadingdownloadimages;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by brady on 2017/4/17.
 */


class CustomListViewAdapter extends ArrayAdapter<ListContent> {

    public CustomListViewAdapter(Context context, List<ListContent> desc) {
        super(context, R.layout.custom_listviw, desc);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        LayoutInflater inflater = LayoutInflater.from( getContext());
        v = inflater.inflate(R.layout.custom_listviw, parent, false);

        ImageView myImageView = (ImageView)v.findViewById(R.id.myImageView);
        TextView myTitle = (TextView)v.findViewById(R.id.myListTitle);
        TextView mySubTitle = (TextView)v.findViewById(R.id.myListSubtitle);

        myImageView.setImageResource(R.drawable.myicon);
        myTitle.setText( getItem(position).title );
        mySubTitle.setText( getItem(position).subtitle );

        return v;
    }
}
