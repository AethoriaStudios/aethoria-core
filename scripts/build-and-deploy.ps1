$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$pluginRoot = Join-Path $projectRoot "aethoria-core"
$serverPlugins = "C:\Users\juanp\Desktop\Servidor\plugins"

if (-not (Test-Path $serverPlugins)) {
    New-Item -ItemType Directory -Force -Path $serverPlugins | Out-Null
}

Push-Location $pluginRoot
try {
    gradle build
    $jar = Join-Path $pluginRoot "build\libs\aethoria-core-0.1.0-SNAPSHOT.jar"
    if (-not (Test-Path $jar)) {
        throw "Built jar not found: $jar"
    }

    Copy-Item $jar -Destination $serverPlugins -Force
    Write-Host "Deployed plugin to $serverPlugins"
} finally {
    Pop-Location
}