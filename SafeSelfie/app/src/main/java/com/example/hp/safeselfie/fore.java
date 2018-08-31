package com.example.hp.safeselfie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class fore extends AppCompatActivity {

    File file, file1;

    private ImageView imageView;
    private String temp;
    Mat result8u, glImage;

    float x, y, z, yy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.fore);

        temp =  getIntent().getStringExtra("FileName");
        file = new File(getIntent().getStringExtra("FileName"));
        file1 = new File(getIntent().getStringExtra("FileName1"));
        file1 = new File(getIntent().getStringExtra("FileName1"));
        try {
            displayImageBack(file1);
            displayImageFront(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    private void displayImageFront(File file) throws FileNotFoundException {
        if(isStoragePermissionGranted()){
            //            Bitmap bm = decodeUri(file);
            System.out.println("HELLO REACHED HERE");
            Intent myintent = new Intent(fore.this,fifthActivity.class);
            long addr = result8u.getNativeObjAddr();
            long addr1 = glImage.getNativeObjAddr();
            System.out.println(file);
            myintent.putExtra("FileName", temp);
            myintent.putExtra("sal", addr);
            myintent.putExtra("orig", addr1);
            myintent.putExtra("X", 0);
            myintent.putExtra("Y", 0);
            myintent.putExtra("Z", 0);
            myintent.putExtra("YY", 0);
            //     myintent.putExtra("FileName2", name1);
//            cameraDevice.close();
            startActivity(myintent);

//
//            Bitmap bm = BitmapFactory.decodeFile(String.valueOf(file));
//            Matrix matrix = new Matrix();
//            matrix.postRotate(270);
//            imageView = findViewById(R.id.imageView);
//            Bitmap rotatedBitmap = Bitmap.createBitmap(bm , 0, 0,  bm.getWidth(), bm.getHeight(), matrix, true);
//
//            Bitmap output;
//            Matrix matrix1 = new Matrix();
//            matrix1.preScale(-1.0f, 1.0f);
//            output = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), matrix1, true);
//
//            imageView.setImageBitmap(BITMAP_RESIZER(output, 128, 128));

        }
        else
        {
            System.out.println("HELLO DIDN'T REACHED HERE");
        }
    }


    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                System.out.print("Permission is granted");
                return true;
            } else {

                System.out.print("Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            System.out.print("Permission is granted");
            return true;
        }
    }

    private void displayImageBack(File file) throws FileNotFoundException{

        if(isStoragePermissionGranted()){
            //            Bitmap bm = decodeUri(file);
            System.out.println("HELLO REACHED HERE");
            Bitmap bm = BitmapFactory.decodeFile(String.valueOf(file));
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            imageView = findViewById(R.id.imageView);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bm , 0, 0,  bm.getWidth(), bm.getHeight(), matrix, true);

            System.out.println("Bitmap "+bm.getWidth());
            System.out.println("Bitmap H "+bm.getHeight());
            Mat mat = new Mat();
            glImage = new Mat();
            Bitmap bmp32 = rotatedBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bmp32, mat);
            Utils.bitmapToMat(bmp32, glImage);
            System.out.println("Initially "+mat.channels());

            System.out.println("Initially size "+mat.size());
            Mat result;
            result = function(mat);
            System.out.println("Finally "+result.channels());
            System.out.println("Finally 1 "+result.get(20,0)[0]);
            System.out.println("Finally size "+result.size());
            System.out.println("Finally type "+result.type());
            Size s = new Size(400,400);
//            Imgproc.resize(result, result, s);

//
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inDither = false;
            o.inSampleSize = 4;
            Bitmap new1 = Bitmap.createBitmap(bmp32.getWidth(), bmp32.getHeight(), Bitmap.Config.ARGB_8888);
            Mat grayMat = new Mat();
            result8u = new Mat(result.rows(), result.cols(), CvType.CV_8UC4);
//            Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY);
//            Imgproc.cvtColor(result, grayMat, Imgproc.);
//            Imgproc.cvtColor(result, grayMat, Imgproc.COLOR_GRAY2RGBA, 4);
            Core.normalize(result, result8u, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);
//            Utils.matToBitmap(result8u  , new1);
//            imageView.setImageBitmap(new1);


        }
        else
        {
            System.out.println("HELLO DIDN'T REACHED HERE");
        }
    }

    private Mat function(Mat mat) {

        float[][] LG = lobGabor(256, 256);

        for(int i=0;i<256;i++)
        {
            for(int j=0;j<256;j++)
            {
                if(LG[i][j] == 0)
                {
                    System.out.print( "Hi");
                }

            }
//			System.out.println();
        }
        List<Float> list = new ArrayList<Float>();
        for (int i = 0; i < LG.length; i++) {
            // tiny change 1: proper dimensions
            for (int j = 0; j < LG[i].length; j++) {
                // tiny change 2: actually store the values
                list.add(LG[i][j]);
            }
        }

        float[] vector = new float[list.size()];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = list.get(i);
        }

        Mat LG1 = Mat.zeros(256, 256, CvType.CV_32FC1);




        int size = (int) (LG1.total() * LG1.channels());

        float[] temp = new float[size];
        LG1.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] = vector[i];
        LG1.put(0, 0, temp);
