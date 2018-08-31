package com.example.hp.safeselfie;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class impose extends AppCompatActivity {

    private Button btnCapture;
    private TextureView textureView;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    float x, y, z, yy;


    private String cameraId;
    private CameraManager manager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;

    //Save to file
    private File file;
    private String name;
    private String name1;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    Mat grab;
    Bitmap res;

    CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            System.out.println("Camera device is about to be entered");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
            System.out.println("Camera device is about to be entered 1");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
//            cameraDevice.close();
            cameraDevice = null;
            System.out.println("Camera device is about to be entered 2");
        }
    };

    SensorManager sensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.impose);

        sensorManager =
                (SensorManager) getSystemService(SENSOR_SERVICE);

        if(OpenCVLoader.initDebug())
        {
            Toast.makeText(getApplicationContext(), "OpenCV loaded successfully", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "OpenCV not loaded successfully", Toast.LENGTH_SHORT).show();

        textureView = findViewById(R.id.textureView);
        //Use 'assert' to check expression true or false

        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        System.out.println("I m here 2");
        btnCapture = findViewById(R.id.btnCapture);
        System.out.println("I m here 4");
        getAngle();
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("I m here 3");
                takePicture();

                System.out.println("Angles are here");
                System.out.println("X is "+x);
                System.out.println("Y is "+y);
                System.out.println("Z is "+z);
            }
        });
