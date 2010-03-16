<?php

$array_keys = array_keys($_REQUEST);
for( $i=0;$i<count($array_keys);$i++ ) {
 $k = $array_keys[$i];
 if ($_REQUEST[$k] == "") {
   unset($_REQUEST[$k]);
 }
}


$lat = $_REQUEST["Latitude"];
$lon = $_REQUEST["Longitude"];
$gspd = $_REQUEST["GPS_Speed"];
$intake_temp = $_REQUEST["Air_Intake_Temp"];
$intake_press = $_REQUEST["Intake_Manifold_Press"];
$bar_press = $_REQUEST["Barometric_Press"];
$air_temp = $_REQUEST["Ambient_Air_Temp"];
$vspd = $_REQUEST["Vehicle_Speed"];
$gtime = $_REQUEST["GPS_Time"];
$otime = $_REQUEST["Obs_Time"];

$link = new mysqli(
            'localhost',  /* The host to connect to */
            'user',       /* The user to connect as */
            'nothing',   /* The password to use */
            'obd_data');     /* The default database to query */

if (mysqli_connect_errno()) {
    echo "Connect failed: " . mysqli_connect_error() . "\n";
    exit();
}

$stmt = $link->prepare("insert into obd_data(lat,lon,gspd,intake_temp,air_temp,intake_press,bar_press,vspd,throttle_pos,gtime,otime) values(?,?,?,?,?,?,?,?,?,?,?)");
$stmt->bind_param("ddddddddddd",$lat,$lon,$gspd,$intake_temp,$air_temp,$intake_press,$bar_press,$vspd,$tpos,$gtime,$otime);
$res = $stmt->execute();
echo $res . "\n";
$stmt->close();
$link->close();

if (!$res) {
  echo $link->error . "\n";
}

?>

