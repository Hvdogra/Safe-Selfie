package com.example.hp.safeselfie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class sixthActivity extends AppCompatActivity {

    private ImageView imageView, imageView1;
    private GestureDetectorCompat gestureDetectorCompat;
    private long addr2, addr3, addr4, addr5;
    Bitmap segMap, origMap, simOut, histOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.sixth_activity);

        long addr = getIntent().getLongExtra("sal", 0);
        long addr1 = getIntent().getLongExtra("orig",0);

        segMap = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("segment"),0,getIntent()
                        .getByteArrayExtra("segment").length);

//        segMap = getIntent().getParcelableExtra("segment");
        origMap = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("orig1"),0,getIntent()
                        .getByteArrayExtra("orig1").length);


//        origMap = getIntent().getParcelableExtra("orig1");
//        addr2 = getIntent().getLongExtra("segment",0);
        addr3 = getIntent().getLongExtra("orig1",0);
        Mat tempImg = new Mat( addr );
        Mat tempImg1 = new Mat( addr1);
        Mat tempImg2 = new Mat();
        Utils.bitmapToMat(segMap, tempImg2);
        Mat img = tempImg.clone();
        Mat img1 = tempImg1.clone();
        Mat img2 = tempImg2.clone();

        System.out.println("img size "+img.size()+" img1 size "+img1.size()+" img2 size "+img2.size());

        Imgproc.resize(img, img, new Size(400, 600));
        Imgproc.resize(img2, img2, new Size(333, 500));
        Bitmap new1 = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
        Imgproc.resize(img1, img1, new Size(400, 600));
        Bitmap new2 = Bitmap.createBitmap(img1.width(), img1.height(), Bitmap.Config.ARGB_8888);
        Bitmap new4 = Bitmap.createBitmap(img1.width(), img1.height(), Bitmap.Config.ARGB_8888);
        Bitmap new3 = Bitmap.createBitmap(img2.width(), img2.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, new1);
        Utils.matToBitmap(img1, new4);

        Utils.matToBitmap(img2, new3);
        //Flipping 3d
//        Matrix matrix = new Matrix();
//        matrix.preScale(1, -1);
//        Bitmap flipImage = Bitmap.createBitmap(new3, 0,0 , new3.getWidth(), new3.getHeight(), matrix, true);
//        Utils.bitmapToMat(flipImage, img2);

        int[] arr = new int[2];
        int sum = 0;int count =0;
        for(int i = 0;i<333;i++)
            for(int j=100;j<600;j++)
            {
                count++;
                sum += new1.getPixel(i,j);
            }
        arr[0] = sum/count;

        sum =0;count =0;
        for(int i = 67;i<400;i++)
            for(int j=100;j<600;j++)
            {
                count++;
                sum += new1.getPixel(i,j);
            }
        arr[1] = sum/count;

//        sum =0;count =0;
//        for(int i = 0;i<333;i++)
//            for(int j=70;j<570;j++)
//            {
//                count++;
//                sum += new1.getPixel(i,j);
//            }
//        arr[2] = sum/count;
//        sum =0;count =0;
//        for(int i = 67;i<400;i++)
//            for(int j=70;j<570;j++)
//            {
//                count++;
//                sum += new1.getPixel(i,j);
//            }
//        arr[3] = sum/count;
//        sum =0;count =0;
//        for(int i = 0;i<333;i++)
//            for(int j=100;j<600;j++)
//            {
//                count++;
//                sum += new1.getPixel(i,j);
//            }
//        arr[4] =sum/count;
//        sum =0;count =0;
//        for(int i = 67;i<400;i++)
//            for(int j=100;j<600;j++)
//            {
//                count++;
//                sum += new1.getPixel(i,j);
//            }
//        arr[5] = sum/count;
        int min = Integer.MAX_VALUE;
        int index = -1;
        int start, end;
        for(int i=0;i<2;i++)
        {
            if(arr[i]<min)
            {
                min = arr[i];
                index = i;

            }
        }
        if(index == 0)
            start = 0;
        else
            start = 67;


        System.out.println("Img 2 is "+img2.size()+" "+img2.channels()+"   "+img2.get(1,1)+" "+img1.channels());
        Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGB2RGBA);

        Mat dif = Mat.zeros(img1.size(), img1.type());
        Imgproc.cvtColor(dif, dif, Imgproc.COLOR_RGB2RGBA);
        if(start == 0)
        {
            for(int i=0;i<333;i++)
                for(int j=0;j<500;j++)
                {
                    dif.put(j+100, i, img2.get(j,i));
                }
        }
        else
        {
            for(int i=0;i<333;i++)
                for(int j=0;j<500;j++)
                {
                    dif.put(j+100, i+67, img2.get(j,i));
                }
        }


        for(int i=0;i<400;i++)
            for(int j=0;j<600;j++)
            {
                if(dif.get(j , i)[0] == 0 && dif.get(j, i)[1] == 0 && dif.get(j, i)[2] == 0)
                {
                    double[] arr1 = new double[4];
                    arr1 = img1.get(j, i);
                    dif.put(j, i, arr1);
                }
            }

        img1 = dif.clone();
        Mat trp = new Mat();
        trp = img1.clone();

