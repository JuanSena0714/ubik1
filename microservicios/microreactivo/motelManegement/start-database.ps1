# Script PowerShell para iniciar PostgreSQL en Docker (Windows)
# Equivalente a start-database.sh para Windows

$ErrorActionPreference = "Stop"

Write-Host "🐘 Iniciando PostgreSQL en Docker..." -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si Docker está corriendo
try {
    docker info | Out-Null
    Write-Host "✅ Docker está corriendo" -ForegroundColor Green
}
catch {
    Write-Host "❌ Docker no está corriendo. Por favor, inicia Docker Desktop." -ForegroundColor Red
    exit 1
}

# Verificar si el contenedor ya existe
$existingContainer = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^motel-postgres-db$"

if ($existingContainer) {
    Write-Host "⚠️  El contenedor ya existe" -ForegroundColor Yellow
    
    # Verificar si está corriendo
    $runningContainer = docker ps --format "{{.Names}}" | Select-String -Pattern "^motel-postgres-db$"
    
    if ($runningContainer) {
        Write-Host "✅ PostgreSQL ya está corriendo" -ForegroundColor Green
    }
    else {
        Write-Host "🔄 Iniciando contenedor existente..." -ForegroundColor Yellow
        docker start motel-postgres-db
        docker start motel-adminer 2>$null
    }
}
else {
    Write-Host "🚀 Creando e iniciando contenedores..." -ForegroundColor Cyan
    docker-compose up -d postgres adminer
}

# Esperar a que PostgreSQL esté listo
Write-Host ""
Write-Host "⏳ Esperando a que PostgreSQL esté listo..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

$maxAttempts = 30
$attempt = 1

while ($attempt -le $maxAttempts) {
    try {
        $result = docker exec motel-postgres-db pg_isready -U postgres -d motel_management_db 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ PostgreSQL está listo y aceptando conexiones" -ForegroundColor Green
            break
        }
    }
    catch {
        # Continuar esperando
    }
    
    Write-Host "   Intento $attempt/$maxAttempts..." -ForegroundColor Gray
    Start-Sleep -Seconds 2
    $attempt++
}

if ($attempt -gt $maxAttempts) {
    Write-Host "❌ PostgreSQL no respondió después de $maxAttempts intentos" -ForegroundColor Red
    Write-Host "Revisa los logs: docker logs motel-postgres-db" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "🎉 PostgreSQL está listo para usar" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Información de conexión:" -ForegroundColor White
Write-Host "   Host:          localhost" -ForegroundColor Gray
Write-Host "   Puerto:        5432" -ForegroundColor Gray
Write-Host "   Base de datos: motel_management_db" -ForegroundColor Gray
Write-Host "   Usuario:       postgres" -ForegroundColor Gray
Write-Host "   Password:      carlosmanuel" -ForegroundColor Gray
Write-Host ""
Write-Host "🌐 Adminer (Web UI):" -ForegroundColor White
Write-Host "   URL:      http://localhost:8081" -ForegroundColor Gray
Write-Host "   Sistema:  PostgreSQL" -ForegroundColor Gray
Write-Host "   Servidor: postgres" -ForegroundColor Gray
Write-Host ""
Write-Host "🔍 Comandos útiles:" -ForegroundColor White
Write-Host "   Ver logs:       docker logs -f motel-postgres-db" -ForegroundColor Gray
Write-Host "   Detener:        docker-compose stop postgres" -ForegroundColor Gray
Write-Host "   Reiniciar:      docker-compose restart postgres" -ForegroundColor Gray
Write-Host "   Eliminar todo:  docker-compose down -v" -ForegroundColor Gray
Write-Host ""
Write-Host "💡 Ahora puedes ejecutar tu aplicación Spring Boot desde tu IDE" -ForegroundColor Cyan
Write-Host ""