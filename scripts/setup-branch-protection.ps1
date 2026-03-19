# =============================================================================
# Setup Branch Protection Rules - Tech Challenge Fase 3
# =============================================================================
# Uso: .\scripts\setup-branch-protection.ps1 -Token "seu_github_token"
# =============================================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$Token
)

$OWNER = "VitorVieira12"
$REPO  = "Tech-Challenge"

$headers = @{
    "Authorization" = "Bearer $Token"
    "Accept"        = "application/vnd.github+json"
    "X-GitHub-Api-Version" = "2022-11-28"
}

function Set-BranchProtection {
    param(
        [string]$Branch,
        [bool]$RequirePR,
        [int]$RequiredApprovals,
        [string[]]$RequiredChecks
    )

    $url = "https://api.github.com/repos/$OWNER/$REPO/branches/$Branch/protection"

    $statusChecks = $null
    if ($RequiredChecks.Count -gt 0) {
        $statusChecks = @{
            strict   = $true
            contexts = $RequiredChecks
        }
    }

    $prReviews = $null
    if ($RequirePR) {
        $prReviews = @{
            dismiss_stale_reviews           = $true
            require_code_owner_reviews      = $false
            required_approving_review_count = $RequiredApprovals
        }
    }

    $body = @{
        required_status_checks        = $statusChecks
        enforce_admins                = $false
        required_pull_request_reviews = $prReviews
        restrictions                  = $null
        allow_force_pushes            = $false
        allow_deletions               = $false
        block_creations               = $false
    } | ConvertTo-Json -Depth 5

    try {
        $response = Invoke-RestMethod -Uri $url -Method Put -Headers $headers -Body $body -ContentType "application/json"
        Write-Host "  OK: Branch protection ativada em '$Branch'" -ForegroundColor Green
        return $true
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $msg = $_.ErrorDetails.Message | ConvertFrom-Json -ErrorAction SilentlyContinue
        Write-Host "  ERRO ($statusCode) em '$Branch': $($msg.message)" -ForegroundColor Red
        return $false
    }
}

function Test-BranchExists {
    param([string]$Branch)
    $url = "https://api.github.com/repos/$OWNER/$REPO/branches/$Branch"
    try {
        Invoke-RestMethod -Uri $url -Method Get -Headers $headers | Out-Null
        return $true
    }
    catch { return $false }
}

# =============================================================================
Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host " Branch Protection Setup - $OWNER/$REPO" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar autenticação
Write-Host "[1/4] Verificando autenticação no GitHub..." -ForegroundColor Yellow
try {
    $user = Invoke-RestMethod -Uri "https://api.github.com/user" -Headers $headers
    Write-Host "  Autenticado como: $($user.login)" -ForegroundColor Green
}
catch {
    Write-Host "  Token inválido ou sem permissão. Verifique o token e tente novamente." -ForegroundColor Red
    exit 1
}

# Verificar branches
Write-Host ""
Write-Host "[2/4] Verificando branches existentes..." -ForegroundColor Yellow
$masterExists = Test-BranchExists -Branch "master"
$faseExists   = Test-BranchExists -Branch "fase-3"

Write-Host "  master : $(if ($masterExists) { 'existe' } else { 'NAO ENCONTRADA' })" -ForegroundColor $(if ($masterExists) { 'Green' } else { 'Red' })
Write-Host "  fase-3 : $(if ($faseExists) { 'existe' } else { 'NAO ENCONTRADA' })" -ForegroundColor $(if ($faseExists) { 'Green' } else { 'Yellow' })

# Configurar proteção - master (PRODUÇÃO)
Write-Host ""
Write-Host "[3/4] Configurando proteção na branch 'master' (PRODUCAO)..." -ForegroundColor Yellow
Write-Host "  Regras: PR obrigatório, 1 aprovação, sem commits diretos" -ForegroundColor Gray

if ($masterExists) {
    Set-BranchProtection `
        -Branch "master" `
        -RequirePR $true `
        -RequiredApprovals 1 `
        -RequiredChecks @("Run Tests", "Validate Pull Request")
}
else {
    Write-Host "  Branch 'master' não encontrada, pulando..." -ForegroundColor Yellow
}

# Configurar proteção - fase-3 (HOMOLOGAÇÃO)
Write-Host ""
Write-Host "[4/4] Configurando proteção na branch 'fase-3' (HOMOLOGACAO)..." -ForegroundColor Yellow
Write-Host "  Regras: PR obrigatório, 0 aprovações, sem commits diretos" -ForegroundColor Gray

if ($faseExists) {
    Set-BranchProtection `
        -Branch "fase-3" `
        -RequirePR $true `
        -RequiredApprovals 0 `
        -RequiredChecks @()
}
else {
    Write-Host "  Branch 'fase-3' não encontrada, pulando..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host " Concluido! Verifique em:" -ForegroundColor Cyan
Write-Host " https://github.com/$OWNER/$REPO/settings/branches" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""
