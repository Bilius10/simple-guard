param(
    [Parameter(Mandatory = $true)]
    [string] $WorkingDirectory,

    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $Command
)

$ErrorActionPreference = 'Stop'

if ($Command.Count -eq 0) {
    throw 'Missing command to execute.'
}

$repositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..')

Push-Location (Join-Path $repositoryRoot $WorkingDirectory)
try {
    $executable = $Command[0]
    $arguments = @()
    if ($Command.Count -gt 1) {
        $arguments = $Command[1..($Command.Count - 1)]
    }

    if ($WorkingDirectory -eq 'android-agent') {
        $env:GRADLE_USER_HOME = Join-Path $repositoryRoot '.gradle-user'
        $env:ANDROID_USER_HOME = Join-Path $repositoryRoot '.android-user-home'

        if ($executable -eq 'gradlew.bat' -and -not ($arguments -contains '--project-cache-dir')) {
            $arguments = @('--project-cache-dir', (Join-Path $repositoryRoot '.gradle-project-cache-temp')) + $arguments
        }
    }

    $localExecutable = Join-Path (Get-Location) $executable
    if (Test-Path -LiteralPath $localExecutable) {
        $executable = $localExecutable
    }

    & $executable @arguments
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
