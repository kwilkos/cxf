@echo off 
rem 
rem  invoke the Celtix wsdl2xml tool
rem 
@setlocal

set CELTIX_HOME=%~dp0..

if not defined JAVA_HOME goto no_java_home

set SUN_TOOL_PATH=%JAVA_HOME%\lib\tools.jar;

if not exist %CELTIX_HOME%\lib\celtix.jar goto no_celtix_jar

set CELTIX_JAR=%CELTIX_HOME%\lib\celtix.jar

"%JAVA_HOME%\bin\java" -cp %CELTIX_JAR%;%SUN_TOOL_PATH%;%CLASSPATH% -Djava.util.logging.config.file="%CELTIX_HOME%\etc\logging.properties" org.objectweb.celtix.tools.WSDLToXML %*

@endlocal

goto end

:no_celtix_jar
echo unable to find celtix.jar in %celtix_home/lib
goto end

:no_java_home
echo Please set JAVA_HOME to point a J2SE 5.0 Development Kit
goto end 
:end
