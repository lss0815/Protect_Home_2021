package com.skku.protecthome;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ListViewAdapter adapter;
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new ListViewAdapter();

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                ListViewItem item = new ListViewItem();
                Date date = new Date();
                item.setDate(dateformat.format(date));

                adapter.addItem(item);

                adapter.notifyDataSetChanged();
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                        handler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

}