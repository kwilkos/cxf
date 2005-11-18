@echo off 
@setlocal

set CELTIX_HOME=%~dp0..

if not defined JAXWS_HOME (
    set JAXWS_HOME=%CELTIX_HOME%\lib\jaxws-ri\20051104
)

if not defined JAVA_HOME goto no_java_home

set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%CLASSPATH%

set CLASSPATH=%CELTIX_HOME%\build\lib\celtix.jar;%CLASSPATH%

"%JAVA_HOME%\bin\java" -Djaxws.home="%JAXWS_HOME%" -Djava.util.logging.config.file="%CELTIX_HOME%\etc\logging.properties" org.objectweb.celtix.tools.WSDLToJava %*

@endlocal

goto end

:no_java_home
echo Please set JAVA_HOME to point a J2SE 5.0 Development Kit
goto end 
:end
