package com.example.hp.safeselfie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;

public class seventhactivity extends AppCompatActivity {

    private ImageView imageView, imageView1;
    private GestureDetectorCompat gestureDetectorCompat;
    private long addr1, addr2;
    Bitmap segMap, origMap, simOut, histOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.seventh_activity);


        segMap = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("segment"),0,getIntent()
                        .getByteArrayExtra("segment").length);

//        segMap = getIntent().getParcelableExtra("segment");
        origMap = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("orig1"),0,getIntent()
                        .getByteArrayExtra("orig1").length);
//        long adddr4 = getIntent().getLongExtra(  "orig1",0);

        simOut = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("o1"),0,getIntent()
                        .getByteArrayExtra("o1").length);

        histOut = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("o2"),0,getIntent()
                        .getByteArrayExtra("o2").length);


        System.out.println(simOut.getWidth()+" "+simOut.getHeight()+" "+histOut.getWidth()+ " "+histOut.getHeight());

//        addr1 = getIntent().getLongExtra("o1",0);
//        addr2 = getIntent().getLongExtra("o2",0);
//        System.out.println("Addr 3 is "+adddr3+" and addr4 is "+adddr4);
//        Mat tempImg1 = new Mat(adddr3);
//        Mat tempImg2 = new Mat(adddr4);
//        Mat img1 = new Mat(addr1);
//        Mat img2 = new Mat(addr2);

//        Mat img1 = new Mat(adddr3);
//        Mat img2 = new Mat(adddr4);

//        System.out.println("Image 1 has size "+img1.size()+" and image 2 has size "+img2.size());
//        Imgproc.resize(img1, img1, new Size(400, 600));
//        Bitmap new1 = Bitmap.createBitmap(img1.width(), img1.height(), Bitmap.Config.ARGB_8888);
//        Bitmap new2 = Bitmap.createBitmap(img2.width(), img2.height(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(img1, simOut);
//        Utils.matToBitmap(img2, histOut);

        imageView = findViewById(R.id.imageView);
        imageView1 = findViewById(R.id.imageView1);

        Bitmap resized = Bitmap.createScaledBitmap(segMap, 300, 227, true);
        imageView1.setImageBitmap(resized);
        resized = Bitmap.createScaledBitmap(origMap, 300, 227, true);
        imageView.setImageBitmap(resized);
//        imageView1.setImageBitmap(segMap);
//        imageView.setImageBitmap(origMap);
        gestureDetectorCompat = new GestureDetectorCompat(this, new My2ndGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class My2ndGestureListener extends GestureDetector.SimpleOnGestureListener {
        //handle 'swipe right' action only

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

         /*
         Toast.makeText(getBaseContext(),
          event1.toString() + "\n\n" +event2.toString(),
          Toast.LENGTH_SHORT).show();
         */

            if(event2.getX() > event1.getX()){
//                Toast.makeText(getBaseContext(),
//                        "Swipe right - finish()",
//                        Toast.LENGTH_SHORT).show();

                finish();
            }

            if(event2.getX() < event1.getX()){
//                Toast.makeText(getBaseContext(),
//                        "Swipe right - finish()",
//                        Toast.LENGTH_SHORT).show();
                System.out.println("Entered here in left wala");

                Intent myintent = new Intent(
                        seventhactivity.this, eighthActivity.class);
                ByteArrayOutputStream bs2 = new ByteArrayOutputStream();
                simOut.compress(Bitmap.CompressFormat.JPEG, 50, bs2);
                myintent.putExtra("o1", bs2.toByteArray());
//            myintent.putExtra("segment", res);
                ByteArrayOutputStream bs3 = new ByteArrayOutputStream();
                histOut.compress(Bitmap.CompressFormat.JPEG, 50, bs3);
                myintent.putExtra("o2", bs3.toByteArray());

//                myintent.putExtra("o1", addr1);
//                myintent.putExtra("o2", addr2);
                System.out.println("left swipe done");
                startActivity(myintent);
            }

            return true;
        }
    }
}
