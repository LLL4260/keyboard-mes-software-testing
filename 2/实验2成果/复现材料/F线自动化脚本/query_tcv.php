<?php
require 'D:/XAMPP/htdocs/testlink-lab2/config_db.inc.php';
$mysqli = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
if ($mysqli->connect_errno) { fwrite(STDERR, "Connect failed: " . $mysqli->connect_error . "\n"); exit(1); }
$mysqli->query("SET NAMES utf8");
// v.id is testcase_version nodes_hierarchy id (node_type_id=4); its parent is testcase (node_type_id=3) holding name
$sql = "SELECT v.id AS tcv_id, v.tc_external_id, parent.name AS name "
     . "FROM tcversions v "
     . "JOIN nodes_hierarchy vnode ON vnode.id=v.id "
     . "JOIN nodes_hierarchy parent ON parent.id=vnode.parent_id "
     . "ORDER BY v.tc_external_id";
$res = $mysqli->query($sql);
if (!$res) { fwrite(STDERR, "Query failed: " . $mysqli->error . "\n"); exit(1); }
$out = fopen(__DIR__ . '/tcv_list.tsv', 'w');
while ($row = $res->fetch_assoc()) {
    fputcsv($out, [$row['tcv_id'], $row['tc_external_id'], $row['name']], "\t");
}
fclose($out);
echo "rows_exported=" . $res->num_rows . "\n";
echo "sample:\n";
$res->data_seek(0);
for ($i=0;$i<3;$i++) { $row=$res->fetch_assoc(); echo "  tcv_id={$row['tcv_id']} ext={$row['tc_external_id']} name={$row['name']}\n"; }
$mysqli->close();
