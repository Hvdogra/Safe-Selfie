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

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;

public class eighthActivity extends AppCompatActivity {

    private ImageView imageView, imageView1;
    private GestureDetectorCompat gestureDetectorCompat;
    Bitmap simOut, histOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.eighth_activity);

        System.out.println("Entered eight activity");
        simOut = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("o1"), 0, getIntent()
                        .getByteArrayExtra("o1").length);

        histOut = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("o2"), 0, getIntent()
                        .getByteArrayExtra("o2").length);

        System.out.println(simOut.getWidth()+" "+simOut.getHeight()+" "+histOut.getWidth()+ " "+histOut.getHeight());

//        long addr1, addr2;
//        addr1 = getIntent().getLongExtra("o1",0);
//        addr2 = getIntent().getLongExtra("o2",0);
//        Mat tempImg = new Mat( addr1 );
//        Mat tempImg1 = new Mat( addr2);
////        Mat tempImg2 = new Mat();
////        Utils.bitmapToMat(segMap, tempImg2);
//        simOut = Bitmap.createBitmap(tempImg.width(), tempImg.height(), Bitmap.Config.ARGB_8888);
//        histOut = Bitmap.createBitmap(tempImg1.width(), tempImg1.height(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(tempImg, simOut);
//        Utils.matToBitmap(tempImg1, histOut);

        imageView = findViewById(R.id.imageView);
//        imageView1 = findViewById(R.id.imageView1);

        Bitmap resized = Bitmap.createScaledBitmap(histOut, 300, 227, true);
//        imageView1.setImageBitmap(resized);
        resized = Bitmap.createScaledBitmap(simOut, 400, 600, true);
        imageView.setImageBitmap(resized);
//        imageView1.setImageBitmap(histOut);
//        imageView.setImageBitmap(simOut);
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

            return true;
        }
    }
}