//	    System.out.println(LG1.dump());
//	    LG1.convertTo(LG1, CvType.CV_8U);
//	    Mat2BufferedImage(LG1);

//		for (int i=0; i<256; i++)
//		{
//		    for(int j=0; j<256; j++)
//		    {
//		         LG1.put(0, 0, (float)LG[i][j]);
//		    }
//		}


//		for(int i=0;i<256;i++)
//		{
//			for(int j=0;j<256;j++)
//			{
//				System.out.print(LG[i][j] + " ");
//			}
//			System.out.println();
//		}


        Mat imageOld = mat;
        Size sizeA = imageOld.size();
//		 for (int i = 0; i < sizeA.height; i++)
//           for (int j = 0; j < sizeA.width; j++) {
//               System.out.println(matrix.get(i,j)[0]);
//               System.out.println(matrix.get(i,j)[1]);
//               System.out.println(matrix.get(i,j)[2]);
//           }

        Mat imgNew = new Mat( imageOld.rows(), imageOld.cols(), CvType.CV_64FC3 );
        imageOld.copyTo(imgNew);


        Mat dsImage = new Mat();
//		 Float zoom1 = (float) (256.0/imageOld.rows());
//		 Float zoom2 = (float) (256.0/imageOld.cols());
//		 System.out.println(zoom1+" "+zoom2);
        Size s = new Size(256, 256);
        Imgproc.resize(imgNew, dsImage, s);


//		 System.out.println(dsImage.size());
//		 for (int i = 0; i < sizeL.height; i++)
//		       for (int j = 0; j < sizeL.width; j++) {
//		           System.out.println(dsImage.get(i,j)[0]);
//		       }
//
//			}
        Mat lab = RGB2Lab(dsImage);




//		 Mat lab = new Mat();
//         Imgproc.cvtColor(dsImage, lab, Imgproc.COLOR_RGB2Lab);
//         System.out.println(lab.get(0, 0)[2]);
//         List<Mat> channels = new ArrayList<>(3);
//         Core.split(lab, channels);
//         Mat L = channels.get(0);
//         Mat A = channels.get(1);
//         Mat B = channels.get(2);
//         Size sizeL = L.size();
//		 for (int i = 0; i < sizeL.height; i++)
//       for (int j = 0; j < sizeL.width; j++) {
////           System.out.println(L.get(i,j)[0]);
//       }
        List<Mat> channeled = new ArrayList<>(3);
        Core.split(lab, channeled);
        Mat src1 = channeled.get(0);
        Mat src2 = channeled.get(1);
        Mat src3 = channeled.get(2);
        int m = Core.getOptimalDFTSize(src1.rows());
        int n = Core.getOptimalDFTSize(src1.cols());
        Mat padded = new Mat(new Size(n, m), CvType.CV_64FC1);
        Core.copyMakeBorder(src1, padded, 0, m - src1.rows(), 0, n - src1.cols(), Core.BORDER_CONSTANT);
        List<Mat> planes = new ArrayList<Mat>();
        planes.add(padded);
        planes.add(Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC1));

        Mat complexI = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

        Mat LFFT = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);


        Core.merge(planes, complexI); // Add to the expanded another plane with
        // zeros

        Core.dft(complexI, LFFT); // this way the result may fit in the
        // source matrix

        m = Core.getOptimalDFTSize(src2.rows());
        n = Core.getOptimalDFTSize(src2.cols());
        padded = new Mat(new Size(n, m), CvType.CV_64FC1);
        Core.copyMakeBorder(src2, padded, 0, m - src2.rows(), 0, n - src2.cols(), Core.BORDER_CONSTANT);
        List<Mat> planes1 = new ArrayList<Mat>();
        planes1.add(padded);
        planes1.add(Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC1));

        complexI = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

        Mat AFFT = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

        Core.merge(planes1, complexI); // Add to the expanded another plane with
        // zeros

        Core.dft(complexI, AFFT); // this way the result may fit in the
        // source matrix

        m = Core.getOptimalDFTSize(src3.rows());
        n = Core.getOptimalDFTSize(src3.cols());
        padded = new Mat(new Size(n, m), CvType.CV_64FC1);
        Core.copyMakeBorder(src3, padded, 0, m - src3.rows(), 0, n - src3.cols(), Core.BORDER_CONSTANT);
        List<Mat> planes2 = new ArrayList<Mat>();
        planes2.add(padded);
        planes2.add(Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC1));

        complexI = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

        Mat BFFT = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

        Core.merge(planes2, complexI); // Add to the expanded another plane with
        // zeros

        Core.dft(complexI, BFFT); // this way the result may fit in the
        // source matrix

