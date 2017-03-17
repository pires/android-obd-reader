android-obd-reader
========================

## NOTICE

**I am no longer involved in any way with OBD and related activities, so don't expect my feedback on issues, pull-requests and most of all, email.**

I can't even remember when I first picked this project from Brice Lambi (the original author). But one thing I'm sure, it was a time my interests changed quite frequently and I'd contribute simultaneously to totally unrelated projects. But for some reason this project stuck with me the longest.

Initially, it was more of an Android hack (sorry, Brice!). With time, I've redesigned the code and split it into two: [a Java API library](https://github.com/pires/obd-java-api/) that could run anywhere the JVM ran without concerning about which transport protocol one would use (because it just asks for one `InputStream/OutputStream` pair) and, after learning about Android development, a revamped Android app.

I know, the UI sucks, but I've never had the eye for UI/UX, I'll admit!

Years went by and a few contributors jumped in with amazing, smart features and fixes. To those fine people, **Thank you**! This is your _baby_, too.

Now, it's time to say goodbye.
Pires

[![CircleCI](https://circleci.com/gh/pires/android-obd-reader.svg?style=svg)](https://circleci.com/gh/pires/android-obd-reader)

Android OBD-II reader designed to connect with Bluetooth Elm327 OBD reader.

![screenshot](/Screenshot.png)

The latest release can be found [here](https://github.com/pires/android-obd-reader/releases/).

## Prerequisites ##
- JDK 8
- Android Studio 1.5.x or newer
- Android SDK (API 22, Build tools 23.0.1)
- [OBD Java API](https://github.com/pires/obd-java-api/) (already included)

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

One may need to turn off echo and headers depending on the dongle in use:
```
AT E0 - Turn echo off. Characters sent to ElmScan are not retransmitted back to the host computer.
AT E1 - Turn echo on. This is the default state, characters are echoed back to the host computer.
AT H0 - Turn headers off. This is the default state, header information and CRC byte are omitted.
AT H1 - Turn headers on. Header information and CRC byte are displayed.
```

## Building with custom `obd-java-api`

This project depends on a [pure-Java OBD library](https://github.com/pires/obd-java-api/). For testing with a custom version of it, do the following:

* Clone obd-java-api it into your project folder:

```
git clone https://github.com/pires/obd-java-api.git
```

* Create `obd-java-api/build.gradle` with the following content:

```
apply plugin: 'java'
```

* Edit `main build.gradle` and change:

```
compile 'com.github.pires:obd-java-api:1.0-RC14'`
```

to

```
compile project(':obd-java-api')
```

* Edit `settings.gradle` and add:

```
include ':obd-java-api'
```

## Tested on ##

* Samsung Galaxy Nexus (Android 4.4.1)
* LG Nexus 5 (Android 6.0  Preview 3)
* Nexus 7 2013 WiFi (Android 4.4.4)
* Samsung Galaxy S4 Active I9295 (Android 5.0.2)
* Samsung Galaxy S6 Edge SM-925F (Android 5.0.2)
* Samsung Galaxy Note 3
