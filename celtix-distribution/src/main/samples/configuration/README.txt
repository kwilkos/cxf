Celtix Configuration
====================

This demo illustrates how to use a Celtix configuration file.
Information in the configuration file is used to change the
URL used by the client application, add a user name and
password to the HTTP header, and direct the client application
to send requests to the target endpoint through a proxy server.

The handlers demo illustrates how to use a configuration file
to specify a handler within the client or server process.  This
use of a configuration file is not covered in this demo.

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

From the samples/configuration directory, the ant build script
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
  
  export CLASSPATH=$CLASSPATH:$CELTIX_HOME/lib/celtix.jar:./build/classes
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
         demo.hw.server.Server &

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         -Dceltix.config.file=file:///$CELTIX_HOME/samples/configuration/client.xml
         demo.hw.client.Client ./wsdl/hello_world.wsdl

The server process starts in the background.  After running the client,
use the kill command to terminate the server process.

For Windows (may use either forward or back slashes):
  start 
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         demo.hw.server.Server

    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         -Dceltix.config.file=file:///%CELTIX_HOME%\samples\configuration\client.xml
         demo.hw.client.Client .\wsdl\hello_world.wsdl

A new command windows opens for the server process.  After running the
client, terminate the server process by issuing Ctrl-C in its command window.

To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:

  ant clean



Understanding the configuration file
------------------------------------

The configuruation file, client.xml, includes two <bean> definitions.
The first <bean> includes the information needed to set the URL that
the client application uses.  The second <bean> includes the
information needed to set the basic authentication values and to
specify the location of the proxy server.

In writing this file, refer to the schema and XML metadata files
that are included within the resources directory under the Celtix
installation.  The tutorial "Using Celtix Configuration" presents a
detailed discussion on how these files are used to compose the
configuration file.

When setting the URL, you are configuring an HTTP port.  The
appropriate XML metadata file to use as a guide is
resources/config-metadata/port-config.xml, and the desired <configItem>
is address.  Assign the value of the configItem's <name> element to the
<property> element's name attribute.  Within the <value>...</value> tags,
you will enter a string that is the desired URL.  Notice that the tags
used to delimit the URL correspond to an element type defined in the
schema file resources/schemas/configuration/std-types.xsd.

When setting authentication data, you are configuring the client service.
The appropriate XML metadata file to use as a guide is
resources/config-metadaata/http-client-config.xml, and the desired
<configItem> is authorization.  Assign the value of the configItem's
<name> element to the <property> element's name attribute.  Look at the
schema file resources/config-metadaata/http-client-config.xml and note that
the authorization entry corresponds to the AuthorizationPolicy type defined
in the schema file resources/schemas/configuration/security.xsd.  However,
since the AuthorizationPolicy type is a complex type, and the configuration
file must include a element type, you identify the authorization element as
a suitable replacement.  Then you look up the composition of the
AuthorizationPolicy type and determine that it is a sequence of elements: 
UserName, Password, AuthorizationType, and Authorization.  Combining this
information, leads to the following <bean> declaration.  Note the use of the
sec: namespace prefix.  You must include the corresponding namespace
declaration at the beginning of the configuration file.

For transport attributes, the XML metadata file resources/config-metadaata/
http-client-config.xml, using the <configItem> httpClient, indicates that
the HTTPClientPolicy type can be used to set transport attributes.  The
HTTPClientPolicy type, defined in the schema file resources/schemas/wsdl/
http-conf.xsd, is a complex type consisting of multiple attributes.  The
client element may be used to reference the HTTPClientPolicy type in a Celtix
configuration file.  Note that the bean class used to configure transport
attributes is the same as the bean class used to configure authorization.  The
http-conf.xsd schema file is described in the namespace 
http://celtix.objectweb.org/transports/http/configuration, and you must include
a prefix definition for this namespace at the beginning of the Celtix
configuration file.
