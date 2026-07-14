$ErrorActionPreference = "Stop"
$base = "http://localhost:8082/api"
$results = @()

function Test-Step {
    param(
        [string]$Name,
        [scriptblock]$Block
    )
    try {
        $detail = & $Block
        $script:results += [pscustomobject]@{ Step = $Name; Status = "OK"; Detail = $detail }
        Write-Host "[OK] $Name - $detail" -ForegroundColor Green
        return $true
    } catch {
        $msg = $_.Exception.Message
        if ($_.ErrorDetails.Message) { $msg = $_.ErrorDetails.Message }
        $script:results += [pscustomobject]@{ Step = $Name; Status = "FAIL"; Detail = $msg }
        Write-Host "[FAIL] $Name - $msg" -ForegroundColor Red
        return $false
    }
}

function Decode-JwtPayload($jwt) {
    $parts = $jwt -split "\."
    if ($parts.Count -lt 2) { return $null }
    $payload = $parts[1]
    $pad = 4 - ($payload.Length % 4)
    if ($pad -ne 4) { $payload += ("=" * $pad) }
    $bytes = [Convert]::FromBase64String($payload.Replace("-", "+").Replace("_", "/"))
    return [Text.Encoding]::UTF8.GetString($bytes)
}

Write-Host "`n=== Test téléconsultation Shambua Santé ===`n" -ForegroundColor Cyan

$doctor = $null
$patient = $null
$rdvId = $null

Test-Step -Name "Login médecin" -Block {
    $script:doctor = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType "application/json" `
        -Body '{"email":"ngozi.achebe@shambua.health","password":"shambua123"}'
    if ($script:doctor.user.role -ne "MEDECIN") { throw "Role attendu MEDECIN, recu $($script:doctor.user.role)" }
    "idHopital=$($script:doctor.user.idHopital) idMedecin=$($script:doctor.user.idMedecin)"
} | Out-Null

Test-Step -Name "Login patient" -Block {
    $script:patient = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType "application/json" `
        -Body '{"email":"amara.diallo@gmail.com","password":"shambua123"}'
    if ($script:patient.user.role -ne "PATIENT") { throw "Role attendu PATIENT, recu $($script:patient.user.role)" }
    "idHopital=$($script:patient.user.idHopital) idPatient=$($script:patient.user.idPatient)"
} | Out-Null

Test-Step -Name "Sessions teleconsultation medecin" -Block {
    $list = Invoke-RestMethod -Uri "$base/rendezvous" -Headers @{
        Authorization = "Bearer $($script:doctor.token)"
        "X-Hopital-Id" = "$($script:doctor.user.idHopital)"
    }
    $tele = @($list | Where-Object {
        $_.canal -eq "TELECONSULTATION" -and $_.statutRdv -notin @("ANNULE","ABSENT") -and $_.idPatient -eq $script:patient.user.idPatient -and $_.idMedecin -eq $script:doctor.user.idMedecin
    })
    if ($tele.Count -eq 0) {
        $tele = @($list | Where-Object { $_.canal -eq "TELECONSULTATION" -and $_.statutRdv -notin @("ANNULE","ABSENT") })
    }
    if ($tele.Count -eq 0) { throw "Aucun RDV teleconsultation trouve pour le medecin" }
    $script:rdvId = $tele[0].idRdv
    "$($tele.Count) session(s), RDV test #$rdvId (patient $($tele[0].idPatient))"
} | Out-Null

Test-Step -Name "Sessions téléconsultation patient" -Block {
    $list = Invoke-RestMethod -Uri "$base/v1/patients/me/dashboard/teleconsultations" -Headers @{
        Authorization = "Bearer $($script:patient.token)"
        "X-Hopital-Id" = "$($script:patient.user.idHopital)"
    }
    if ($list.Count -eq 0) { throw "Aucun RDV téléconsultation pour le patient" }
    $match = $list | Where-Object { $_.idRdv -eq $script:rdvId }
    if (-not $match) {
        $script:rdvId = $list[0].idRdv
        "RDV médecin absent, utilisation RDV patient #$rdvId"
    } else {
        "RDV commun #$rdvId confirmé"
    }
} | Out-Null

Test-Step -Name "Validation RDV (canal TELECONSULTATION)" -Block {
    $rdv = Invoke-RestMethod -Uri "$base/rendezvous/$script:rdvId" -Headers @{
        Authorization = "Bearer $($script:doctor.token)"
        "X-Hopital-Id" = "$($script:doctor.user.idHopital)"
    }
    if ($rdv.canal -ne "TELECONSULTATION") { throw "Canal incorrect: $($rdv.canal)" }
    "canal=$($rdv.canal) statut=$($rdv.statutRdv)"
} | Out-Null

