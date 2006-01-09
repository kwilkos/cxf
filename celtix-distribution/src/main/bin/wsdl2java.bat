@echo off 
rem 
rem  invoke the Celtix wsdl2java tool
rem 
@setlocal

set CELTIX_HOME=%~dp0..

if not defined JAVA_HOME goto no_java_home

set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%CLASSPATH%

rem add the celtix jar  to the class path
rem
if exist %CELTIX_HOME%\lib\celtix.jar (
    set CLASSPATH=%CELTIX_HOME%\lib\celtix.jar;%CLASSPATH%
)
if exist %CELTIX_HOME%\build\lib\celtix.jar (
    set CLASSPATH=%CELTIX_HOME%\build\lib\celtix.jar;%CLASSPATH%
)

IF "%1"=="-celtix" GOTO CELTIX_TOOL

"%JAVA_HOME%\bin\java" -Djaxws.home="%CELTIX_HOME%" -Djava.util.logging.config.file="%CELTIX_HOME%\etc\logging.properties" org.objectweb.celtix.tools.Wsdl2Java %*
GOTO END

:CELTIX_TOOL

"%JAVA_HOME%\bin\java"  -Djava.util.logging.config.file="%CELTIX_HOME%\etc\logging.properties" org.objectweb.celtix.tools.WSDLToJava %*

@endlocal

goto end

:no_java_home
echo Please set JAVA_HOME to point a J2SE 5.0 Development Kit
goto end 
:end
