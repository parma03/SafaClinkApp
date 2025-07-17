<?php
// searchOrders.php
include_once '../dbset/dbconnect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $keyword = $_POST['keyword'];

    // Sanitize input
    $keyword = mysqli_real_escape_string($conn, $keyword);

    $response["code"] = 1;
    $response["message"] = "Data ditemukan";
    $response["data"] = array();

    // Search in transactions (tb_transaksi with JOIN to tb_paket and tb_user)
    $transactionQuery = "SELECT
                            t.*,
                            p.nama_paket,
                            p.tipe_paket,
                            p.deskripsi as paket_deskripsi,
                            u.nama as nama_pelanggan,
                            u.nohp as nohp_pelanggan
                         FROM tb_transaksi t
                         LEFT JOIN tb_paket p ON t.id_paket = p.id_paket
                         LEFT JOIN tb_user u ON t.id_pelanggan = u.id_user
                         WHERE t.status_transaksi = 'paid' AND (
                            t.order_id LIKE '%$keyword%' OR
                            p.nama_paket LIKE '%$keyword%' OR
                            p.tipe_paket LIKE '%$keyword%' OR
                            p.deskripsi LIKE '%$keyword%' OR
                            u.nama LIKE '%$keyword%' OR
                            u.nohp LIKE '%$keyword%' OR
                            t.alamat LIKE '%$keyword%'
                         )
                         ORDER BY t.created_at DESC";

    $transactionExecute = mysqli_query($conn, $transactionQuery);
    $transactions = array();
    if ($transactionExecute && mysqli_num_rows($transactionExecute) > 0) {
        while ($retrieve = mysqli_fetch_object($transactionExecute)) {
            $transactions[] = $retrieve;
        }
    }

    // Search in orders (tb_orders with JOIN to tb_transaksi, tb_paket, and tb_user)
    $orderQuery = "SELECT
                      o.*,
                      t.id_pelanggan,
                      t.id_paket,
                      t.alamat,
                      t.lokasi,
                      t.item,
                      t.foto_barang,
                      t.status_transaksi,
                      t.total_harga,
                      t.snap_token,
                      t.created_at as transaction_created_at,
                      p.nama_paket,
                      p.tipe_paket,
                      p.deskripsi as paket_deskripsi,
                      u.nama as nama_pelanggan,
                      u.nohp as nohp_pelanggan
                   FROM tb_orders o
                   LEFT JOIN tb_transaksi t ON o.order_id = t.order_id
                   LEFT JOIN tb_paket p ON t.id_paket = p.id_paket
                   LEFT JOIN tb_user u ON t.id_pelanggan = u.id_user
                   WHERE (
                      o.order_id LIKE '%$keyword%' OR
                      o.status_order LIKE '%$keyword%' OR
                      p.nama_paket LIKE '%$keyword%' OR
                      p.tipe_paket LIKE '%$keyword%' OR
                      p.deskripsi LIKE '%$keyword%' OR
                      u.nama LIKE '%$keyword%' OR
                      u.nohp LIKE '%$keyword%' OR
                      t.alamat LIKE '%$keyword%'
                   )
                   ORDER BY o.created_at DESC";

    $orderExecute = mysqli_query($conn, $orderQuery);
    $orders = array();
    if ($orderExecute && mysqli_num_rows($orderExecute) > 0) {
        while ($retrieve = mysqli_fetch_object($orderExecute)) {
            $orders[] = $retrieve;
        }
    }

    // Check if any results found
    if (count($transactions) > 0 || count($orders) > 0) {
        $response["code"] = 1;
        $response["message"] = "Data ditemukan";
        $response["data"] = array(
            "transactions" => $transactions,
            "orders" => $orders
        );
    } else {
        $response["code"] = 0;
        $response["message"] = "Data tidak ditemukan";
    }

} else {
    $response["code"] = 0;
    $response["message"] = "Method tidak diizinkan";
}

echo json_encode($response);
mysqli_close($conn);
?>