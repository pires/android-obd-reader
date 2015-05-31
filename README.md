android-obd-reader
========================

![logo](/src/main/res/drawable-xxhdpi/ic_btcar.png)

Android OBD-II reader designed to connect with Bluetooth Elm327 OBD reader.

![screenshot](/Screenshot.png)

## Prerequisites ##
- JDK 7
- Android Studio 1.1.0
- Gradle 2.2.1
- Android SDK (API 21, Build tools 21.1.2)

## Test with device ##

Be sure to have the device connected to your computer.

```
cd whatever_directory_you_cloned_this_repository
gradle clean build installDebug
```

## Test with OBD Server ##

If you want to upload data to a server, for now, check the following:
* [OBD Server](https://github.com/pires/obd-server/) - a simple implementation of a RESTful app, compiled into a runnable JAR.
* Enable the upload functionality in preferences
* Set proper endpoint address and port in preferences.

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

## building with custom obd-java-api ##
this project uses [obd-java-api](https://github.com/pires/obd-java-api/), to use a custom version of this library do the following:

* clone obd-java-api it into your project folder:
`git clone https://github.com/pires/obd-java-api.git`

* create obd-java-api/build.gradle with the following content:
`apply plugin: 'java'`

* in main build.gradle change
`compile 'pt.lighthouselabs.obd:obd-java-api:0.8'`
to
`compile project(':obd-java-api')`

* in settings.gradle add include `:obd-java-api`

## Tested on ##

* Samsung Galaxy Nexus (Android 4.3)
* LG Nexus 5 (Android 4.4.4)
* Nexus 7 2013 WiFi (Android 4.4.4)
* Samsung Galaxy S4 Active I9295 (Android 5.0.2)
