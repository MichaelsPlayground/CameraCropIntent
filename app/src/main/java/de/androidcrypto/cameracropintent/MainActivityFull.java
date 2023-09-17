package de.androidcrypto.cameracropintent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivityFull extends AppCompatActivity {

    private final String TAG ="MainActivity";
    private Button openFromGallery, takePhoto;
    private ImageView ivG02;
    private Uri image_uri;

    Button btn01, btn02, btn03, btn04,
            btn05, btn06, btn07, btn08;

    TextView tvG02;



    public static final int CAMERA_IMAGE_PERM_CODE = 101;

    String currentPhotoPath;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openFromGallery = findViewById(R.id.btnOpenGallery);
        takePhoto = findViewById(R.id.btnTakePhoto);
Log.d(TAG, "s");

        btn01 = findViewById(R.id.btnG02B01);
        btn02 = findViewById(R.id.btnG02B02);
        btn03 = findViewById(R.id.btnG02B03); // take a photo via intent and full resolution
        btn04 = findViewById(R.id.btnG02B04);
        btn05 = findViewById(R.id.btnG02B05);
        btn06 = findViewById(R.id.btnG02B06);
        btn07 = findViewById(R.id.btnG02B07);
        btn08 = findViewById(R.id.btnG02B08);

        tvG02 = findViewById(R.id.tvG02);
        ivG02 = findViewById(R.id.ivG02);

        askPermissions();
        /*
        WRITE_EXTERNAL_STORAGE is deprecated (and is not granted) when targeting Android 13+.
        If you need to write to shared storage, use the MediaStore.createWriteRequest intent.
         */

        openFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryActivityResultLauncher.launch(galleryIntent);
            }
        });

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "takePhoto.setOnClickListener");

                // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image
                openCamera();

            }
        });

    }

    // gallery part

    ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri image_uri = result.getData().getData();
                        ivG02.setImageURI(image_uri);
                        // add the new image to the gallery content
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(image_uri);
                        context.sendBroadcast(mediaScanIntent);
                    }
                }
            });

    // camera activity

    private void openCamera() {
        Log.d(TAG, "openCamera");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        Log.d(TAG, "cameraActivityResultLauncher.launch(cameraIntent)");
        cameraActivityResultLauncher.launch(cameraIntent);
    }

    ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "ActivityResultLauncher<Intent> cameraActivityResultLauncher");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "if (result.getResultCode() == Activity.RESULT_OK)");
                        Log.d(TAG, "image_uri: " + image_uri);
                        Bitmap inputImage = uriToBitmap(image_uri);
                        Bitmap rotated = rotateBitmap(inputImage);
                        ivG02.setImageBitmap(rotated);
                    }
                }
            });

    //TODO takes URI of the image and returns bitmap
    private Bitmap uriToBitmap(Uri selectedFileUri) {
        try {
            Log.d(TAG, "uriToBitmap with Uri: " + selectedFileUri);
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            Log.d(TAG, "return image");
            return image;
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
            e.printStackTrace();
        }
        Log.d(TAG, "return null");
        return  null;
    }

    //TODO rotate image if image captured on samsung devices
    //TODO Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
    @SuppressLint("Range")
    public Bitmap rotateBitmap(Bitmap input){
        Log.d(TAG, "rotateBitmap");
        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = getContentResolver().query(image_uri, orientationColumn, null, null, null);
        int orientation = -1;
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
        }
        Log.d("tryOrientation",orientation+"");
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(orientation);
        Log.d(TAG, "before Bitmap cropped");
        Bitmap cropped = Bitmap.createBitmap(input,0,0, input.getWidth(), input.getHeight(), rotationMatrix, true);
        return cropped;
    }



    // section take a photo in full resolution and store in external app's public directory start

    // initial permissions

    private void askPermissions() {
        Log.d(TAG, "askPermissions");
        //TODO ask for permission of camera upon first launch of application
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)");
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
                Log.d(TAG, "if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED");
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, CAMERA_IMAGE_PERM_CODE);
            }
        }
    }

    // ==============================================================================

}