//         Mat FinalLResult = LFFT.mul(LG1);
        List<Mat> chan = new ArrayList<>(2);
        Core.split(LFFT, chan);
        Mat Src1 = chan.get(0);
        Mat Src2 = chan.get(1);
        size = (int) (Src1.total() * Src1.channels());
        double[] temp1 = new double[size];
        double[] temp2 = new double[size];
        double[] temp4 = new double[size];
        double[] temp5 = new double[size];
        double[] temp6 = new double[size];
        float[] temp7 = new float[size];
        byte[] temp3 = new byte[size];
        Src1.get(0, 0, temp1);
        Src2.get(0, 0, temp2);
        System.out.println(LG1.size());
        System.out.println(LG1.channels());
        LG1.get(0, 0, temp7);
        for (int i = 0; i < size; i++)
        {
            temp1[i] = temp1[i]*temp7[i];
            temp2[i] = temp2[i]*temp7[i];
        }
        Src1.put(0, 0, temp1);
        Src2.put(0, 0, temp2);
        List<Mat> listMat = Arrays.asList(Src1, Src2);

        Core.merge(listMat, LFFT);
        Mat FinalLResult1 = new Mat();
        Mat FinalLResult = new Mat();
        Core.idft(LFFT, FinalLResult1);
        Core.split(FinalLResult1, chan);
        Core.normalize(chan.get(0), FinalLResult, -128, 127, Core.NORM_MINMAX);
//         FinalLResult = chan.get(0);

        Core.split(AFFT, chan);
        Src1 = chan.get(0);
        Src2 = chan.get(1);
        size = (int) (Src1.total() * Src1.channels());
        Src1.get(0, 0, temp1);
        Src2.get(0, 0, temp2);
        LG1.get(0, 0, temp7);
        for (int i = 0; i < size; i++)
        {
            temp1[i] = temp1[i]*temp7[i];
            temp2[i] = temp2[i]*temp7[i];
        }
        Src1.put(0, 0, temp1);
        Src2.put(0, 0, temp2);
        List<Mat> listMat1 = Arrays.asList(Src1, Src2);

        Core.merge(listMat1, AFFT);
        Mat FinalAResult1 = new Mat();
        Mat FinalAResult = new Mat();
        Core.idft(AFFT, FinalAResult1);
        Core.split(FinalAResult1, chan);
        Core.normalize(chan.get(0), FinalAResult, -128, 127, Core.NORM_MINMAX);

        Core.split(BFFT, chan);
        Src1 = chan.get(0);
        Src2 = chan.get(1);
        size = (int) (Src1.total() * Src1.channels());
        Src1.get(0, 0, temp1);
        Src2.get(0, 0, temp2);
        LG1.get(0, 0, temp7);
        for (int i = 0; i < size; i++)
        {
            temp1[i] = temp1[i]*temp7[i];
            temp2[i] = temp2[i]*temp7[i];
        }
        Src1.put(0, 0, temp1);
        Src2.put(0, 0, temp2);
        List<Mat> listMat2 = Arrays.asList(Src1, Src2);

        Core.merge(listMat2, BFFT);
        Mat FinalBResult1 = new Mat();
        Mat FinalBResult = new Mat();
        Core.idft(BFFT, FinalBResult1);
        Core.split(FinalBResult1, chan);
        Core.normalize(chan.get(0), FinalBResult, -128, 127, Core.NORM_MINMAX);

        Mat SFMap = FinalLResult.clone();
        FinalLResult.get(0, 0, temp1);
        FinalAResult.get(0, 0, temp2);
        FinalBResult.get(0, 0, temp4);
        SFMap.get(0, 0, temp5);
        for (int i = 0; i < size; i++)
        {
            temp1[i] = Math.sqrt(Math.pow(temp1[i], 2) + Math.pow(temp2[i], 2) + Math.pow(temp4[i], 2));
        }
        SFMap.put(0, 0, temp1);



//         FinalLResult = chan.get(0);
//         System.out.print(SFMap.dump());

//         System.out.println(BFFT.get(0,0)[0]);

        //the central areas will have a bias towards attention
        Mat coordinateMtx = Mat.zeros(256, 256, CvType.CV_64FC2);
        Core.split(coordinateMtx, chan);
        Mat dummy1 = chan.get(0);
        size = (int) (dummy1.total() * dummy1.channels());
        dummy1.get(0, 0, temp1);
        int j=0, var = 1;
        for (int i = 0; i < size; i++)
        {
            if(j<=255)
            {
                temp1[i] = var;
                j++;
            }
            else
            {
                j=0;
                var++;
                temp1[i] = var;
            }
        }
        dummy1.put(0, 0, temp1);
//         System.out.print(dummy1.get(7,245)[0]);

        Mat dummy2 = chan.get(1);
        dummy2.get(0, 0, temp1);
        j=0;var = 1;

        for (int i = 0; i < size; i++)
        {
            if(j<=255)
            {
                temp1[i] = var;
                j++;
                var++;
            }
            else
            {
                j=1;
                var=1;
                temp1[i] = var;
            }
        }
        dummy2.put(0, 0, temp1);
        List<Mat> listMat3 = Arrays.asList(dummy1, dummy2);

        Core.merge(listMat3, coordinateMtx);

        Mat centreMtx = Mat.zeros(256, 256, CvType.CV_64FC2);
        Core.split(centreMtx, chan);
        Mat dummy3 = chan.get(0);
//         dummy1 = chan.get(0);
        size = (int) (dummy3.total() * dummy3.channels());
        dummy3.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
        {
            temp1[i] = 128;
        }
        dummy3.put(0, 0, temp1);
