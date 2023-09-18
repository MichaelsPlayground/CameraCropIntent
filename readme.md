# Camera & Crop Intent

This is a sample app showing how to archive these tasks on an Android smartphone:

1) load an image from device's gallery using the new **PhotoPicker** in full resolution
2) take a photo with the device's camera application in full resolution
3) save the image in full resolution to the 'Picture' folder in external storage
4) crop the image in full resolution with an installed cropping application (e.g. standard gallery application)
5) save the cropped image in full resolution to the 'Picture' folder in external storage

## 1 load an image from device's gallery using the PhotoPicker in full resolution

The app is using the **PhotoPicker** that was introduced by Google in Android 13 but fortunately this is backported 
to Android 11 and 12 devices. For older versions the older 'ACTION_OPEN_DOCUMENT' intent is called.

From the documentation: 
*If the photo picker isn't available on a device, the library automatically invokes the ACTION_OPEN_DOCUMENT 
intent action instead. This intent is supported on devices that run Android 4.4 (API level 19) or higher. 
You can verify whether the photo picker is available on a given device by calling isPhotoPickerAvailable().*

The good news are: the uri provided in the 'ActivityResultLauncher<PickVisualMediaRequest>' contains the link 
to the image in full resolution.  

For further processing (e.g. cropping) a file with the image is stored in the app's cache folder. The image is shown 
in the image view (after a previous resizing procedure).

## 2 take a photo with the device's camera application in full resolution

The app calls the device's (default) camera application so you are familiar with the operation of the camera. The 
image is shown in the image view (after a previous resizing procedure).

For further processing (e.g. cropping) a file with the image is stored in the app's cache folder. The image is shown 
in the image view (after a previous resizing procedure).

# 3 save the image in full resolution to the 'Picture' folder in external storage

The image shot by action 2 is stored in the external storage folder 'Pictures' in full resolution. The app gives a 
filename in the format 'YYYYMMDD_HHmmSS.jpg' (e.g. '20230918_132909.jpg').

Please note: as the image is in full resolution the storage may last some seconds to complete. **During that time 
the app gets inactive and unusable** because it's running on the main thread (bad style, I know). For your own app 
consider of using threads or other background operation techniques.

## 4 crop the image in full resolution with an installed cropping application (e.g. standard gallery application)

When starting the app it checks whether an application on the device offers a cropping functionality. If there is 
no available app the cropping related buttons get disabled ("greyed out"). If there is just one application a radio button 
chooser appears but that is fixed to "use fixed chooser 0". If there are several applications available you can opt between 
a chooser popping up to select one or use the one with the highest internal rating (e.g. the Google Photos cropper).

After cutting out an area of the image this area is shown in a second imageview below the original image (after a previous 
resizing procedure). 

## 5 save the cropped image in full resolution to the 'Picture' folder in external storage

The image shot by action 4 is stored in the external storage folder 'Pictures' in full resolution. The app gives a 
filename in the format 'YYYYMMDD_HHmmSS.jpg' (e.g. '20230918_132909.jpg').

Please note: as the image is in full resolution the storage may last some seconds to complete. **During that time 
the app gets inactive and unusable** because it's running on the main thread (bad style, I know). For your own app 
consider of using threads or other background operation techniques.

## Permission handling

This sample app is using the build in camera(s) and stores the images in the external storage, so these 
'uses-permissions' are set in AndroidManifest.xml. On app's first start the app is asking for **granting 
runtime permissions** for Camera usage and file write access - please do not prohibit the usage as there 
are no further checks on that.

For permissions add this to AndroidManifest.xml:

```plaintext
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.CAMERA"/>
<uses-feature android:name="android.hardware.camera"
              android:required="true" />
```

As we are querying files through intents we do need these queries in AndroidManifest.xml as well:

```plaintext
<queries>
    <intent>
        <action android:name="android.media.action.IMAGE_CAPTURE" />
    </intent>
    <intent>
        <action android:name="com.android.camera.action.CROP" />
    </intent>
    <intent>
        <action android:name="android.intent.action.PICK" />
        <data android:mimeType="vnd.android.cursor.dir/image" />
    </intent>
</queries>
```

Last but not least - for (internal) caching of files we do need a **file provider** initiated by this entry 
in AndroidManifest.xml:

```plaintext
<!-- Provider to cache images to the internal App cache -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:grantUriPermissions="true"
    android:exported="false"
    tools:replace="android:authorities">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>

<!-- Trigger Google Play services to install the backported photo picker module. -->
<!-- Trigger Google Play services to install the backported photo picker module. -->
<service android:name="com.google.android.gms.metadata.ModuleDependencies"
    android:enabled="false"
    android:exported="false"
    tools:ignore="MissingClass">
    <intent-filter>
        <action android:name="com.google.android.gms.metadata.MODULE_DEPENDENCIES" />
    </intent-filter>
    <meta-data android:name="photopicker_activity:0:required" android:value="" />
</service>
```

Don't forget to place a 'file-paths.xml' file in 'res/xml' folder:

```plaintext
<?xml version="1.0" encoding="utf-8"?>
<paths>```
    <cache-path name="shared_images" path="."/>
</paths>
```

## Android version handling

This app is usable for Android SDK version 26+ (Android 8). It is tested on real devices (Samsung) using 
SDK 26 (Android 8) and SDK 33 (Android 13).

PhotoPicker documentation: https://developer.android.com/training/data-storage/shared/photopicker



