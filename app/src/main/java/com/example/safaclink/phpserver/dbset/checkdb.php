<?php
$conn = include 'dbconnect.php';

if (!$conn) {
    echo json_encode(['success' => false, 'message' => 'Database tidak ditemukan. Migrasi diperlukan.']);
    exit;
}

// Cek apakah tabel sudah ada
$result = mysqli_query($conn, "SHOW TABLES");
if ($result && mysqli_num_rows($result) > 0) {
    echo json_encode(['success' => true, 'message' => 'Database dan tabel ada.']);
} else {
    echo json_encode(['success' => false, 'message' => 'Database ada tetapi kosong.']);
}

mysqli_close($conn);
?>
