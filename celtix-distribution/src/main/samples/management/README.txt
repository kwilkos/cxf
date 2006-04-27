Celtix Management
====================

This demo illustrates how to use the Celtix management facilities.
Information in the configuration file is used to change the
management facilities operations. 

Instrumentation is enabled by setting the bus to handle the celtix basic 
component created and removed events. JMX is enabled to export the
basic componnet information through the JMX MBeanServer.

When setting up the JMX MBServer, you need to do the following:
1. Set the MBServer to use either the PlatformMBeanServer or your prefered
   MBeanServer implementation.
2. Specify the connector URL which the manager console uses to
   connect to the MBServer.
3. Set the connector to run in a seperated thread or to run in the deamon
   mode. 

Please review the README in the samples directory before
continuing.



Prerequisites
-------------

1. celtix.jar must be on your CLASSPATH.
2. JDK 1.5 or higher must be on your PATH
3. ant must be on your PATH

If your environment is not properly configured, or if you are planning on
using wsdl2java, javac, and java to build and run the demos, you must set
the environment by running the script.

If you set the Celtix configuration properties instrumentEnabled and
JMXEnabled to true, and set up the MBServer to use PlateformMBeanserver, you
can use jconsole, which comes with  JDK1.5, to explore the Celtix managed
components.



Building and running the demo using ant
---------------------------------------

From the samples/management directory, the ant build script
can be used to build and run the demo.

Using either UNIX or Windows:

  ant build
  ant server
  ant client

To run the demo of how to get and set celtix instrumentation components
attributes and invoke the components' operation
 
  ant jmxconsole 


To explore the celtix managed components:

  jconsole (Porvider with JDK 1.5)
    
  The server JMX Service URL is:
    service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi/server  


  NOTE: Just enter the JMX service URL as above, and leave the 
username and password blank in this sample.

  
To remove the code generated from the WSDL file and the .class
files, run:

  ant clean


Buildng the demo using wsdl2java and javac
------------------------------------------

From the samples/hello_world directory:
1. create the target directory build/classes.

UNIX:
  mkdir -p build/classes

Windows:
  mkdir build\classes

2. Generate code from the WSDL file.

UNIX:
  wsdl2java -d build/classes -compile ./wsdl/hello_world.wsdl

Windows:
  wsdl2java -d build\classes -compile .\wsdl\hello_world.wsdl

3. Compile the provided client and server applications.

UNIX:  
  
  export CLASSPATH=$CLASSPATH:$CELTIX_HOME/lib/celtix.jar:./build/classes
  javac -d build/classes src/demo/hw/client/*.java
  javac -d build/classes src/demo/hw/server/*.java
  javac src/demo/hw/jmxconsole/*.java

Windows:
  set classpath=%classpath%;%CELTIX_HOME%\lib\celtix.jar:.\build\classes
  javac -d build\classes src\demo\hw\client\*.java
  javac -d build\classes src\demo\hw\server\*.java
  javac src\demo\hw\jmxconsole\*.java


Running the demo using java
---------------------------

From the samples/hello_world directory run the commands, entered on a
single command line:

UNIX:
    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
	-Dceltix.config.file=file:///$CELTIX_HOME/samples/management/server.xml
         demo.hw.server.Server &

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         demo.hw.client.Client ./wsdl/hello_world.wsdl

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
	 demo.hw.jmxconsole.Client

The server process starts in the background.  After running the client,
use the kill command to terminate the server process.

Windows:
  start 
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
	 -Dceltix.config.file=file:///%CELTIX_HOME%\samples\management\server.xml 
         demo.hw.server.Server 

    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         demo.hw.client.Client .\wsdl\hello_world.wsdl

    java  -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         demo.hw.jmxconsole.Client

A new command windows opens for the server process.  After running the
client, terminate the server process by issuing Ctrl-C in its command window.

To explore the celtix managed components   
  jconsole
  
  The Server JMX Service URL is:
  service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi/server
  

To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:
  ant clean



Understanding the configuration file
------------------------------------

The configuration files, server.xml and client.xml, are mostly the same.
They each include one <bean> definition which has two properties. 
The first property specifies the information needed to set the
Instrumentation manager and JMX manager enabled property.
The second property specifies the information needed to set the 
MBServer to use the PlatformMBeanServer, the JMXConnectorServer's 
basic run mode, and the JMXConnectorServer's connector URL. 

In writing this file, refer to the schema and XML metadata files
that are included within the resources directory under the Celtix
installation.  The tutorial "Using Celtix Configuration" presents a
detailed discussion on how these files are used to compose the
configuration file.

When setting the celtix instrumentation facilities, you are 
configuring the Celtix bus to be instrumented and to use JMX 
to export the managed resources.

Because the instrumentation is a part of the bus, we put the 
instrumentation configuration as a child of the bus . The configuration
id is "instrumentation". The appropriate XML metadata file to use 
as a guide is resources/config-metadata/instrumentation-config.xml, 
and the desired <configItem> is instrumentationControl. Assign 
the value of the configItem's <name> element to the <property> element's
name attribute.  Within the <value>...</value> tags,
you will enter the proper XML as the configuration value for your
defined service.  

Look at the schema file resources/config-metadaata/instrumentation-config.xml
and note that the instrumentationContorl entry corresponds to the
InstrumentationPolicy type defined in the schema file 
resources/schemas.wsdl/instrumentation.xsd. However,
since the InstrumentationPolicy type is a complex type, and the 
configuration file must include two element types, you identify the
InstrumentationEnabled and JMXEnabled elements as a suitable replacement. 
Then you look up the composition of the InstrumentationPolicy type
and determine that it is a sequence of elements:
InstrumentationEnabled and JMXEnabled.  Combining this
information, leads to the following <bean> declaration.  Note the use of the
sec: namespace prefix. You must include the corresponding namespace
declaration at the beginning of the configuration file. 

If we set JMXEnabled to be true, we should set up the MBServer's 
<property> to make sure the MBServer can be accessed by jconsole.
We can set the JMXConnectorServer to run in a separated thread 
or to run in the daemon mode, and we can set the JMXServiceURL 
which is used by Jconsol to connect with the MBeanServer.  

