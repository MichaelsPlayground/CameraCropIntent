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
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private final String TAG ="MainActivity";
    private Button openFromGallery, takePhoto, cropImage;
    private ImageView ivG02;
    private Uri image_uri;

    Button btn01, btn02, btn03, btn04,
            btn05, btn06, btn07, btn08;

    TextView tvG02;



    public static final int CAMERA_IMAGE_PERM_CODE = 101;
    public static final int CAMERA_VIDEO_PERM_CODE = 102;
    String currentPhotoPath;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openFromGallery = findViewById(R.id.btnOpenGallery);
        takePhoto = findViewById(R.id.btnTakePhoto);
        cropImage = findViewById(R.id.btnCropPhoto);

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

        cropImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "cropImage.setOnClickListener");
                cropTheImage();
            }
        });


/*
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "takePhoto.setOnClickListener");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    Log.d(TAG, "if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)");
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        Log.d(TAG, "if (checkSelfPermission(Manifest.permission.CAMERA)");
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        Log.d(TAG, "requestPermissions(permission, 112)");
                        requestPermissions(permission, 112);
                    }
                    else {
                        Log.d(TAG, "else if (checkSelfPermission(Manifest.permission.CAMERA)");
                        openCamera();
                    }
                }
                else {
                    Log.d(TAG, "else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)");
                    openCamera();
                }
            }
        });
*/










        btn03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // take a photo in full resolution and view in gallery
                context = v.getContext();
                //verifyPermissionsLow();
                dispatchTakePictureIntentFullGallery();
            }
        });






        btn01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Capture a picture and return it
                // https://developer.android.com/guide/components/intents-common
                // https://developer.android.com/reference/android/provider/MediaStore#constants_1
                // https://developer.android.com/training/camera/photobasics
                /*
                <uses-feature android:name="android.hardware.camera"
                  android:required="true" />
                 */
                /*
                If your application uses, but does not require a camera in order to function,
                instead set android:required to false. In doing so, Google Play will allow
                devices without a camera to download your application. It's then your
                responsibility to check for the availability of the camera at runtime by
                calling hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY). If a camera is
                not available, you should then disable your camera features.
                 */
                /*
                <intent-filter>
        <action android:name="android.media.action.IMAGE_CAPTURE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
                 */
                /*
                On Android 9 (API level 28) and lower, reading and writing to this directory
                requires the READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions,
                respectively:
                <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
                 */
                /*
                <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="de.androidcrypto.androidcommonintents.fileprovider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths"></meta-data>
    </provider>
                 */

                System.out.println("### 01 take a photo");
                // https://developer.android.com/training/camera/photobasics
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    takePictureThumbnailActivityResultLauncher.launch(takePictureIntent);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
            }
        });

        btn02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                application
                    android:requestLegacyExternalStorage="true"
                 */
                System.out.println("### 02 take a photo full");
                dispatchTakePictureAppStorageIntent();
            }
        });

        btn03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // take a photo in full resolution and view in gallery
                context = v.getContext();
                verifyPermissions();
            }
        });

        btn04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Capture a video and return it
                // ACTION_IMAGE_CAPTURE_SECURE
                // https://developer.android.com/reference/android/provider/MediaStore#ACTION_IMAGE_CAPTURE_SECURE
                // https://developer.android.com/reference/android/os/Environment
                context = v.getContext();
                verifyPermissionsVideo();
            }
        });

        btn05.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open the gallery
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setType("image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btn06.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open the gallery and pick a file
                // create an instance of the
                // intent of the type image
                Intent i = new Intent();
                // this is for images only
                i.setType("image/*");
                // this is for videos only
                //i.setType("video/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                selectFileFromGalleryActivityResultLauncher.launch(i);
            }
        });

        btn07.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn08.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri image_uri = result.getData().getData();
                        ivG02.setImageURI(image_uri);
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
                        Bitmap rotated = rotateBitmap(inputImage, getCameraOrientation());
                        ivG02.setImageBitmap(rotated);

                        //ivG02.setImageURI(image_uri);
                        int height = ivG02.getHeight();
                        int width = ivG02.getWidth();
                        String imageInfo = "height: " + height + " width: " + width + " resolution: " + (height * width);
                        tvG02.setText(imageInfo);
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
        Log.d(TAG, "tryOrientation: " + orientation+"");
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(orientation);
        Log.d(TAG, "before Bitmap cropped");
        Bitmap cropped = Bitmap.createBitmap(input,0,0, input.getWidth(), input.getHeight(), rotationMatrix, true);
        return cropped;
    }

    /*
    https://stackoverflow.com/a/38647301/8166854
    You can just read the orientation of the camera sensor like indicated by Google in the documentation: https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html

SENSOR_ORIENTATION

Added in API level 21
Key<Integer> SENSOR_ORIENTATION
Clockwise angle through which the output image needs to be rotated to be upright on the device screen in its native orientation.

Also defines the direction of rolling shutter readout, which is from top to bottom in the sensor's coordinate system.

Units: Degrees of clockwise rotation; always a multiple of 90

Range of valid values:
0, 90, 180, 270

This key is available on all devices.
     */
    public Bitmap rotateBitmap(Bitmap input, int orientation){
        Log.d(TAG, "rotateBitmap");
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(orientation);
        Log.d(TAG, "before Bitmap cropped");
        return Bitmap.createBitmap(input,0,0, input.getWidth(), input.getHeight(), rotationMatrix, true);
    }

    private int getCameraOrientation(){
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        int orientation = 0;
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        }
        catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
        }
        return orientation;
    }

    // section take a photo in full resolution and store in external app's public directory start

    private void cropTheImage() {
        Log.d(TAG, "cropTheImage");

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        cropImageActivityResultLauncher.launch(intent);
        /*
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        Log.d(TAG, "cameraActivityResultLauncher.launch(cameraIntent)");
        cropImageActivityResultLauncher.launch(cameraIntent);

         */
    }

    ActivityResultLauncher<Intent> cropImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "ActivityResultLauncher<Intent> cropImageActivityResultLauncher");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "if (result.getResultCode() == Activity.RESULT_OK)");
                        Log.d(TAG, "image_uri: " + image_uri);

                        Bitmap inputImage = uriToBitmap(image_uri);
                        Bitmap rotated = rotateBitmap(inputImage, getCameraOrientation());
                        ivG02.setImageBitmap(rotated);

                        //ivG02.setImageURI(image_uri);
                        int height = ivG02.getHeight();
                        int width = ivG02.getWidth();
                        String imageInfo = "height: " + height + " width: " + width + " resolution: " + (height * width);
                        tvG02.setText(imageInfo);
                    } else {
                        Log.e(TAG, "if (result.getResultCode() == Activity.RESULT_NOT OK)");
                        Log.e(TAG, "resultCode: " + result.toString());
                    }
                }
            });



    // take a photo and show in gallery start

    private void askPermissions() {
        Log.d(TAG, "askPermissions");
        //TODO ask for permission of camera upon first launch of application
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)");
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
                Log.d(TAG, "if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED");
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, 112);
            }
        }
    }

    // ==============================================================================
    private void verifyPermissionsLow() {
        String[] permissions = {
                Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                ) {
            dispatchTakePictureIntentFullGallery();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    CAMERA_IMAGE_PERM_CODE);
        }
    }


    private void verifyPermissions() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntentFullGallery();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    CAMERA_IMAGE_PERM_CODE);
        }
    }

    private File createImageFileFullGallery() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_IMAGE_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntentFullGallery();
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CAMERA_VIDEO_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakeVideoIntentFullGallery();
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntentFullGallery() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFileFullGallery();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "de.androidcrypto.androidcommonintents.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                // deprecated startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                takePictureFullGalleryActivityResultLauncher.launch(takePictureIntent);
            }
        }
    }

    ActivityResultLauncher<Intent> takePictureFullGalleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        //Intent resultData = result.getData();
                        // and no resultData is given
                        File f = new File(currentPhotoPath);
                        ivG02.setImageURI(Uri.fromFile(f));
                        Log.d("tag", "ABsolute Url of Image is " + Uri.fromFile(f));
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(f);
                        mediaScanIntent.setData(contentUri);
                        context.sendBroadcast(mediaScanIntent);
                    }
                }
            });


    // section take a photo in full resolution and store in external app's public directory end












    // section take photo as thumbnail start

    // takes a photo and shows it as thumbnail only
    ActivityResultLauncher<Intent> takePictureThumbnailActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        Bundle extras = resultData.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        ivG02.setImageBitmap(imageBitmap);
                        String info = "thumbnail height: " + imageBitmap.getHeight()
                                + " width: " + imageBitmap.getWidth();
                        tvG02.setText(info);
                    }
                }
            });

    // section take photo as thumbnail end

    // section take a photo in full resolution and store in external app's directory start

    ActivityResultLauncher<Intent> takePictureAppStorageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        setPic();
                    }
                }
            });

    private void dispatchTakePictureAppStorageIntent() {
        // https://developer.android.com/reference/android/provider/MediaStore.Images#TaskPath
        System.out.println("dispatchTakePictureIntentFullResolutionV2");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            System.out.println("takePictureIntent.resolveActivity(getPackageManager()) != null");
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFileAppStorage();
                System.out.println("photoFile: " + photoFile.getAbsolutePath());
            } catch (IOException ex) {
                // Error occurred while creating the File
                System.out.println("IOExeception: " + ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                System.out.println("photoFile created");
                Uri photoURI = FileProvider.getUriForFile(this,
                        "de.androidcrypto.androidcommonintents.fileprovider",
                        photoFile);
                System.out.println("uri: " + photoURI.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    takePictureAppStorageActivityResultLauncher.launch(takePictureIntent);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
            }
        }
    }

    private File createImageFileAppStorage() throws IOException {
        // https://developer.android.com/training/camera/photobasics#TaskPath
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // getExternalFilesDir = files stay private
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        System.out.println("createImageFileAppStorage currentPhotoPath: " + currentPhotoPath);
        return image;
    }

    // scale picture to imageView sizes
    private void setPic() {
        System.out.println("galleryAddPic currentPhotoPath: " + currentPhotoPath);
        Bitmap bm = BitmapFactory.decodeFile(currentPhotoPath);
        String info = "setPic height: "  + bm.getHeight() + " width: " + bm.getWidth();
        tvG02.setText(info);
        // Get the dimensions of the View
        int targetW = ivG02.getWidth();
        int targetH = ivG02.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        //bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        ivG02.setImageBitmap(bitmap);
    }

    // section take a photo in full resolution and store in external app's directory end



    // section take a video in full resolution and store in external app's public directory start

    // take a video and show in gallery start
    private void verifyPermissionsVideo() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakeVideoIntentFullGallery();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    CAMERA_VIDEO_PERM_CODE);
        }
    }

    // record a video and show in gallery
    private void dispatchTakeVideoIntentFullGallery() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                //photoFile = createImageFileFullGallery();
                photoFile = createVideoFileFullGallery();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "de.androidcrypto.androidcommonintents.fileprovider",
                        photoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                // limit the video to 5 seconds
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 3);
                takeVideoActivityResultLauncher.launch(takeVideoIntent);
            }
        }
    }

    ActivityResultLauncher<Intent> takeVideoActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        //Intent resultData = result.getData();
                        // and no resultData is given
                        File f = new File(currentPhotoPath);
                        Log.d("tag", "ABsolute Url of Image is " + Uri.fromFile(f));
                        System.out.println("*** Absolute Url of Image is " + Uri.fromFile(f));
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(f);
                        mediaScanIntent.setData(contentUri);
                        context.sendBroadcast(mediaScanIntent);
                        String info = "video file length: " + f.length();
                        tvG02.setText(info);
                    }
                }
            });

    private File createVideoFileFullGallery() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MP4_" + timeStamp + "_";
        // File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // section take a video in full resolution and store in external app's public directory end

    // section select file from gallery start

    ActivityResultLauncher<Intent> selectFileFromGalleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        Uri selectedImageUri = resultData.getData();
                        if (null != selectedImageUri) {
                            // update the preview image in the layout
                            ivG02.setImageURI(selectedImageUri);
                            String info = "file name: " + selectedImageUri.toString();
                            tvG02.setText(info);
                        }
                    }
                }
            });

    // section select file from gallery end
}