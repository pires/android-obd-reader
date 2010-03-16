<?php

$array_keys = array_keys($_REQUEST);
$date_str = date("Hi")
$f = fopen("/opt/data/$date_str","w")
for( $i=0;$i<count($array_keys);$i++ ) {

 $str = $_REQUEST[$array_keys[$i]] . "\n";
 fwrite($f,$str)
}
fclose($f);
?>
