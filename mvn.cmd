@echo off
if "%JAVA_HOME%"=="" set JAVA_HOME=C:\Program Files\Java\jdk-17
"%~dp0tools\apache-maven-3.9.9\bin\mvn.cmd" %*
