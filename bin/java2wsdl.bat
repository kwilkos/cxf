@echo off
rem 
rem  invoke the Celtix java2wsdl tool
rem 
@setlocal

if not defined CELTIX_HOME goto no_celtix_home

call %CELTIX_HOME%\bin\celtix_env.bat 
%JAVA_HOME%\bin\java -Djaxws.home=%JAXWS_HOME% org.objectweb.celtix.tools.Java2Wsdl %*
@endlocal

goto end 

:no_celtix_home
    echo The CELTIX_HOME environment variable is unset.  Please set CELTIX_HOME
    echo envionment variable to the location of the Celtix installation
goto end

:end