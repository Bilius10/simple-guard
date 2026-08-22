param(
    [Parameter(Mandatory = $true)]
    [string] $WorkingDirectory,

    [Parameter(Mandatory = $true)]
    [string] $Check
)

$ErrorActionPreference = 'Stop'
$repositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..')
$workdirPath = Join-Path $repositoryRoot $WorkingDirectory

function Invoke-CommandInWorkdir {
    param(
        [Parameter(Mandatory = $true)] [string] $Executable,
        [string[]] $Arguments = @()
    )

    Push-Location $workdirPath
    try {
        $localExecutable = Join-Path (Get-Location) $Executable
        if (Test-Path -LiteralPath $localExecutable) {
            $Executable = $localExecutable
        }
        & $Executable @Arguments
        exit $LASTEXITCODE
    } finally {
        Pop-Location
    }
}

function Skip-Check {
    param([string] $Reason)
    Write-Host "Skipping ${WorkingDirectory}:${Check} - $Reason"
    exit 0
}

function Read-TextFile {
    param([string] $Path)
    if (-not (Test-Path -LiteralPath $Path)) {
        return ''
    }
    return Get-Content -Path $Path -Raw
}

function Test-CargoSubcommand {
    param([string] $Name)

    Push-Location $workdirPath
    try {
        & cargo $Name --version *> $null
        return $LASTEXITCODE -eq 0
    } catch {
        return $false
    } finally {
        Pop-Location
    }
}

if ($WorkingDirectory -eq 'android-agent') {
    $env:GRADLE_USER_HOME = Join-Path $repositoryRoot '.gradle-user'
    $env:ANDROID_USER_HOME = Join-Path $repositoryRoot '.android-user-home'
}

switch ("${WorkingDirectory}:${Check}") {
    'api:spotless' {
        $pom = Read-TextFile (Join-Path $workdirPath 'pom.xml')
        if ($pom -notmatch 'spotless-maven-plugin') { Skip-Check 'spotless-maven-plugin is not configured in api/pom.xml.' }
        Invoke-CommandInWorkdir 'mvnw.cmd' @('spotless:check')
    }
    'api:checkstyle' {
        $pom = Read-TextFile (Join-Path $workdirPath 'pom.xml')
        if ($pom -notmatch 'maven-checkstyle-plugin') { Skip-Check 'maven-checkstyle-plugin is not configured in api/pom.xml.' }
        Invoke-CommandInWorkdir 'mvnw.cmd' @('checkstyle:check')
    }
    'api:pmd' {
        $pom = Read-TextFile (Join-Path $workdirPath 'pom.xml')
        if ($pom -notmatch 'maven-pmd-plugin') { Skip-Check 'maven-pmd-plugin is not configured in api/pom.xml.' }
        Invoke-CommandInWorkdir 'mvnw.cmd' @('pmd:check')
    }
    'api:spotbugs' {
        $pom = Read-TextFile (Join-Path $workdirPath 'pom.xml')
        if ($pom -notmatch 'spotbugs-maven-plugin') { Skip-Check 'spotbugs-maven-plugin is not configured in api/pom.xml.' }
        Invoke-CommandInWorkdir 'mvnw.cmd' @('spotbugs:check')
    }
    'android-agent:ktlint' {
        $gradle = (Read-TextFile (Join-Path $workdirPath 'build.gradle.kts')) + "`n" + (Read-TextFile (Join-Path $workdirPath 'app/build.gradle.kts'))
        if ($gradle -notmatch 'ktlint') { Skip-Check 'ktlint Gradle plugin is not configured.' }
        Invoke-CommandInWorkdir 'gradlew.bat' @('--project-cache-dir', (Join-Path $repositoryRoot '.gradle-project-cache-temp'), '--no-daemon', '--max-workers=1', 'ktlintCheck')
    }
    'android-agent:detekt' {
        $gradle = (Read-TextFile (Join-Path $workdirPath 'build.gradle.kts')) + "`n" + (Read-TextFile (Join-Path $workdirPath 'app/build.gradle.kts'))
        if ($gradle -notmatch 'detekt') { Skip-Check 'detekt Gradle plugin is not configured.' }
        Invoke-CommandInWorkdir 'gradlew.bat' @('--project-cache-dir', (Join-Path $repositoryRoot '.gradle-project-cache-temp'), '--no-daemon', '--max-workers=1', 'detekt')
    }
    'web-admin:eslint' {
        $packageJsonPath = Join-Path $workdirPath 'package.json'
        $package = if (Test-Path -LiteralPath $packageJsonPath) { Get-Content -Path $packageJsonPath -Raw | ConvertFrom-Json } else { $null }
        if ($null -eq $package -or $null -eq $package.scripts -or $null -eq $package.scripts.lint) { Skip-Check 'npm script "lint" is not configured.' }
        Invoke-CommandInWorkdir 'npm' @('run', 'lint')
    }
    'web-admin:prettier' {
        $packageJsonPath = Join-Path $workdirPath 'package.json'
        $package = if (Test-Path -LiteralPath $packageJsonPath) { Get-Content -Path $packageJsonPath -Raw | ConvertFrom-Json } else { $null }
        if ($null -eq $package -or $null -eq $package.scripts) { Skip-Check 'package.json scripts are not configured.' }
        if ($null -ne $package.scripts.'format:check') { Invoke-CommandInWorkdir 'npm' @('run', 'format:check') }
        if ($null -ne $package.scripts.'prettier:check') { Invoke-CommandInWorkdir 'npm' @('run', 'prettier:check') }
        Skip-Check 'no format:check or prettier:check npm script is configured.'
    }
    'desktop-agent:audit' {
        if (-not (Test-CargoSubcommand 'audit')) { Skip-Check 'cargo-audit is not installed.' }
        Invoke-CommandInWorkdir 'cargo' @('audit')
    }
    'desktop-agent:deny' {
        if (-not (Test-CargoSubcommand 'deny')) { Skip-Check 'cargo-deny is not installed.' }
        Invoke-CommandInWorkdir 'cargo' @('deny', 'check')
    }
    default {
        Write-Error "Unsupported quality check: ${WorkingDirectory}:${Check}"
        exit 1
    }
}

