@echo off
rem  
rem set the environment for the Celtix runtime 
rem

rem check we have all we need to configure the 
rem environment 

if defined CELTIX_ENV_SET goto celtix_env_already_set 
if not defined JAVA_HOME goto no_java_home 

rem figure out celtix home 
rem 
if not defined CELTIX_HOME (
	set CELTIX_HOME=%~dp0..
)

if not defined JAXWS_HOME (
    set JAXWS_HOME=%CELTIX_HOME%\lib\jaxws-ri\20050929
)

if not exist %JAVA_HOME%\lib\tools.jar goto cannot_find_tools_jar
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%CLASSPATH%

rem add the celtix jar and wsdl4j.jar to the class path
rem
set CLASSPATH=%CELTIX_HOME%\lib\celtix.jar;%CELTIX_HOME%\lib\wsdl4j\1.5.1\wsdl4j.jar;%CLASSPATH%

set PATH=%CELTIX_HOME%\bin;%PATH% 
set CELTIX_ENV_SET=true 
rem all done
goto end 


:celtix_env_already_set
    echo Celtix environment already set.
goto end

:no_java_home
    echo The JAXWS Tools require that a JDK be installed and that tools.jar is 
    echo on the classpath.  Please ensure that the JAVA_HOME environment variable
    echo references a JDK 1.5 installation
goto end 

:no_celtix_home
    echo The CELTIX_HOME environment variable is unset.  Please set CELTIX_HOME
    echo envionment variable to the location of the Celtix installation
goto end

:cannot_find_tools_jar
    echo Unable to find tools.jar.  Please unsure that the JDK is installed
    echo and set the JAVA_HOME environment variable
goto end


:end 

