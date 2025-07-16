<?php
// updateTransactionStatus.php - Fixed version
include_once '../dbset/dbconnect.php';

// Initialize response array
$response = array();

// Error handling function
function sendResponse($code, $message) {
    $response = array(
        'code' => $code,
        'message' => $message
    );

    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit;
}

// Check request method
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(0, 'Method tidak diizinkan');
}

// Check if required POST parameters exist
if (!isset($_POST['order_id']) || !isset($_POST['status'])) {
    sendResponse(0, 'Parameter tidak lengkap');
}

try {
    // Sanitize input data
    $order_id = mysqli_real_escape_string($conn, trim($_POST['order_id']));
    $status = mysqli_real_escape_string($conn, trim($_POST['status']));
    $updated_at = date('Y-m-d H:i:s');

    // Validate required fields
    if (empty($order_id) || empty($status)) {
        sendResponse(0, 'Order ID dan Status harus diisi');
    }

    // Validate status values
    $valid_statuses = ['pending', 'paid', 'failed', 'cancelled', 'expired'];
    if (!in_array($status, $valid_statuses)) {
        sendResponse(0, 'Status tidak valid. Status yang valid: ' . implode(', ', $valid_statuses));
    }

    // Check if transaction exists first
    $check_query = "SELECT id_transaksi FROM tb_transaksi WHERE order_id = ?";
    $stmt_check = mysqli_prepare($conn, $check_query);
    mysqli_stmt_bind_param($stmt_check, "s", $order_id);
    mysqli_stmt_execute($stmt_check);
    $result_check = mysqli_stmt_get_result($stmt_check);

    if (mysqli_num_rows($result_check) == 0) {
        mysqli_stmt_close($stmt_check);
        sendResponse(0, 'Transaksi dengan order ID tersebut tidak ditemukan');
    }

    mysqli_stmt_close($stmt_check);

    // Update transaction status using prepared statement
    $query = "UPDATE tb_transaksi SET status_transaksi = ?, updated_at = ? WHERE order_id = ?";
    $stmt = mysqli_prepare($conn, $query);

    if (!$stmt) {
        sendResponse(0, 'Gagal mempersiapkan query: ' . mysqli_error($conn));
    }

    mysqli_stmt_bind_param($stmt, "sss", $status, $updated_at, $order_id);
    $result = mysqli_stmt_execute($stmt);

    if ($result) {
        $affected_rows = mysqli_stmt_affected_rows($stmt);
        if ($affected_rows > 0) {
            sendResponse(1, 'Status transaksi berhasil diupdate');
        } else {
            sendResponse(0, 'Tidak ada perubahan data');
        }
    } else {
        sendResponse(0, 'Gagal mengupdate status transaksi: ' . mysqli_error($conn));
    }

    mysqli_stmt_close($stmt);

} catch (Exception $e) {
    error_log("Error in updateTransactionStatus.php: " . $e->getMessage());
    sendResponse(0, 'Terjadi kesalahan sistem');
}

mysqli_close($conn);
?>