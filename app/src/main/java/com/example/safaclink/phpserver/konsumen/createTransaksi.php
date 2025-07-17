<?php
// Set proper headers for JSON response
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Add error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set maximum execution time
set_time_limit(120);

include_once '../dbset/dbconnect.php';

$response = array();

// Custom logging function
function writeLog($message, $type = 'INFO') {
    $log_dir = '../logs/';

    // Create logs directory if it doesn't exist
    if (!is_dir($log_dir)) {
        mkdir($log_dir, 0755, true);
    }

    $log_file = $log_dir . 'transaction_' . date('Y-m-d') . '.log';
    $timestamp = date('Y-m-d H:i:s');
    $log_entry = "[{$timestamp}] [{$type}] {$message}" . PHP_EOL;

    file_put_contents($log_file, $log_entry, FILE_APPEND | LOCK_EX);
}

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    try {
        // Validate required fields
        $required_fields = ['id_pelanggan', 'id_paket', 'alamat', 'total_harga', 'order_id'];
        foreach ($required_fields as $field) {
            if (!isset($_POST[$field]) || trim($_POST[$field]) === '') {
                $response["code"] = 0;
                $response["message"] = "Field {$field} is required";
                writeLog("Missing required field: {$field}", 'ERROR');
                goto output_response;
            }
        }

        // Get POST parameters with proper sanitization
        $id_pelanggan = mysqli_real_escape_string($conn, trim($_POST['id_pelanggan']));
        $id_paket = mysqli_real_escape_string($conn, trim($_POST['id_paket']));
        $alamat = mysqli_real_escape_string($conn, trim($_POST['alamat']));
        $lokasi = isset($_POST['lokasi']) ? mysqli_real_escape_string($conn, trim($_POST['lokasi'])) : '';
        $item = isset($_POST['item']) ? mysqli_real_escape_string($conn, trim($_POST['item'])) : '1';
        $foto_barang = isset($_POST['foto_barang']) ? $_POST['foto_barang'] : '';
        $total_harga = mysqli_real_escape_string($conn, trim($_POST['total_harga']));
        $order_id = mysqli_real_escape_string($conn, trim($_POST['order_id']));

        // Validate numeric fields
        if (!is_numeric($total_harga) || $total_harga <= 0) {
            $response["code"] = 0;
            $response["message"] = "Total harga tidak valid";
            writeLog("Invalid total_harga: " . $total_harga, 'ERROR');
            goto output_response;
        }

        if (!is_numeric($item) || $item <= 0) {
            $response["code"] = 0;
            $response["message"] = "Jumlah item tidak valid";
            writeLog("Invalid item: " . $item, 'ERROR');
            goto output_response;
        }

        writeLog("Processing order_id: " . $order_id, 'INFO');

        // Check if order_id already exists
        $query = "SELECT * FROM tb_transaksi WHERE order_id='$order_id'";
        $checkquery = mysqli_query($conn, $query);

        if (mysqli_error($conn)) {
            writeLog("MySQL Error: " . mysqli_error($conn), 'ERROR');
            $response["code"] = 0;
            $response["message"] = "Database error";
        } else if (mysqli_num_rows($checkquery) > 0) {
            writeLog("Order ID already exists: " . $order_id, 'WARNING');
            $response["code"] = 2;
            $response["message"] = "Order ID sudah ada";
        } else {
            // Create Snap Token
            $snapToken = createSnapToken($id_pelanggan, $id_paket, $alamat, $lokasi, $item, $total_harga, $order_id);

            if ($snapToken) {
                // Save transaction to database
                $created_at = date('Y-m-d H:i:s');
                $updated_at = date('Y-m-d H:i:s'); // Add updated_at value

                // Fixed: Prepare the SQL statement with correct column count
                $stmt = mysqli_prepare($conn, "INSERT INTO tb_transaksi (id_pelanggan, id_paket, alamat, lokasi, item, foto_barang, status_transaksi, total_harga, snap_token, order_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, 'pending', ?, ?, ?, ?, ?)");

                if ($stmt) {
                    // Fixed: Bind 11 parameters (including updated_at)
                    mysqli_stmt_bind_param($stmt, "sssssssssss", $id_pelanggan, $id_paket, $alamat, $lokasi, $item, $foto_barang, $total_harga, $snapToken, $order_id, $created_at, $updated_at);

                    $execute = mysqli_stmt_execute($stmt);

                    if ($execute) {
                        $check = mysqli_stmt_affected_rows($stmt);

                        if ($check > 0) {
                            writeLog("Transaction successfully created for order_id: " . $order_id, 'SUCCESS');
                            $response["code"] = 1;
                            $response["message"] = "Transaksi berhasil dibuat";
                            $response["snap_token"] = $snapToken;
                            $response["order_id"] = $order_id;
                        } else {
                            writeLog("Failed to save transaction for order_id: " . $order_id, 'ERROR');
                            $response["code"] = 0;
                            $response["message"] = "Gagal menyimpan transaksi";
                        }
                    } else {
                        writeLog("Execute Error: " . mysqli_error($conn), 'ERROR');
                        $response["code"] = 0;
                        $response["message"] = "Database execute error";
                    }

                    mysqli_stmt_close($stmt);
                } else {
                    writeLog("Prepare Error: " . mysqli_error($conn), 'ERROR');
                    $response["code"] = 0;
                    $response["message"] = "Database prepare error";
                }
            } else {
                writeLog("Failed to create snap token for order_id: " . $order_id, 'ERROR');
                $response["code"] = 0;
                $response["message"] = "Gagal membuat snap token";
            }
        }
    } catch (Exception $e) {
        writeLog("Exception: " . $e->getMessage(), 'ERROR');
        $response["code"] = 0;
        $response["message"] = "Server error: " . $e->getMessage();
    }
} else {
    writeLog("Invalid request method: " . $_SERVER['REQUEST_METHOD'], 'WARNING');
    $response["code"] = 0;
    $response["message"] = "Invalid request method";
}

