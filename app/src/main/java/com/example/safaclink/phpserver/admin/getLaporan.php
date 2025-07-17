<?php
// getLaporan.php
include_once '../dbset/dbconnect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    $response["code"] = 1;
    $response["message"] = "Data ditemukan";
    $response["data"] = array();
    $response["summary"] = array();

    // Ambil parameter filter tanggal jika ada
    $tanggal_awal = isset($_GET['tanggal_awal']) ? mysqli_real_escape_string($conn, $_GET['tanggal_awal']) : null;
    $tanggal_akhir = isset($_GET['tanggal_akhir']) ? mysqli_real_escape_string($conn, $_GET['tanggal_akhir']) : null;

    // Base query untuk mengambil data transaksi yang sudah paid
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
              WHERE t.status_transaksi = 'paid'";

    // Tambahkan filter tanggal jika ada
    if ($tanggal_awal && $tanggal_akhir) {
        $query .= " AND DATE(t.created_at) BETWEEN '$tanggal_awal' AND '$tanggal_akhir'";
    } elseif ($tanggal_awal) {
        $query .= " AND DATE(t.created_at) >= '$tanggal_awal'";
    } elseif ($tanggal_akhir) {
        $query .= " AND DATE(t.created_at) <= '$tanggal_akhir'";
    }

    $query .= " ORDER BY t.created_at DESC";

    $execute = mysqli_query($conn, $query);

    if ($execute && mysqli_num_rows($execute) > 0) {
        $total_transaksi = 0;
        $total_pendapatan = 0;
        $paket_stats = array();

        while ($retrieve = mysqli_fetch_object($execute)) {
            $response["data"][] = $retrieve;

            // Hitung statistik
            $total_transaksi++;
            $total_pendapatan += (int)$retrieve->total_harga;

            // Statistik per paket
            if (!isset($paket_stats[$retrieve->nama_paket])) {
                $paket_stats[$retrieve->nama_paket] = array(
                    'count' => 0,
                    'total' => 0
                );
            }
            $paket_stats[$retrieve->nama_paket]['count']++;
            $paket_stats[$retrieve->nama_paket]['total'] += (int)$retrieve->total_harga;
        }

        // Summary data
        $response["summary"] = array(
            'total_transaksi' => $total_transaksi,
            'total_pendapatan' => $total_pendapatan,
            'paket_stats' => $paket_stats,
            'periode' => array(
                'tanggal_awal' => $tanggal_awal,
                'tanggal_akhir' => $tanggal_akhir
            )
        );
    } else {
        $response["code"] = 0;
        $response["message"] = "Data tidak ditemukan untuk periode ini";
    }

} else {
    $response["code"] = 0;
    $response["message"] = "Method tidak diizinkan";
}

echo json_encode($response);
mysqli_close($conn);
?>