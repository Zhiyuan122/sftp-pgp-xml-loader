@echo off
REM ProviderLoader Run Script - Windows Version
REM This script demonstrates how to compile and run the ProviderLoader application

echo ProviderLoader - SFTP XML File Downloader
echo ==========================================

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java not found. Please install Java 11 or higher.
    pause
    exit /b 1
)

echo Java version:
java -version
echo.

REM Create necessary directories
echo Creating necessary directories...
if not exist "target\classes" mkdir "target\classes"
if not exist "logs" mkdir "logs"
if not exist "inbox" mkdir "inbox"

REM Check if Maven is available
mvn -version >nul 2>&1
if %errorlevel% equ 0 (
    echo Using Maven to compile project...
    mvn clean compile
    
    if %errorlevel% equ 0 (
        echo Compilation successful!
        echo.
        echo Running application...
        mvn exec:java -Dexec.mainClass="com.quikcard.providerloader.App"
    ) else (
        echo Maven compilation failed. Please check dependencies and code.
        pause
        exit /b 1
    )
) else (
    echo Warning: Maven not found.
    echo Please install Maven to automatically manage dependencies and compile the project.
    echo.
    echo Steps to install Maven:
    echo 1. Visit https://maven.apache.org/download.cgi
    echo 2. Download and install Maven
    echo 3. Add Maven's bin directory to PATH environment variable
    echo.
    echo Or install using package managers:
    echo   Chocolatey: choco install maven
    echo   Scoop: scoop install maven
    echo.
    echo After installing Maven, you can run the project with these commands:
    echo   mvn clean compile
    echo   mvn exec:java -Dexec.mainClass="com.quikcard.providerloader.App"
)

echo.
echo Note: Before running the application, please ensure:
echo 1. Edit src\main\resources\application.properties file
echo 2. Configure correct SFTP server connection information
echo 3. Ensure network connection is working
echo.
pause
