<#
.SYNOPSIS
    E3 external validator for the GT-03 burnout GameTest run.
.DESCRIPTION
    Validates that a captured GT-03 runtime log contains the structured E1/E2
    PASS markers, the profile guard, positive GameTest completion, matching run
    IDs, and no relevant test failure, campfire BlockEntity exception or Mixin
    application error. The raw log is never modified.
.PARAMETER LogPath
    Path to the captured GT-03 latest.log.
.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts/validate_e3.ps1 -LogPath run/gametest-gt03-burnout/logs/latest.log
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$LogPath
)

$ErrorActionPreference = 'Stop'

function Test-KnownNoise([string]$line) {
    # Narrow, documented environmental noise only. No broad suppression.
    return ($line -cmatch 'campful' -or
            $line -cmatch 'Yggdrasil' -or
            $line -cmatch 'Realms' -or
            $line -cmatch "Can't keep up!")
}

if (-not (Test-Path -LiteralPath $LogPath -PathType Leaf)) {
    Write-Output "E3 RESULT: FAIL"
    Write-Output "Reason: log file not found: $LogPath"
    exit 2
}

$rawHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $LogPath).Hash
$lines = @(Get-Content -LiteralPath $LogPath)

$failures = New-Object System.Collections.Generic.List[string]

# 1. Positive GameTest completion markers.
$completion = @($lines | Where-Object { $_ -cmatch 'All \d+ required tests passed :\)' })
if ($completion.Count -lt 1) {
    $failures.Add('Missing positive GameTest completion marker "All N required tests passed :)"')
}
$shutdown = @($lines | Where-Object { $_ -cmatch 'Game test server shutting down' })
if ($shutdown.Count -lt 1) {
    $failures.Add('Missing "Game test server shutting down" line')
}

# 2/3. Exactly one E1 and one E2 PASS marker.
$e1 = @($lines | Where-Object { $_ -cmatch '\[P3-T2-EVIDENCE\]\s+scenario=E1\s+result=PASS' })
$e2 = @($lines | Where-Object { $_ -cmatch '\[P3-T2-EVIDENCE\]\s+scenario=E2\s+result=PASS' })
if ($e1.Count -ne 1) { $failures.Add("Expected exactly one E1 PASS marker but found $($e1.Count)") }
if ($e2.Count -ne 1) { $failures.Add("Expected exactly one E2 PASS marker but found $($e2.Count)") }

# 4/5/6. Profile guard and run ID binding.
$guards = @($lines | Where-Object { $_ -cmatch '\[P3-T2-PROFILE\]\s+kind=GUARD\s+result=PASS\s+profile=GT03_BURNOUT' })
if ($guards.Count -ne 1) {
    $failures.Add("Expected exactly one GT03_BURNOUT profile guard marker but found $($guards.Count)")
}

$guardRunId = $null
if ($guards.Count -ge 1) {
    $m = [regex]::Match($guards[0], 'runId=(\S+)')
    if (-not $m.Success) { $failures.Add('Profile guard marker has no runId') } else { $guardRunId = $m.Groups[1].Value }

    foreach ($pair in @(@('E1', $e1), @('E2', $e2))) {
        $id = $pair[0]
        $marks = $pair[1]
        foreach ($mark in $marks) {
            $rm = [regex]::Match($mark, 'runId=(\S+)')
            if (-not $rm.Success) {
                $failures.Add("$id PASS marker has no runId")
            } elseif ($rm.Groups[1].Value -ne $guardRunId) {
                $failures.Add("$id marker runId '$($rm.Groups[1].Value)' does not match guard runId '$guardRunId'")
            }
        }
    }

    # Verify all four guard values (case-sensitive to avoid soul/normal substring confusion).
    $required = @(
        'campfireInfiniteLifeTime=false',
        'soulCampfireInfiniteLifeTime=false',
        'campfireDestroyedOnBurnout=true',
        'soulCampfireDestroyedOnBurnout=false'
    )
    foreach ($expected in $required) {
        if (-not $guards[0].Contains($expected)) {
            $failures.Add("Profile guard marker missing expected value: $expected")
        }
    }
}

# 7/8/9. Relevant failures and exceptions after narrow known-noise exclusion.
$failureMarkers = @($lines | Where-Object {
        $_ -cmatch 'There were failing tests' -or
        $_ -cmatch 'required tests did not pass' -or
        $_ -cmatch 'Test failed' -or
        $_ -cmatch 'tests failed' -or
        $_ -cmatch 'AssertionError' -or
        $_ -cmatch 'Failed to load (GameTest|gametest|template|test class)' -or
        $_ -cmatch '(?i)Mixin.*(failed|error|exception)'
    })
foreach ($line in $failureMarkers) {
    if (-not (Test-KnownNoise $line)) { $failures.Add("Relevant failure marker: $line") }
}

# Relevance filter: only GameTest failures, campfire BlockEntity exceptions and
# Mixin application errors are in scope. This is a positive relevance filter,
# not a broad suppression: absent-mod recipe parse errors and missing
# server.properties are environmental, not campfire/Mixin failures.
$errorLike = @($lines | Where-Object {
        (($_ -cmatch '\[(ERROR|FATAL)\]') -or ($_ -cmatch 'Exception')) -and
        (($_ -cmatch 'CampfireBlockEntity') -or
         ($_ -cmatch 'campfireOverhaul[$_]') -or
         ($_ -cmatch '(?i)mixin.*(failed|error|exception)'))
    })
$relevantErrors = @($errorLike | Where-Object { -not (Test-KnownNoise $_) })
foreach ($line in $relevantErrors) {
    $failures.Add("Relevant campfire/Mixin error: $line")
}

# 10/11/12. Concise validation summary and raw log fingerprint.
Write-Output "E3 VALIDATOR SUMMARY"
Write-Output "  log: $LogPath"
Write-Output "  sha256: $rawHash"
Write-Output "  lines: $($lines.Count)"
Write-Output "  completionMarkers: $($completion.Count)"
Write-Output "  shutdownMarker: $($shutdown.Count)"
Write-Output "  e1PassMarkers: $($e1.Count)"
Write-Output "  e2PassMarkers: $($e2.Count)"
Write-Output "  profileGuardMarkers: $($guards.Count)"
Write-Output "  runId: $guardRunId"
Write-Output "  knownNoiseExclusions: campful; Yggdrasil; Realms; Can't keep up!"
Write-Output "  relevantErrors: $($relevantErrors.Count)"

if ($failures.Count -gt 0) {
    Write-Output "E3 RESULT: FAIL"
    foreach ($f in $failures) { Write-Output "  - $f" }
    exit 1
}

Write-Output "E3 RESULT: PASS"
exit 0
