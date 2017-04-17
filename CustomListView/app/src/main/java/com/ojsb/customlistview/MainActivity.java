package com.ojsb.customlistview;

import android.icu.lang.UCharacter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    private ListView myListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myListView = (ListView) findViewById(R.id.myListView);
        String[] items = {"item1", "item2", "item3"};
        ListAdapter myAdapter = new CustomListAdapter(MainActivity.this, items);
        myListView.setAdapter(myAdapter);
    }
}