//         System.out.print(dummy1.get(7,245)[0]);

        Mat dummy4 = chan.get(0);
        dummy4 = chan.get(1);
        dummy4.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
        {
            temp1[i] = 128;
        }
        dummy4.put(0, 0, temp1);
        List<Mat> listMat4 = Arrays.asList(dummy3, dummy4);

        Core.merge(listMat4, centreMtx);
        int sigmaD = 114;

        Mat SDMap = dummy1.clone();
        SDMap.get(0, 0, temp1);
        dummy1.get(0, 0, temp2);
        dummy2.get(0, 0, temp4);
        dummy3.get(0, 0, temp5);
        dummy4.get(0, 0, temp6);
        for (int i = 0; i < size; i++)
        {
            int sum1 = (int) Math.pow((temp2[i]-temp5[i]),2);
            int sum2 = (int) Math.pow((temp4[i]-temp6[i]),2);
            temp1[i] = Math.exp(-(sum1+sum2)/Math.pow(sigmaD, 2));
        }
        SDMap.put(0, 0, temp1);



//			System.out.println(SDMap.dump());
        float max = -1000, min = 1000;
        size = (int) (src2.total() * src2.channels());
        src2.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
        {
            if(temp1[i]>max)
            {
                max = (float) temp1[i];

            }
            if(temp1[i]<min)
            {
                min = (float) temp1[i];
            }
        }
        Mat normalizedA = src2.clone();
        normalizedA.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
        {
            temp1[i] = (temp1[i]-min)/(max-min);
        }
        normalizedA.put(0, 0, temp1);

        max = -1000; min = 1000;
        size = (int) (src3.total() * src3.channels());
        src3.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
        {
            if(temp1[i]>max)
            {
                max = (float) temp1[i];

            }
            if(temp1[i]<min)
            {
                min = (float) temp1[i];
            }
        }
        Mat normalizedB = src3.clone();
        normalizedB.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
        {
            temp1[i] = (temp1[i]-min)/(max-min);
        }
        normalizedB.put(0, 0, temp1);

        Mat labDistSquare = normalizedA.clone();
        labDistSquare.get(0, 0, temp1);
        normalizedA.get(0, 0, temp2);
        normalizedB.get(0, 0, temp4);
        for (int i = 0; i < size; i++)
        {
            temp1[i] = Math.pow(temp2[i], 2) + Math.pow(temp4[i], 2);
        }
        labDistSquare.put(0, 0, temp1);

        float sigmaC = (float) 0.25;
        Mat SCMap = labDistSquare.clone();
        labDistSquare.get(0, 0, temp1);
        SCMap.get(0, 0, temp2);
        for (int i = 0; i < size; i++)
        {
            temp2[i] = 1-Math.exp(-(temp1[i]/Math.pow(sigmaC, 2)));
        }
        SCMap.put(0, 0, temp2);


//         System.out.println(SCMap.dump());

        Mat VSMap = SCMap.clone();
        VSMap.get(0, 0, temp1);
        SCMap.get(0, 0, temp2);
        SFMap.get(0, 0, temp4);
        SDMap.get(0, 0, temp5);
        for (int i = 0; i < size; i++)
        {
            temp1[i] = temp2[i]*temp4[i]*temp5[i];
        }
        VSMap.put(0, 0, temp1);


        Float zoom3 = (float) (imageOld.rows()/256.0);
        Float zoom4 = (float) (imageOld.cols()/256.0);
        Imgproc.resize(VSMap, VSMap, imageOld.size(),zoom3, zoom4, Imgproc.INTER_LINEAR );
        System.out.println(VSMap.size());
        max = -1000; min = 1000;
        size = (int) (VSMap.total() * VSMap.channels());
//         System.out.println(VSMap.dump());
        double[] temp11 = new double[size];
        VSMap.get(0, 0, temp11);
        for (int i = 0; i < size; i++)
        {
            if(temp11[i]>max)
            {
                max = (float) temp11[i];

            }
            if(temp11[i]<min)
            {
                min = (float) temp11[i];
            }
        }

        VSMap.convertTo(VSMap, CvType.CV_8U);


//        Mat dummyImage = VSMap.clone();
//        System.out.println(dummyImage.size());
//        VSMap.convertTo(dummyImage, CvType.CV_8U);
//        Core.normalize(dummyImage, dummyImage, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);
//        System.out.println(dummyImage.channels());
//        Mat2BufferedImage(dummyImage);
////         double[] temp9 = new double[size];
//         VSMap.get(0, 0, temp9);
//         for (int i = 0; i < size; i++)
//	     {
//        	 double variable = (temp9[i]-min)/(max-min);
//        	 temp9[i] =  (byte) (255*variable);
//		 }
//         VSMap.put(0, 0, temp9);

