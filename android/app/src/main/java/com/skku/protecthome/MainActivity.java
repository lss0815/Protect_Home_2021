package com.skku.protecthome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ListViewAdapter adapter;
    private static Handler handler;

    StorageReference mStorageRef;

    String logTag = "LSS_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context myContext = this.getApplicationContext();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        adapter = new ListViewAdapter();

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListViewItem item = adapter.getItem(position);
                Intent detectionPageIntent = new Intent(MainActivity.this, DetectionPage.class);
                detectionPageIntent.putExtra("DetectionName", item.getDate());
                startActivity(detectionPageIntent);
            }
        });


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mStorageRef.listAll()
                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                adapter.clear();
                                ListViewItem item = new ListViewItem();

                                for (StorageReference prefix : listResult.getPrefixes()){
                                    String prefixStr = prefix.toString();
                                    String detectionNameStr = prefixStr.substring(prefixStr.length()-15, prefixStr.length());

                                    item.setDate(detectionNameStr);
                                    adapter.addItem(item);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(myContext, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
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