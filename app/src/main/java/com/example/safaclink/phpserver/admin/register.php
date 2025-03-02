<?php
include_once '../dbset/dbconnect.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $nama = $_POST['nama'];
    $nohp = $_POST['nohp'];
    $email = $_POST['email'];
    $username = $_POST['username'];
    $password = $_POST['password'];

    $query = "SELECT * FROM tb_user WHERE username='$username'";
    $checkquery = mysqli_query($conn, $query);
    if (mysqli_num_rows($checkquery) > 0) {
        $response["code"] = 2;
    } else {
        $query = "INSERT INTO tb_user (nama,nohp,email,username,password,role) VALUES('$nama','$nohp','$email','$username','$password','Konsumen');";
        $execute = mysqli_query($conn, $query);
        $response["code"] = 1;
    }
} else {
    $response["code"] = 0;
}
echo json_encode($response);
mysqli_close($conn);
?>