//         System.out.println(VSMap.get(100,150)[0]);



        return VSMap;
        //System.out.println(VSMap.dump());



    }

    private Mat RGB2Lab(Mat dsImage) {

        Mat work = dsImage.clone();
        Mat normalizedR = new Mat();
        Mat normalizedG = new Mat();
        Mat normalizedB = new Mat();
        List<Mat> channels = new ArrayList<>(3);
        Core.split(dsImage, channels);
        normalizedR = channels.get(0);
        normalizedG = channels.get(1);
        normalizedB = channels.get(2);
        normalizedR.convertTo(normalizedR, CvType.CV_32F);
        normalizedG.convertTo(normalizedG, CvType.CV_32F);
        normalizedB.convertTo(normalizedB, CvType.CV_32F);
//        System.out.println(normalizedR.dump());
//        Core.divide(1.0/25, normalizedR, normalizedR);
//        System.out.println(1.0/255);
        int size = (int) (normalizedR.total() * normalizedR.channels());
        float[] temp = new float[size];
        float[] temp1 = new float[size];
        float[] temp2 = new float[size];
        float[] temp3 = new float[size];
        normalizedR.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  (temp[i] / 255);
        normalizedR.put(0, 0, temp);
        normalizedG.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  (temp[i] / 255);
        normalizedG.put(0, 0, temp);
        normalizedB.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  (temp[i] / 255);
        normalizedB.put(0, 0, temp);

        //For G
        Mat GSmallerOrEqualto4045 = new Mat();
        GSmallerOrEqualto4045 = normalizedG.clone();
        normalizedG.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] <= 0.04045)
                temp[i] = (float) 1.0;
            else
                temp[i] = 0;
        GSmallerOrEqualto4045.put(0, 0, temp);

        Mat GGreaterThan4045 = new Mat();
        GGreaterThan4045 = GSmallerOrEqualto4045.clone();
        GGreaterThan4045.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] == 0.0)
                temp[i] = (float) 1.0;
            else
                temp[i] =0;
        GGreaterThan4045.put(0, 0, temp);

        normalizedG.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  (temp[i]/(float)12.92);
        normalizedG.put(0, 0, temp);
        Mat tmpG = new Mat();
        tmpG = normalizedG.mul(GSmallerOrEqualto4045);


        normalizedG.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  (float) Math.pow(((temp[i]*(float)12.92)+0.055)/1.055,2.4);
        normalizedG.put(0, 0, temp);
        Mat tArray = new Mat();
        tArray = normalizedG.mul(GGreaterThan4045);
        tmpG.get(0, 0, temp);
        tArray.get(0, 0,temp1);
        for (int i = 0; i < size; i++)
            temp[i] =  temp[i]+temp1[i];
        tmpG.put(0, 0, temp);


        //For B
        Mat BSmallerOrEqualto4045 = new Mat();
        BSmallerOrEqualto4045 = normalizedB.clone();
        normalizedB.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] <= 0.04045)
                temp[i] = (float) 1.0;
            else
                temp[i] = 0;
        BSmallerOrEqualto4045.put(0, 0, temp);

        Mat BGreaterThan4045 = new Mat();
        BGreaterThan4045 = BSmallerOrEqualto4045.clone();
        BGreaterThan4045.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] == 0.0)
                temp[i] = (float) 1.0;
            else
                temp[i] =0;
        BGreaterThan4045.put(0, 0, temp);

        normalizedB.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  (temp[i]/(float)12.92);
        normalizedB.put(0, 0, temp);
        Mat tmpB = new Mat();
        tmpB = normalizedB.mul(BSmallerOrEqualto4045);


        normalizedB.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  (float) Math.pow(((temp[i]*(float)12.92)+0.055)/1.055,2.4);
        normalizedB.put(0, 0, temp);
        tArray = normalizedB.mul(BGreaterThan4045);
        tmpB.get(0, 0, temp);
        tArray.get(0, 0,temp1);
        for (int i = 0; i < size; i++)
            temp[i] =  temp[i]+temp1[i];
        tmpB.put(0, 0, temp);

        //For R
        Mat RSmallerOrEqualto4045 = new Mat();
        RSmallerOrEqualto4045 = normalizedR.clone();
        normalizedR.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] <= 0.04045)
                temp[i] = (float) 1.0;
            else
                temp[i] = 0;
        RSmallerOrEqualto4045.put(0, 0, temp);

        Mat RGreaterThan4045 = new Mat();
        RGreaterThan4045 = RSmallerOrEqualto4045.clone();
        RGreaterThan4045.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] == 0.0)
                temp[i] = (float) 1.0;
            else
                temp[i] =0;
        RGreaterThan4045.put(0, 0, temp);

        normalizedR.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  (temp[i]/(float)12.92);
        normalizedR.put(0, 0, temp);
        Mat tmpR = new Mat();
        tmpR = normalizedR.mul(RSmallerOrEqualto4045);


        normalizedR.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  (float) Math.pow(((temp[i]*(float)12.92)+0.055)/1.055,2.4);
        normalizedR.put(0, 0, temp);
        tArray = normalizedR.mul(RGreaterThan4045);
        tmpR.get(0, 0, temp);
        tArray.get(0, 0,temp1);
        for (int i = 0; i < size; i++)
            temp[i] =  temp[i]+temp1[i];
        tmpR.put(0, 0, temp);



        Mat X = new Mat();
        X = tmpR.clone();
        X.get(0, 0, temp);
        tmpR.get(0, 0, temp1);
        tmpG.get(0, 0, temp2);
        tmpB.get(0, 0, temp3);
        for (int i = 0; i < size; i++)
            temp[i] =  temp1[i]*(float)0.4124564+temp2[i]*(float)0.3575761+temp3[i]*(float)0.1804375;
        X.put(0, 0, temp);

        Mat Y = new Mat();
        Y = tmpR.clone();
        Y.get(0, 0, temp);
        tmpR.get(0, 0, temp1);
        tmpG.get(0, 0, temp2);
        tmpB.get(0, 0, temp3);
        for (int i = 0; i < size; i++)
            temp[i] =  temp1[i]*(float)0.2126729+temp2[i]*(float)0.7151522+temp3[i]*(float)0.0721750;
        Y.put(0, 0, temp);

        Mat Z = new Mat();
        Z = tmpR.clone();
        Z.get(0, 0, temp);
        tmpR.get(0, 0, temp1);
        tmpG.get(0, 0, temp2);
        tmpB.get(0, 0, temp3);
        for (int i = 0; i < size; i++)
            temp[i] =  temp1[i]*(float)0.0193339+temp2[i]*(float)0.1191920+temp3[i]*(float)0.9503041;
        Z.put(0, 0, temp);

        float epsilon = (float) 0.008856;
        float kappa   = (float) 903.3;		//actual CIE standard

        float Xr = (float) 0.9642;	//reference white D50
        float Yr = (float) 1.0;	 	//reference white
        float Zr = (float) 0.8251;	//reference white

        Mat xr = new Mat();
        xr = X.clone();
        X.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  temp1[i]/Xr;
        xr.put(0, 0, temp);

        Mat yr = new Mat();
        yr = X.clone();
        Y.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  temp1[i]/Yr;
        yr.put(0, 0, temp);

        Mat zr = new Mat();
        zr = X.clone();
        Z.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            temp[i] =  temp1[i]/Zr;
        zr.put(0, 0, temp);

        Mat xrGreaterThanEpsilon = new Mat();
        xrGreaterThanEpsilon = xr.clone();
        xr.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] >  epsilon)
                temp[i] = 1;
            else
                temp[i] = 0;
        xrGreaterThanEpsilon.put(0, 0, temp);

        Mat xrSmallerOrEqualtoEpsilon = new Mat();
        xrSmallerOrEqualtoEpsilon = xr.clone();
        xrGreaterThanEpsilon.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] ==  1)
                temp[i] = 0;
            else
                temp[i] = 1;
        xrSmallerOrEqualtoEpsilon.put(0, 0, temp);

        Mat fx = new Mat();
        fx = xr.clone();
        xr.get(0, 0, temp);
        xrGreaterThanEpsilon.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
            temp[i] = (float) (Math.pow(temp[i], 1.0/3.0)*temp1[i]);
        fx.put(0, 0, temp);

        fx.get(0, 0, temp);
        xr.get(0, 0,temp2);
        xrSmallerOrEqualtoEpsilon.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
            temp[i] = (float) (temp[i]+ ((kappa*temp2[i] +16.0)/116.0)*temp1[i]);
        fx.put(0, 0, temp);

        Mat yrGreaterThanEpsilon = new Mat();
        yrGreaterThanEpsilon = yr.clone();
        yr.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] >  epsilon)
                temp[i] = 1;
            else
                temp[i] = 0;
        yrGreaterThanEpsilon.put(0, 0, temp);

        Mat yrSmallerOrEqualtoEpsilon = new Mat();
        yrSmallerOrEqualtoEpsilon = yr.clone();
        yrGreaterThanEpsilon.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] ==  1)
                temp[i] = 0;
            else
                temp[i] = 1;
        yrSmallerOrEqualtoEpsilon.put(0, 0, temp);

        Mat fy = new Mat();
        fy = yr.clone();
        yr.get(0, 0, temp);
        yrGreaterThanEpsilon.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
            temp[i] = (float) (Math.pow(temp[i], 1.0/3.0)*temp1[i]);
        fy.put(0, 0, temp);

        fy.get(0, 0, temp);
        yr.get(0, 0,temp2);
        yrSmallerOrEqualtoEpsilon.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
            temp[i] = (float) (temp[i]+ ((kappa*temp2[i] +16.0)/116.0)*temp1[i]);
        fy.put(0, 0, temp);

        Mat zrGreaterThanEpsilon = new Mat();
        zrGreaterThanEpsilon = zr.clone();
        zr.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] >  epsilon)
                temp[i] = 1;
            else
                temp[i] = 0;
        zrGreaterThanEpsilon.put(0, 0, temp);

        Mat zrSmallerOrEqualtoEpsilon = new Mat();
        zrSmallerOrEqualtoEpsilon = zr.clone();
        zrGreaterThanEpsilon.get(0, 0, temp);
        for (int i = 0; i < size; i++)
            if(temp[i] ==  1)
                temp[i] = 0;
            else
                temp[i] = 1;
        zrSmallerOrEqualtoEpsilon.put(0, 0, temp);

        Mat fz = new Mat();
        fz = zr.clone();
        zr.get(0, 0, temp);
        zrGreaterThanEpsilon.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
            temp[i] = (float) (Math.pow(temp[i], 1.0/3.0)*temp1[i]);
        fz.put(0, 0, temp);

        fz.get(0, 0, temp);
        zr.get(0, 0,temp2);
        zrSmallerOrEqualtoEpsilon.get(0, 0, temp1);
        for (int i = 0; i < size; i++)
            temp[i] = (float) (temp[i]+ ((kappa*temp2[i] +16.0)/116.0)*temp1[i]);
        fz.put(0, 0, temp);

        Mat labImage = new Mat();
        labImage = dsImage.clone();
        dsImage.convertTo(dsImage, CvType.CV_64FC3);
        size = (int) (labImage.total() * labImage.channels());
        double[] temp3D = new double[size];
        dsImage.get(0, 0, temp3D);
        for (int i = 0; i < size; i++)
            temp3D[i] = 0;
        labImage.put(0, 0, temp3D);


        Mat src1 = new Mat(labImage.rows(), labImage.cols(), CvType.CV_32FC1, new Scalar(1));
        Mat src2 = new Mat(labImage.rows(), labImage.cols(), CvType.CV_32FC1, new Scalar(2));
        Mat src3 = new Mat(labImage.rows(), labImage.cols(), CvType.CV_32FC1, new Scalar(3));
        List<Mat> channeled = new ArrayList<>(3);
        Core.split(dsImage, channeled);
        src1 = channeled.get(0);
        src2 = channeled.get(1);
        src3 = channeled.get(2);
        size = (int) (src1.total() * src1.channels());
        double[] temp1D = new double[size];
        src1.get(0, 0, temp1D);
        fy.get(0, 0,temp1);
        for (int i = 0; i < size; i++)
            temp1D[i] = (116.0 * temp1[i] - 16.0);
        src1.put(0, 0, temp1D);

        src2.get(0, 0, temp1D);
        fy.get(0, 0,temp1);
        fx.get(0, 0,temp2);
        for (int i = 0; i < size; i++)
            temp1D[i] = (500.0 * (temp2[i] - temp1[i]));
        src2.put(0, 0, temp1D);

        src3.get(0, 0, temp1D);
        fy.get(0, 0,temp1);
        fz.get(0, 0,temp2);
        for (int i = 0; i < size; i++)
            temp1D[i] = (200.0 * (temp1[i] - temp2[i]));
        src3.put(0, 0, temp1D);

        List<Mat> listMat = Arrays.asList(src1, src2, src3);

        Core.merge(listMat, labImage);
        //Mat truth = new Mat(labImage.rows(), labImage.cols(), CvType.CV_32FC3, new Scalar(1, 2, 3));
