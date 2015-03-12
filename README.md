android-obd-reader
========================

Android OBD-II reader designed to connect with Bluetooth Elm327 OBD reader.

I'm still migrating things over here, so keep with me :-)

## Prerequisites ##
- JDK 7
- Android Studio 1.1.0
- Gradle 2.2.1
- Android SDK (API 19, Build tools 19.1)

## Test with device ##

Be sure to have the device connected to your computer.

```
cd whatever_directory_you_cloned_this_repository
gradle clean build installDebug
```

## Test with OBD Server ##

If you want to upload data to a server, for now, check the following:
* [OBD Server](https://github.com/pires/obd-server/) - a simple implementation of a RESTful app, compiled into a runnable JAR.
* Enable the upload functionality, by defining ```private static final boolean UPLOAD = true;``` in ```MainActivity.java```;
* Set proper endpoint address and port in class ```UploadAsyncTask``` defined in ```MainActivity.java```.

## Troubleshooting ##

As *@dembol* noted:

Have you checked your ELM327 adapter with Torque or Scanmaster to see if it works with your car? Maybe the problem is with your device?

Popular OBD diagnostic tools reset state and disable echo, spaces etc before protocol selection. Download some elm327 terminal for android and try following commands in order:
```
ATD
ATZ
AT E0
AT L0
AT S0
AT H0
AT SP 0
```

## Tested on ##

* Samsung Galaxy Nexus (Android 4.3)
* LG Nexus 5 (Android 4.4.4)
* Nexus 7 2013 WiFi (Android 4.4.4)
