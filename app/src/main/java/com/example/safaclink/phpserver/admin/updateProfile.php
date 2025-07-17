<?php
// updateProfile.php
header('Content-Type: application/json');
include "koneksi.php"; // Sesuaikan dengan file koneksi database Anda

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $id_user = $_POST['id_user'];
    $nama = $_POST['nama'];
    $email = $_POST['email'];
    $nohp = $_POST['nohp'];
    $username = $_POST['username'];
    $password = $_POST['password'];

    // Validasi input
    if (empty($id_user) || empty($nama) || empty($email) || empty($nohp) || empty($username)) {
        echo json_encode([
            'code' => '0',
            'message' => 'Semua field harus diisi'
        ]);
        exit;
    }

    // Cek apakah username sudah digunakan oleh user lain
    $checkUsername = "SELECT id_user FROM tb_user WHERE username = '$username' AND id_user != $id_user";
    $resultCheck = mysqli_query($koneksi, $checkUsername);

    if (mysqli_num_rows($resultCheck) > 0) {
        echo json_encode([
            'code' => '0',
            'message' => 'Username sudah digunakan oleh user lain'
        ]);
        exit;
    }

    // Cek apakah email sudah digunakan oleh user lain
    $checkEmail = "SELECT id_user FROM tb_user WHERE email = '$email' AND id_user != $id_user";
    $resultCheckEmail = mysqli_query($koneksi, $checkEmail);

    if (mysqli_num_rows($resultCheckEmail) > 0) {
        echo json_encode([
            'code' => '0',
            'message' => 'Email sudah digunakan oleh user lain'
        ]);
        exit;
    }

    // Buat query update
    if (empty($password)) {
        // Update tanpa password
        $query = "UPDATE tb_user SET
                  nama = '$nama',
                  email = '$email',
                  nohp = '$nohp',
                  username = '$username'
                  WHERE id_user = $id_user";
    } else {
        // Update dengan password
        $query = "UPDATE tb_user SET
                  nama = '$nama',
                  email = '$email',
                  nohp = '$nohp',
                  username = '$username',
                  password = '$password'
                  WHERE id_user = $id_user";
    }

    $result = mysqli_query($koneksi, $query);

    if ($result) {
        echo json_encode([
            'code' => '1',
            'message' => 'Profile berhasil diperbarui'
        ]);
    } else {
        echo json_encode([
            'code' => '0',
            'message' => 'Gagal memperbarui profile: ' . mysqli_error($koneksi)
        ]);
    }

} else {
    echo json_encode([
        'code' => '0',
        'message' => 'Method tidak diizinkan'
    ]);
}
?>