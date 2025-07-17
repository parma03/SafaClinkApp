<?php
// admin/konfirmasiOrders.php
include_once '../dbset/dbconnect.php';

// Set header untuk JSON response
header('Content-Type: application/json');

$response = array();

// Log untuk debugging
error_log("konfirmasiOrders.php called");
error_log("REQUEST_METHOD: " . $_SERVER['REQUEST_METHOD']);
error_log("POST data: " . print_r($_POST, true));

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Ambil parameter
    $order_id = isset($_POST['order_id']) ? trim($_POST['order_id']) : '';
    $current_status = isset($_POST['current_status']) ? trim($_POST['current_status']) : null;
    $transaction_status = isset($_POST['transaction_status']) ? trim($_POST['transaction_status']) : null;

    // Normalize empty strings to null
    if ($current_status === '' || $current_status === 'null') {
        $current_status = null;
    }
    if ($transaction_status === '' || $transaction_status === 'null') {
        $transaction_status = null;
    }

    error_log("Processed parameters:");
    error_log("order_id: " . $order_id);
    error_log("current_status: " . ($current_status ?? 'NULL'));
    error_log("transaction_status: " . ($transaction_status ?? 'NULL'));

    // Validasi input
    if (empty($order_id)) {
        $response["code"] = 0;
        $response["message"] = "Order ID tidak boleh kosong";
        error_log("Error: Order ID kosong");
        echo json_encode($response);
        exit;
    }

    // Mulai transaksi
    if (!mysqli_begin_transaction($conn)) {
        $response["code"] = 0;
        $response["message"] = "Gagal memulai transaksi database";
        error_log("Error: Gagal memulai transaksi");
        echo json_encode($response);
        exit;
    }

    try {
        // Tentukan status baru berdasarkan status saat ini
        $new_status = '';
        $action_taken = '';

        if ($current_status === null && $transaction_status === 'paid') {
            // Pesanan baru dibayar, belum ada di tb_orders -> buat entry baru dengan status dijemput
            $new_status = 'dijemput';
            $action_taken = 'insert_new_order';

            error_log("Action: Insert new order with status dijemput");

            // Cek apakah order_id sudah ada di tb_orders
            $check_query = "SELECT order_id FROM tb_orders WHERE order_id = ?";
            $stmt_check = mysqli_prepare($conn, $check_query);

            if (!$stmt_check) {
                throw new Exception("Gagal menyiapkan query pengecekan: " . mysqli_error($conn));
            }

            mysqli_stmt_bind_param($stmt_check, "s", $order_id);
            mysqli_stmt_execute($stmt_check);
            $result_check = mysqli_stmt_get_result($stmt_check);

            if (mysqli_num_rows($result_check) > 0) {
                // Order sudah ada, update saja
                $update_query = "UPDATE tb_orders SET status_order = ?, updated_at = NOW() WHERE order_id = ?";
                $stmt_update = mysqli_prepare($conn, $update_query);

                if (!$stmt_update) {
                    throw new Exception("Gagal menyiapkan query update: " . mysqli_error($conn));
                }

                mysqli_stmt_bind_param($stmt_update, "ss", $new_status, $order_id);

                if (!mysqli_stmt_execute($stmt_update)) {
                    throw new Exception("Gagal mengupdate status order: " . mysqli_stmt_error($stmt_update));
                }

                $action_taken = 'update_existing_order';
                error_log("Order sudah ada, di-update ke status: " . $new_status);
            } else {
                // Insert entry baru
                $insert_query = "INSERT INTO tb_orders (order_id, status_order, created_at, updated_at) VALUES (?, ?, NOW(), NOW())";
                $stmt_insert = mysqli_prepare($conn, $insert_query);

                if (!$stmt_insert) {
                    throw new Exception("Gagal menyiapkan query insert: " . mysqli_error($conn));
                }

                mysqli_stmt_bind_param($stmt_insert, "ss", $order_id, $new_status);

                if (!mysqli_stmt_execute($stmt_insert)) {
                    throw new Exception("Gagal membuat entry order baru: " . mysqli_stmt_error($stmt_insert));
                }

                error_log("Order baru berhasil dibuat dengan status: " . $new_status);
            }

        } else if ($current_status === 'dijemput') {
            // Update status ke dikerjakan
            $new_status = 'dikerjakan';
            $action_taken = 'update_to_dikerjakan';

        } else if ($current_status === 'dikerjakan') {
            // Update status ke diantar
            $new_status = 'diantar';
            $action_taken = 'update_to_diantar';

        } else if ($current_status === 'dikonfirmasi') {
            // Update status ke selesai
            $new_status = 'selesai';
            $action_taken = 'update_to_selesai';

        } else {
            throw new Exception("Status tidak valid untuk konfirmasi. Current status: " . ($current_status ?? 'NULL') . ", Transaction status: " . ($transaction_status ?? 'NULL'));
        }

        // Update status jika bukan insert baru untuk transaksi paid
        if ($action_taken !== 'insert_new_order' && $current_status !== null) {
            error_log("Updating status to: " . $new_status);

            $update_query = "UPDATE tb_orders SET status_order = ?, updated_at = NOW() WHERE order_id = ?";
            $stmt_update = mysqli_prepare($conn, $update_query);

            if (!$stmt_update) {
                throw new Exception("Gagal menyiapkan query update: " . mysqli_error($conn));
            }

            mysqli_stmt_bind_param($stmt_update, "ss", $new_status, $order_id);

            if (!mysqli_stmt_execute($stmt_update)) {
                throw new Exception("Gagal mengupdate status order: " . mysqli_stmt_error($stmt_update));
            }

            $affected_rows = mysqli_stmt_affected_rows($stmt_update);
            error_log("Affected rows: " . $affected_rows);

            if ($affected_rows === 0) {
                throw new Exception("Order ID tidak ditemukan atau status tidak berubah");
            }
        }

        // Commit transaksi
        if (!mysqli_commit($conn)) {
            throw new Exception("Gagal melakukan commit transaksi: " . mysqli_error($conn));
        }

        error_log("Transaksi berhasil di-commit");

        $response["code"] = 1;
        $response["message"] = "Pesanan berhasil dikonfirmasi";
        $response["data"] = array(
            "order_id" => $order_id,
            "old_status" => $current_status,
            "new_status" => $new_status,
            "action_taken" => $action_taken,
            "updated_at" => date('Y-m-d H:i:s')
        );

        error_log("Response success: " . json_encode($response));

    } catch (Exception $e) {
        // Rollback transaksi jika terjadi error
        mysqli_rollback($conn);

        error_log("Error occurred: " . $e->getMessage());
        error_log("Stack trace: " . $e->getTraceAsString());

        $response["code"] = 0;
        $response["message"] = $e->getMessage();
        $response["debug_info"] = array(
            "order_id" => $order_id,
            "current_status" => $current_status,
            "transaction_status" => $transaction_status,
            "error_line" => $e->getLine(),
            "error_file" => $e->getFile()
        );
    }

} else {
    $response["code"] = 0;
    $response["message"] = "Method tidak diizinkan. Hanya POST yang diperbolehkan.";
    error_log("Error: Method tidak diizinkan - " . $_SERVER['REQUEST_METHOD']);
}

echo json_encode($response);
mysqli_close($conn);
?>