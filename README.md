android-obd-reader
========================

Android OBD-II reader designed to connect with Bluetooth Elm327 OBD reader.

I'm still migrating things over here, so keep with me :-)

## Prerequisites ##
- JDK 7
- Android Studio 0.8.1
- Gradle 1.12
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

## Tested on ##

* Samsung Galaxy S4 (Android 4.4.2)

## Changes in this Branch
* introduced GPS information
* introduced some of the missing connections between the preferences and the actual code
* modified the way data is uploaded to the server:
* 1. not every reading has long/lat and time. This is a waste of data upload. Data is compacted into a 
common structure and then it is uploaded in one go
* taken care of cases where the server is not responding: created a local database where data is temporarily stored
* the interface does not have a table with all data but a textview with only the current data: this is much easier to read 
* the font color has been changed to greyish to white (I could not read it)
* the names of the views in the interface have now a positional name (row1_column2). This is because in the future it would be great to have the allocation of data flexible (so that you can change what is displayed where
* other minor changes


## Issues
The data displayed is wrong. This is not an issue with this branch. It is something that happens also in the original repository.