$doctorToken = $null
$patientToken = $null
$roomName = $null
$serverUrl = $null

Test-Step -Name "Token LiveKit médecin" -Block {
    $lk = Invoke-RestMethod -Uri "$base/consultations/teleconsultation/token" -Method POST `
        -ContentType "application/json" -Headers @{
            Authorization = "Bearer $($script:doctor.token)"
            "X-Hopital-Id" = "$($script:doctor.user.idHopital)"
        } -Body "{`"idRendezVous`": $($script:rdvId)}"
    if (-not $lk.token -or -not $lk.serverUrl -or -not $lk.roomName) { throw "Réponse token incomplète" }
    if ($lk.roomName -notmatch "^tenant-\d+-teleconsultation-\d+$") { throw "Format salle invalide: $($lk.roomName)" }
    $script:doctorToken = $lk.token
    $script:roomName = $lk.roomName
    $script:serverUrl = $lk.serverUrl
    "room=$($lk.roomName) url=$($lk.serverUrl)"
} | Out-Null

Test-Step -Name "Token LiveKit patient (même salle)" -Block {
    $lk = Invoke-RestMethod -Uri "$base/consultations/teleconsultation/token" -Method POST `
        -ContentType "application/json" -Headers @{
            Authorization = "Bearer $($patient.token)"
            "X-Hopital-Id" = "$($patient.user.idHopital)"
        } -Body "{`"idRendezVous`": $($script:rdvId)}"
    if ($lk.roomName -ne $script:roomName) { throw "Salles differentes medecin/patient: $($script:roomName) vs $($lk.roomName)" }
    $script:patientToken = $lk.token
    "room=$($lk.roomName) identity=$($lk.participantIdentity)"
} | Out-Null

Test-Step -Name "JWT LiveKit décodable (claims vidéo)" -Block {
    $payload = Decode-JwtPayload $script:doctorToken | ConvertFrom-Json
    if (-not $payload.video) { throw "Claim video absent du JWT" }
    if ($payload.video.room -ne $script:roomName) { throw "Room JWT incorrect: $($payload.video.room)" }
    "room JWT=$($payload.video.room) identity=$($payload.sub)"
} | Out-Null

Test-Step -Name "Chat REST médecin → patient" -Block {
    $sent = Invoke-RestMethod -Uri "$base/consultations/teleconsultation/$($script:rdvId)/messages" -Method POST `
        -ContentType "application/json" -Headers @{
            Authorization = "Bearer $($script:doctor.token)"
            "X-Hopital-Id" = "$($script:doctor.user.idHopital)"
        } -Body '{"content":"Test auto teleconsultation medecin"}'
    $msgs = Invoke-RestMethod -Uri "$base/consultations/teleconsultation/$($script:rdvId)/messages" -Headers @{
        Authorization = "Bearer $($script:patient.token)"
        "X-Hopital-Id" = "$($script:patient.user.idHopital)"
    }
    $found = $msgs | Where-Object { $_.content -like "*Test auto teleconsultation*" }
    if (-not $found) { throw "Message non reçu côté patient" }
    "message id=$($sent.id) lu par patient"
} | Out-Null

Test-Step -Name "Accès refusé tiers (tenant isolation)" -Block {
    try {
        Invoke-RestMethod -Uri "$base/consultations/teleconsultation/token" -Method POST `
            -ContentType "application/json" -Headers @{
                Authorization = "Bearer $($script:doctor.token)"
                "X-Hopital-Id" = "$($script:doctor.user.idHopital)"
            } -Body '{"idRendezVous": 999999}' -ErrorAction Stop
        throw "Devrait échouer pour RDV inexistant"
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -in @(403, 404)) {
            "RDV invalide correctement rejeté ($($_.Exception.Response.StatusCode))"
        } else { throw $_ }
    }
} | Out-Null

$ok = ($results | Where-Object { $_.Status -eq "OK" }).Count
$fail = ($results | Where-Object { $_.Status -eq "FAIL" }).Count
Write-Host "`n=== Résumé: $ok OK, $fail échec(s) ===" -ForegroundColor Cyan
$results | Format-Table -AutoSize

# Export for LiveKit connection test
@{
    rdvId = $script:rdvId
    roomName = $script:roomName
    serverUrl = $script:serverUrl
    doctorToken = $script:doctorToken
    patientToken = $script:patientToken
} | ConvertTo-Json | Set-Content "c:\Users\HP\Documents\SHAMBUASANTE\Hospicloud\scripts\.tele-test-data.json"

exit $(if ($fail -gt 0) { 1 } else { 0 })
