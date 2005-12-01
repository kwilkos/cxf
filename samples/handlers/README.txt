Handler Demo
============

This demo shows how JAXWS handlers can be used.  The server uses a
SOAP protocol handler which simply logs incoming and outgoing messages
to the console.  

The server code registers a handler using the @HandlerChain annotation
within the service implementation class. For this demo, LoggingHandler
is SOAPHandler that logs the entire SOAP message content to stdout.

While the annotation in the service implementation class specifies
that the server should use the LoggingHandler, the demo shows how
this behaviour is superceded by information obtained from the
celtix-server.xml configuration file, thus allowing control over the
server's behaviour without changing the code.  When the server process
uses the configuration file, LoggingHandler is replaced with
FileLoggingHandler, which logs simple informative messages, not the
entire message content, to the console and adds information to the
demo.log file.

The client includes a logical handler that checks the parameters on
outbound requests and short-circuits the invocation in certain
circumstances. This handler is not specified programatically but
through configuration in the file celtix-client.xml.  Alternatively,
you can run the client without applying the configuration.
In this case, the client does not instantiate a handler.

Please review the README in the samples directory before continuing.


Prerequisite
------------

If your environment already includes celtix.jar on the CLASSPATH,
and the JDK and ant bin directories on the PATH, it is not necessary to
run the environment script described in the samples directory README.
If your environment is not properly configured, or if you are planning
on using wsdl2java, javac, and java to build and run the demos, you must
set the environment by running the script.


Building and running the demo using ant
---------------------------------------

From the samples/handlers directory, the ant build script can be used to
build and run the demo.  The server and client targets automatically build
the demo.

Using either UNIX or Windows:

  ant server  (in the background or another window)
  ant client

When using these ant targets, the server process uses the FileLoggingHandler
and the client process uses the SmallNumberHandler.  Notice that the both
the client and server consoles display short informative messages.  The server's
handler also appends information to the file demo.log.  The client handler
examines the operation parameters and, depending on the parameter values, may
not forward the request to the server.

Look in the build.xml file to see how an argument to the java executable specifies
use of the configuration file.  For example:

  <target name="client" description="run demo client" depends="build">
    <celtixrun classname="demo.handlers.client.Client"
               param1="${basedir}/wsdl/addNumbers.wsdl"
               jvmarg1="-Dceltix.config.file=file:///${basedir}/celtix-client.xml"/>
  </target>

After running the client, terminate the server process.

Now run the server process using the LoggingHandler.

Using either UNIX or Windows:

  ant server2  (in the background or another window)
  ant client2

The ant targets client2 and server2 do not include the attribute that
specifies the configuration file.  For example:

  <target name="client2" description="run demo client" depends="build">
    <celtixrun classname="demo.handlers.client.Client"
               param1="${basedir}/wsdl/addNumbers.wsdl"/>
  </target>

Now, the server displays the entire content of each message in its console and
the client no longer uses a handler.  The @HandlerChain annotation in the
implementation class indicates that the file demo_handler.xml includes the
information needed to identify the handler class.

  @HandlerChain(file = "../common/demo_handlers.xml", name = "DemoHandlerChain")

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean


Buildng the demo using wsdl2java and javac
------------------------------------------

From the samples/handlers directory, first create the target directory
build/classes and then generate code from the WSDL file.

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

Finally, copy the demo_handlers.xml file from the src/demo/handlers/common
directory into the build/classes/demo/handlers/common directory.

For UNIX:
  cp ./src/demo/handlers/common/demo_handlers.xml ./build/classes/demo/handlers/common

For Windows:
  copy src\demo\handlers\common\demo_handlers.xml build\classes\demo\handlers\common


Running the demo using java
---------------------------

Run the applications using the configuration information in the files
celtix-server.xml and celtix-client.xml.  The server will use the FileLoggingHandler
and the client will use the SmallNumberHandler.

From the samples/handlers directory run the commands (entered on a single command line):

For UNIX (must use forward slashes):
    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         -Dcatalina.home=../../lib/tomcat/5.5.9/ 
         -Dceltix.config.file=file:///$CELTIX_HOME/samples/handlers/celtix-server.xml
         demo.handlers.server.Server &

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         -Dceltix.config.file=file:///$CELTIX_HOME/samples/handlers/celtix-client.xml
         demo.handlers.client.Client ./wsdl/addNumbers.wsdl

The server process starts in the background.

For Windows (may use either forward or back slashes):
  start 
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         -Dcatalina.home=..\..\lib\tomcat\5.5.9\
         -Dceltix.config.file=file:///%CELTIX_HOME%\samples\handlers\celtix-server.xml
         demo.handlers.server.Server

    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         -Dceltix.config.file=file:///%CELTIX_HOME%\samples\handlers\celtix-client.xml
         demo.handlers.client.Client .\wsdl\addNumbers.wsdl

The server process starts in a new command window.

Notice that the FileLoggingHandler, specified in the configuration file
celtix-server.xml, logs information to the console and makes entries into
the file demo.log, which is in the samples/handler directory.  Also, the
SmallNumberHandler, specified in the configuration file celtix-client.xml,
logs information to the console in which the client application runs.

Now run the server process using the LoggingHandler.  The client does not use a handler.

From the samples/handlers directory run the commands (entered on a single command line):

For UNIX (must use forward slashes):
    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         -Dcatalina.home=../../lib/tomcat/5.5.9/ 
         demo.handlers.server.Server &

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         demo.handlers.client.Client ./wsdl/addNumbers.wsdl

The server process starts in the background.

For Windows (may use either forward or back slashes):
  start 
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         -Dcatalina.home=..\..\lib\tomcat\5.5.9\
         demo.handlers.server.Server

    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         demo.handlers.client.Client .\wsdl\addNumbers.wsdl

The server process starts in a new command window.

After running the client, terminate the server process.

To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:

  ant clean