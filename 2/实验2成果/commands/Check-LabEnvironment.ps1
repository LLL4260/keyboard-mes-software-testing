param([string]$XamppRoot = 'D:\XAMPP')
$ErrorActionPreference = 'Continue'
Write-Output '=== Program checks ==='
$programs = @{
    PHP = "$XamppRoot\php\php.exe"
    Apache = "$XamppRoot\apache\bin\httpd.exe"
    MySQL = 'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe'
    Java17 = 'C:\Program Files\Java\jdk-17\bin\java.exe'
    Maven = 'D:\IntelliJ IDEA 2026.1.3\plugins\maven\lib\maven3\bin\mvn.cmd'
    Redis = 'D:\Redis-x64-5.0.14.1\redis-cli.exe'
}
$programs.GetEnumerator() | ForEach-Object {
    [pscustomobject]@{Program=$_.Key;Exists=(Test-Path -LiteralPath $_.Value);Path=$_.Value}
} | Format-Table -AutoSize
Write-Output '=== Listening ports ==='
netstat -ano -p tcp | Select-String '^\s*TCP\s+\S+:(3306|6379|8080|8443|8088|5173)\s+.*LISTENING' | ForEach-Object { $_.Line }
if (Test-Path $programs.PHP) { & $programs.PHP -v }
if (Test-Path $programs.Apache) { & $programs.Apache -t }
foreach ($url in @('http://localhost:8080/phpmyadmin/','http://localhost:8080/testlink-lab2/login.php','http://localhost:8088/api/health')) {
    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 4
        $fatal = $response.Content -match 'Fatal error|Uncaught Error'
        [pscustomobject]@{URL=$url;HTTP=[int]$response.StatusCode;FatalError=$fatal}
    } catch { [pscustomobject]@{URL=$url;Error=$_.Exception.Message} }
}
Write-Output 'Read-only check finished. HTTP 200 alone does not establish application health.'
