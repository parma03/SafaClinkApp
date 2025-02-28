<?php
include_once '../dbconnect.php';

$response = array();
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $username = $_POST['username'];
    $password = $_POST['password'];

    $query = "SELECT * FROM tb_user WHERE email='$username' AND password='$password' AND role='Administrator'";
    $execute = mysqli_query($conn, $query);
    $check = mysqli_affected_rows($conn);
    if ($check > 0) {
        $response["code"] = 1;
        $response["message"] = "input user login berhasil";
        $response["data"] = array();
        $F = array();
        while ($retrieve = mysqli_fetch_object($execute)) {
            $F[] = $retrieve;
        }
        $response["data"] = $F;
    } else {
        $response["code"] = 0;
        $response["message"] = "input user login gagal";
    }
} else {
    $response["code"] = 0;
    $response["message"] = "Tidak ada input user login";
}
echo json_encode($response);
mysqli_close($conn);