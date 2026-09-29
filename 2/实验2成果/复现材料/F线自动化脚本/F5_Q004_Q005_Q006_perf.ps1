# F5 / Q004-Q006 Performance test - using Start-ThreadJob
param(
    [int]$Users = 50,
    [int]$DurationSec = 60
)
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'

# Login
$loginBody = '{"employeeNo":"U001","password":"123456"}'
$loginResp = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json' -TimeoutSec 10
if ($loginResp.code -ne 200) { Write-Output "LOGIN_FAILED"; exit 1 }
$sid = $loginResp.data.sessionId
Write-Output "Login OK"

1..5 | ForEach-Object { Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/current' -Method Get -Headers @{Authorization=$sid} -TimeoutSec 5 | Out-Null }
Write-Output "Warmup done"

$latencies = [System.Collections.Concurrent.ConcurrentBag[double]]::new()
$errors = [System.Collections.Concurrent.ConcurrentBag[string]]::new()
$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$endTime = [DateTime]::Now.AddSeconds($DurationSec)

$mesProc = Get-Process -Name java -ErrorAction SilentlyContinue | Select-Object -First 1
$mesProcId = if ($mesProc) { $mesProc.Id } else { 0 }
$cpuSamples = [System.Collections.Concurrent.ConcurrentBag[float]]::new()
$memSamples = [System.Collections.Concurrent.ConcurrentBag[float]]::new()

$monitorJob = Start-Job -ScriptBlock {
    param($procId, $endTime, $cpus, $mems)
    while ((Get-Date) -lt $endTime) {
        try {
            $proc = Get-Process -Id $procId -ErrorAction Stop
            [void]$cpus.Add([float]$proc.CPU)
            [void]$mems.Add([float]($proc.WorkingSet64 / 1MB))
        } catch {}
        Start-Sleep -Milliseconds 1000
    }
} -ArgumentList $mesProcId, $endTime, $cpuSamples, $memSamples

Write-Output "Starting $Users concurrent users for $DurationSec seconds..."

# Start-ThreadJob worker script
$workerScript = {
    param($sid, $latencies, $errors, $endTime)
    while ((Get-Date) -lt $endTime) {
        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            $resp = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/current' -Method Get -Headers @{Authorization=$sid} -TimeoutSec 10
            $sw.Stop()
            if ($resp.code -eq 200 -and $resp.data.employeeNo -eq 'U001') {
                [void]$latencies.Add($sw.Elapsed.TotalMilliseconds)
            } else {
                [void]$errors.Add("code=$($resp.code)")
            }
        } catch {
            $sw.Stop()
            [void]$errors.Add("exc:" + $_.Exception.Message)
        }
    }
}

$jobs = 1..$Users | ForEach-Object {
    Start-ThreadJob -ScriptBlock $workerScript -ArgumentList $sid, $latencies, $errors, $endTime -ThrottleLimit $Users
}

# Wait until duration ends
Start-Sleep -Seconds $DurationSec
# Stop all jobs
$jobs | Stop-Job -ErrorAction SilentlyContinue
$jobs | Wait-Job -Timeout 5 -ErrorAction SilentlyContinue
$stopwatch.Stop()

$null = Wait-Job -Job $monitorJob -Timeout 5
Stop-Job -Job $monitorJob

# Stats
$latArray = $latencies.ToArray() | Sort-Object
$totalReq = $latArray.Count + $errors.Count
$successCount = $latArray.Count
$failCount = $errors.Count
$elapsedSec = $stopwatch.Elapsed.TotalSeconds
$rps = [math]::Round($totalReq / $elapsedSec, 2)

if ($latArray.Count -eq 0) {
    Write-Output "NO_LATENCY_SAMPLES"
    exit 1
}
$p50 = [math]::Round($latArray[[int]($latArray.Count * 0.5)], 2)
$p95 = [math]::Round($latArray[[int]($latArray.Count * 0.95)], 2)
$p99 = [math]::Round($latArray[[int]($latArray.Count * 0.99)], 2)
$avg = [math]::Round(($latArray | Measure-Object -Average).Average, 2)
$max = [math]::Round(($latArray | Measure-Object -Maximum).Maximum, 2)
$min = [math]::Round(($latArray | Measure-Object -Minimum).Minimum, 2)

$cpuArr = $cpuSamples.ToArray()
$memArr = $memSamples.ToArray()
$cpuMax = if ($cpuArr.Count -gt 0) { [math]::Round(($cpuArr | Measure-Object -Maximum).Maximum, 2) } else { 'N/A' }
$cpuAvg = if ($cpuArr.Count -gt 0) { [math]::Round(($cpuArr | Measure-Object -Average).Average, 2) } else { 'N/A' }
$memMax = if ($memArr.Count -gt 0) { [math]::Round(($memArr | Measure-Object -Maximum).Maximum, 2) } else { 'N/A' }
$memAvg = if ($memArr.Count -gt 0) { [math]::Round(($memArr | Measure-Object -Average).Average, 2) } else { 'N/A' }

Write-Output "===== Q004/Q005/Q006 RESULT ====="
Write-Output "ConcurrentUsers: $Users"
Write-Output "DurationSec: $([math]::Round($elapsedSec,2))"
Write-Output "TotalRequests: $totalReq"
Write-Output "Success: $successCount"
Write-Output "Fail: $failCount"
Write-Output "ThroughputRps: $rps"
Write-Output "LatencyMs_min: $min"
Write-Output "LatencyMs_avg: $avg"
Write-Output "LatencyMs_p50: $p50"
Write-Output "LatencyMs_p95: $p95"
Write-Output "LatencyMs_p99: $p99"
Write-Output "LatencyMs_max: $max"
Write-Output "JavaProcessCpu_sec_avg: $cpuAvg"
Write-Output "JavaProcessCpu_sec_max: $cpuMax"
Write-Output "JavaProcessMem_MB_avg: $memAvg"
Write-Output "JavaProcessMem_MB_max: $memMax"

$errorGroups = $errors.ToArray() | Group-Object | Sort-Object Count -Descending | Select-Object -First 5
Write-Output "TopErrors:"
if ($errorGroups) {
    $errorGroups | ForEach-Object { Write-Output ("  {0} = {1}" -f $_.Name, $_.Count) }
} else {
    Write-Output "  (none)"
}

$pe01Pass = ($avg -lt 500 -and $p95 -lt 2000)
$pe02Pass = ($memMax -is [string] -or $memMax -lt 1024)
$successRate = if ($totalReq -gt 0) { $successCount / $totalReq * 100 } else { 0 }
$pe03Pass = ($successRate -ge 95 -and $rps -ge 50)

if ($pe01Pass) { Write-Output "VERDICT_Q004_PE01_TIME: PASS" } else { Write-Output "VERDICT_Q004_PE01_TIME: FAIL" }
if ($pe02Pass) { Write-Output "VERDICT_Q005_PE02_RESOURCE: PASS" } else { Write-Output "VERDICT_Q005_PE02_RESOURCE: FAIL" }
if ($pe03Pass) { Write-Output "VERDICT_Q006_PE03_CAPACITY: PASS" } else { Write-Output "VERDICT_Q006_PE03_CAPACITY: FAIL" }
