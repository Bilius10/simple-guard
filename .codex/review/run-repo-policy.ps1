$ErrorActionPreference = 'Stop'

$forbiddenPatterns = @(
    '^\.codex/(?!review/)',
    '^(\.gradle|\.gradle-cache|\.gradle-user|\.gradle-project-cache|\.gradle-project-cache-temp|\.android-user-home|\.android-cov-temp|\.m2)/',
    '^web-admin/(node_modules|dist|coverage|\.angular)/',
    '^android-agent/(\.gradle|\.gradle-user|\.gradle-codex-coverage|\.kotlin|build|.*/build)/',
    '^android-agent/local\.properties$',
    '^api/(\.maven-local|\.m2|target|AppData)/',
    '^desktop-agent/(target|gen)/',
    '^deploy-wifi/'
)

$trackedFiles = git ls-files
$violations = foreach ($file in $trackedFiles) {
    $normalized = $file -replace '\\', '/'
    foreach ($pattern in $forbiddenPatterns) {
        if ($normalized -match $pattern) {
            $normalized
            break
        }
    }
}

if ($violations) {
    Write-Error ("Forbidden tracked files detected:`n" + ($violations | Sort-Object -Unique | ForEach-Object { "- $_" } | Out-String))
    exit 1
}

Write-Host 'No forbidden tracked files detected.'
