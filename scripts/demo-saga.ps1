<#
.SYNOPSIS
  Script de demonstração ao vivo do Saga Pattern - Tech Challenge Fase 4.

.DESCRIPTION
  Roda os 2 cenários do Saga contra o ambiente EKS:
    - feliz:    OS -> Orçamento -> Aprovação -> Execução -> Finalização
    - rollback: OS -> Orçamento -> Rejeição (compensação)

  Faz pausas explicativas entre passos para sincronizar com a narração do vídeo.

.PARAMETER Cenario
  'feliz' (default) ou 'rollback'.

.PARAMETER Pausa
  Segundos de pausa entre as etapas (default 6). Aumente se a sua narração for mais longa.

.EXAMPLE
  .\demo-saga.ps1 -Cenario feliz
  .\demo-saga.ps1 -Cenario rollback
  .\demo-saga.ps1 -Cenario feliz -Pausa 10
#>

param(
    [ValidateSet('feliz','rollback')]
    [string] $Cenario = 'feliz',

    [int] $Pausa = 6
)

$ErrorActionPreference = 'Stop'

$OS      = "http://aedfcadcab1a6470a9138cf64ae56407-787906964.us-east-1.elb.amazonaws.com"
$BILLING = "http://a35b4dcfcb7cb4487a4ab7175fda6c4b-848840900.us-east-1.elb.amazonaws.com"
$EXEC    = "http://a305e8e2cb34347939684c26616f618e-1728333382.us-east-1.elb.amazonaws.com"

function Step($n, $titulo) {
    Write-Host ""
    Write-Host "===============================================" -ForegroundColor Cyan
    Write-Host " [$n] $titulo" -ForegroundColor Cyan
    Write-Host "===============================================" -ForegroundColor Cyan
}

function Wait-Saga($motivo) {
    Write-Host ""
    Write-Host "   ... aguardando $Pausa s ($motivo) ..." -ForegroundColor DarkGray
    Start-Sleep -Seconds $Pausa
}

function Show-Status($obj) {
    Write-Host "   id     : $($obj.id)"        -ForegroundColor Yellow
    Write-Host "   status : $($obj.status)"    -ForegroundColor Yellow
    if ($obj.valorTotalOrcamento) { Write-Host "   valor  : R$ $($obj.valorTotalOrcamento)" -ForegroundColor Yellow }
}

# ---------- Banner ----------
Write-Host ""
Write-Host "###########################################" -ForegroundColor Green
Write-Host "#  TECH CHALLENGE - FASE 4                #" -ForegroundColor Green
Write-Host "#  Demonstracao do Saga Pattern           #" -ForegroundColor Green
Write-Host "#  Cenario: $($Cenario.PadRight(28))  #" -ForegroundColor Green
Write-Host "###########################################" -ForegroundColor Green

# ---------- Login ----------
Step 1 "LOGIN no OS Service (admin/admin) - obtem JWT"
$loginBody = '{ "username":"admin", "password":"admin" }'
$login = Invoke-RestMethod -Uri "$OS/api/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
$JWT = $login.token
$auth = @{ Authorization = "Bearer $JWT" }
Write-Host "   token  : $($JWT.Substring(0, [Math]::Min(50,$JWT.Length)))..." -ForegroundColor Yellow

# ---------- Criar OS ----------
Step 2 "POST /api/ordens-servico  -> publica 'os.criada' (OS Service)"
$osPayload = @{
    cpfCnpjCliente = "12674781602"
    veiculo = @{ placa = "ABC1234" }
    servicos = @(@{ servicoId = 1; quantidade = 1 })
    pecas = @(@{ pecaInsumoId = 3; quantidade = 1 })
    observacoes = "Demo Saga - $Cenario - $(Get-Date -Format 'HH:mm:ss')"
} | ConvertTo-Json -Depth 5

$novaOS = Invoke-RestMethod -Uri "$OS/api/ordens-servico" -Method POST -Headers $auth -ContentType "application/json" -Body $osPayload
Show-Status $novaOS
$osId = $novaOS.id
Write-Host "   >>> Status esperado: EM_DIAGNOSTICO" -ForegroundColor Magenta

# ---------- Billing consome -> orcamento.gerado ----------
Wait-Saga "Billing Service consumir 'os.criada' e publicar 'orcamento.gerado'"

Step 3 "GET /api/ordens-servico/$osId  -> OS Service ja consumiu 'orcamento.gerado'"
$os1 = Invoke-RestMethod -Uri "$OS/api/ordens-servico/$osId" -Headers $auth
Show-Status $os1
Write-Host "   >>> Status esperado: AGUARDANDO_APROVACAO" -ForegroundColor Magenta

