# Script para ejecutar la aplicación de Árbol AVL

echo "======================================"
echo "   ÁRBOL AVL CON MONGODB ATLAS"
echo "======================================"
echo ""

# Verificar si Java está instalado
echo "Verificando Java..."
java -version
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Java no está instalado o no está en el PATH" -ForegroundColor Red
    Write-Host "Por favor, instale Java 11 o superior" -ForegroundColor Yellow
    exit 1
}

# Verificar si Maven está instalado
echo ""
echo "Verificando Maven..."
mvn -version
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Maven no está instalado o no está en el PATH" -ForegroundColor Red
    Write-Host "Por favor, instale Maven 3.6 o superior" -ForegroundColor Yellow
    exit 1
}

# Verificar la configuración: archivo .env o variable de entorno
if (-Not (Test-Path ".env")) {
    if (-Not $env:MONGODB_URI) {
        Write-Host "❌ No se encontró configuración de MongoDB" -ForegroundColor Red
        Write-Host "Cree un archivo .env (vea .env.example) o defina la variable MONGODB_URI" -ForegroundColor Yellow
        Write-Host "Ejemplo:" -ForegroundColor Cyan
        Write-Host "MONGODB_URI=mongodb+srv://usuario:password@cluster.mongodb.net/avltree?retryWrites=true&w=majority" -ForegroundColor Cyan
        exit 1
    }
    Write-Host "✓ Usando MONGODB_URI de las variables de entorno" -ForegroundColor Green
}

echo ""
echo "✓ Prerequisitos verificados"
echo ""

# Compilar y probar el proyecto
echo "Compilando y ejecutando las pruebas..."
mvn clean verify
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al compilar o probar el proyecto" -ForegroundColor Red
    exit 1
}

echo ""
echo "✓ Compilación y pruebas exitosas"
echo ""

# Ejecutar la aplicación
echo "Iniciando la aplicación..."
echo "======================================"
mvn exec:java