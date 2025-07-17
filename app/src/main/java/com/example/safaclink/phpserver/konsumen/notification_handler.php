<?php
// notification_handler.php - Webhook untuk menangani notifikasi dari Midtrans
include_once '../dbset/dbconnect.php';

// Midtrans Configuration
$server_key = 'Mid-server-CpNVYcSpc48rJb3C4TS0wQMc';
$is_production = false;

// Get notification data
$json_result = file_get_contents('php://input');
$result = json_decode($json_result, true);

// Log notification for debugging
error_log("Midtrans Notification: " . $json_result);

// Validate notification
if (empty($result)) {
    http_response_code(400);
    echo json_encode(array('status' => 'error', 'message' => 'Invalid notification'));
    exit;
}

// Extract important data
$order_id = $result['order_id'];
$transaction_status = $result['transaction_status'];
$fraud_status = isset($result['fraud_status']) ? $result['fraud_status'] : '';
$signature_key = $result['signature_key'];

// Verify signature
$expected_signature = hash('sha512', $order_id . $result['status_code'] . $result['gross_amount'] . $server_key);

if ($signature_key !== $expected_signature) {
    http_response_code(401);
    echo json_encode(array('status' => 'error', 'message' => 'Invalid signature'));
    exit;
}

// Determine transaction status
$status = 'pending';
if ($transaction_status == 'capture') {
    if ($fraud_status == 'accept') {
        $status = 'paid';
    } else if ($fraud_status == 'challenge') {
        $status = 'pending';
    }
} else if ($transaction_status == 'settlement') {
    $status = 'paid';
} else if ($transaction_status == 'pending') {
    $status = 'pending';
} else if ($transaction_status == 'deny') {
    $status = 'failed';
} else if ($transaction_status == 'expire') {
    $status = 'expired';
} else if ($transaction_status == 'cancel') {
    $status = 'cancelled';
}

// Update transaction status in database
$updated_at = date('Y-m-d H:i:s');
$query = "UPDATE tb_transaksi SET status_transaksi = ?, updated_at = ? WHERE order_id = ?";
$stmt = mysqli_prepare($conn, $query);
mysqli_stmt_bind_param($stmt, "sss", $status, $updated_at, $order_id);
$result_update = mysqli_stmt_execute($stmt);

if ($result_update) {
    $affected_rows = mysqli_stmt_affected_rows($stmt);
    if ($affected_rows > 0) {
        // Log successful update
        error_log("Transaction status updated successfully for order_id: $order_id, status: $status");

        // Send success response to Midtrans
        echo json_encode(array('status' => 'success', 'message' => 'Transaction updated'));
    } else {
        // Log error
        error_log("No transaction found for order_id: $order_id");
        echo json_encode(array('status' => 'error', 'message' => 'Transaction not found'));
    }
} else {
    // Log error
    error_log("Failed to update transaction status for order_id: $order_id, error: " . mysqli_error($conn));
    echo json_encode(array('status' => 'error', 'message' => 'Failed to update transaction'));
}

mysqli_stmt_close($stmt);
mysqli_close($conn);
?>