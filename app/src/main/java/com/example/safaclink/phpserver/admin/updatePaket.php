<?php
include_once '../dbset/dbconnect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $id_paket = $_POST['id_paket'];
    $nama_paket = $_POST['nama_paket'];
    $tipe_paket = $_POST['tipe_paket'];
    $deskripsi = $_POST['deskripsi'];
    $harga = $_POST['harga'];

    $query = "SELECT * FROM tb_paket WHERE nama_paket='$nama_paket'";
    $checkquery = mysqli_query($conn, $query);

    if (mysqli_num_rows($checkquery) > 1) {
        $response["code"] = 2;
    } else {
        $query = "UPDATE tb_paket SET
                    nama_paket='$nama_paket',
                    tipe_paket='$tipe_paket',
                    deskripsi='$deskripsi',
                    harga='$harga'
                    WHERE id_paket='$id_paket'";
        $execute = mysqli_query($conn, $query);
        $check = mysqli_affected_rows($conn);
        if ($check > 0) {
            $response["code"] = 1;
        } else {
            $response["code"] = 0;
            $response["message"] = "Update gagal";
        }
    }
} else {
    $response["code"] = 0;
    $response["message"] = "Tidak ada input";
}

echo json_encode($response);
mysqli_close($conn);
?>