@echo off
setlocal
set "JAVA_HOME=C:\Users\Ryzen\.tools\jdk-17"
set "M2_HOME=C:\Users\Ryzen\.tools\maven"
set "PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%"

echo ========================================================
echo   Iniciando Pousada Paradiso (Tomcat Embutido + H2)
echo   Acesse no navegador: http://localhost:8080/controller.do
echo ========================================================
mvn compile exec:java