output_response:
writeLog("Response: " . json_encode($response), 'INFO');

// Ensure clean JSON output
ob_clean();
echo json_encode($response, JSON_UNESCAPED_UNICODE);

if (isset($conn)) {
    mysqli_close($conn);
}

function createSnapToken($id_pelanggan, $id_paket, $alamat, $lokasi, $item, $total_harga, $order_id) {
    global $conn;

    try {
        // Get user data
        $query_user = "SELECT * FROM tb_user WHERE id_user = '$id_pelanggan'";
        $result_user = mysqli_query($conn, $query_user);
        $user = mysqli_fetch_assoc($result_user);

        if (!$user) {
            writeLog("User not found for id: " . $id_pelanggan, 'ERROR');
            return false;
        }

        // Get package data
        $query_paket = "SELECT * FROM tb_paket WHERE id_paket = '$id_paket'";
        $result_paket = mysqli_query($conn, $query_paket);
        $paket = mysqli_fetch_assoc($result_paket);

        if (!$paket) {
            writeLog("Package not found for id: " . $id_paket, 'ERROR');
            return false;
        }

        // Midtrans Configuration
        $server_key = 'Mid-server-CpNVYcSpc48rJb3C4TS0wQMc';
        $is_production = false;

        $api_url = $is_production ?
            'https://app.midtrans.com/snap/v1/transactions' :
            'https://app.sandbox.midtrans.com/snap/v1/transactions';

        // Calculate item price (total_harga / item)
        $item_price = (int)($total_harga / $item);

        // Prepare transaction data for Midtrans
        $transaction_data = array(
            'transaction_details' => array(
                'order_id' => $order_id,
                'gross_amount' => (int)$total_harga
            ),
            'customer_details' => array(
                'first_name' => $user['nama'] ? $user['nama'] : 'Customer', // Fixed: use 'nama' instead of 'nama_user'
                'email' => !empty($user['email']) ? $user['email'] : 'customer@example.com',
                'phone' => isset($user['nohp']) ? $user['nohp'] : '' // Fixed: use 'nohp' instead of 'no_hp'
            ),
            'item_details' => array(
                array(
                    'id' => $paket['id_paket'],
                    'price' => $item_price,
                    'quantity' => (int)$item,
                    'name' => $paket['nama_paket']
                )
            )
        );

        writeLog("Midtrans request data: " . json_encode($transaction_data), 'DEBUG');

        $ch = curl_init();

        $curl_options = array(
            CURLOPT_URL => $api_url,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_POST => true,
            CURLOPT_HEADER => false,
            CURLOPT_TIMEOUT => 60,
            CURLOPT_CONNECTTIMEOUT => 30,
            CURLOPT_SSL_VERIFYPEER => false,
            CURLOPT_SSL_VERIFYHOST => false,
            CURLOPT_HTTPHEADER => array(
                'Content-Type: application/json',
                'Accept: application/json',
                'Authorization: Basic ' . base64_encode($server_key . ':')
            ),
            CURLOPT_POSTFIELDS => json_encode($transaction_data)
        );

        curl_setopt_array($ch, $curl_options);

        $response = curl_exec($ch);
        $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);

        writeLog("Midtrans response: " . $response, 'DEBUG');
        writeLog("HTTP Code: " . $http_code, 'DEBUG');

        if (curl_error($ch)) {
            writeLog("Curl Error: " . curl_error($ch), 'ERROR');
            curl_close($ch);
            return false;
        }

        curl_close($ch);

        if ($http_code == 200 || $http_code == 201) {
            $response_data = json_decode($response, true);
            if (isset($response_data['token'])) {
                writeLog("Snap token created successfully for order_id: " . $order_id, 'SUCCESS');
                return $response_data['token'];
            } else {
                writeLog("No token in Midtrans response: " . $response, 'ERROR');
                return false;
            }
        }

        writeLog("Failed to create snap token - HTTP Code: " . $http_code . " Response: " . $response, 'ERROR');
        return false;
    } catch (Exception $e) {
        writeLog("Snap token creation error: " . $e->getMessage(), 'ERROR');
        return false;
    }
}
?>