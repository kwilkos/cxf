@setlocal
call .\celtix_env.bat 
java -Djaxws.home=%JAXWS_HOME% org.objectweb.celtix.tools.Java2Wsdl %*
@endlocal