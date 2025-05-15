<?php
include_once '../dbset/dbconnect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $id_user = $_POST['id_user'];
    $nama = $_POST['nama'];
    $nohp = $_POST['nohp'];
    $role = $_POST['role'];
    $email = $_POST['email'];
    $password = $_POST['password'];

    $query = "SELECT * FROM tb_user WHERE email='$email'";
    $checkquery = mysqli_query($conn, $query);

    if (mysqli_num_rows($checkquery) > 1) {
        $response["code"] = 2;
    } else {
        if (!empty($_FILES['gambar']['name'])) {
            $gambar = $_FILES['gambar']['name'];
            $targetDir = "../fotoHewan/";
            $fileMenu = basename($gambar);
            if (move_uploaded_file($_FILES["gambar"]["tmp_name"], $targetDir . $fileMenu)) {
                if (!empty($_POST['gambar'])) {
                    $gambar_pet_lama = $_POST['gambar'];
                    unlink($targetDir . $gambar_pet_lama);
                }
                $query = "UPDATE tb_user SET email='$email',password='$password',nama='$nama',nohp='$nohp',role='$role',profile='$gambar' WHERE id_user='$id_user'";
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
            $query = "UPDATE tb_user SET email='$email',password='$password',nama='$nama',nohp='$nohp',role='$role' WHERE id_user='$id_user'";
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