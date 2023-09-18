# Camera & Crop Intent

This is a sample app showing how to archive these tasks on an Android smartphone:

1 load an image from device's gallery using the new **PhotoPicker** in full resolution
2 take a photo with the device's camera application in full resolution
3 save the image in full resolution to the 'Picture' folder in external storage
4 crop the image in full resolution with an installed cropping application (e.g. standard gallery application)
5 save the cropped image in full resolution to the 'Picture' folder in external storage


This app takes a photo with the Android smartphones camera using an Intent and storing 
the image in full resolution in an external directory like downloads folder.

Additionally a cop operation is available on the image and store the cropped image as 
well in the external directory.

As we are using the file choser via Intent no access permissions are needed.

But as we use the camera we need this usage permission in AndroidManifest.xml:
```plaintext
<uses-feature android:name="android.hardware.camera"
        android:required="true" />
```

PhotoPicker documentation: https://developer.android.com/training/data-storage/shared/photopicker

Android versions 26 (Android 8) up to SDK 33 (Android 13) tested on emulated and real  
devices.


