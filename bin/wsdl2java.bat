@setlocal
call .\celtix_env.bat 
java -Djaxws.home=%JAXWS_HOME% org.objectweb.celtix.tools.Wsdl2Java "%*"

@endlocal