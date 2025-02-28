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

// If database doesn't exist or is empty, run the migration script
if (!database_exists()) {
    try {
        // Path to the SQL file
        $sql_file = 'db_safaclink.sql';

        // Check if file exists
        if (file_exists($sql_file)) {
            // Read SQL file
            $sql = file_get_contents($sql_file);

            // Execute SQL commands
            if (mysqli_multi_query($conn, $sql)) {
                echo json_encode(array('success' => true, 'message' => 'Database migration completed successfully.'));
            } else {
                echo json_encode(array('success' => false, 'message' => 'Error executing SQL: ' . mysqli_error($conn)));
            }
        } else {
            echo json_encode(array('success' => false, 'message' => 'SQL file not found.'));
        }
    } catch (Exception $e) {
        echo json_encode(array('success' => false, 'message' => 'Error during migration: ' . $e->getMessage()));
    }
} else {
    echo json_encode(array('success' => true, 'message' => 'Database already exists.'));
}
?>