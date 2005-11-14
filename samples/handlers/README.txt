Handler Demo
============

This demo shows how the JAXWS handlers can be used.  The server
registers a SOAP protocol handler which simply logs incoming and
outgoing messages to the console.  

The server code registers a handler using the @HandlerChain
annotation on the service implementation class. For this demo, the
handler is SOAPHandler that logs the SOAP message to stdout.

The client includes a logical handler that checks the parameters on
outbound requests and short-circuits the invocation in certain
circumstances.

Please review the README in the samples directory before
continuing.


Prerequisite
------------

If your environment already includes celtix.jar on the
CLASSPATH, and the JDK and ant bin directories on the PATH
it is not necessary to run the environment script described in
the samples directory README.  If your environment is not
properly configured, or if you are planning on using wsdl2java,
javac, and java to build and run the demos, you must set the
environment by running the script.


Building and running the demo using ant
---------------------------------------

From the samples/handlers directory, the ant build script can
be used to build and run the demo.  The server and client
targets automatically build the demo.

Using either UNIX or Windows:

  ant server  (in the background or another window)
  ant client
    

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean


Buildng the demo using wsdl2java and javac
------------------------------------------

From the samples/handlers directory, first create the target
directory build/classes and then generate code from the WSDL file.

For UNIX:
  mkdir -p build/classes

  wsdl2java -d build/classes ./wsdl/addNumbers.wsdl

For Windows:
  mkdir build\classes
    Must use back slashes.

  wsdl2java -d build\classes .\wsdl\addNumbers.wsdl

    May use either forward or back slashes.

Now compile both the generated code and the provided client and
server applications with the commands:

  javac -d build/classes src/demo/handlers/common/*.java
  javac -d build/classes src/demo/handlers/client/*.java
  javac -d build/classes src/demo/handlers/server/*.java

Windows may use either forward or back slashes.

Finally, copy the demo_handlers.xml file from the 
src/demo/handlers/common directory into the
build/classes/demo/handlers/common directory.

For UNIX:
  cp ./src/demo/handlers/common/demo_handlers.xml ./build/classes/demo/handlers/common

For Windows:
  copy src\demo\handlers\common\demo_handlers.xml build\classes\demo\handlers\common


Running the demo using java
---------------------------

From the samples/handlers directory run the commands, entered on a
single command line:

For UNIX (must use forward slashes):
    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         -Dcatalina.home=../../lib/tomcat/5.5.9/ 
         demo.handlers.server.Server &

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         demo.handlers.client.Client ./wsdl/addNumbers.wsdl

The server process starts in the background.  After running the client,
use the kill command to terminate the server process.

For Windows (may use either forward or back slashes):
  start 
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         -Dcatalina.home=..\..\lib\tomcat\5.5.9\ 
         demo.handlers.server.Server

    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
       demo.handlers.client.Client .\wsdl\addNumbers.wsdl

A new command windows opens for the server process.  After running the
client, terminate the server process by issuing Ctrl-C in its command window.

To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:

  ant clean