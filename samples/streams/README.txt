Hello World Demo using StreamHandler to transform message
=========================================================

This demo illustrates how to use a stream handler to transform a
message.  A StreamHandler is installed on both client and server
side. On an outgoing message, the handler replaces the OutputStream
used by the binding to unmarshal the message with an OutputStream
which compresses the message.  For an incoming message, a
decompressing InputStream is used.

The StreamHandler is specified in an external JAXB Bindings file
(./wsdl/bindings.xml) which is passed to the wsdl2java code generator.
The binding file is written in the JAXWS Binding Language.  In this
case, the generated Service Endpoint Interface includes a
@HandlerChain annotation which results in a handler chain being
created both on the client and server side.

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

From the samples/streams directory, the ant build script
can be used to build and run the demo.

Using either UNIX or Windows:

  ant build
  ant server  (in the background or another window)
  ant client
    

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean



Buildng the demo using wsdl2java and javac
------------------------------------------

From the samples/streams directory, first create the target
directory build/classes and then generate code from the WSDL file.

For UNIX:
  mkdir -p build/classes

  wsdl2java -d build/classes ./wsdl/hello_world.wsdl

For Windows:
  mkdir build\classes
    Must use back slashes.

  wsdl2java -d build\classes .\wsdl\hello_world.wsdl
    May use either forward or back slashes.

Now compile both the generated code and the provided client and
server applications with the commands:

  javac -d build/classes src/demo/streams/common/*.java
  javac -d build/classes src/demo/streams/client/*.java
  javac -d build/classes src/demo/streams/server/*.java


Windows may use either forward or back slashes.



Running the demo using java
---------------------------

From the samples/streams directory run the commands, entered on a
single command line:

For UNIX (must use forward slashes):
    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         demo.hw.server.Server &

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         demo.hw.client.Client ./wsdl/hello_world.wsdl

The server process starts in the background.  After running the client,
use the kill command to terminate the server process.

For Windows (may use either forward or back slashes):
  start 
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         demo.hw.server.Server

    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
       demo.hw.client.Client .\wsdl\hello_world.wsdl

A new command windows opens for the server process.  After running the
client, terminate the server process by issuing Ctrl-C in its command window.

To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:

  ant clean
