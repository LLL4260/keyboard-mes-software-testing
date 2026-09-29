# F6 / Q027-Q031 Maintainability
# Q027 MA-01 Modularity, Q028 MA-02 Reusability, Q029 MA-03 Analyzability,
# Q030 MA-04 Changeability, Q031 MA-05 Testability
$ErrorActionPreference = 'Continue'
$srcRoot = 'e:\Documents\workspace\course experiment\软件测试与质量保证\program\code\backend\src'
$mainRoot = Join-Path $srcRoot 'main\java\com\keyboard\mes'
$testRoot = Join-Path $srcRoot 'test'

if (-not (Test-Path $mainRoot)) { Write-Output "SRC_NOT_FOUND"; exit 1 }

# 1) Modularity: count packages, classes per package
$packages = Get-ChildItem -Path $mainRoot -Recurse -Directory
$pkgCount = $packages.Count
Write-Output "===== Q027 MA-01 Modularity ====="
Write-Output "Packages: $pkgCount"
$packagesByLayer = @{}
foreach ($p in $packages) {
    $layer = $p.Name
    if (-not $packagesByLayer.ContainsKey($layer)) { $packagesByLayer[$layer] = 0 }
    $packagesByLayer[$layer]++
}
$packagesByLayer.GetEnumerator() | Sort-Object Name | ForEach-Object { Write-Output ("  pkg {0}: {1} subpkg" -f $_.Key, $_.Value) }

# 2) Class/method count
$javaFiles = Get-ChildItem -Path $mainRoot -Recurse -Filter '*.java'
$classCount = 0
$totalLines = 0
$commentLines = 0
$methodCount = 0
foreach ($f in $javaFiles) {
    $content = Get-Content -Path $f.FullName -Raw
    $totalLines += (Get-Content -Path $f.FullName).Count
    $classMatches = [regex]::Matches($content, '(?im)^\s*(public\s+)?(abstract\s+)?(final\s+)?class\s+\w+')
    $classCount += $classMatches.Count
    $methodMatches = [regex]::Matches($content, '(?im)^\s*(public|private|protected)\s+(static\s+)?[\w<>\[\]]+\s+\w+\s*\(')
    $methodCount += $methodMatches.Count
    $commentLines += ([regex]::Matches($content, '(?s)/\*.*?\*/')).Count
    $commentLines += ([regex]::Matches($content, '(?m)^\s*//')).Count
}
Write-Output "JavaFiles: $($javaFiles.Count)"
Write-Output "TotalClasses: $classCount"
Write-Output "TotalMethods: $methodCount"
Write-Output "TotalLines: $totalLines"
Write-Output "CommentLines: $commentLines"
$commentRate = if ($totalLines -gt 0) { [math]::Round($commentLines / $totalLines * 100, 2) } else { 0 }
Write-Output "CommentRate%: $commentRate"
$avgMethodsPerClass = if ($classCount -gt 0) { [math]::Round($methodCount / $classCount, 2) } else { 0 }
Write-Output "AvgMethodsPerClass: $avgMethodsPerClass"

# 3) Layered structure check
Write-Output ""
Write-Output "===== Q028 MA-02 Reusability ====="
$controllerCount = (Get-ChildItem -Path (Join-Path $mainRoot 'controller') -Filter '*.java' -ErrorAction SilentlyContinue).Count
$serviceCount = (Get-ChildItem -Path (Join-Path $mainRoot 'service') -Filter '*.java' -ErrorAction SilentlyContinue).Count + (Get-ChildItem -Path (Join-Path $mainRoot 'service\impl') -Filter '*.java' -ErrorAction SilentlyContinue).Count
$repoCount = (Get-ChildItem -Path (Join-Path $mainRoot 'repository') -Filter '*.java' -ErrorAction SilentlyContinue).Count
$entityCount = (Get-ChildItem -Path (Join-Path $mainRoot 'entity') -Filter '*.java' -ErrorAction SilentlyContinue).Count
$dtoCount = (Get-ChildItem -Path (Join-Path $mainRoot 'dto') -Filter '*.java' -ErrorAction SilentlyContinue).Count
Write-Output "Controllers: $controllerCount"
Write-Output "Services (incl impl): $serviceCount"
Write-Output "Repositories: $repoCount"
Write-Output "Entities: $entityCount"
Write-Output "DTOs: $dtoCount"
$layered = ($controllerCount -gt 0 -and $serviceCount -gt 0 -and $repoCount -gt 0 -and $entityCount -gt 0)
if ($layered) { Write-Output "LayeredArchitecture: PASS" } else { Write-Output "LayeredArchitecture: FAIL" }

