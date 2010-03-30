<?php

/*$_REQUEST["Latitude"] = 3.0;
$_REQUEST["Longitude"] = 5.0;
$_REQUEST["GPS_Speed"] = 0.0;
$_REQUEST["Air_Intake_Temp"] = 34;
$_REQUEST["Intake_Manifold_Press"] = 54;
$_REQUEST["Barometric_Press"] = 78;
$_REQUEST["Ambient_Air_Temp"] = 98;
$_REQUEST["Vehicle_Speed"] = 23;
$_REQUEST["GPS_Time"] = 45;
$_REQUEST["Obs_Time"] = 65;
$_REQUEST["Vehicle_ID"] = "civic";
$_REQUEST["Mass_Air_Flow"] = 87;
$_REQUEST["Fuel_Economy"] = 23;
$_REQUEST["Long_Term_Fuel_Trim"] = 45;
$_REQUEST["Short_Term_Fuel_Trim"] = 98;
$_REQUEST["Engine_Runtime"] = 45;*/


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
$vid = $_REQUEST["Vehicle_ID"];
$maf = $_REQUEST["Mass_Air_Flow"];
$f_econ = $_REQUEST["Fuel_Economy"];
$ltft = $_REQUEST["Long_Term_Fuel_Trim"];
$stft = $_REQUEST["Short_Term_Fuel_Trim"];
$run_time = $_REQUEST["Engine_Runtime"];

$link = new mysqli(
            'localhost',  /* The host to connect to */
            'root',       /* The user to connect as */
            'bogus',   /* The password to use */
            'obd_data');     /* The default database to query */

if (mysqli_connect_errno()) {
    echo "Connect failed: " . mysqli_connect_error() . "\n";
    exit();
}

$stmt = $link->prepare("insert into obd_data(lat,lon,gspd,intake_temp,air_temp,intake_press,bar_press,vspd,throttle_pos,gtime,otime,vehicle,maf,fuel_econ,stft,ltft,run_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
if (!$stmt) {
  echo $link->error . "\n";
}
$stmt->bind_param("dddddddddddsddddd",$lat,$lon,$gspd,$intake_temp,$air_temp,$intake_press,$bar_press,$vspd,$tpos,$gtime,$otime,$vid,$maf,$f_econ,$stft,$ltft,$run_time);
$res = $stmt->execute();
if (!$res) {
  echo $link->error . "\n";
}
$stmt->close();
$link->close();

?>

