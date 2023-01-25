package com.example.apprestrictor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.Manifest;
import android.os.Environment;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.apprestrictor.ml.Model;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FaceDetection extends CameraActivity {
    CameraBridgeViewBase cameraBridgeViewBase;
    CascadeClassifier cascadeClassifier;
    File cascFile;
    private Mat mRgba, mGray;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        getPermission();

        cameraBridgeViewBase=findViewById(R.id.cameraView);
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseCallback);
        } else{
            try{
                baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        //enabling front camera
        int numberOfCameras = Camera.getNumberOfCameras();
        if(numberOfCameras>1){
            cameraBridgeViewBase.setCameraIndex(1); // front camera
        }else{
            cameraBridgeViewBase.setCameraIndex(0); // back camera
        }
        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                mRgba = new Mat();
                mGray = new Mat();
            }

            @Override
            public void onCameraViewStopped() {
                mRgba.release();
                mGray.release();
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                mRgba = inputFrame.rgba();
                mGray = inputFrame.gray();

                // Detect faces
                MatOfRect faces = new MatOfRect();
                cascadeClassifier.detectMultiScale(mRgba, faces);

                // Draw rectangles around the faces
                for (Rect rect : faces.toArray()) {
                    Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x +
                            rect.width, rect.y + rect.height), new Scalar(255, 255, 255));
                    takePicture();
                }
                return mRgba;
            }
        });
    }

    public void model(){

    }
    private void takePicture() {
        // Convert the Mat to a Bitmap
        Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, bmp);

        // Resize the image to match the model's expected dimensions
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, 128, 128, true);

        // Convert the image to RGB format
        Bitmap rgbBitmap = resizedBitmap.copy(Bitmap.Config.RGB_565, true);

        float[] floatValues = new float[resizedBitmap.getWidth() * resizedBitmap.getHeight() * 3];
        int[] intValues = new int[resizedBitmap.getWidth() * resizedBitmap.getHeight()];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0,
                resizedBitmap.getWidth(), resizedBitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
            floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
        }

        try {
            Model model = Model.newInstance(this);

            // Creates inputs for reference.
            TensorBuffer input = TensorBuffer.createFixedSize(new int[]{1, resizedBitmap.getWidth(),
                    resizedBitmap.getHeight(), 3}, DataType.FLOAT32);
            input.loadArray(floatValues);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(input);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] floatValue = outputFeature0.getFloatArray();
            int[] intValue = new int[floatValue.length];
            for (int i = 0; i < floatValue.length; i++) {
                intValue[i] = Math.round(floatValue[i]);
            }

            for (int i = 0; i < intValue.length; i++) {
                if (intValue[i] >= 1 && intValue[i] <= 5) {
                    Log.d("age", String.valueOf(intValue[i]));
                    finish();
                }
                if (intValue[i] >= 6 && intValue[i]<= 10) {
                    Log.d("age", String.valueOf(intValue[i]));
                    finish();
                }
                if (intValue[i] >= 11 && intValue[i] <= 17) {
                    Log.d("age", String.valueOf(intValue[i]));
                    finish();
                }
                if (intValue[i] >= 18 && intValue[i] <= 23) {
                    Log.d("age", String.valueOf(intValue[i]));
                    finish();
                    String packageName = "com.google.android.youtube";
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    startActivity(launchIntent);
                }
                if (intValue[i] >= 24 && intValue[i] <= 30) {
                    Log.d("age", String.valueOf(intValue[i]));
                    finish();
                    String packageName = "com.google.android.youtube";
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    startActivity(launchIntent);
                }
                if (intValue[i] >= 31 && intValue[i] <= 40) {
                    Log.d("age", String.valueOf(intValue[i]));
                    finish();
                    String packageName = "com.google.android.youtube";
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    startActivity(launchIntent);
                }
                if (intValue[i] >= 41 && intValue[i] <= 60) {
                    Log.d("age", String.valueOf(intValue[i]));
                    finish();
                    String packageName = "com.google.android.youtube";
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    startActivity(launchIntent);
                }
            }
            // Releases model resources.
            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }
    public void getPermission(){
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            getPermission();
        }
    }

    private BaseLoaderCallback baseCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                {
                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    cascFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");

                    FileOutputStream fos = new FileOutputStream(cascFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1){
                        fos.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    fos.close();

                    cascadeClassifier = new CascadeClassifier(cascFile.getAbsolutePath());

                    if(cascadeClassifier.empty()){
                        cascadeClassifier = null;
                    } else{
                        cascadeDir.delete();
                    } cameraBridgeViewBase.enableView();
                }
                break;

                default:{
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}
