<?php
include 'dbconnect.php'; // Pastikan kita sudah meng-include file koneksi

if (!$conn) {
    echo json_encode(['success' => false, 'message' => 'Gagal koneksi ke MySQL.']);
    exit;
}

// Cek apakah database sudah ada dengan cara yang aman
$db_selected = mysqli_query($conn, "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" . DB_NAME . "'");

if (!$db_selected || mysqli_num_rows($db_selected) == 0) {
    // Database belum ada, buat database
    if (!mysqli_query($conn, "CREATE DATABASE " . DB_NAME)) {
        echo json_encode(['success' => false, 'message' => 'Gagal membuat database: ' . mysqli_error($conn)]);
        exit;
    }
}

// Pilih database yang sudah dipastikan ada
mysqli_select_db($conn, DB_NAME);

// Jalankan file SQL migrasi jika ada
$sql_file = 'db_safaclink.sql';
if (file_exists($sql_file)) {
    $sql = file_get_contents($sql_file);
    if (mysqli_multi_query($conn, $sql)) {
        echo json_encode(['success' => true, 'message' => 'Migrasi database berhasil.']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Error saat menjalankan SQL: ' . mysqli_error($conn)]);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'File SQL tidak ditemukan.']);
}

mysqli_close($conn);
?>
