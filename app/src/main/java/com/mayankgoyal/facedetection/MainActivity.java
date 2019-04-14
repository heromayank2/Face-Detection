package com.mayankgoyal.facedetection;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.mayankgoyal.facedetection.Helper.GraphicOverlay;
import com.mayankgoyal.facedetection.Helper.RectOverlay;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }
    AlertDialog waitingDialog;
    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    CameraView cameraView;
    GraphicOverlay graphicOverlay;
    Button btnDetect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView=(CameraView)findViewById(R.id.camera_view);
        graphicOverlay=(GraphicOverlay)findViewById(R.id.graphic_overlay);
        btnDetect=(Button)findViewById(R.id.btn_detect);
        waitingDialog= new SpotsDialog.Builder().setContext(MainActivity.this)
                .setMessage("Please Wait")
                .setCancelable(false)
                .build();
        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
                graphicOverlay.clear();
            }
        });
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
            Bitmap bitmap =cameraKitImage.getBitmap();
            bitmap=Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
            cameraView.stop();

            runFaceDetector(bitmap);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

    private void runFaceDetector(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();


        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        processFaceResult(firebaseVisionFaces);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void processFaceResult(List<FirebaseVisionFace> firebaseVisionFaces) {
        int count=0;
        for(FirebaseVisionFace face:firebaseVisionFaces){
            Rect bounds=face.getBoundingBox();
            //Draw Rectangle
            RectOverlay rect=new RectOverlay(graphicOverlay,bounds);
            graphicOverlay.add(rect);
            count++;
        }
        waitingDialog.dismiss();
        Toast.makeText(MainActivity.this,String.format("Detected %d faces",count),Toast.LENGTH_SHORT).show();

    }
}
//Todo: can be used for later purposes
//for (FirebaseVisionFace face : faces) {
//    Rect bounds = face.getBoundingBox();
//    float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
//    float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees
//
//    // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
//    // nose available):
//    FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
//    if (leftEar != null) {
//        FirebaseVisionPoint leftEarPos = leftEar.getPosition();
//    }
//
//    // If contour detection was enabled:
//    List<FirebaseVisionPoint> leftEyeContour =
//            face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
//    List<FirebaseVisionPoint> upperLipBottomContour =
//            face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();
//
//    // If classification was enabled:
//    if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
//        float smileProb = face.getSmilingProbability();
//    }
//    if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
//        float rightEyeOpenProb = face.getRightEyeOpenProbability();
//    }
//
//    // If face tracking was enabled:
//    if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
//        int id = face.getTrackingId();
//    }
//}