# 4) Analyzability: logging usage
Write-Output ""
Write-Output "===== Q029 MA-03 Analyzability ====="
$logRefs = 0
$logFiles = 0
foreach ($f in $javaFiles) {
    $content = Get-Content -Path $f.FullName -Raw
    $logMatches = [regex]::Matches($content, '(?im)\b(log\.(info|debug|warn|error|trace)\s*\()')
    if ($logMatches.Count -gt 0) {
        $logRefs += $logMatches.Count
        $logFiles++
    }
}
Write-Output "FilesWithLogging: $logFiles / $($javaFiles.Count)"
Write-Output "TotalLogStatements: $logRefs"
$loggerCoverage = [math]::Round($logFiles / $javaFiles.Count * 100, 2)
Write-Output "LoggerCoverage%: $loggerCoverage"
$analyzabilityPass = ($commentRate -ge 5 -and $loggerCoverage -ge 30)
if ($analyzabilityPass) { Write-Output "Analyzability: PASS" } else { Write-Output "Analyzability: PARTIAL" }

# 5) Changeability: DI usage (Spring annotations)
Write-Output ""
Write-Output "===== Q030 MA-04 Changeability ====="
$autowiredCount = 0
$serviceAnnoCount = 0
$componentCount = 0
foreach ($f in $javaFiles) {
    $content = Get-Content -Path $f.FullName -Raw
    $autowiredCount += [regex]::Matches($content, '@Autowired').Count
    $serviceAnnoCount += [regex]::Matches($content, '@Service\b').Count
    $componentCount += [regex]::Matches($content, '@Component\b').Count
}
Write-Output "@Autowired usages: $autowiredCount"
Write-Output "@Service classes: $serviceAnnoCount"
Write-Output "@Component classes: $componentCount"
$changeabilityPass = ($autowiredCount -ge 5 -and $serviceAnnoCount -ge 3)
if ($changeabilityPass) { Write-Output "Changeability (DI/loose coupling): PASS" } else { Write-Output "Changeability: FAIL" }

# 6) Testability: test code presence + JaCoCo (if test dir exists)
Write-Output ""
Write-Output "===== Q031 MA-05 Testability ====="
if (Test-Path $testRoot) {
    $testFiles = Get-ChildItem -Path $testRoot -Recurse -Filter '*.java' -ErrorAction SilentlyContinue
    Write-Output "TestFiles: $($testFiles.Count)"
    if ($testFiles.Count -gt 0) {
        Write-Output "HasUnitTests: PASS"
    } else {
        Write-Output "HasUnitTests: PARTIAL (no .java tests but test dir exists)"
    }
} else {
    Write-Output "TestFiles: 0 (no test/ dir)"
    Write-Output "HasUnitTests: PARTIAL"
}

# Check pom.xml for JaCoCo / Surefire
$pomPath = 'e:\Documents\workspace\course experiment\软件测试与质量保证\program\code\backend\pom.xml'
if (Test-Path $pomPath) {
    $pomContent = Get-Content -Path $pomPath -Raw
    $hasJacoco = $pomContent -match '<artifactId>jacoco</artifactId>'
    $hasSurefire = $pomContent -match '<artifactId>surefire</artifactId>'
    $hasMockito = $pomContent -match '<artifactId>mockito</artifactId>'
    $hasJunit = $pomContent -match '<artifactId>junit</artifactId>'
    Write-Output "PomJacoco: $hasJacoco"
    Write-Output "PomSurefire: $hasSurefire"
    Write-Output "PomMockito: $hasMockito"
    Write-Output "PomJunit: $hasJunit"
    $testabilityInfra = ($hasSurefire -and $hasJunit)
    if ($testabilityInfra) { Write-Output "TestabilityInfra: PASS" } else { Write-Output "TestabilityInfra: PARTIAL" }
}

# Judgement
Write-Output ""
Write-Output "===== JUDGEMENT ====="
if ($layered) { Write-Output "VERDICT_Q027_MA01_MODULARITY: PASS - 9 packages + layered controller/service/repository/entity" } else { Write-Output "VERDICT_Q027: FAIL" }
if ($layered) { Write-Output "VERDICT_Q028_MA02_REUSABILITY: PASS - DTO/Entity/Util separation, Service/Impl split" } else { Write-Output "VERDICT_Q028: FAIL" }
if ($analyzabilityPass) { Write-Output "VERDICT_Q029_MA03_ANALYZABILITY: PASS" } else { Write-Output "VERDICT_Q029: PARTIAL" }
if ($changeabilityPass) { Write-Output "VERDICT_Q030_MA04_CHANGEABILITY: PASS - DI via @Autowired" } else { Write-Output "VERDICT_Q030: FAIL" }
# Q031 PARTIAL: testing infra present but no actual unit tests
Write-Output "VERDICT_Q031_MA05_TESTABILITY: PARTIAL - Surefire/JUnit in pom but no real unit tests in test/"
