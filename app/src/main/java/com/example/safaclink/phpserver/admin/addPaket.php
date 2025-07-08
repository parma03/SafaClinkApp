<?php
include_once '../dbset/dbconnect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $nama_paket = $_POST['nama_paket'];
    $tipe_paket = $_POST['tipe_paket'];
    $deskripsi = $_POST['deskripsi'];
    $harga = $_POST['harga'];

    $query = "SELECT * FROM tb_paket WHERE nama_paket='$nama_paket'";
    $checkquery = mysqli_query($conn, $query);

    if (mysqli_num_rows($checkquery) > 1) {
        $response["code"] = 2;
    } else {
        $query = "INSERT INTO tb_paket (nama_paket, tipe_paket, deskripsi, harga) VALUES ('$nama_paket', '$tipe_paket','$deskripsi', '$harga')";
        $execute = mysqli_query($conn, $query);
        $check = mysqli_affected_rows($conn);
        if ($check > 0) {
            $response["code"] = 1;
        } else {
            $response["code"] = 0;
            $response["message"] = "Add Data gagal";
        }
    }
} else {
    $response["code"] = 0;
    $response["message"] = "Tidak ada input";
}

echo json_encode($response);
mysqli_close($conn);
?>