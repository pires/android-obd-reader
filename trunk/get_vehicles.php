<?php

$link = new mysqli(
            'localhost',  /* The host to connect to */
            'root',       /* The user to connect as */
            'bogus',   /* The password to use */
            'obd_data');     /* The default database to query */

if (mysqli_connect_errno()) {
    echo "Connect failed: " . mysqli_connect_error() . "\n";
    exit();
}

$stmt = $link->prepare("select id from vehicle");
$res = $stmt->execute();
if (!$res) {
  echo $link->error . "\n";
}
$stmt->bind_result($id);
$ids = array();
while ($stmt->fetch()) {
    $ids[] = $id;
}
$stmt->close();
$link->close();

echo json_encode($ids);

?>