//        Core.addWeighted( img1, 0.5p, dif, 1, 0.0, img1);
//        int a=0, b;
//        for(int i = start;i<start+333;i++)
//        {
//            for (int j = 100; j < 600; j++)
//            {
//                if (img2.get(j - 100, i - start)[0] != 0 && img2.get(j - 100, i - start)[1] != 0 && img2.get(j - 100, i - start)[2] != 0)
//                {
//
//                    if (i-start+4<333 && img2.get(j - 100, i - start+4)[0] != 0 && img2.get(j - 100, i - start+4)[1] != 0 && img2.get(j - 100, i - start+4)[2] != 0) {
////                        for(int al = -5; al<5;al++) {
//                        double[] arr1 = new double[4];
//                        arr1 = img2.get(j - 100, i - start + 3);
//                        double[] arr2 = new double[4];
//                        arr2 = trp.get(j, i + 3);
//                        if (arr1 != null && arr2 != null) {
////                                arr1[3] = 0.5 * arr1[3] + 0.5 * arr2[3];
//                            System.out.println("Arr2[3] is " + arr2[3] + " " + arr2[0] + " " + arr2[1] + " " + arr2[2]);
////                                arr2[3] = 0;
//                            img1.put(j, i + 3, arr1);
//                        }
////                        }
//                    }
//
//
//                }
//            }
//        }

        addr4 = img1.getNativeObjAddr();
        simOut = Bitmap.createBitmap(img1.width(), img1.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img1, simOut);
        //Gaussian blur
        Imgproc.GaussianBlur(img1, img1,new Size(3,3), 1);

        //Sharpening
        Mat destination = new Mat(img1.rows(), img1.cols(), img1.type());

        Imgproc.GaussianBlur(img1, destination, new Size(0,0), 10);
        Core.addWeighted(img1, 1.5, destination, -0.5, 0, destination);

        System.out.println("Destination details "+img1.channels());

        Imgproc.cvtColor(destination, destination, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(destination, destination, Imgproc.COLOR_RGB2Lab);
        List<Mat> lab_list = new ArrayList(3);
        Core.split(destination,lab_list);
        Mat lab;
        lab = lab_list.get(0);
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.apply(lab, lab);
//            Mat new1 = Mat.zeros(lab.size(), lab.type());
        lab_list.set(0, lab);
        Core.merge(lab_list,destination);
        Imgproc.cvtColor(destination, destination, Imgproc.COLOR_Lab2BGR);

        addr5 = destination.getNativeObjAddr();

        histOut = Bitmap.createBitmap(destination.width(), destination.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(destination, histOut);

        Utils.matToBitmap(img1, new2);

        imageView = findViewById(R.id.imageView);
        imageView1 = findViewById(R.id.imageView1);

        Bitmap resized = Bitmap.createScaledBitmap(new1, 300, 227, true);
        imageView1.setImageBitmap(resized);
        resized = Bitmap.createScaledBitmap(new4, 300, 227, true);
        imageView.setImageBitmap(resized);

        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        //handle 'swipe left' action only

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

         /*
         Toast.makeText(getBaseContext(),
          event1.toString() + "\n\n" +event2.toString(),
          Toast.LENGTH_SHORT).show();
         */

            if(event2.getX() < event1.getX()){
//                Toast.makeText(getBaseContext(),
//                        "Swipe left - new Activity",
//                        Toast.LENGTH_SHORT).show();

                //switch another activity
                System.out.println("Addr2 is "+addr2);
                Intent myintent = new Intent(
                        sixthActivity.this, seventhactivity.class);

                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                segMap.compress(Bitmap.CompressFormat.JPEG, 25, bs);
                myintent.putExtra("segment", bs.toByteArray());
//            myintent.putExtra("segment", res);
                ByteArrayOutputStream bs1 = new ByteArrayOutputStream();
                origMap.compress(Bitmap.CompressFormat.JPEG, 25, bs1);
                myintent.putExtra("orig1", bs1.toByteArray());

                ByteArrayOutputStream bs2 = new ByteArrayOutputStream();
                simOut.compress(Bitmap.CompressFormat.JPEG, 25, bs2);
                myintent.putExtra("o1", bs2.toByteArray());
//            myintent.putExtra("segment", res);
                ByteArrayOutputStream bs3 = new ByteArrayOutputStream();
                histOut.compress(Bitmap.CompressFormat.JPEG, 25, bs3);
                myintent.putExtra("o2", bs3.toByteArray());

//                myintent.putExtra("segment", segMap);
//                myintent.putExtra("orig1", origMap);
//                myintent.putExtra("o1", addr4);
//                myintent.putExtra("o2", addr5);
                System.out.println("left swipe done");
                startActivity(myintent);
            }

            return true;
        }
    }
}
