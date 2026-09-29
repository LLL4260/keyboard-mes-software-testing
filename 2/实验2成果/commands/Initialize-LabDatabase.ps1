param(
    [string]$DatabaseUser = 'root',
    [string]$MySqlExe = 'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe'
)
$ErrorActionPreference = 'Stop'
$courseRoot = (Get-Item -LiteralPath $PSScriptRoot).Parent.Parent.Parent.FullName
$sqlRoot = Join-Path $courseRoot 'program\code\backend\src\main\resources\sql'
$schema = Join-Path $sqlRoot 'schema.sql'
$seed = Join-Path $sqlRoot 'reset_rich_mock_data.sql'
foreach ($p in @($MySqlExe,$schema,$seed)) {
    if (-not (Test-Path -LiteralPath $p)) { throw "Required file missing: $p" }
}
foreach ($p in @($schema,$seed)) {
    $sqlText = [IO.File]::ReadAllText($p)
    if ($sqlText -match '(?im)^\s*(USE\s|CREATE\s+DATABASE|DROP\s+DATABASE)' -or $sqlText -match '(?i)`?keyboard_mes`?\s*\.') {
        throw "Unexpected database selection in source SQL. Review before using: $p"
    }
}
Write-Output 'Creating NEW database keyboard_mes_lab2. If it already exists, this script stops without importing or clearing data.'
Write-Output 'MySQL will ask for the database password up to three times. It is not stored by this script.'
& $MySqlExe --protocol=TCP --host=127.0.0.1 --port=3306 "--user=$DatabaseUser" --password --default-character-set=utf8mb4 '--execute=CREATE DATABASE keyboard_mes_lab2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;'
if ($LASTEXITCODE -ne 0) { throw 'CREATE DATABASE failed. No imports attempted. Do not delete an existing database just to rerun this script.' }
foreach ($p in @($schema,$seed)) {
    $sourcePath = $p.Replace('\','/')
    & $MySqlExe --protocol=TCP --host=127.0.0.1 --port=3306 "--user=$DatabaseUser" --password --default-character-set=utf8mb4 --database=keyboard_mes_lab2 "--execute=source $sourcePath"
    if ($LASTEXITCODE -ne 0) { throw "Import failed: $p. Preserve the database and inspect the error; do not blindly repeat the reset script." }
}
Write-Output 'New isolated database initialized. The existing keyboard_mes database was not selected.'