Step 4 "GET /orcamentos/os/$osId  -> orcamento criado no Billing"
$orc = $null
$retries = 0
while ($null -eq $orc -and $retries -lt 5) {
    try {
        $orc = Invoke-RestMethod -Uri "$BILLING/orcamentos/os/$osId"
    } catch {
        Write-Host "   (orcamento ainda nao criado, retry $($retries+1)/5)" -ForegroundColor DarkGray
        Start-Sleep -Seconds 3
        $retries++
    }
}
if ($null -eq $orc) { throw "Orcamento nao foi criado pelo Billing." }
Write-Host "   id     : $($orc.id)" -ForegroundColor Yellow
Write-Host "   osId   : $($orc.osId)" -ForegroundColor Yellow
Write-Host "   status : $($orc.status)" -ForegroundColor Yellow
Write-Host "   valor  : R$ $($orc.valorTotal)" -ForegroundColor Yellow
$orcId = $orc.id

if ($Cenario -eq 'rollback') {
    # ===================== ROLLBACK =====================
    Step 5 "POST /orcamentos/$orcId/rejeitar  -> compensacao (publica 'orcamento.rejeitado')"
    $rej = Invoke-RestMethod -Uri "$BILLING/orcamentos/$orcId/rejeitar?motivo=Preco%20muito%20elevado" -Method POST
    Write-Host "   orcamento status: $($rej.status)" -ForegroundColor Yellow

    Wait-Saga "OS Service consumir 'orcamento.rejeitado' e CANCELAR a OS"

    Step 6 "GET /api/ordens-servico/$osId  -> OS deve estar CANCELADA"
    $osFinal = Invoke-RestMethod -Uri "$OS/api/ordens-servico/$osId" -Headers $auth
    Show-Status $osFinal
    Write-Host "   >>> Status esperado: CANCELADA" -ForegroundColor Magenta
    Write-Host ""
    Write-Host "ROLLBACK FUNCIONOU: a compensacao via evento atualizou a OS sem nenhum orquestrador." -ForegroundColor Green

} else {
    # ===================== FLUXO FELIZ =====================
    Step 5 "POST /orcamentos/$orcId/aprovar  -> publica 'orcamento.aprovado'"
    $apr = Invoke-RestMethod -Uri "$BILLING/orcamentos/$orcId/aprovar" -Method POST
    Write-Host "   orcamento status: $($apr.status)" -ForegroundColor Yellow

    Wait-Saga "OS Service ir para EM_EXECUCAO e Execution Service entrar na fila"

    Step 6 "GET /api/ordens-servico/$osId  -> OS deve estar EM_EXECUCAO"
    $os2 = Invoke-RestMethod -Uri "$OS/api/ordens-servico/$osId" -Headers $auth
    Show-Status $os2
    Write-Host "   >>> Status esperado: EM_EXECUCAO" -ForegroundColor Magenta

    Step 7 "GET /execucoes/os/$osId  -> execucao criada no MongoDB"
    $execItem = $null
    $retries = 0
    while ($null -eq $execItem -and $retries -lt 5) {
        try {
            $execItem = Invoke-RestMethod -Uri "$EXEC/execucoes/os/$osId"
        } catch {
            Write-Host "   (execucao ainda nao criada, retry $($retries+1)/5)" -ForegroundColor DarkGray
            Start-Sleep -Seconds 3
            $retries++
        }
    }
    if ($null -eq $execItem) { throw "Execucao nao foi criada pelo Execution Service." }
    Write-Host "   id     : $($execItem.id)"     -ForegroundColor Yellow
    Write-Host "   osId   : $($execItem.osId)"   -ForegroundColor Yellow
    Write-Host "   status : $($execItem.status)" -ForegroundColor Yellow

    Step 8 "PATCH /execucoes/os/$osId/status  -> EM_EXECUCAO (tecnico iniciou)"
    $body1 = '{ "status":"EM_EXECUCAO", "tecnico":"Vitor Vieira", "observacoes":"Iniciando o servico" }'
    $u1 = Invoke-RestMethod -Uri "$EXEC/execucoes/os/$osId/status" -Method PATCH -ContentType "application/json" -Body $body1
    Write-Host "   execucao status: $($u1.status)" -ForegroundColor Yellow

    Step 9 "PATCH /execucoes/os/$osId/status  -> FINALIZADA (publica 'execucao.finalizada')"
    $body2 = '{ "status":"FINALIZADA", "tecnico":"Vitor Vieira", "observacoes":"Servico concluido" }'
    $u2 = Invoke-RestMethod -Uri "$EXEC/execucoes/os/$osId/status" -Method PATCH -ContentType "application/json" -Body $body2
    Write-Host "   execucao status: $($u2.status)" -ForegroundColor Yellow

    Wait-Saga "OS Service consumir 'execucao.finalizada'"

    Step 10 "GET /api/ordens-servico/$osId  -> OS deve estar FINALIZADA"
    $osFinal = Invoke-RestMethod -Uri "$OS/api/ordens-servico/$osId" -Headers $auth
    Show-Status $osFinal
    Write-Host "   >>> Status esperado: FINALIZADA" -ForegroundColor Magenta
    Write-Host ""
    Write-Host "SAGA COMPLETO: 3 microsservicos coordenados via eventos, sem orquestrador." -ForegroundColor Green
}

Write-Host ""
Write-Host "###########################################" -ForegroundColor Green
Write-Host "#  Fim da demonstracao (cenario: $Cenario)  " -ForegroundColor Green
Write-Host "###########################################" -ForegroundColor Green
Write-Host ""
