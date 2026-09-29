<?php
require 'D:/XAMPP/htdocs/testlink-lab2/config_db.inc.php';
$mysqli = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
if ($mysqli->connect_errno) { fwrite(STDERR, "Connect failed: " . $mysqli->connect_error . "\n"); exit(1); }
$mysqli->query("SET NAMES utf8");
$res = $mysqli->query("SELECT status, COUNT(*) c FROM executions WHERE testplan_id=108 GROUP BY status");
while ($row = $res->fetch_assoc()) echo "  status={$row['status']} count={$row['c']}\n";
$mysqli->close();
