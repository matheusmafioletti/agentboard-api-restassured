@REM Maven Wrapper script for Windows.
@REM Downloads Maven 3.9.6 on first run, then delegates all arguments to it.
@echo off
setlocal

set "MAVEN_VERSION=3.9.6"
set "MAVEN_DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_HOME_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"
set "MAVEN_BIN=%MAVEN_HOME_DIR%\bin\mvn.cmd"

if "%JAVA_HOME%"=="" set "JAVA_HOME=C:\Program Files\Amazon Corretto\jdk21.0.8_9"

if exist "%MAVEN_BIN%" goto run_maven

echo Downloading Apache Maven %MAVEN_VERSION%...
if not exist "%MAVEN_HOME_DIR%" mkdir "%MAVEN_HOME_DIR%"
set "TEMP_ZIP=%TEMP%\apache-maven-%MAVEN_VERSION%-bin.zip"
powershell -NoProfile -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%MAVEN_DIST_URL%', '%TEMP_ZIP%') }"
powershell -NoProfile -Command "& { Expand-Archive -Path '%TEMP_ZIP%' -DestinationPath '%MAVEN_HOME_DIR%' -Force }"
for /d %%G in ("%MAVEN_HOME_DIR%\apache-maven-*") do (
  xcopy /e /i /y "%%G\*" "%MAVEN_HOME_DIR%\" >nul
  rmdir /s /q "%%G"
)
del /f /q "%TEMP_ZIP%"

:run_maven
"%JAVA_HOME%\bin\java.exe" -jar "%MAVEN_HOME_DIR%\lib\maven-wrapper.jar" %* 2>nul || "%MAVEN_BIN%" %*
endlocal
