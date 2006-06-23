Hello World Demo using Document/Literal Style
=============================================

This demo demonstrates how Celtix server interoperates seamlessly with .NET client. 
We use Microsoft Visual Studio .NET to create a .NET console application client 
for an Celtix service.



Prerequisite
------------

1. This demo is designed to run on Windows only.

2. Install Visual Studio .NET 2003 into the default location on your
   Windows System.

3. If your environment already includes celtix.jar on the
CLASSPATH, and the JDK and ant bin directories on the PATH
it is not necessary to run the environment script described in
the samples directory README.  If your environment is not
properly configured, or if you are planning on using wsdl2java,
javac, and java to build and run the demos, you must set the
environment by running the script.



Building and running the demo using ant
---------------------------------------

From the samples/integration/dotnet/celtix_server_dotnet_client directory, the ant build script
can be used to build and run the demo.

Using Windows:

  ant build
  ant server
  ant client
    

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean



Buildng the demo server using wsdl2java and javac
------------------------------------------

From the samples\integration\dotnet\celtix_server_dotnet_client directory, first create the target
directory build\classes and then generate code from the WSDL file.

For Windows:
  mkdir build\classes
    Must use back slashes.

  wsdl2java -d build\classes -compile .\wsdl\hello_world.wsdl
    May use either forward or back slashes.

Now compile the provided client and server applications with the commands:

For Windows:
  set classpath=%classpath%;%CELTIX_HOME%\lib\celtix.jar;.\build\classes
  javac -d build\classes src\demo\hw\server\*.java



Running the demo server using java
---------------------------

From the samples\integration\dotnet\celtix_server_dotnet_client directory run the commands, entered on a
single command line:

For Windows (may use either forward or back slashes):
  start 
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         demo.hw.server.Server


A new command windows opens for the server process.  After running the
client, terminate the server process by issuing Ctrl-C in its command window.

To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:

  ant clean



