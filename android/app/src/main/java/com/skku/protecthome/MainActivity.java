package com.skku.protecthome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ListViewAdapter adapter;
    private static Handler handler;

    StorageReference mStorageRef;
    ArrayList<String> currentDetections;

    String logTag = "LSS_DEBUG";
    int notificationCnt = 1;

    String CHANNEL_ID = "ProtectHome";
    String CHANNEL_NAME = "ProtectHome";
    String CHANNEL_DESCRIPTION = "Check Invasion to House";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context myContext = this.getApplicationContext();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        adapter = new ListViewAdapter();
        currentDetections = new ArrayList<String>();

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.warning)
                .setContentTitle("Protect Home")
                .setContentText("Default Context")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);


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

        createNotificationChannel();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ArrayList<String> tempStringArrayList = new ArrayList<String>();
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
                                    tempStringArrayList.add(detectionNameStr);
                                }
                                adapter.notifyDataSetChanged();

                                int diffCnt = 0;
                                String lastString = "";
                                for(int i=0; i<tempStringArrayList.size(); i++){
                                    boolean found = false;
                                    for(int j=0; j<currentDetections.size(); j++){
                                        Log.d(logTag, currentDetections.get(j));
                                        Log.d(logTag, tempStringArrayList.get(i));
                                        if(currentDetections.get(j).equals(tempStringArrayList.get(i))){
                                            found = true;
                                            break;
                                        }
                                    }
                                    if(found == false) {
                                        diffCnt += 1;
                                        currentDetections.add(tempStringArrayList.get(i));
                                        lastString = tempStringArrayList.get(i);
                                    }
                                }
                                if(diffCnt == 1){
                                    builder.setContentText(lastString + " invasion has been occurred");
                                    notificationManager.from(myContext).notify(notificationCnt, builder.build());
                                    notificationCnt += 1;
                                } else if(diffCnt > 1){
                                    builder.setContentText("Until " + lastString + " " + Integer.toString(diffCnt) + " invasions have been occurred");
                                    notificationManager.from(myContext).notify(notificationCnt, builder.build());
                                    notificationCnt += 1;
                                }
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_NAME;
            String description = CHANNEL_DESCRIPTION;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}