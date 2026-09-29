param(
    [string]$DatabaseUser = 'root',
    [string]$MavenExe = 'D:\IntelliJ IDEA 2026.1.3\plugins\maven\lib\maven3\bin\mvn.cmd',
    [string]$JavaHome = 'C:\Program Files\Java\jdk-17',
    [int]$RedisDatabase = 15
)
$ErrorActionPreference = 'Stop'
$courseRoot = (Get-Item -LiteralPath $PSScriptRoot).Parent.Parent.Parent.FullName
$backend = Join-Path $courseRoot 'program\code\backend'
if (-not (Test-Path -LiteralPath $MavenExe)) { throw "Maven not found: $MavenExe" }
if (-not (Test-Path -LiteralPath (Join-Path $JavaHome 'bin\java.exe'))) { throw 'Java 17 location is invalid.' }
if (netstat -ano -p tcp | Select-String '^\s*TCP\s+\S+:8088\s+.*LISTENING') { throw 'Port 8088 is already listening. Identify the existing instance before starting another.' }
$keys = @('JAVA_HOME','DB_HOST','DB_PORT','DB_NAME','DB_USERNAME','DB_PASSWORD','REDIS_HOST','REDIS_PORT','REDIS_DATABASE','REDIS_PASSWORD')
$saved = @{}
foreach ($key in $keys) { $saved[$key] = [Environment]::GetEnvironmentVariable($key,'Process') }
$dbSecret = Read-Host 'MySQL password (not saved to a file)' -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbSecret)
try {
    $env:JAVA_HOME = $JavaHome
    $env:DB_HOST = '127.0.0.1'
    $env:DB_PORT = '3306'
    $env:DB_NAME = 'keyboard_mes_lab2'
    $env:DB_USERNAME = $DatabaseUser
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    $env:REDIS_HOST = '127.0.0.1'
    $env:REDIS_PORT = '6379'
    $env:REDIS_DATABASE = [string]$RedisDatabase
    Write-Output "Using keyboard_mes_lab2 and Redis logical database $RedisDatabase. Verify that this Redis database is reserved for this lab."
    Push-Location -LiteralPath $backend
    try { & $MavenExe spring-boot:run } finally { Pop-Location }
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    foreach ($key in $keys) { [Environment]::SetEnvironmentVariable($key,$saved[$key],'Process') }
}
