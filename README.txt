This is a OBD II reader, it is designed to connect to a bluetooth Elm327 OBD reader.
Please take a look at the Wiki for more information. This application is free from the Android Market.

As of the 13th of September, Paulo Pires joined the project and migrated the 
code from Subversion to Git and implemented Maven (android-maven-plugin) support.

For more info, please point your browser at http://code.google.com/p/android-obd-reader/

==============================================================================

== How to build APK installer file ==

 1. Install Maven 3
 2. "cd /<path>/<to>/<this>/<project>"
 3. "mvn clean package"
 4. You'll find the signed APK file in "${PWD}/target" directory.
 5. ...
 6. Profit