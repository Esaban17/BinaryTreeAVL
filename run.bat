@echo off
echo ======================================
echo    ARBOL AVL CON MONGODB ATLAS
echo ======================================
echo.

REM Verificar si Java está instalado
echo Verificando Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java no está instalado o no está en el PATH
    echo Por favor, instale Java 11 o superior
    pause
    exit /b 1
)

REM Verificar si Maven está instalado
echo.
echo Verificando Maven...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven no está instalado o no está en el PATH
    echo Por favor, instale Maven 3.6 o superior
    pause
    exit /b 1
)

REM Verificar si existe el archivo .env
if not exist ".env" (
    echo ❌ Archivo .env no encontrado
    echo Por favor, configure el archivo .env con sus credenciales de MongoDB Atlas
    echo Ejemplo:
    echo MONGODB_URI=mongodb+srv://usuario:password@cluster.mongodb.net/avltree?retryWrites=true^&w=majority
    echo DATABASE_NAME=avltree
    echo COLLECTION_NAME=nodes
    pause
    exit /b 1
)

echo.
echo ✓ Prerequisitos verificados
echo.

REM Compilar el proyecto
echo Compilando el proyecto...
mvn clean compile
if %errorlevel% neq 0 (
    echo ❌ Error al compilar el proyecto
    pause
    exit /b 1
)

echo.
echo ✓ Compilación exitosa
echo.

REM Ejecutar la aplicación
echo Iniciando la aplicación...
echo ======================================
mvn exec:java -Dexec.mainClass="com.avltree.Main"

pause