package com.skku.protecthome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

public class DetectionPage extends AppCompatActivity {
    String logTag = "LSS_DEBUG";
    String detectionTimeStr;
    GridView gridView;

    TextView titleTV;
    StorageReference mStorageRef;

    ImageView imageView;
    Button playButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detection_page);

        Context myContext = this.getApplicationContext();

        Intent curIntent = getIntent();
        detectionTimeStr = curIntent.getStringExtra("DetectionName");
        mStorageRef = FirebaseStorage.getInstance().getReference().child(detectionTimeStr);

        titleTV = (TextView) findViewById(R.id.textView);
        titleTV.setText(detectionTimeStr);

        imageView = (ImageView) findViewById(R.id.imageView);

        playButton = (Button) findViewById(R.id.playButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStorageRef.listAll()
                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                for (StorageReference item : listResult.getItems()) {
                                    item.getBytes(1024*1024).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(myContext, e.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            imageView.setImageBitmap(bitmap);
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
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
        });

        mStorageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference item : listResult.getItems()) {
                            item.getBytes(1024*1024).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(myContext, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    imageView.setImageBitmap(bitmap);
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
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
}
/*
{
        tempStorageRef.getBytes(TEN_MEGABYTE).addOnFailureListener(new OnFailureListener() {
@Override
public void onFailure(@NonNull Exception e) {

        }
        }).addOnSuccessListener(new OnSuccessListener<byte[]>() {
@Override
public void onSuccess(byte[] bytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        drawerImageView.setImageBitmap(bitmap);
        }
        });

        drawerImageView.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, 777);
        }
        });
}*/