//           assertMatEqual(truth, labImage, EPS);
        return labImage;


    }

    private float[][] lobGabor(int ii, int jj) {

        int rows = ii;
        int cols = jj;
        float[][] array1 = new float[rows][cols];
        float[][] array2 = new float[rows][cols];
        float[][] array3 = new float[rows][cols];
        float[] hor = new float[cols];
        float[] ver = new float[rows];
        int value1 = (cols/2)+1;
        float value2 = cols - (cols%2);
        for(float i = 1; i<=cols; i++)
        {
            float res = (i-value1)/value2;
            int j = (int)(i-1);
            hor[j]= res;
//			System.out.println(res);
        }
        int value3 = (rows/2)+1;
        float value4 = rows - (rows%2);
        for(float i = 1; i<=rows; i++)
        {
            float res = (i-value3)/value4;
            int j = (int)(i-1);
            ver[j]= res;
//			System.out.println(res);
        }
        for(int i=0;i<rows;i++)
        {
            for(int j=0;j<cols;j++)
            {
                array1[i][j] = hor[j];
            }
        }

        for(int i=0;i<rows;i++)
        {
            for(int j=0;j<cols;j++)
            {
                array3[i][j] = 1;
                array2[i][j] = ver[i];
            }
        }
        for(int i=0;i<rows;i++)
        {
            System.out.println("\n");
            for(int j=0;j<cols;j++)
            {
//				System.out.println(array2[i][j]);
            }
        }
        for(int i=0;i<rows;i++)
        {
            for(int j=0;j<cols;j++)
            {
                if(Math.pow(array1[i][j], 2) + Math.pow(array2[i][j], 2) > 0.25)
                {
                    array3[i][j] = 0;
                }
            }
        }
        for(int i=0;i<rows;i++)
        {
            for(int j=0;j<cols;j++)
            {
                array1[i][j] = array3[i][j]*array1[i][j];
                array2[i][j] = array3[i][j]*array2[i][j];
//				System.out.print(array2[i][j] + " ");
            }
//			System.out.println();
        }

        float[][] quad1 = new float[rows/2][cols/2];
        float[][] quad2 = new float[rows/2][cols/2];
        float[][] quad3 = new float[rows-(rows/2)][cols-(cols/2)];
        float[][] quad4 = new float[rows-(rows/2)][cols-(cols/2)];
        for(int i=0;i<rows;i++)
        {
            for(int j=0;j<cols;j++)
            {
                if(i<rows/2 && j<cols/2 )
                    quad1[i][j] = array1[i][j];
                else if(i<rows/2 && j>=cols/2)
                    quad2[i][j-cols/2] = array1[i][j];
                else if(i>=rows/2 && j<cols/2)
                    quad3[i-rows/2][j] = array1[i][j];
                else if(i>=rows/2 && j>=cols/2)
                    quad4[i-rows/2][j-cols/2] = array1[i][j];
            }
        }

        for(int i=0;i<(rows-rows/2);i++)
        {
            for(int j=0;j<(cols-cols/2);j++)
            {
                array1[i][j] = quad4[i][j];
                array1[i][j+cols/2] = quad3[i][j];
            }
        }

        for(int i=0;i<rows/2;i++)
        {
            for(int j=0;j<cols/2;j++)
            {
                array1[i+rows-rows/2][j+cols-cols/2] = quad1[i][j];
                array1[i+rows-rows/2][j] = quad2[i][j];
            }
        }
        for(int i=0;i<rows;i++)
        {
            for(int j=0;j<cols;j++)
            {
//				System.out.print(array1[i][j] + " ");
            }
//			System.out.println();
        }

        for(int i=0;i<rows;i++)
        {
            for(int j=0;j<cols;j++)
            {
                if(i<rows/2 && j<cols/2 )
                    quad1[i][j] = array2[i][j];
                else if(i<rows/2 && j>=cols/2)
                    quad2[i][j-cols/2] = array2[i][j];
                else if(i>=rows/2 && j<cols/2)
                    quad3[i-rows/2][j] = array2[i][j];
                else if(i>=rows/2 && j>=cols/2)
                    quad4[i-rows/2][j-cols/2] = array2[i][j];
            }
        }

        for(int i=0;i<(rows-rows/2);i++)
        {
            for(int j=0;j<(cols-cols/2);j++)
            {
                array2[i][j] = quad4[i][j];
                array2[i][j+cols/2] = quad3[i][j];
            }
        }

        for(int i=0;i<rows/2;i++)
        {
            for(int j=0;j<cols/2;j++)
            {
                array2[i+rows-rows/2][j+cols-cols/2] = quad1[i][j];
                array2[i+rows-rows/2][j] = quad2[i][j];
            }
        }

        float[][] radius = new float[rows][cols];
        for(int i=0;i<rows;i++)
        {
            for(int j=0;j<cols;j++)
            {
                radius[i][j] = (float) Math.sqrt(Math.pow(array1[i][j], 2)+Math.pow(array2[i][j], 2));
            }
        }
        radius[0][0] = 1;
        float[][] LG = new float[rows][cols];
        float omega0 = (float) 0.0020;
        float sigmaF = (float) 6.2000;
        for(int i=0;i<rows;i++)
        {
            for(int j=0;j<cols;j++)
            {
                float var = (float) Math.exp(-(Math.pow(Math.log(radius[i][j]/omega0), 2))/(2*Math.pow(sigmaF, 2))) ;
                LG[i][j] = var;
            }
        }

        LG[0][0] = 0;


        return LG;

    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException
    {
        System.out.println("ENTERED FUNCTION");
        BitmapFactory.Options o = new BitmapFactory.Options();

        o.inJustDecodeBounds = true;

        System.out.println("ENTERED FUNCTION3");
        BitmapFactory.decodeStream(getContentResolver()
                .openInputStream(selectedImage), null, o);

        System.out.println("ENTERED FUNCTION4");
        final int REQUIRED_SIZE = 72;

        int width_tmp = o.outWidth, height_tmp = o.outHeight;

        int scale = 1;

        System.out.println("ENTERED FUNCTION1");

        while (true)
        {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
            {
                break;
            }
            width_tmp /= 2;

            height_tmp /= 2;

            scale *= 2;
        }

        System.out.println("ENTERED FUNCTION2");
        BitmapFactory.Options o2 = new BitmapFactory.Options();

        o2.inSampleSize = scale;

        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
                .openInputStream(selectedImage), null, o2);
        System.out.println("BYE");
        return bitmap;
    }

    private void displayImg(Mat mat) {

        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);

        Utils.matToBitmap(mat, bitmap);

