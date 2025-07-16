<?php
// searchPaket.php
include_once '../dbset/dbconnect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $keyword = $_POST['keyword'];

    $query = "SELECT * FROM tb_paket WHERE
              nama_paket LIKE '%$keyword%' OR
              tipe_paket LIKE '%$keyword%' OR
              deskripsi LIKE '%$keyword%'";

    $execute = mysqli_query($conn, $query);
    $check = mysqli_affected_rows($conn);

    if ($check > 0) {
        $response["code"] = 1;
        $response["message"] = "Data ditemukan";
        $response["data"] = array();
        $F = array();
        while ($retrieve = mysqli_fetch_object($execute)) {
            $F[] = $retrieve;
        }
        $response["data"] = $F;
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