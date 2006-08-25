Router XML-HTTP Serviceto SOAP-JMS Service Routing Demo
============

This demo shows how a message is routed from a XML_HTTP service to a
SOAP-JMS service.  The celtix router loads the configuration specified
wsdl file and then adds a route from XML_HTTP service to SOAP-JMS service.

The demo demonstrates the routing of two-way invocations from a
jms client to a http server. It also demosntrates the propagation of faults from
server to client via router.

The client loads the source.wsdl which has a XML_HTTP wsdl service.
The server loads the target.wsdl which has a SOAP-JMS wsdl service.
The router has a xmlhttp_to_soapjms route defined. In the router wsdl the http endpoint of
AddNumbersXMLService matches with the http endpoint of AddNumbersXMLService in source.wsdl.
Similarly jms endpoint of AddNumbersSOAPService matches with the jms endpoint
of AddNumbersSOAPService in target.wsdl.
The generated code is based upon target.wsdl.
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

From the samples/routing/xml_http_soap_jms directory, the ant build script can be used to
build and run the demo.  The server and client targets automatically build
the demo.

Using either UNIX or Windows:

  ant server
  ant jmsbroker
  ant router
  ant client


Look in the build.xml file to see how an argument to the java executable specifies
use of the configuration file for router ant target.  For example:

    <target name="router" description="run celtix router">
        <celtixrun classname="org.objectweb.celtix.routing.RouterManager"
            jvmarg1="-Dceltix.config.file=file:///${basedir}/celtix-router.xml"
            param1="-BUSid" param2="celtix-st"/>
    </target>

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean


Buildng the demo using wsdl2java and javac
------------------------------------------

From the samples/routing/xml_http_soap_jms directory, first create the target directory
build/classes and then generate code from the WSDL file.

For UNIX:
  mkdir -p build/classes

  wsdl2java -d build/classes -compile ./wsdl/target.wsdl

For Windows:
  mkdir build\classes
    Must use back slashes.

  wsdl2java -d build\classes -compile .\wsdl\target.wsdl
    May use either forward or back slashes.

Now compile the provided client and server applications with the commands:

For UNIX:

  export CLASSPATH=$CLASSPATH:$CELTIX_HOME/lib/celtix.jar:.:./build/classes
  javac -d build/classes src/demo/client/*.java
  javac -d build/classes src/demo/server/*.java

For Windows:
  set classpath=%classpath%;%CELTIX_HOME%\lib\celtix.jar;.;.\build\classes
  javac -d build\classes src\demo\client\*.java
  javac -d build\classes src\demo\server\*.java

Running the demo using java
---------------------------

From ActiveMQ insallation launch ActiveMQ JMS Broker in seperate window or in
background using the commandline:

For Unix:

cd <activemq.home.dir>/bin
activemq ../conf/activemq.xml

For Windows:
cd <activemq.home.dir>\bin
activemq.bat ..\conf\activemq.xml

The location of <activemq.home.dir> depends on whether you have installed the
binary or source release.  In the binary release, <activemq.home.dir> is in
lib/activemq/3.2.  In the source release, <activemq.home.dir> is in
tools/activemq/3.2.

From the samples/routing directory run the commands (entered on a single command line):

For UNIX (must use forward slashes):
    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         demo.routing.server.Server &

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         -Dceltix.config.file=file:///$CELTIX_HOME/samples/routing/xml_http_soap_jms/celtix-router.xml
         org.objectweb.celtix.routing.RouterManager -BUSid celtix-st &

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         demo.routing.client.Client ./wsdl/source.wsdl

The server and the router process starts in the background.

For Windows (may use either forward or back slashes):
  start
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         demo.routing.server.Server

  start
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         -Dceltix.config.file=file:///%CELTIX_HOME%\samples\routing\xml_http_soap_jms\celtix-router.xml
         org.objectweb.celtix.routing.RouterManager -BUSid celtix-st

    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         demo.routing.client.Client .\wsdl\source.wsdl

The server and router process starts in a new command window.

After running the client, terminate the router, jmsbroker, server process.

To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:

  ant clean
