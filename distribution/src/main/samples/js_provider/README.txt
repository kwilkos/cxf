Hello World Demo using JavaScript and E4X Implementations
=========================================================

The demo demonstrates the use of the JavaScript and E4X dynamic
languages to implement JAX-WS Providers.

The client side makes two Dispatch-based invocations. The first uses
SOAPMessage data in MESSAGE mode, and the second uses DOMSource in
PAYLOAD mode. The first service is implemented using E4X, the second
using JavaScript.

The two messages are constructed by reading in the XML files found in
the demo/hwDispatch/client directory.

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

From the samples/dispatch_provider directory, the ant build script
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

From the samples/dispatch_provider directory, first create the target
directory build/classes and then generate code from the WSDL file.

For UNIX:
  mkdir -p build/classes

  wsdl2java -d build/classes ./wsdl/hello_world.wsdl

For Windows:
  mkdir build\classes
    Must use back slashes.

  wsdl2java -d build\classes .\wsdl\hello_world.wsdl
    May use either forward or back slashes.

Now compile both the generated code and the provided client
application with the commands:

  javac -d build/classes src/demo/hwDispatch/client/*.java

Windows may use either forward or back slashes.

Since JavaScript and E4X are interpreted at runtime and do not require
compilation, there is no server-side java code requiring compilation.



Running the demo using java
---------------------------

From the samples/js_dispatch directory run the commands, entered on a
single command line:

For UNIX (must use forward slashes):
    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         org.objectweb.celtix.js.rhino.ServerApp -v
         -b http://localhost:9000/SoapContext impl.jsx impl.js

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         demo.hwDispatch.client.Client ./wsdl/hello_world.wsdl

The server process starts in the background.  After running the client,
use the kill command to terminate the server process.

For Windows (may use either forward or back slashes):
  start 
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         org.objectweb.celtix.js.rhino.ServerApp -v
         -b http://localhost:9000/SoapContext impl.jsx impl.js

    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
       demo.hwDispatch.client.Client .\wsdl\hello_world.wsdl

A new command windows opens for the server process.  After running the
client, terminate the server process by issuing Ctrl-C in its command window.

To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:

  ant clean
