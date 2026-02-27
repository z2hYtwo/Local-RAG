# Bmad-method Environment Check Script
# System: Windows (PowerShell)

$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Bmad-method Env Check Tool (v1.0)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$allPassed = $true

# 1. Check JDK
Write-Host "[1/5] Checking JDK (17 or 21)..." -NoNewline
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host " [PASS]" -ForegroundColor Green
    } else {
        Write-Host " [FAIL]" -ForegroundColor Red
        Write-Host "      Java not found. Install JDK 17+." -ForegroundColor Yellow
        $allPassed = $false
    }
} catch {
    Write-Host " [FAIL]" -ForegroundColor Red
    $allPassed = $false
}

# 2. Check Node.js
Write-Host "[2/5] Checking Node.js (18+)..." -NoNewline
try {
    $nodeVersion = node -v 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host " [PASS]" -ForegroundColor Green
    } else {
        Write-Host " [FAIL]" -ForegroundColor Red
        Write-Host "      Node.js not found." -ForegroundColor Yellow
        $allPassed = $false
    }
} catch {
    Write-Host " [FAIL]" -ForegroundColor Red
    $allPassed = $false
}

# 3. Check CMake
Write-Host "[3/6] Checking CMake (3.10+)..." -NoNewline
try {
    $cmakeVersion = cmake --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host " [PASS]" -ForegroundColor Green
    } else {
        Write-Host " [FAIL]" -ForegroundColor Red
        Write-Host "      CMake not found. Run 'winget install kitware.cmake'" -ForegroundColor Yellow
        $allPassed = $false
    }
} catch {
    Write-Host " [FAIL]" -ForegroundColor Red
    $allPassed = $false
}

# 3.5 Check Gradle
Write-Host "[4/6] Checking Gradle..." -NoNewline
try {
    $gradleVersion = gradle -v 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host " [PASS]" -ForegroundColor Green
    } else {
        Write-Host " [FAIL]" -ForegroundColor Red
        Write-Host "      Gradle not found. Run 'winget install gradle'" -ForegroundColor Yellow
        $allPassed = $false
    }
} catch {
    Write-Host " [FAIL]" -ForegroundColor Red
    $allPassed = $false
}

# 4. Check MSVC
Write-Host "[5/6] Checking MSVC (cl.exe)..." -NoNewline
$clPath = where.exe cl.exe 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host " [PASS]" -ForegroundColor Green
} else {
    Write-Host " [WARN]" -ForegroundColor Yellow
    Write-Host "      cl.exe not in PATH. Use 'Developer PowerShell for VS'." -ForegroundColor Gray
}

# 5. Check Model File
Write-Host "[6/6] Checking Model Path..." -NoNewline
$configPath = "backend/src/main/resources/application.yml"
if (Test-Path $configPath) {
    $content = Get-Content $configPath
    $modelLine = $content | Select-String -Pattern 'path: "(.*)"'
    if ($modelLine) {
        $modelPath = $modelLine.Matches.Groups[1].Value
        if ($modelPath -and (Test-Path $modelPath)) {
            Write-Host " [PASS]" -ForegroundColor Green
        } else {
            Write-Host " [FAIL]" -ForegroundColor Red
            Write-Host "      Invalid path: $modelPath" -ForegroundColor Yellow
            $allPassed = $false
        }
    } else {
        Write-Host " [FAIL]" -ForegroundColor Red
        $allPassed = $false
    }
} else {
    Write-Host " [ERR]" -ForegroundColor Red
    $allPassed = $false
}

Write-Host "========================================" -ForegroundColor Cyan
if ($allPassed) {
    Write-Host " Done! Run: ./gradlew build" -ForegroundColor Green
} else {
    Write-Host " Please fix the issues above." -ForegroundColor Red
}
Write-Host "========================================" -ForegroundColor Cyan
