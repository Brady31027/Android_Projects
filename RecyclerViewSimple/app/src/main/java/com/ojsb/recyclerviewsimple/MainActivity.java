package com.ojsb.recyclerviewsimple;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SimpleAdapter;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class MainActivity extends Activity {

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView)findViewById(R.id.rv_main);


        String [] skills = {"Android", "Swift", "Computer Graphics", "Computer Vision", "Python"};

        mRecyclerView.setLayoutManager( new LinearLayoutManager(this));
        mAdapter = new RecyclerViewAdapter(this, skills);
        mRecyclerView.setAdapter(mAdapter);

    }
}
