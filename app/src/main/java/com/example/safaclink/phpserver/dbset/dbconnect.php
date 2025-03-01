<?php
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_NAME', 'db_safaclink');

$conn = mysqli_connect(DB_HOST, DB_USER, DB_PASS);
if (!$conn) {
    die(json_encode(['success' => false, 'message' => 'Gagal koneksi ke MySQL: ' . mysqli_connect_error()]));
}

// Cek apakah database ada tanpa menyebabkan error fatal
$db_selected = mysqli_query($conn, "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" . DB_NAME . "'");

if (!$db_selected || mysqli_num_rows($db_selected) == 0) {
    return false; // Database belum ada, tangani di file lain
}

mysqli_select_db($conn, DB_NAME);
return $conn;
?>
