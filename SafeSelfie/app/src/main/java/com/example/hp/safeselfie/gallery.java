package com.example.hp.safeselfie;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class gallery extends AppCompatActivity {

    Button btn1, btn2, btn3;
    private int PICK_IMAGE_REQUEST = 1;
    Uri imageUri;
    String file, file1;
    int a;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.gallery);
        btn1 = findViewById(R.id.button7);
        btn2 = findViewById(R.id.button8);
        btn3 = findViewById(R.id.button9);

        a = 0;
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(gallery.this, fore.class);
//                startActivityForResult(i, 1);

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Picture"), PICK_IMAGE_REQUEST);

            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(gallery.this, fore.class);
//                startActivityForResult(i, 1);

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Picture"), PICK_IMAGE_REQUEST);

            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("Image file is "+file);
                System.out.println("Image file1 is "+file1);

                Intent myintent = new Intent(gallery.this,fore.class);
                myintent.putExtra("FileName", file);
                myintent.putExtra("FileName1", file1);
                startActivity(myintent);
            }
        });



    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            imageUri = data.getData();
            if(a == 0) {
                file = imageUri.getPath();
                file = getRealPathFromURI(imageUri);
                a = 1;
            }
            else {
                file1 = imageUri.getPath();
                file1 = getRealPathFromURI(imageUri);
//                /storage/emulated/0/8144c78b-3d36-47b2-b48d-26fff37b048c.jpg
            }

        }else {
//            Toast.makeText(PostImage.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
            int aa = 1;
        }
    }

    public String getRealPathFromURI(Uri uri) {
        String realPath="";
        String wholeID = DocumentsContract.getDocumentId(uri);
        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];
        String[] column = { MediaStore.Images.Media.DATA };
        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{ id }, null);
        int columnIndex = 0;
        if (cursor != null) {
            columnIndex = cursor.getColumnIndex(column[0]);
            if (cursor.moveToFirst()) {
                realPath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return realPath;
    }

}
