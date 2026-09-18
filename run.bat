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

REM Verificar la configuracion: archivo .env o variable de entorno
if not exist ".env" (
    if "%MONGODB_URI%"=="" (
        echo ❌ No se encontro configuracion de MongoDB
        echo Cree un archivo .env (vea .env.example^) o defina la variable MONGODB_URI
        echo Ejemplo:
        echo MONGODB_URI=mongodb+srv://usuario:password@cluster.mongodb.net/avltree?retryWrites=true^&w=majority
        pause
        exit /b 1
    )
    echo ✓ Usando MONGODB_URI de las variables de entorno
)

echo.
echo ✓ Prerequisitos verificados
echo.

REM Compilar y probar el proyecto
echo Compilando y ejecutando las pruebas...
mvn clean verify
if %errorlevel% neq 0 (
    echo ❌ Error al compilar o probar el proyecto
    pause
    exit /b 1
)

echo.
echo ✓ Compilacion y pruebas exitosas
echo.

REM Ejecutar la aplicación
echo Iniciando la aplicación...
echo ======================================
mvn exec:java

pause