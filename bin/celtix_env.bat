@echo off
rem  
rem set the environment for the Celtix runtime 
rem

rem check we have all we need to configure the 
rem environment 

if defined CELTIX_ENV_SET goto celtix_env_already_set 
if not defined JAXWS_HOME goto no_jaxws_home
if not defined CELTIX_HOME goto no_celtix_home

set CELTIX_ENV_SET=true 

rem add the celtix jar to the class path
rem
set CLASSPATH=%CELTIX_HOME%\lib\celtix.jar;%CELTIX_HOME%\lib\wsdl4j\1.5.1\wsdl4j.jar

rem set jaxws classpath
for %%i in (%JAXWS_HOME%\lib\*.jar) do call %$CELTIX_HOME%\bin\cp.bat %%i 
for %%i in (%CELTIX_HOME%\lib\wsdl4j\1.5.1\*.jar) do call %CELTIX_HOME%\bin\cp.bat %%i 


rem all done
goto end 


:celtix_env_already_set
    echo Celtix environment already set.
goto end

:no_celtix_home
    echo The CELTIX_HOME environment variable is unset.  Please set CELTIX_HOME
    echo envionment variable to the location of the Celtix installation
goto end


:no_jaxws_home
    echo The JAXWS_HOME  environment variable is unset.  We use the
    echo JAX-WS interfaces and the JAXB jars that are included in the
    echo JAX-WS 2.0 Early Access release.   You need to download that from
    echo https:\\jax-rpc.dev.java.net\jaxws20-ea2\, install it, and set
    echo the JAXWS_HOME environment variable to the installation
    echo directory.
goto end

:end 

