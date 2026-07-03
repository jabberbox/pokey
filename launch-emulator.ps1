# Boots the Light Phone III AVD with a writable system partition.
# -writable-system is required every time; without it you can't push files
# into /system (see docs/system_app for the LightOS-emulator-as-system-app setup).

$AvdName = "Light_Phone_III_AOSP"

$sdkDir = $env:ANDROID_HOME
if (-not $sdkDir) {
    $sdkDir = $env:ANDROID_SDK_ROOT
}
if (-not $sdkDir) {
    $localProps = Join-Path $PSScriptRoot "local.properties"
    if (Test-Path $localProps) {
        $line = Get-Content $localProps | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
        if ($line) {
            $sdkDir = (($line -replace '^sdk\.dir=', '') -replace '\\:', ':') -replace '\\\\', '\'
        }
    }
}
if (-not $sdkDir) {
    throw "Could not determine Android SDK location. Set ANDROID_HOME or add sdk.dir to local.properties."
}

$emulatorExe = Join-Path $sdkDir "emulator\emulator.exe"
if (-not (Test-Path $emulatorExe)) {
    throw "emulator.exe not found at $emulatorExe"
}

& $emulatorExe -avd $AvdName -writable-system
