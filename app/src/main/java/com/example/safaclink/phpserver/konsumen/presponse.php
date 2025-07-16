<?php
// presponse.php - Midtrans Proxy Handler
// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Set your server key (Note: Server key for sandbox and production mode are different)
//$server_key = '';
// Set true for production, set false for sandbox
$is_production = false;

$api_url = $is_production ?
  'https://app.midtrans.com/snap/v1/transactions' :
  'https://app.sandbox.midtrans.com/snap/v1/transactions';

// Check if method is not HTTP POST, display 404
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(404);
    echo json_encode(array('error' => 'Page not found or wrong HTTP request method is used'));
    exit();
}

// get the HTTP POST body of the request
$request_body = file_get_contents('php://input');

// Log request for debugging
error_log("Midtrans Request: " . $request_body);

// Validate request body
if (empty($request_body)) {
    http_response_code(400);
    echo json_encode(array('error' => 'Empty request body'));
    exit();
}

// call charge API using request body passed by mobile SDK
$charge_result = chargeAPI($api_url, $server_key, $request_body);

// Log response for debugging
error_log("Midtrans Response: " . $charge_result['body']);

// set the response http status code
http_response_code($charge_result['http_code']);

// then print out the response body
echo $charge_result['body'];

/**
 * call charge API using Curl
 * @param string  $api_url
 * @param string  $server_key
 * @param string  $request_body
 */
function chargeAPI($api_url, $server_key, $request_body) {
    $ch = curl_init();
    $curl_options = array(
        CURLOPT_URL => $api_url,
        CURLOPT_RETURNTRANSFER => 1,
        CURLOPT_POST => 1,
        CURLOPT_HEADER => 0,
        CURLOPT_TIMEOUT => 30,
        CURLOPT_CONNECTTIMEOUT => 10,
        CURLOPT_SSL_VERIFYPEER => false,
        CURLOPT_SSL_VERIFYHOST => false,
        // Add header to the request, including Authorization generated from server key
        CURLOPT_HTTPHEADER => array(
            'Content-Type: application/json',
            'Accept: application/json',
            'Authorization: Basic ' . base64_encode($server_key . ':')
        ),
        CURLOPT_POSTFIELDS => $request_body
    );

    curl_setopt_array($ch, $curl_options);

    $body = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    // Check for curl errors
    if (curl_error($ch)) {
        error_log("Curl Error: " . curl_error($ch));
        $result = array(
            'body' => json_encode(array('error' => 'Connection failed: ' . curl_error($ch))),
            'http_code' => 500,
        );
    } else {
        $result = array(
            'body' => $body,
            'http_code' => $http_code,
        );
    }

    curl_close($ch);
    return $result;
}
?>