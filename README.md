# godot-plugin-firebase
This plugin is based on https://github.com/FrogSquare/GodotFireBase, so all credit goes to FrogSquare.

I adjusted this plugin so it works with the new Android plugin system for Godot 3.2 (at least alpha 3 is needed).

Steps to follow to make this plugin work:

- download and start Godot 3.2 (alpha 3 or greater). No need to build it on your own (compile, ...).

- in Godot with your project opened select 'Project > Install Android Build Template'. This will install the files in your project's directory (by adding android/...)

- git clone https://github.com/mrcrb/godot-plugin-sql in [GODOT-PROJECT]/android/ (for this plugin no further configuration is needed)

- git clone https://github.com/mrcrb/godot-plugin-firebase in [GODOT-PROJECT]/android/

- from Firebase console download 'google-services.json' and copy/move it to [GODOT-PROJECT]/android/build/

- for AdMob edit [GODOT-PROJECT]/android/godot-plugin-firebase/assets/godot-firebase-config.json to match your needs
 - currently only AdMob and Analytics is working. I removed other services from the Java code (as I was interested only in those two services).

- edit [GODOT-PROJECT]/android/godot-plugin-firebase/gradle.conf
 - if you are not interested in AdMob's mediation feature (with Unity ads), comment out following two lines from [GODOT-PROJECT]/android/godot-plugin-firebase/gradle.conf (just put two slashes at the beginning of the lines -> //)
  - implementation 'com.google.ads.mediation:unity:3.2.0.1'
  - implementation 'com.unity3d.ads:unity-ads:3.2.0'
 - edit your applicationId
  - applicationId 'com.mycompany.myappname'

- edit [GODOT-PROJECT]/android/godot-plugin-firebase/AndroidManifest.conf
 - edit the following section to match your AdMob App ID
    <!-- AdMob -->
        <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-ADMOB-APP-ID"/>
    <!-- AdMob -->

- now you should be able to build
 - in Godot with your project opened select 'Project > Export'
 - add an Android Export preset and enable 'Use Custom Build'

That's it. For further instructions on how to call AdMob functions refer to https://github.com/FrogSquare/GodotFireBase

