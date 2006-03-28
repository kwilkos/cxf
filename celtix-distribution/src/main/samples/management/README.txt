Celtix Management
====================

This demo illustrates how to use a Celtix management facilities.
Information in the configuration file is used to change the
management facilities operations. 

Instrumentation enabled is setting the bus to handle the celtix basic 
component created and removed event. JMX enabled is to export the
basic componnet information through the JMX MBeanServer.

When you setting up the JMX MBServer, you need to choice the MBServer to
use the PlatformMBeanServer or not, and set the connector URL which
is use for the manager client connector to the MBServer and set the 
connector to run in a seperated thread or run in the Deamoned mode. 
 

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

If you set the celtix instrumentEnabled and JMXEnabled true, and
set up the MBServer to use PlateformMBeanserver, you can use 
jconsole which comes with  JDK1.5, to explore the celtix managed components.

Building and running the demo using ant
---------------------------------------

From the samples/management directory, the ant build script
can be used to build and run the demo.

Using either UNIX or Windows:
    
  ant build
  ant server  (in the background or another window)
  ant client

To explore the celtix managed components   
  jconsole  (in the background or another window)
  
The server JMX Service URL is:
  service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi/server  

The client JMX Service URL is:
  service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi/client

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean


Buildng the demo using wsdl2java and javac
------------------------------------------

From the samples/hello_world directory, first create the target
directory build/classes and then generate code from the WSDL file.

For UNIX:
  mkdir -p build/classes

  wsdl2java -d build/classes -compile ./wsdl/hello_world.wsdl

For Windows:
  mkdir build\classes
    Must use back slashes.

  wsdl2java -d build\classes -compile .\wsdl\hello_world.wsdl
    May use either forward or back slashes.

Now compile the provided client and server applications with the commands:

For UNIX:  
  
  export classpath=$classpath:$CELTIX_HOME/lib/celtix.jar:./build/classes
  javac -d build/classes src/demo/hw/client/*.java
  javac -d build/classes src/demo/hw/server/*.java

For Windows:
  set classpath=%classpath%;%CELTIX_HOME%\lib\celtix.jar:.\build\classes
  javac -d build\classes src\demo\hw\client\*.java
  javac -d build\classes src\demo\hw\server\*.java



Running the demo using java
---------------------------

From the samples/hello_world directory run the commands, entered on a
single command line:

For UNIX (must use forward slashes):
    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
	-Dceltix.config.file=file:///$CELTIX_HOME/samples/management/server.xml
         demo.hw.server.Server &

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         -Dceltix.config.file=file:///$CELTIX_HOME/samples/management/client.xml
         demo.hw.client.Client ./wsdl/hello_world.wsdl

The server process starts in the background.  After running the client,
use the kill command to terminate the server process.

For Windows (may use either forward or back slashes):

  start 
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
	 -Dceltix.config.file=file:///%CELTIX_HOME%\samples\management\server.xml 
         demo.hw.server.Server 

    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
        -Dceltix.config.file=file:///%CELTIX_HOME%\samples\management\client.xml
         demo.hw.client.Client .\wsdl\hello_world.wsdl

A new command windows opens for the server process.  After running the
client, terminate the server process by issuing Ctrl-C in its command window.

To explore the celtix managed components   
  jconsole  (in the background or another window)
  
  The Server JMX Service URL is:
  service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi/server
  
  The Client JMX Service URL is:
  service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi/client
  

To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:

  ant clean



Understanding the configuration file
------------------------------------

The configuration file, server.xml and client.xml are mostly same, 
which include one <bean> definitions which has two properties. 
The first property includes the information needed to set the
Instrumentation manager and JMX manager enabled property.
The second property includes the information needed to set the 
MBServer to use the PlatformMBeanServer and the JMXConnectorServer's 
basic run mode and connector URL. 

In writing this file, refer to the schema and XML metadata files
that are included within the resources directory under the Celtix
installation.  The tutorial "Using Celtix Configuration" presents a
detailed discussion on how these files are used to compose the
configuration file.

When setting the celtix instrumentation facilities, you are 
configuring the celtix bus to be instrumented and using the JMX 
to export the managed resource.

Because of the instrumentation is a part of the bus, we put the 
instrumentation configuration as a child of bus . The configuration
id is "instrumentation". The appropriate XML metadata file to use 
as a guide is resources/config-metadata/instrumentation-config.xml, 
and the desired <configItem> is instrumentationControl.  Assign 
the value of the configItem's <name> element to the
<property> element's name attribute.  Within the <value>...</value> tags,
you will enter the proper XML as the configuration value for your
defined service.  

Look at the schema file resources/config-metadaata/instrumentation-config.xml
and note that the instrumentationContorl entry corresponds to the
InstrumentationPolicy type defined in the schema file 
resources/schemas.wsdl/instrumentation.xsd. However,
since the InstrumentationPolicy type is a complex type, and the 
configuration file must include two element type, you identify the
InstrumentationEnabled and JMXEnabled elements as  suitable replacement. 
Then you look up the composition of the InstrumentationPolicy type
and determine that it is a sequence of elements: 
InstrumentationEnabled and JMXEnabled.  Combining these
information, lead to the following <bean> declaration.  Note the use of the
sec: namespace prefix.  You must include the corresponding namespace
declaration at the beginning of the configuration file. 

If we set JMXEnabled to be true, we should set up the MBServer's 
<property> to make sure the MBServer can be accessed by jconsole.
Setting the PlatformMBeanServer to be true means that celtix's 
MBServer is getted from PlatformMBeanServer. Note: in this case we should 
pass an option to JVM "-Dcom.sun.management.jmxremote", which means the 
platformMBeanServer can be monitored by local jconsole. 

Setting the PlatformMBeanServer to be false means that celtix's 
MBeanServer is created by itself and we could set the JMXConnectorServer 
run in a separated thread or run in the Daemon mode, and set the 
JMXServiceURL which is use for the Jconsol to connect. 
Note: in this case we should run an rmiregistry to listen to port 
9913 for the JMXConnectorServer to register.
