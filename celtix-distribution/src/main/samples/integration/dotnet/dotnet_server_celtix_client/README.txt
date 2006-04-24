Hello World Demo using Document/Literal Style
=============================================


This demo demonstrates how Celtix client interoperates seamlessly with .NET Web services. 
Microsoft Visual Studio .NET is used to create an ASP.NET Web service and 
we will create an Artix client for an ASP.NET web service.



Prerequisite
------------

1. This demo is designed to run on Windows only.

2. Install IIS into the default location on your Windows System.

3. Install Visual Studio .NET 2003 into the default location on your Windows System.

4. Configure .NET to allow you to create an ASP.NET web services. 

5. If your environment already includes celtix.jar on the
CLASSPATH, and the JDK and ant bin directories on the PATH
it is not necessary to run the environment script described in
the samples directory README.  If your environment is not
properly configured, or if you are planning on using wsdl2java,
javac, and java to build and run the demos, you must set the
environment by running the script.


Deploying the demo server to IIS
------------------------------------
1. Copy the subdirectory server from:
   
   samples\integration\dotnet\dotnet_server_celtix_client\dotnet\server  

   to C:\Inetpub\wwwroot


2. Rename the directory name C:\Inetpub\wwwroot\server to C:\Inetpub\wwwroot\HelloWorld_doclit.
   Open IIS manager from Contral Panel. Right click HelloWorld_doclit folder under Default Web Site. 
   Go to the Properties dialog and click "Create" button to create the virtual directory. 
   Then click "Ok" button. 

3. Test .NET server by opening an explore with the following URL address:

   http://localhost/HelloWorld_doclit/HelloWorldService.asmx
   
   
You will see sayHi and greetMe two methods and click any of them to test.

4. run the demo client to see console output

Building and running the demo client using ant
---------------------------------------

From the samples\integration\dotnet\dotnet_server_celtix_client, the ant build script
can be used to build and run the demo.

Using Windows:

  ant build
  ant client
    

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean




Buildng the demo client using wsdl2java and javac
------------------------------------------

From the samples\integration\dotnet\dotnet_server_celtix_client directory, first create the target
directory build\classes and then generate code from the WSDL file.


For Windows:
  mkdir build\classes
    Must use back slashes.

  wsdl2java -d build\classes -compile .\wsdl\hello_world.wsdl
    May use either forward or back slashes.

Now compile the provided client and server applications with the commands:


For Windows:
  set classpath=%classpath%;%CELTIX_HOME%\lib\celtix.jar;.\build\classes
  javac -d build\classes src\demo\hw\client\*.java



Running the demo client using java
---------------------------

From the samples\integration\dotnet\dotnet_server_celtix_client directory run the commands, entered on a
single command line:

For Windows (may use either forward or back slashes):

    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
       demo.hw.client.Client .\wsdl\hello_world.wsdl


To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:

  ant clean



