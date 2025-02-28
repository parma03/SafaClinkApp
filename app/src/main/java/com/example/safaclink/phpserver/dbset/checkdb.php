<?php
include_once 'dbconnect.php';

// Function to check if database exists
function database_exists() {
    global $conn;

    // Check if we can query a table (this assumes at least one table exists)
    $result = mysqli_query($conn, "SHOW TABLES");

    if ($result && mysqli_num_rows($result) > 0) {
        return true;
    }

    return false;
}

if (database_exists()) {
    echo json_encode(array('success' => true, 'message' => 'Database exists.'));
} else {
    echo json_encode(array('success' => false, 'message' => 'Database does not exist or is empty.'));
}
?>