@echo off 
rem 
rem  invoke the Celtix xsd2wsdl tool
rem 
@setlocal

set CELTIX_HOME=%~dp0..

if not defined JAVA_HOME goto no_java_home

set SUN_TOOL_PATH=%JAVA_HOME%\lib\tools.jar;

if not exist "%CELTIX_HOME%\lib\celtix.jar" goto no_celtix_jar

set CELTIX_JAR=%CELTIX_HOME%\lib\celtix.jar

"%JAVA_HOME%\bin\java" -cp "%CELTIX_JAR%;%SUN_TOOL_PATH%;%CLASSPATH%" -Djava.util.logging.config.file="%CELTIX_HOME%\etc\logging.properties" org.objectweb.celtix.tools.misc.XSDToWSDL %*

@endlocal

goto end

:no_celtix_jar
echo ERROR: Unable to find celtix.jar in %celtix_home/lib
goto end

:no_java_home
echo ERROR: Set JAVA_HOME to the path where the J2SE 5.0 (JDK5.0) is installed
goto end 
:end
