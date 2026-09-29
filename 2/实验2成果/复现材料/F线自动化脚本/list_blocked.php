<?php
require 'D:/XAMPP/htdocs/testlink-lab2/config_db.inc.php';
$mysqli = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
if ($mysqli->connect_errno) { fwrite(STDERR, "Connect failed: " . $mysqli->connect_error . "\n"); exit(1); }
$mysqli->query("SET NAMES utf8");
// List BLOCKED executions with notes
$res = $mysqli->query("SELECT e.id, e.tcversion_id, v.tc_external_id, parent.name AS name, e.notes "
     . "FROM executions e "
     . "JOIN tcversions v ON v.id=e.tcversion_id "
     . "JOIN nodes_hierarchy vnode ON vnode.id=v.id "
     . "JOIN nodes_hierarchy parent ON parent.id=vnode.parent_id "
     . "WHERE e.testplan_id=108 AND e.status='b' "
     . "ORDER BY v.tc_external_id");
while ($row = $res->fetch_assoc()) {
    echo "id={$row['id']} tcv={$row['tcversion_id']} ext={$row['tc_external_id']} name={$row['name']}\n";
    echo "  notes: " . substr($row['notes'], 0, 100) . "\n";
}
$mysqli->close();
