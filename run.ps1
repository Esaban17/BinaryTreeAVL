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

# Verificar si existe el archivo .env
if (-Not (Test-Path ".env")) {
    Write-Host "❌ Archivo .env no encontrado" -ForegroundColor Red
    Write-Host "Por favor, configure el archivo .env con sus credenciales de MongoDB Atlas" -ForegroundColor Yellow
    Write-Host "Ejemplo:" -ForegroundColor Cyan
    Write-Host "MONGODB_URI=mongodb+srv://usuario:password@cluster.mongodb.net/avltree?retryWrites=true&w=majority" -ForegroundColor Cyan
    Write-Host "DATABASE_NAME=avltree" -ForegroundColor Cyan
    Write-Host "COLLECTION_NAME=nodes" -ForegroundColor Cyan
    exit 1
}

echo ""
echo "✓ Prerequisitos verificados"
echo ""

# Compilar el proyecto
echo "Compilando el proyecto..."
mvn clean compile
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al compilar el proyecto" -ForegroundColor Red
    exit 1
}

echo ""
echo "✓ Compilación exitosa"
echo ""

# Ejecutar la aplicación
echo "Iniciando la aplicación..."
echo "======================================"
mvn exec:java -Dexec.mainClass="com.avltree.Main"