//        imageView.setImageBitmap(bitmap);

    }

    Mat sampledImg;
    private void loadImage(String path) {

        Mat originalImage = Imgcodecs.imread(path);
        if(originalImage==null)
        {
            System.out.println("This is an empty path");
        }
        else{
            System.out.println("This is a non empty image");
        }
//        Log.i("aaa2", originalImage.dump());
        Mat rgbImg = new Mat();

        Imgproc.cvtColor(originalImage, rgbImg, Imgproc.COLOR_BGR2RGB);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int mobile_width = (int) size.x;
        int mobile_height = (int) size.y;

        sampledImg = new Mat();

        double downSampleRatio = calculateSubSimpleSize(rgbImg, mobile_width, mobile_height);

        Imgproc.resize(rgbImg, sampledImg, new Size(), downSampleRatio, downSampleRatio, Imgproc.INTER_AREA);

        try{
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    sampledImg = sampledImg.t();
                    Core.flip(sampledImg, sampledImg, 1);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    sampledImg = sampledImg.t();
                    Core.flip(sampledImg, sampledImg, 0);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private double calculateSubSimpleSize(Mat src, int mobile_width, int mobile_height) {

        final int width = src.width();
        final int height = src.height();
        double inSampleSize = 1;

        if(height > mobile_height || width > mobile_width)
        {
            final double heightRatio = (double) mobile_height/(double)height;
            final double widthRatio = (double) mobile_width/(double)width;
            inSampleSize = heightRatio<widthRatio ? height:width;
        }
        return inSampleSize;
    }



}
