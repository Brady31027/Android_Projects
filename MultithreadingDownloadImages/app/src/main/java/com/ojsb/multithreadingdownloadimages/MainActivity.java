package com.ojsb.multithreadingdownloadimages;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private TextView myTextView;
    private Button myButton;
    private ListView myListView;
    private List<ListContent> myData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myTextView = (TextView) findViewById(R.id.tvURL);
        myButton = (Button) findViewById(R.id.btnDownload);
        myListView = (ListView) findViewById(R.id.lvURL);

        myData = new ArrayList<ListContent>();
        myData.add( new ListContent("Google Logo 1", "https://www.google.com/logos/doodles/2017/bangladesh-independence-day-2017-5697900649644032-hp2x.png"));
        myData.add( new ListContent("Google Logo 2", "https://www.google.com/logos/doodles/2017/childrens-day-2017-taiwan-6303352519393280-hp2x.png"));
        myData.add( new ListContent("Google Logo 3", "https://www.google.com/logos/doodles/2017/misuzu-kanekos-114th-birthday-6343326507728896-2x.jpg"));
        myData.add( new ListContent("Google Logo 4", "https://www.google.com/logos/doodles/2017/finland-municipal-elections-2017-6301478309330944-2x.jpg"));
        myData.add( new ListContent("Google Logo 5", "https://www.google.com/logos/doodles/2017/jamini-roys-130th-birthday-5130800180756480.2-2x.jpg"));
        myListView.setAdapter( new CustomListViewAdapter( MainActivity.this, myData));

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = myData.get(position).subtitle;
                myTextView.setText(Uri.parse(url).getLastPathSegment());
            }
        });

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = myTextView.getText().toString();
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "Select an image to download first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
