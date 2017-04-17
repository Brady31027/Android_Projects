package com.ojsb.customlistview;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by brady on 2017/4/17.
 */

class CustomListAdapter extends ArrayAdapter<String> {

    public CustomListAdapter(Context context, String[] items) {
        super(context, R.layout.custom_row, items);
    }

    @Override
    public View getView(int pos, View v, ViewGroup parent) {
        LayoutInflater myInflater = LayoutInflater.from(getContext());
        View customView = myInflater.inflate(R.layout.custom_row, parent, false);
        String itemTitle = getItem(pos);
        TextView myTitle = (TextView) customView.findViewById(R.id.myItem);
        ImageView myImage = (ImageView) customView.findViewById(R.id.myImage);
        myTitle.setText(itemTitle);
        myImage.setImageResource(R.drawable.myicon);
        return customView;
    }
}
