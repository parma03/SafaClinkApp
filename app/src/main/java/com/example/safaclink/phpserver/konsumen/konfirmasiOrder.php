<?php
// konsumen/konfirmasiOrder.php
include_once '../dbset/dbconnect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    // Ambil parameter
    $order_id = $_POST['order_id'];

    // Validasi input
    if (empty($order_id)) {
        $response["code"] = 0;
        $response["message"] = "Order ID tidak boleh kosong";
        echo json_encode($response);
        exit;
    }

    // Mulai transaksi
    mysqli_begin_transaction($conn);

    try {
        // Update status di tb_orders (jika ada)
        $query_orders = "UPDATE tb_orders SET status_order = 'dikonfirmasi', updated_at = NOW() WHERE order_id = ?";
        $stmt_orders = mysqli_prepare($conn, $query_orders);
        mysqli_stmt_bind_param($stmt_orders, "s", $order_id);
        $execute_orders = mysqli_stmt_execute($stmt_orders);

        if (!$execute_orders) {
            throw new Exception("Gagal update status di tb_orders");
        }

        // Periksa apakah ada baris yang terpengaruh di tb_orders
        $affected_rows_orders = mysqli_stmt_affected_rows($stmt_orders);

        if ($affected_rows_orders === 0) {
            throw new Exception("Order ID tidak ditemukan atau sudah dikonfirmasi");
        }

        // Commit transaksi
        mysqli_commit($conn);

        $response["code"] = 1;
        $response["message"] = "Pesanan berhasil dikonfirmasi";
        $response["data"] = array(
            "order_id" => $order_id,
            "new_status" => "dikonfirmasi",
            "updated_at" => date('Y-m-d H:i:s')
        );

    } catch (Exception $e) {
        // Rollback transaksi jika terjadi error
        mysqli_rollback($conn);

        $response["code"] = 0;
        $response["message"] = $e->getMessage();
    }

} else {
    $response["code"] = 0;
    $response["message"] = "Method tidak diizinkan";
}

echo json_encode($response);
mysqli_close($conn);
?>