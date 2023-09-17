package de.androidcrypto.cameracropintent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
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
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private Button openFromGallery, takePhoto, cropImage;
    private ImageView ivFull, ivCrop;
    private TextView tvFull, tvCrop;


    public final String APP_TAG = "crop";
    public String intermediateName = "1.jpg";
    public String resultName = "2.jpg";

    Uri intermediateProvider;
    Uri resultProvider;

    ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    ActivityResultLauncher<Intent> galleryActivityResultLauncher;
    ActivityResultLauncher<Intent> cropActivityResultLauncher;
    ActivityResultLauncher<PickVisualMediaRequest> pickMediaActivityResultLauncher;


    private Uri imageUriFull, imageUriFullCached, imageUriCrop;


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
        ivFull = findViewById(R.id.ivFull);
        ivCrop = findViewById(R.id.ivCrop);
        tvFull = findViewById(R.id.tvFull);
        tvCrop = findViewById(R.id.tvCrop);

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

                onPickPhoto();
            }
        });

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "takePhoto.setOnClickListener");

                // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image
                //openCamera();
                onLaunchCamera();
            }
        });

        cropImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "cropImage.setOnClickListener");
                //cropTheImage();
                onCropImage();
            }
        });


        cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap takenImage = loadFromUri(intermediateProvider);
                        ivFull.setImageBitmap(getResizedBitmap(takenImage, 400));
                        onCropImage();
                    }
                });

        galleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        saveBitmapFileToIntermediate(result.getData().getData());
                        Bitmap selectedImage = loadFromUri(intermediateProvider);

                        ivFull.setImageBitmap(getResizedBitmap(selectedImage, 400));
                        onCropImage();
                    }
                });

        cropActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap cropImage = loadFromUri(resultProvider);

                        ivCrop.setImageBitmap(getResizedBitmap(cropImage, 400));
                    }
                });

        // android 13 photo picker
        // Registers a photo picker activity launcher in single-select mode.
        // https://developer.android.com/training/data-storage/shared/photopicker
        pickMediaActivityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d(TAG, "Selected URI: " + uri);
                        imageUriFull = uri;

                        saveBitmapFileToIntermediate(imageUriFull);
                        Bitmap inputImage = loadFromUri(intermediateProvider);





                        //ivFull.setImageURI(uri);
                        //Bitmap inputImage=((BitmapDrawable) ivFull.getDrawable()).getBitmap();

                        //Bitmap inputImage = uriToBitmap(uri);
                        Bitmap rotated = rotateBitmap(inputImage, getCameraOrientation());

                        ivFull.setImageBitmap(getResizedBitmap(inputImage, 400));
                        //ivFull.setImageBitmap(rotated);

                        // other way for rotating
                        //Bitmap inputImage = uriToBitmap(uri);
                        //Bitmap rotated = rotateBitmap2(uri, inputImage);
                        //ivFull.setImageBitmap(rotated);

                        int height = ivFull.getHeight();
                        int width = ivFull.getWidth();

                        //Bitmap inputImage = uriToBitmap(imageUriFull);
                        String imageInfo = "height: " + height + " width: " + width + " resolution: " + (height * width) +
                                "\nOriginal Bitmap height: " + inputImage.getHeight() + " width: " + inputImage.getWidth() +
                                " res: " + (inputImage.getHeight() * inputImage.getWidth());
                        tvFull.setText(imageInfo);

                        //saveBitmapFileToIntermediate(imageUriFull);

                    } else {
                        Log.d(TAG, "No media selected");
                    }
                });

    }

    public void onLaunchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = getPhotoFileUri(intermediateName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            intermediateProvider = FileProvider.getUriForFile(MainActivity.this, "de.androidcrypto.cameracropintent" + ".provider", photoFile);
        // intermediateProvider = FileProvider.getUriForFile(MainActivity.this, "de.androidcrypto.cameracropintent" + ".provider", imageFile);
        else
            intermediateProvider = Uri.fromFile(photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, intermediateProvider);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraActivityResultLauncher.launch(intent);
        }
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto() {



        // Launch the photo picker and let the user choose only images.
        https:
//developer.android.com/training/data-storage/shared/photopicker

        pickMediaActivityResultLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());

        /*
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (intent.resolveActivity(getPackageManager()) != null) {
            galleryActivityResultLauncher.launch(intent);
        }

         */
    }

    private void onCropImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            grantUriPermission("com.android.camera", intermediateProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(intermediateProvider, "image/*");

            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

            int size = 0;

            if (list != null) {
                grantUriPermission(list.get(0).activityInfo.packageName, intermediateProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                size = list.size();
            }

            if (size == 0) {
                Toast.makeText(this, "Error, wasn't taken image!", Toast.LENGTH_SHORT).show();
            } else {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra("crop", "true");

                intent.putExtra("scale", true);

                File photoFile = getPhotoFileUri(resultName);
                // wrap File object into a content provider
                // required for API >= 24
                // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
                resultProvider = FileProvider.getUriForFile(MainActivity.this, "de.androidcrypto.cameracropintent" + ".provider", photoFile);
                // intermediateProvider = FileProvider.getUriForFile(MainActivity.this, "de.androidcrypto.cameracropintent" + ".provider", imageFile);
                intent.putExtra("return-data", false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, resultProvider);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

                Intent cropIntent = new Intent(intent);
                ResolveInfo res = list.get(0);
                cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                grantUriPermission(res.activityInfo.packageName, resultProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                cropIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                cropActivityResultLauncher.launch(cropIntent);
            }
        } else {
            File photoFile = getPhotoFileUri(resultName);
            resultProvider = Uri.fromFile(photoFile);

            Intent intentCrop = new Intent("com.android.camera.action.CROP");
            intentCrop.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intentCrop.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentCrop.setDataAndType(intermediateProvider, "image/*");
            intentCrop.putExtra("crop", "true");
            intentCrop.putExtra("scale", true);
            intentCrop.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intentCrop.putExtra("noFaceDetection", true);
            intentCrop.putExtra("return-data", false);
            intentCrop.putExtra(MediaStore.EXTRA_OUTPUT, resultProvider);
            cropActivityResultLauncher.launch(intentCrop);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // see https://developer.android.com/reference/androidx/core/content/FileProvider
        // for correct internal / external naming
        //File mediaStorageDir = new File(getExternalFilesDir(""), APP_TAG);
        File mediaStorageDir = new File(getCacheDir(), APP_TAG);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory");
        }
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        Log.d(TAG, "getPhotoFileUri for fileName: " + fileName + " is: " + file.getAbsolutePath());
        return file;
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void saveBitmapFileToIntermediate(Uri sourceUri) {
        Log.d(TAG, "saveBitmapFileToIntermediate for URI: " + sourceUri);
        try {
            Bitmap bitmap = loadFromUri(sourceUri);

            File imageFile = getPhotoFileUri(intermediateName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                //intermediateProvider = FileProvider.getUriForFile(MainActivity.this, context.getPackageName() + ".provider", imageFile);
                intermediateProvider = FileProvider.getUriForFile(MainActivity.this, "de.androidcrypto.cameracropintent" + ".provider", imageFile);
            //de.androidcrypto.cameracropintent
            else
                intermediateProvider = Uri.fromFile(imageFile);

            OutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            Log.d(TAG, "intermediate file written to intermediateProvider: " + intermediateName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    // gallery opener
/*
    ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if ((result.getResultCode() == Activity.RESULT_OK) && (result.getData() != null)) {
                        imageUriFull = result.getData().getData();
                        ivFull.setImageURI(imageUriFull);
                        int height = ivFull.getHeight();
                        int width = ivFull.getWidth();

                        Bitmap inputImage = uriToBitmap(imageUriFull);
                        String imageInfo = "height: " + height + " width: " + width + " resolution: " + (height * width) +
                                "\nOriginal Bitmap height: " + inputImage.getHeight() + " width: " + inputImage.getWidth() +
                                " res: " + (inputImage.getHeight() * inputImage.getWidth());
                        tvFull.setText(imageInfo);

                        //imageUriFullCached = getImageUri(getApplicationContext(), uriToBitmap(imageUriFull));

                        Cache cache = new Cache(getApplicationContext());
                        imageUriFullCached = cache.saveToCacheAndGetUri(uriToBitmap(imageUriFull));
                        Log.d(TAG, "*** galleryActivityResultLauncher imageUriFullCached: " + imageUriFullCached);


                    }
                }
            });
*/
    // camera activity

    private void openCamera() {
        Log.d(TAG, "openCamera");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        imageUriFull = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriFull);
        Log.d(TAG, "cameraActivityResultLauncher.launch(cameraIntent)");
        cameraActivityResultLauncher.launch(cameraIntent);
    }

    /*
        ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.d(TAG, "ActivityResultLauncher<Intent> cameraActivityResultLauncher");
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.d(TAG, "if (result.getResultCode() == Activity.RESULT_OK)");
                            Log.d(TAG, "image_uri: " + imageUriFull);

                            Bitmap inputImage = uriToBitmap(imageUriFull);
                            Bitmap rotated = rotateBitmap(inputImage, getCameraOrientation());
                            ivFull.setImageBitmap(rotated);

                            //ivG02.setImageURI(image_uri);
                            int height = ivFull.getHeight();
                            int width = ivFull.getWidth();
                            String imageInfo = "height: " + height + " width: " + width + " resolution: " + (height * width) +
                                    "\nOriginal Bitmap height: " + inputImage.getHeight() + " width: " + inputImage.getWidth() +
                                    " res: " + (inputImage.getHeight() * inputImage.getWidth());
                            tvFull.setText(imageInfo);

                            imageUriFullCached = getImageUri(getApplicationContext(), uriToBitmap(imageUriFull));
                        }
                    }
                });
    */
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
        return null;
    }

    //TODO rotate image if image captured on samsung devices
    //TODO Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
    @SuppressLint("Range")
    public Bitmap rotateBitmap(Bitmap input) {
        Log.d(TAG, "rotateBitmap");
        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = getContentResolver().query(image_uri, orientationColumn, null, null, null);
        int orientation = -1;
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
        }
        Log.d(TAG, "tryOrientation: " + orientation + "");
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(orientation);
        Log.d(TAG, "before Bitmap cropped");
        Bitmap cropped = Bitmap.createBitmap(input, 0, 0, input.getWidth(), input.getHeight(), rotationMatrix, true);
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
    public Bitmap rotateBitmap(Bitmap input, int orientation) {
        Log.d(TAG, "rotateBitmap");
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(orientation);
        Log.d(TAG, "before Bitmap cropped");
        return Bitmap.createBitmap(input, 0, 0, input.getWidth(), input.getHeight(), rotationMatrix, true);
    }

    private int getCameraOrientation() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        int orientation = 0;
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return orientation;
    }

    // section take a photo in full resolution and store in external app's public directory start

    private void cropTheImage() {
        Log.d(TAG, "cropTheImage");

        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        cropIntent.setDataAndType(imageUriFullCached, "image/*");
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        //cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.putExtra("crop", true);
        cropIntent.putExtra("outputX", 180);
        cropIntent.putExtra("outputY", 180);

        cropIntent.putExtra("aspectX", 3);
        cropIntent.putExtra("aspectY", 3);
        cropIntent.putExtra("scaleUpIfNeeded", true);
        cropIntent.putExtra("return-data", true);
        Log.d(TAG, "cropTheImage call cropImageActivityResultLauncher");
        cropImageActivityResultLauncher.launch(cropIntent);


        //intent.setType("image/*");
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        //cropImageActivityResultLauncher.launch(intent);
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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    ActivityResultLauncher<Intent> cropImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "ActivityResultLauncher<Intent> cropImageActivityResultLauncher");
                    if ((result.getResultCode() == Activity.RESULT_OK) && (result.getData() != null)) {
                        Log.d(TAG, "if (result.getResultCode() == Activity.RESULT_OK)");

                        // the following code depends on the cropping library in use:
                        // regular Samsung Gallery = 'Bundle' way
                        // Google Photos provides an URI
                        int heightCropped = 0;
                        int widthCropped = 0;
                        try {
                            Bundle bundle = result.getData().getExtras();
                            Log.d(TAG, "*** regular way, bundle: " + bundle.toString());
                            Bitmap bitmap = bundle.getParcelable("data");
                            heightCropped = bitmap.getHeight();
                            widthCropped = bitmap.getWidth();
                            ivCrop.setImageBitmap(bitmap);
                        } catch (NullPointerException npe) {
                            // the result may contain an Uri
                            Log.d(TAG, "received a NPE on using Bundle, trying Uri now");
                            try {
                                // google photos way
                                //Uri uri = Uri.parse(result.getData());
                                Uri uri = Uri.parse(result.getData().toUri(0));
                                //ivCrop.setImageURI(uri);
                                //Bitmap bitmap = uriToBitmap(uri);

                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                ivCrop.setImageBitmap(bitmap);

                                heightCropped = bitmap.getHeight();
                                widthCropped = bitmap.getWidth();
                            } catch (Exception e) {
                                Log.e(TAG, "Received an Exception, aborted: " + e.getMessage());
                                return;
                            }
                        }
                        int height = ivCrop.getHeight();
                        int width = ivCrop.getWidth();
                        String imageInfo = "height: " + height + " width: " + width + " resolution: " + (height * width) +
                                "\nOriginal Bitmap height: " + heightCropped + " width: " + widthCropped +
                                " res: " + (heightCropped * widthCropped);
                        tvCrop.setText(imageInfo);
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

            if (Build.VERSION.SDK_INT <= 32) {
                Log.d(TAG, "Build.VERSION.SDK_INT <= 32");
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, "if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED");
                    String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permission, 112);
                }
            } else {
                Log.d(TAG, "Build.VERSION.SDK_INT > 32");
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, "if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED");
                    String[] permission = {Manifest.permission.CAMERA};
                    requestPermissions(permission, 112);
                }
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
                        "de.androidcrypto.cameracropintent" + ".provider",
                        photoFile);

                // intermediateProvider = FileProvider.getUriForFile(MainActivity.this, "de.androidcrypto.cameracropintent" + ".provider", imageFile);

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
                        "de.androidcrypto.cameracropintent" + ".provider",
                        photoFile);
                // intermediateProvider = FileProvider.getUriForFile(MainActivity.this, "de.androidcrypto.cameracropintent" + ".provider", imageFile);
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
        String info = "setPic height: " + bm.getHeight() + " width: " + bm.getWidth();
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
        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

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