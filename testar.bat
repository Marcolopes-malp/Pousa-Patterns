@echo off
setlocal
set "JAVA_HOME=C:\Users\Ryzen\.tools\jdk-17"
set "M2_HOME=C:\Users\Ryzen\.tools\maven"
set "PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%"

echo ========================================================
echo   Executando Suíte de Testes Automatizados (JUnit 5)
echo ========================================================
mvn test
