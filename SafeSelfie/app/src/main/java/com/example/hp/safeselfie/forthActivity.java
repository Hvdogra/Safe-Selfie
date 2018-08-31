package com.example.hp.safeselfie;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;

public class forthActivity extends AppCompatActivity{
    private ImageView imageView;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView imageView4;
    private ImageView imageView5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.forth_activity);

        System.out.println("Reached forth activity");
        long addr = getIntent().getLongExtra("Mat", 0);
        long addr1 = getIntent().getLongExtra("Mat1",0);
        long addr2 = getIntent().getLongExtra("Mat2",0);
        long addr3 = getIntent().getLongExtra("Mat3",0);
        long addr4 = getIntent().getLongExtra("Mat4",0);
        long addr5 = getIntent().getLongExtra("Mat5",0);
        long addr6 = getIntent().getLongExtra("Mat6",0);
        int y = getIntent().getIntExtra("value", 0);
        int yy = getIntent().getIntExtra("value1", 0);
        Mat tempImg = new Mat( addr );
        Mat tempImg1 = new Mat( addr1);
        Mat tempImg2 = new Mat( addr2);
        Mat tempImg3 = new Mat( addr3);
        Mat tempImg4 = new Mat( addr4);
        Mat tempImg5 = new Mat( addr5);
        Mat tempImg6 = new Mat( addr6);
        Mat img = tempImg.clone();
        Mat img1 = tempImg1.clone();
        Mat img2 = tempImg2.clone();
        Mat img3 = tempImg3.clone();
        Mat img4 = tempImg4.clone();
        Mat img5 = tempImg5.clone();
        Mat img6 = tempImg6.clone();

        Imgproc.resize(img4, img4, new Size(128, 128));
        Imgproc.resize(img5, img5, new Size(128, 128));

        Mat gray = new Mat(img.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY);
        System.out.println("Wanted size is "+ img6.size());
        System.out.println("Wanted y is "+ y);
        Bitmap new1 = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);

        Bitmap new2 = Bitmap.createBitmap(img1.width(), img1.height(), Bitmap.Config.ARGB_8888);

        Bitmap new3 = Bitmap.createBitmap(img2.width(), img2.height(), Bitmap.Config.ARGB_8888);
        Bitmap new4 = Bitmap.createBitmap(img3.width(), img3.height(), Bitmap.Config.ARGB_8888);

        Bitmap new6 = Bitmap.createBitmap(img4.width(), img4.height(), Bitmap.Config.ARGB_8888);
        Bitmap new7 = Bitmap.createBitmap(img5.width(), img5.height(), Bitmap.Config.ARGB_8888);
        Bitmap new8 = Bitmap.createBitmap(img6.width(), img6.height(), Bitmap.Config.ARGB_8888);

        imageView = findViewById(R.id.imageView);
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);
        imageView5 = findViewById(R.id.imageView5);
        Utils.matToBitmap(gray, new1);
        Utils.matToBitmap(img1, new2);
        Utils.matToBitmap(img2, new3);
        Utils.matToBitmap(img3, new4);
        Utils.matToBitmap(img4, new6);

        Utils.matToBitmap(img6, new8);

        imageView.setImageBitmap(new6);
        imageView1.setImageBitmap(new2);

        imageView3.setImageBitmap(new4);
        imageView5.setImageBitmap(new8);
        Mat grab = Mat.zeros(img3.size(),img3.type());
        for(int i=0;i<img.width();i++)
            for(int j=0;j<img.height();j++)
            {
                if(img2.get(i,j)[0] == 255)
                {
                    grab.put(i,j,img3.get(i,j));
                }
            }



        for(int i=y-3;i<img.width();i++)
            for(int j=0;j<img.height();j++)
            {
                    grab.put(i,j,img3.get(i,j));
            }

        Size s = new Size(3,3);
//        Imgproc.GaussianBlur(grab, grab, s, 2);

        Imgproc.medianBlur(grab, grab, 1);

        Point p1 = new Point(0,yy);
        Point p2 = new Point(grab.width(), grab.height());
        Rect rect = new Rect(p1, p2);
        System.out.println("Rectangle is "+ rect);
//        Imgproc.resize(grab, grab, new Size(43, 43));

        Mat grab1 = new Mat(grab, rect);
        Bitmap new5 = Bitmap.createBitmap(grab1.width(), grab1.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(grab1, new5);

//        Mat gray1 = new Mat(grab1.size(), CvType.CV_8UC1);
//        Imgproc.cvtColor(grab1, gray1, Imgproc.COLOR_RGB2GRAY);
//        Mat edge = new Mat();
//        Mat edgeDest = new Mat();
//        Imgproc.Canny(gray1, edge, 80, 90);
//        Imgproc.cvtColor(edge, edgeDest, Imgproc.COLOR_GRAY2RGBA,4);



        int[] arr = new int[6];
        int sum = 0;int count =0;
        for(int i = 43;i<86;i++)
            for(int j=0;j<43;j++)
            {
                count++;
                sum += new6.getPixel(i,j);
            }
        arr[0] = sum/count;

        sum =0;count =0;
        for(int i = 43;i<86;i++)
            for(int j=43;j<86;j++)
            {
                count++;
                sum += new6.getPixel(i,j);
            }
        arr[1] = sum/count;

        sum =0;count =0;
        for(int i = 43;i<86;i++)
            for(int j=86;j<128;j++)
            {
                count++;
                sum += new6.getPixel(i,j);
            }
        arr[2] = sum/count;

        sum =0;count =0;
        for(int i = 86;i<128;i++)
            for(int j=0;j<43;j++)
            {
                count++;
                sum += new6.getPixel(i,j);
            }
        arr[3] = sum/count;

        sum =0;count =0;
        for(int i = 86;i<128;i++)
            for(int j=43;j<86;j++)
            {
                count++;
                sum += new6.getPixel(i,j);
            }
        arr[4] = sum/count;

        sum =0;count =0;
        for(int i = 86;i<128;i++)
            for(int j=86;j<128;j++)
            {
                count++;
                sum += new6.getPixel(i,j);
            }
        arr[5] = sum/count;

        int min = Integer.MAX_VALUE;
        int index = -1;
        int start1 =0, start2 = 0;

        for(int i=0;i<6;i++)
        {
            if(arr[i]<min)
            {
                min = arr[i];
                index = i;

            }
            if(i>=3)
            {
                start1 = 84;
                start2 = (i-3)*42;
            }
            else
            {
                start1 = 42;
                start2 = (i)*42;
            }
        }

        Imgproc.resize(grab1, grab1, new Size(42, 42));

        Imgproc.cvtColor(grab1, grab1, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(img5, img5, Imgproc.COLOR_RGBA2RGB);
        System.out.println("Grab 1 data "+grab1.channels()+" "+grab1.type());
        System.out.println("Img 5 data "+img5.channels()+" "+img5.type());
        for(int i = start1;i<start1+42;i++)
            for(int j=start2;j<start2+42;j++)
            {
                if(grab1.get(i-start1,j-start2)[0] != 0 || grab1.get(i-start1,j-start2)[0] != 0 || grab1.get(i-start1,j-start2)[2] != 0)
                {
                    img5.put(i , j, grab1.get(i-start1 , j-start2));
                }
            }
        Utils.matToBitmap(img5, new7);
        imageView2.setImageBitmap(new7);

        imageView4.setImageBitmap(new5);

    }
}