//        btnCapture.setOnClickListener(null);


    }


    private void getAngle() {

        Sensor rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        // Create a listener
        SensorEventListener rvListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float[] rotationMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(
                        rotationMatrix, sensorEvent.values);

                // Remap coordinate system
                float[] remappedRotationMatrix = new float[16];
                SensorManager.remapCoordinateSystem(rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remappedRotationMatrix);

                // Convert to orientations
                float[] orientations = new float[3];
                SensorManager.getOrientation(remappedRotationMatrix, orientations);

                for(int i = 0; i < 3; i++) {
                    orientations[i] = (float)(Math.toDegrees(orientations[i]));
                }

                x = orientations[0];
                y = orientations[1];
                z = orientations[2];


            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
//        rvListener.onSensorChanged(null);


        // Register it
        sensorManager.registerListener(rvListener,
                rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

//        disableAccelerometerListener(rvListener);


    }

    public void disableAccelerometerListener(SensorEventListener listenerFromActivity)
    {
        try
        {
            if(listenerFromActivity!=null)
                sensorManager.unregisterListener(listenerFromActivity);
            else
            {

                System.out.println("listenerFromActivity is null");
            }
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    private void takePicture() {
        System.out.println("Take picture entered");
        if(cameraDevice == null) {
            System.out.println("Take picture entered failed");
            return;
        }
        manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if(characteristics !=null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

            //Capture image with Custom Size
            int width = 400;
            int height = 400;
            if(jpegSizes !=null && jpegSizes.length > 0)
            {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Check Orientation Base on Device
            int mSensorOrientation = getSensorOrientation();
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, mSensorOrientation);

            //new UUID();
            name = Environment.getExternalStorageDirectory()+"/"+ UUID.randomUUID().toString()+".jpg";
            file =  new File(name);
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally {
                        {
                            if(image != null)
                                image.close();
                            segment();
//                            cameraDevice.close();
                            Intent myintent = new Intent(impose.this,front.class);
                            ByteArrayOutputStream bs = new ByteArrayOutputStream();
                            res.compress(Bitmap.CompressFormat.PNG, 50, bs);
                            myintent.putExtra("segment", bs.toByteArray());
                            myintent.putExtra("X", x);
                            myintent.putExtra("Y", y);
                            myintent.putExtra("Z", z);
                            System.out.println("Am i here Before");
                            System.out.println("Angles are here");
                            System.out.println("X is "+x);
                            System.out.println("Y is "+y);
                            System.out.println("Z is "+z);
                            startActivity(myintent);

                        }
                    }
                }

                private void save(byte[] bytes) throws IOException{
                    OutputStream outputStream = null;
                    try{
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);

                    }
                    finally {
                        if(outputStream != null)
                            outputStream.close();

                    }
                }

            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(impose.this, "Saved "+file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            },mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
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


    private void segment() {
        if(isStoragePermissionGranted()) {
            System.out.println("Harsh here");
            Bitmap bm = BitmapFactory.decodeFile(String.valueOf(new File(name)));
            Matrix matrix = new Matrix();
            matrix.postRotate(270);

            Bitmap rotatedBitmap1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

            Mat temp = new Mat();
            Mat temp1 = new Mat();
            Utils.bitmapToMat(rotatedBitmap1, temp);
            Utils.bitmapToMat(rotatedBitmap1, temp1);
            System.out.println(temp.type());
            Imgproc.resize(temp, temp, new org.opencv.core.Size(400, 600));
            Imgproc.resize(temp1, temp1, new org.opencv.core.Size(400, 600));

            //Convert to CIELAB for CLAHE AND convert back
            Imgproc.cvtColor(temp, temp, Imgproc.COLOR_BGR2RGB);
            Imgproc.cvtColor(temp, temp, Imgproc.COLOR_RGB2Lab);
            List<Mat> lab_list = new ArrayList(3);
            Core.split(temp,lab_list);
            Mat lab;
            lab = lab_list.get(0);
            CLAHE clahe = Imgproc.createCLAHE();
            clahe.apply(lab, lab);
//            Mat new1 = Mat.zeros(lab.size(), lab.type());
            lab_list.set(0, lab);
            Core.merge(lab_list,temp);
            Imgproc.cvtColor(temp, temp, Imgproc.COLOR_Lab2BGR);

//            Imgproc.cvtColor(temp, temp, Imgproc.COLOR_RGB2Lab);
            Bitmap rotatedBitmap = Bitmap.createBitmap(temp.width(), temp.height(), Bitmap.Config.RGB_565);
            Bitmap rotatedBitmap2 = Bitmap.createBitmap(temp1.width(), temp1.height(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(temp, rotatedBitmap);
            Utils.matToBitmap(temp1, rotatedBitmap2);


            Bitmap output, output1;
            Matrix matrix1 = new Matrix();
            matrix1.preScale(-1.0f, 1.0f);
            output = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), matrix1, true);
            output1 = Bitmap.createBitmap(rotatedBitmap2, 0, 0, rotatedBitmap2.getWidth(), rotatedBitmap2.getHeight(), matrix1, true);
//            output1 = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), matrix1, true);
            Mat image = new Mat();
            Utils.bitmapToMat(output, image);
            Mat image1 = new Mat();
            Utils.bitmapToMat(output1, image1);

            Paint myRectPaint = new Paint();
            myRectPaint.setStrokeWidth(5);
            myRectPaint.setColor(Color.GREEN);
            myRectPaint.setStyle(Paint.Style.STROKE);

            Bitmap tempBitmap = Bitmap.createBitmap(output.getWidth(), output.getHeight(), Bitmap.Config.RGB_565);
            Canvas tempCanvas = new Canvas(tempBitmap);
            tempCanvas.drawBitmap(output, 0, 0, null);

            FaceDetector faceDetector = new
                    FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                    .build();
            if(!faceDetector.isOperational()){
//                new AlertDialog.Builder(v.getContext()).setMessage("Could not set up the face detector!").show();
                System.out.println("face detector not operational");
                return;
            }

            Frame frame = new Frame.Builder().setBitmap(output).build();
            SparseArray<Face> faces = faceDetector.detect(frame);

            System.out.println("Face size is "+faces.size());
            int x1=0, y1=0, x2=0, y2=0;
            for(int i=0; i<faces.size(); i++) {
                Face thisFace = faces.valueAt(i);
                x1 = (int) thisFace.getPosition().x;
                y1 = (int) thisFace.getPosition().y;
                x2 = (int) (x1 + thisFace.getWidth());
                y2 = (int) (y1 + thisFace.getHeight());
                tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2+10), 2, 2, myRectPaint);
            }

            System.out.println("Values are "+x1+" "+y1+" "+x2+" "+y2);

            Point p1 = new Point(x1,y1);
            Point p2 = new Point(x2,y2+10);
            Point p3 = new Point(0, y2);
            Point p4 = new Point(image.width(), image.height());
            Point p5 = new Point(x1, y2-100);
            Point p6 = new Point(x2, y2+100);
            Rect rect  = new Rect(p1, p2);
            Rect rect1 = new Rect(p3, p4);
            Rect rect2 = new Rect(p5, p6);
            System.out.println("Here rect is "+rect);
            Mat mask = new Mat();
            Mat fgdModel = new Mat();
            Mat bgdModel = new Mat();
            Imgproc.cvtColor(image, image, Imgproc.COLOR_RGBA2RGB);
            Imgproc.grabCut(image, mask, rect, bgdModel, fgdModel, 2, Imgproc.GC_INIT_WITH_RECT);

            Core.convertScaleAbs(mask, mask, 100, 0);
            Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2RGB,3);
            System.out.println("Image size is "+image.size());
            System.out.println("Mask size is "+ mask.size());
//            System.out.println(mask.dump());

            grab = Mat.zeros(image.size(),image.type());
//            for(int i=0;i<image.width();i++)
//                for(int j=0;j<image.height();j++)
//                {
//                    if(mask.get(j,i)[0] == 255)
//                    {
//                        grab.put(j,i,image.get(j,i));
//                    }
//                }

            Mat mask2 = new Mat();
            Mat fgdModel2 = new Mat();
            Mat bgdModel2 = new Mat();
            Imgproc.cvtColor(image, image, Imgproc.COLOR_RGBA2RGB);
            Imgproc.grabCut(image, mask2, rect2, bgdModel2, fgdModel2, 2, Imgproc.GC_INIT_WITH_RECT);

            Core.convertScaleAbs(mask2, mask2, 100, 0);
            Imgproc.cvtColor(mask2, mask2, Imgproc.COLOR_GRAY2RGB,3);
            System.out.println("Image size is "+image.size());
            System.out.println("Mask size is "+ mask2.size());
//            System.out.println(mask.dump());

//            Mat grab = Mat.zeros(image.size(),image.type());
//            for(int i=0;i<image.width();i++)
//                for(int j=0;j<image.height();j++)
//                {
//                    if(mask2.get(j,i)[0] == 255)
//                    {
//                        grab.put(j,i,image.get(j,i));
//                    }
//                }



            Mat mask1 = new Mat();
            Mat fgdModel1 = new Mat();
            Mat bgdModel1 = new Mat();
            Imgproc.cvtColor(image, image, Imgproc.COLOR_RGBA2RGB);
            Imgproc.grabCut(image, mask1, rect1, bgdModel1, fgdModel1, 2, Imgproc.GC_INIT_WITH_RECT);

            Core.convertScaleAbs(mask1, mask1, 100, 0);
            Imgproc.cvtColor(mask1, mask1, Imgproc.COLOR_GRAY2RGB,3);

//            Mat grab = Mat.zeros(image.size(),image.type());
//            for(int i=0;i<image.width();i++)
//                for(int j=0;j<image.height();j++)
//                {
//                    if(mask1.get(j,i)[0] == 255)
//                    {
//                        grab.put(j,i,image.get(j,i));
//                    }
//                }

            Mat mask3 = Mat.zeros(mask.size(), mask.type());
            for(int i=0;i<mask3.width();i++)
                for(int j=0;j<mask3.height();j++)
                {
                    if(mask1.get(j,i)[0] == 255)
                    {
                        mask3.put(j,i,mask1.get(j,i));
                    }
                    if(mask.get(j,i)[0] == 255)
                    {
                        mask3.put(j,i,mask.get(j,i));
                    }
                    if(mask2.get(j,i)[0] == 255)
                    {
                        mask3.put(j,i,mask2.get(j,i));
                    }

                }

//            int erosion_size = 7;
//            int dilation_size = 5;
//
//            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(2*erosion_size + 1, 2*erosion_size+1));
//            Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(2*dilation_size + 1, 2*dilation_size+1));
////            Imgproc.erode(mask3, mask3, element);
//            Imgproc.dilate(mask3, mask3, element1);
//            Point seedPoint = new Point((x1+x2)/2, (y1+y2)/2);
//            Rect rect7 = new Rect();
//            Imgproc.floodFill(mask3, mask3, seedPoint, new Scalar(255, 255, 255));

//            Rect rect7 = new Rect();
//            Mat floodfilled = Mat.zeros(mask3.rows() + 2, mask3.cols() + 2, CvType.CV_8U);
//            Imgproc.floodFill(mask3, floodfilled, seedPoint, new Scalar(255, 255, 255), rect7, new Scalar(0), new Scalar(0), 4 + (255 << 8) + Imgproc.FLOODFILL_FIXED_RANGE);
//
//            Core.subtract(floodfilled, Scalar.all(0), floodfilled);
//            Rect roi = new Rect(0, 0, mask3.cols() - 2, mask3.rows() - 2);
//            Mat temp0 = new Mat();
//
//            floodfilled.submat(roi).copyTo(temp0);
//
//            mask3 = temp0;



            Imgproc.cvtColor(image1, image1, Imgproc.COLOR_RGBA2RGB);

            for(int i=0;i<mask3.width();i++)
                for(int j=0;j<mask3.height();j++)
                {
                    if(mask3.get(j,i)[0] == 255)
                    {
                        grab.put(j,i,image1.get(j,i));
                    }
                }


            System.out.println("In fifth Activity X is "+x+" and y is "+y+" and z is "+z+" and yy is "+yy);
//            double xSkew = x/1800;
//            double xSkew = 0;
//            double ySkew = 0;
//            double ySkew = y/180;
            // According to the skew ratio of the picture, calculate the size of the image after the transformation.
//            int xAfterSkew = (int)(grab.width() * (1 + xSkew));
//            int yAfterSkew = (int)(grab.height() * (1 + ySkew));
//
//            Bitmap skewBitmap = Bitmap.createBitmap(xAfterSkew, yAfterSkew, Bitmap.Config.RGB_565);
//
//            Canvas skewCanvas = new Canvas(skewBitmap);
//
//            Matrix skewMatrix = new Matrix();
//
//             Set x y skew value.
//            skewMatrix.setSkew((float)xSkew, (float)ySkew, grab.width()/2, grab.height()/2);
//
//            Paint paint = new Paint();
//            Bitmap bmps = Bitmap.createBitmap(grab.width(), grab.height(), Bitmap.Config.RGB_565);
//            Utils.matToBitmap(grab, bmps);
//            skewCanvas.drawBitmap(bmps, skewMatrix, paint);
//            return skewBitmap;
//            Mat grabShear = new Mat();
//            Utils.bitmapToMat(skewBitmap, grabShear);

            Bitmap bmpd = Bitmap.createBitmap(grab.width(), grab.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(grab, bmpd);
            Point l1 = new Point(0,0);
            Point l2 = new Point(grab.width(), 0);
            Point l3 = new Point(0, grab.height());
            Point l4 = new Point(grab.width(), grab.height());
            float theta = 0;

//            theta = y-yy;
//            //Check theta
//            if(y>=0)
//            {
//                if(yy>0)
//                {
//                    theta = Math.abs(y-yy);
//                }
//                else{
//                    theta = y-yy;
//                    if(theta>=90)
//                        theta -=90;
//                }
//            }
//            else
//            if(y<0)
//            {
//                if(yy>0)
//                {
//                    theta = Math.abs(y-yy);
//                }
//                else{
//                    theta = Math.abs(y-yy);
//                    if(theta>=90)
//                        theta -=90;
//                }
//            }
//


            res = warp(bmpd, l1, l2, l3, l4, theta);

            Utils.bitmapToMat(res, grab);


            System.out.println("Grab size is "+grab.size());

            System.out.println("Mask details are "+mask3.size()+" "+mask3.type()+" "+mask3.channels());
            System.out.println("Bitmap details are "+tempBitmap.getWidth()+"x"+tempBitmap.getHeight());
//            Bitmap tempBitmap1 = Bitmap.createBitmap(mask3.width(), mask3.height(), Bitmap.Config.RGB_565);
//            Utils.matToBitmap(grabShear, tempBitmap);

//            long addr = getIntent().getLongExtra("sal", 0);
//            long addr1 = getIntent().getLongExtra("orig",0);
//            long addr2 = grab.getNativeObjAddr();
//            long addr3 = image1.getNativeObjAddr();
//            Bitmap tmpo = Bitmap.createBitmap(image1.width(), image1.height(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(image1, tmpo);
//            Intent myintent = new Intent(fifthActivity.this,sixthActivity.class);
//            myintent.putExtra("sal", addr);
//            myintent.putExtra("orig", addr1);
//            ByteArrayOutputStream bs = new ByteArrayOutputStream();
//            res.compress(Bitmap.CompressFormat.PNG, 50, bs);
//            myintent.putExtra("segment", bs.toByteArray());
////            myintent.putExtra("segment", res);
//            ByteArrayOutputStream bs1 = new ByteArrayOutputStream();
//            tmpo.compress(Bitmap.CompressFormat.PNG, 50, bs1);
//            myintent.putExtra("orig1", bs1.toByteArray());
////            myintent.putExtra("orig1", tmpo);
//            startActivity(myintent);


//            imageView.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
//            imageView.setImageBitmap(tmp);
        }

    }

    public Bitmap warp(Bitmap image, Point topLeft, Point topRight, Point bottomLeft, Point bottomRight, float theta) {
        int resultWidth = (int)(topRight.x - topLeft.x);
        int bottomWidth = (int)(bottomRight.x - bottomLeft.x);
        if(bottomWidth > resultWidth)
            resultWidth = bottomWidth;


        int resultHeight = (int)(bottomLeft.y - topLeft.y);
        int bottomHeight = (int)(bottomRight.y - topRight.y);
        if(bottomHeight > resultHeight)
            resultHeight = bottomHeight;


        resultHeight = (int) ((int)(bottomLeft.y - topLeft.y)*Math.cos(theta));
        resultHeight = Math.abs(resultHeight);

        Mat inputMat = new Mat(image.getHeight(), image.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(image, inputMat);
        System.out.println("Result width is "+resultWidth+" and result height is "+resultHeight+" and theta is "+theta);
        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC1);

        List<Point> source = new ArrayList<>();
        source.add(topLeft);
        source.add(topRight);
        source.add(bottomLeft);
        source.add(bottomRight);
        Mat startM = Converters.vector_Point2f_to_Mat(source);

        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(resultWidth, 0);
        Point ocvPOut3 = new Point(0, resultHeight);
        Point ocvPOut4 = new Point(resultWidth, resultHeight);
        List<Point> dest = new ArrayList<>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform, new org.opencv.core.Size(resultWidth, resultHeight));

        Bitmap output = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputMat, output);
        return output;
    }


    private void createCameraPreview() {

        try {
            System.out.println("Create camera preview entered");
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(1080, 1920);
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(impose.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(impose.this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    private void openCamera() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[1];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallBack, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable()) {
            openCamera();
        }
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    public int getSensorOrientation() throws CameraAccessException {
        return manager.getCameraCharacteristics(cameraId).get(
                CameraCharacteristics.SENSOR_ORIENTATION);
    }
}

