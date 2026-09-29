<?php
require 'D:/XAMPP/htdocs/testlink-lab2/config_db.inc.php';
$mysqli = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
if ($mysqli->connect_errno) { fwrite(STDERR, "Connect failed: " . $mysqli->connect_error . "\n"); exit(1); }
$mysqli->query("SET NAMES utf8");

echo "executions columns:\n";
$res = $mysqli->query("SHOW COLUMNS FROM executions");
while ($row = $res->fetch_assoc()) echo "  " . $row['Field'] . " " . $row['Type'] . "\n";

echo "\nexisting executions (testplan=108, build=LAB2-B01):\n";
$res = $mysqli->query("SELECT id, testplan_id, build_id, tcversion_id, status, execution_ts, notes FROM executions WHERE testplan_id=108 ORDER BY tcversion_id");
$n=0;
while ($row = $res->fetch_assoc()) {
    echo "  id={$row['id']} tcv={$row['tcversion_id']} status={$row['status']} ts={$row['execution_ts']} notes_len=" . strlen($row['notes']) . "\n";
    $n++;
}
echo "total existing executions: $n\n";
$mysqli->close();
