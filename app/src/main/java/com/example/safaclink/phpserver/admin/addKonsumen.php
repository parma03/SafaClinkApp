<?php
include_once '../dbset/dbconnect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $nama = $_POST['nama'];
    $nohp = $_POST['nohp'];
    $username = $_POST['username'];
    $email = $_POST['email'];
    $password = $_POST['password'];

    $query = "SELECT * FROM tb_user WHERE username='$username'";
    $checkquery = mysqli_query($conn, $query);

    if (mysqli_num_rows($checkquery) > 1) {
        $response["code"] = 2;
    } else {
        if (!empty($_FILES['gambar']['name'])) {
            $gambar = $_FILES['gambar']['name'];
            $targetDir = "../profile/";
            $fileMenu = basename($gambar);
            if (move_uploaded_file($_FILES["gambar"]["tmp_name"], $targetDir . $fileMenu)) {
                if (!empty($_POST['gambar'])) {
                    $gambar_lama = $_POST['gambar'];
                    unlink($targetDir . $gambar_lama);
                }
                $query = "INSERT INTO tb_user (nama, email, nohp, username, password, role, profile) VALUES ('$nama', '$email','$nohp', '$username', '$password', 'Konsumen', '$gambar')";
                $execute = mysqli_query($conn, $query);
                $check = mysqli_affected_rows($conn);
                if ($check > 0) {
                    $response["code"] = 1;
                } else {
                    $response["code"] = 0;
                    $response["message"] = "Update Gambar gagal";
                }
            } else {
                $response["code"] = 0;
                $response["message"] = "Gagal upload gambar";
            }
        } else {
            $query = "INSERT INTO tb_user (nama, email, nohp, username, password, role) VALUES ('$nama', '$email','$nohp', '$username', '$password', 'Konsumen')";
            $execute = mysqli_query($conn, $query);
            $check = mysqli_affected_rows($conn);

            if ($check > 0) {
                $response["code"] = 1;
                $response["message"] = "Update Gambar berhasil";
            } else {
                $response["code"] = 0;
                $response["message"] = "Update Gambar gagal";
            }
        }
    }
} else {
    $response["code"] = 0;
    $response["message"] = "Tidak ada input Konsumen";
}

echo json_encode($response);
mysqli_close($conn);
?>