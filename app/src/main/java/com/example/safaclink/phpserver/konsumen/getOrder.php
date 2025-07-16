<?php
// getOrder.php
include_once '../dbset/dbconnect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    $response["code"] = 1;
    $response["message"] = "Data ditemukan";
    $response["data"] = array();

    // Query untuk mengambil data transaksi yang sudah paid
    // Termasuk yang sudah ada di tb_orders maupun yang belum
    $query = "SELECT
                t.*,
                p.nama_paket,
                p.tipe_paket,
                p.deskripsi as paket_deskripsi,
                u.nama as nama_pelanggan,
                u.nohp as nohp_pelanggan,
                u.email as email_pelanggan,
                o.status_order,
                o.created_at as order_created_at,
                o.updated_at as order_updated_at
              FROM tb_transaksi t
              LEFT JOIN tb_paket p ON t.id_paket = p.id_paket
              LEFT JOIN tb_user u ON t.id_pelanggan = u.id_user
              LEFT JOIN tb_orders o ON t.order_id = o.order_id
              WHERE t.status_transaksi = 'paid'
              ORDER BY t.created_at DESC";

    $execute = mysqli_query($conn, $query);

    if ($execute && mysqli_num_rows($execute) > 0) {
        while ($retrieve = mysqli_fetch_object($execute)) {
            $response["data"][] = $retrieve;
        }
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