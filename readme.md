# Camera & Crop Intent

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

Android versions 21 (Android 5) up to SDK 33 (Android 13) tested on emulated and real  
devices.


